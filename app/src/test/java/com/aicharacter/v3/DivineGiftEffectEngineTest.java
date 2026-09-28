package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class DivineGiftEffectEngineTest {

 @Test public void coldResilienceActivatesFromRealColdAndReducesThermalBurden(){
  long prior=1000L,now=31L*60000L;
  WorldState control=state(prior),gifted=state(prior);
  gifted.thermal.coldLoad=.72;control.thermal.coldLoad=.72;
  gifted.thermal.skinWetness=.42;control.thermal.skinWetness=.42;
  DivineGiftState gift=grant(gifted,"cold_gift","COLD_RESILIENCE",1.0,"ENTITY","haru",prior+1);
  double beforePower=gifted.divineOntology.power.reserve;

  DivineGiftEffectEngine.advance(gifted,now);
  assertEquals("ACTIVE",gift.status);
  assertTrue(gifted.divineOntology.power.reserve<beforePower);
  assertEquals(1.0,DivineGiftEffectEngine.coldResilience(gifted,"haru",now),1e-9);

  ThermalEngine.advance(control,now);
  ThermalEngine.advance(gifted,now);
  assertTrue(gifted.thermal.coldLoad<control.thermal.coldLoad);
 }

 @Test public void areaWardOnlyProtectsItsActualArea(){
  long prior=1000L,now=21L*60000L;
  WorldState control=state(prior),gifted=state(prior);
  DivineGiftState gift=grant(gifted,"ward_a","AREA_WARD",1.0,"AREA","area_a",prior+1);
  DivineGiftEffectEngine.advance(gifted,now);
  assertEquals("ACTIVE",gift.status);
  assertEquals(1.0,DivineGiftEffectEngine.areaWard(gifted,"area_a",now),1e-9);
  assertEquals(0.0,DivineGiftEffectEngine.areaWard(gifted,"area_b",now),1e-9);

  ThermalEngine.advance(control,now);
  ThermalEngine.advance(gifted,now);
  assertTrue(gifted.thermal.skinWetness<control.thermal.skinWetness);
 }

 @Test public void rollbackRemovesPhysicalModifierImmediately(){
  long now=10_000L;WorldState s=state(1000L);s.thermal.coldLoad=.7;
  DivineGiftState gift=grant(s,"cold_rollback","COLD_RESILIENCE",.8,"ENTITY","haru",2000L);
  DivineGiftEffectEngine.advance(s,now);
  assertEquals("ACTIVE",gift.status);assertTrue(DivineGiftEffectEngine.coldResilience(s,"haru",now)>.7);
  assertTrue(DivineGiftContract.rollback(s,gift.giftId,"ThermalEngine.COLD_RESILIENCE",now+1));
  assertEquals("ROLLED_BACK",gift.status);assertEquals(0,DivineGiftEffectEngine.coldResilience(s,"haru",now+2),0);
 }

 @Test public void unsupportedConsumerDoesNotInventHealingOrCognitionChanges(){
  long now=10_000L;WorldState s=state(1000L);s.body.health=63;s.body.pain=37;s.currentIntention="observe_lake";
  int thoughts=s.thoughts.size();double health=s.body.health,pain=s.body.pain;String intention=s.currentIntention;
  DivineGiftState gift=grant(s,"restore_wait","MINOR_RESTORATION",1.0,"ENTITY","haru",2000L);
  DivineGiftEffectEngine.advance(s,now);
  assertEquals("GRANTED",gift.status);
  assertEquals(health,s.body.health,0);assertEquals(pain,s.body.pain,0);assertEquals(intention,s.currentIntention);assertEquals(thoughts,s.thoughts.size());
 }

 @Test public void physicalGiftActivationDoesNotWriteHaruMind(){
  long now=20_000L;WorldState s=state(1000L);s.thermal.coldLoad=.8;s.currentIntention="reflect";
  int thoughts=s.thoughts.size(),memories=s.memories.size();String intention=s.currentIntention;
  grant(s,"cold_mind_boundary","COLD_RESILIENCE",.9,"ENTITY","haru",2000L);
  DivineGiftEffectEngine.advance(s,now);
  assertEquals(intention,s.currentIntention);assertEquals(thoughts,s.thoughts.size());assertEquals(memories,s.memories.size());
 }

 private static DivineGiftState grant(WorldState s,String id,String kind,double intensity,String scope,String target,long at){
  DivineFollowerState f=s.divineOntology.followers.get("haru");if(f==null){f=new DivineFollowerState();f.entityId="haru";f.speciesKey="haru_kind";f.chosen=true;f.rank="CHOSEN";f.devotionSnapshot=.9;s.divineOntology.followers.put("haru",f);}
  s.divineOntology.power.reserve=50;s.divineOntology.power.clamp();
  WorldHistoryEntry e=WorldEventBus.publishId(s,at,"gift_source_"+id,"DIVINE_FOLLOWER_CHOSEN","haru","Chosen from durable evidence.");
  DivineGiftContract.Result r=DivineGiftContract.grant(s,id,"haru",kind,intensity,120,scope,target,e.eventId,at+1);
  assertTrue(r.message,r.ok);return r.gift;
 }

 private static WorldState state(long prior){
  WorldState s=WorldState.fresh();s.world=new WorldModel();
  s.world.areas.add(new WorldArea("area_a","Open A","a",0,100,0,true,"open"));
  s.world.areas.add(new WorldArea("area_b","Open B","b",100,200,0,true,"open"));
  s.haruX=20;s.environment.weather="RAIN";s.environment.weatherIntensity=.90;s.environment.wind=.65;s.worldWetness=0;
  s.thermal=new ThermalState();s.thermal.lastUpdatedAt=prior;s.thermal.skinWetness=.10;s.thermal.coldLoad=.20;
  s.divineOntology=new DivineOntologyState();return s;
 }
}
