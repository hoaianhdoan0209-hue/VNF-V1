package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class DivineFollowerOfferingGiftTest {

 @Test public void followerScoreDoesNotUseSpeciesIdentity(){
  DivineFollowerState a=new DivineFollowerState(),b=new DivineFollowerState();
  a.entityId="a";a.speciesKey="reedling";b.entityId="b";b.speciesKey="root_husher";
  a.devotionSnapshot=b.devotionSnapshot=.82;a.influence=b.influence=.71;a.reliability=b.reliability=.77;a.service=b.service=.64;a.offeringCommitment=b.offeringCommitment=.42;
  assertEquals(DivineFollowerEngine.score(a),DivineFollowerEngine.score(b),1e-12);
 }

 @Test public void devotionAloneCannotMakeSomeoneChosen(){
  WorldState s=state();DivineFollowerState f=new DivineFollowerState();f.entityId="quiet_follower";f.speciesKey="any_species";f.devotionSnapshot=1.0;f.reliability=.5;s.divineOntology.followers.put(f.entityId,f);
  DivineFollowerEngine.observe(s,1000L);
  assertFalse(f.chosen);assertTrue(f.candidateScore<.72);
 }

 @Test public void durableInfluenceAndServiceCanProduceChosenFollower(){
  WorldState s=state();DivineFollowerState f=new DivineFollowerState();f.entityId="wanderer_7";f.speciesKey="unclassified";f.devotionSnapshot=.86;f.reliability=.62;s.divineOntology.followers.put(f.entityId,f);
  WorldHistoryEntry e=WorldEventBus.publishId(s,900L,"service_evt","COMMUNITY_SERVICE","area_a","A durable service event occurred.");
  assertTrue(DivineFollowerEngine.recordEvidence(s,f.entityId,e.eventId,.82,.80,.16,950L));
  f.offeringCommitment=.44;
  DivineFollowerEngine.observe(s,1000L);
  assertTrue(f.chosen);assertEquals("CHOSEN",f.rank);assertTrue(f.chosenAt>0);assertTrue(hasType(s,"DIVINE_FOLLOWER_CHOSEN"));
 }

 @Test public void offeringRequiresRealSurrenderEvidenceAndCreatesPowerNotKnowledge(){
  WorldState s=state();int knowledgeBefore=s.characterGod.conceptKnowledge.size();
  DivineOfferingEngine.Result missing=DivineOfferingEngine.submit(s,"off_missing","giver","reedling","area_a","RESOURCE",.6,.8,.2,true,"WEATHER_SENSE","not_real",1000L);
  assertFalse(missing.ok);assertEquals(0,s.divineOntology.power.reserve,0);
  WorldHistoryEntry surrender=WorldEventBus.publishId(s,1001L,"surrender_evt","RITUAL_RESOURCE_SURRENDERED","giver","A real resource was relinquished.");
  DivineOfferingEngine.Result accepted=DivineOfferingEngine.submit(s,"off_1","giver","reedling","area_a","RESOURCE",.6,.8,.2,true,"WEATHER_SENSE",surrender.eventId,1010L);
  assertTrue(accepted.ok);assertTrue(accepted.offering.accepted);assertTrue(accepted.offering.divineYield>0);assertTrue(s.divineOntology.power.reserve>0);
  assertEquals(knowledgeBefore,s.characterGod.conceptKnowledge.size());assertFalse(s.characterGod.conceptKnowledge.containsKey("offering_truth"));
  assertFalse(DivineOfferingEngine.submit(s,"off_1","giver","reedling","area_a","RESOURCE",.6,.8,.2,true,"","surrender_evt",1020L).ok);
 }

 @Test public void involuntaryOfferingIsRefusedAndLifeCostIsNotMechanicallyFavored(){
  WorldState forced=state();WorldHistoryEntry fsrc=WorldEventBus.publishId(forced,1000L,"forced_src","RITUAL_RESOURCE_SURRENDERED","giver","A surrender event.");
  DivineOfferingEngine.Result refused=DivineOfferingEngine.submit(forced,"forced","giver","reedling","area_a","RESOURCE",.8,.9,.7,false,"",fsrc.eventId,1010L);
  assertTrue(refused.ok);assertFalse(refused.offering.accepted);assertEquals(0,forced.divineOntology.power.reserve,0);

  WorldState resource=state(),life=state();
  WorldHistoryEntry rsrc=WorldEventBus.publishId(resource,1000L,"r_src","RITUAL_RESOURCE_SURRENDERED","giver","Resource surrendered.");
  WorldHistoryEntry lsrc=WorldEventBus.publishId(life,1000L,"l_src","RITUAL_LIFE_COST","giver","Life cost was voluntarily committed.");
  DivineOfferingState ro=DivineOfferingEngine.submit(resource,"r","giver","reedling","area_a","RESOURCE",.5,.7,.5,true,"",rsrc.eventId,1010L).offering;
  DivineOfferingState lo=DivineOfferingEngine.submit(life,"l","giver","reedling","area_a","LIFE_COST",.5,.7,.5,true,"",lsrc.eventId,1010L).offering;
  assertTrue(ro.accepted);assertTrue(lo.accepted);assertTrue(lo.divineYield<=ro.divineYield+1e-9);
 }

 @Test public void giftRequiresChosenFollowerPowerAndExplicitConsumer(){
  WorldState s=state();DivineFollowerState chosen=new DivineFollowerState();chosen.entityId="chosen_1";chosen.speciesKey="reedling";chosen.chosen=true;chosen.rank="CHOSEN";chosen.devotionSnapshot=.8;s.divineOntology.followers.put(chosen.entityId,chosen);s.divineOntology.power.reserve=20;
  WorldHistoryEntry source=WorldEventBus.publishId(s,1000L,"chosen_source","DIVINE_FOLLOWER_CHOSEN","chosen_1","Chosen from prior evidence.");
  double energy=s.body.energy;String intention=s.currentIntention;int knowledge=s.characterGod.conceptKnowledge.size();
  DivineGiftContract.Result g=DivineGiftContract.grant(s,"gift_1","chosen_1","WEATHER_SENSE",.6,60,"ENTITY","chosen_1",source.eventId,1010L);
  assertTrue(g.ok);assertEquals("GRANTED",g.gift.status);assertTrue(s.divineOntology.power.reservedForGifts>0);assertEquals(20,s.divineOntology.power.reserve,1e-9);
  assertEquals(energy,s.body.energy,0);assertEquals(intention,s.currentIntention);assertEquals(knowledge,s.characterGod.conceptKnowledge.size());
  double cost=g.gift.powerCost;
  assertTrue(DivineGiftContract.activate(s,"gift_1","WeatherSenseConsumer",1020L));assertEquals("ACTIVE",g.gift.status);assertEquals(20-cost,s.divineOntology.power.reserve,1e-9);assertEquals(0,s.divineOntology.power.reservedForGifts,1e-9);
  assertEquals(energy,s.body.energy,0);assertEquals(intention,s.currentIntention);
 }

 @Test public void unchosenFollowerCannotReceiveGift(){
  WorldState s=state();DivineFollowerState f=new DivineFollowerState();f.entityId="ordinary";f.devotionSnapshot=.9;s.divineOntology.followers.put(f.entityId,f);s.divineOntology.power.reserve=50;
  WorldHistoryEntry source=WorldEventBus.publishId(s,1000L,"ordinary_source","WORLD_EVENT","ordinary","Evidence exists.");
  assertFalse(DivineGiftContract.grant(s,"gift_x","ordinary","DIVINE_SIGNAL",.3,20,"ENTITY","ordinary",source.eventId,1010L).ok);
  assertTrue(s.divineOntology.gifts.isEmpty());assertEquals(50,s.divineOntology.power.reserve,0);
 }

 @Test public void ritualAndOfferingTraditionEmergeWithoutSpeciesPreset()throws Exception{
  WorldState s=state();pop(s,"alpha","area_a",.8);pop(s,"beta","area_a",.8);
  GodWorldEventProposal p=proposal("shared_sign","area_a",.70,1000L);assertTrue(GodWorldConditionContract.propose(s,p,1000L).ok);assertTrue(GodWorldConditionContract.markApplied(s,"shared_sign","WorldPhysicsTest",1010L));DivineWorshipEngine.advance(s,1,1020L);
  SpeciesDivineState a=s.livingWorld.divine("alpha","area_a"),b=s.livingWorld.divine("beta","area_a");
  assertTrue(a.ritualization>.02);assertEquals(a.ritualization,b.ritualization,1e-9);
  WorldHistoryEntry sa=WorldEventBus.publishId(s,1030L,"alpha_surrender","RITUAL_RESOURCE_SURRENDERED","alpha_giver","Alpha offering was actually surrendered.");
  WorldHistoryEntry sb=WorldEventBus.publishId(s,1031L,"beta_surrender","RITUAL_RESOURCE_SURRENDERED","beta_giver","Beta offering was actually surrendered.");
  DivineOfferingState oa=DivineOfferingEngine.submit(s,"alpha_offer","alpha_giver","alpha","area_a","RESOURCE",.4,.7,.2,true,"",sa.eventId,1040L).offering;
  DivineOfferingState ob=DivineOfferingEngine.submit(s,"beta_offer","beta_giver","beta","area_a","RESOURCE",.4,.7,.2,true,"",sb.eventId,1041L).offering;
  assertTrue(oa.accepted);assertTrue(ob.accepted);assertTrue(a.offeringTradition>0);assertEquals(a.offeringTradition,b.offeringTradition,1e-9);
 }

 @Test public void saveRoundTripPreservesChosenOfferingGiftAndPower()throws Exception{
  WorldState s=state();DivineFollowerState f=new DivineFollowerState();f.entityId="chosen_save";f.chosen=true;f.rank="CHOSEN";f.devotionSnapshot=.9;s.divineOntology.followers.put(f.entityId,f);s.divineOntology.power.reserve=15;
  WorldHistoryEntry surrender=WorldEventBus.publishId(s,1000L,"save_surrender","RITUAL_RESOURCE_SURRENDERED","chosen_save","Resource surrendered.");
  DivineOfferingEngine.Result offer=DivineOfferingEngine.submit(s,"save_offer","chosen_save","reedling","area_a","RELIC",.7,.8,.2,true,"DIVINE_SIGNAL",surrender.eventId,1010L);assertTrue(offer.offering.accepted);
  WorldHistoryEntry grantSource=WorldEventBus.publishId(s,1020L,"save_grant_source","DIVINE_FOLLOWER_CHOSEN","chosen_save","Chosen evidence.");
  assertTrue(DivineGiftContract.grant(s,"save_gift","chosen_save","DIVINE_SIGNAL",.4,45,"ENTITY","chosen_save",grantSource.eventId,1030L).ok);
  WorldState x=WorldState.fromJson(s.toJson());
  assertTrue(x.divineOntology.followers.get("chosen_save").chosen);assertTrue(x.divineOntology.offerings.get("save_offer").accepted);assertEquals("GRANTED",x.divineOntology.gifts.get("save_gift").status);assertEquals(s.divineOntology.power.reserve,x.divineOntology.power.reserve,1e-9);assertEquals(s.divineOntology.power.reservedForGifts,x.divineOntology.power.reservedForGifts,1e-9);
 }

 private static GodWorldEventProposal proposal(String id,String area,double intensity,long now)throws Exception{org.json.JSONObject j=new org.json.JSONObject();j.put("kind","god-world-condition-v2");j.put("id",id);j.put("condition","WIND");j.put("desiredValue","BREEZE");j.put("scopeType","AREA");j.put("scopeTarget",area);j.put("intensity",intensity);j.put("durationMinutes",30);j.put("provenanceLayer","GOD_PROPOSAL");j.put("sourceRef","god:test");j.put("rollbackPolicy","RESTORE_PREVIOUS_BASELINE");j.put("requestedAt",now);return GodWorldEventProposal.fromJson(j,now);}
 private static SpeciesPopulationState pop(WorldState s,String species,String area,double abundance){SpeciesPopulationState p=s.livingWorld.population(species,area);p.relativeAbundance=abundance;p.carryingCapacity=.9;p.lastUpdatedAt=1;p.clamp();return p;}
 private static boolean hasType(WorldState s,String type){for(WorldHistoryEntry e:s.worldHistory)if(type.equals(e.type))return true;return false;}
 private static WorldState state(){WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("area_a","A","a",0,100,0,true,"open"));s.world.areas.add(new WorldArea("area_b","B","b",100,200,0,true,"open"));s.haruX=20;s.catX=30;s.catState.x=30;s.catState.areaId="area_a";s.livingWorld=new LivingWorldState();s.divineOntology=new DivineOntologyState();return s;}
}
