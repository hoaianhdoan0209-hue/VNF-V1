package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class CatSocialEngineTest {

 @Test public void calmRepeatedProximityBuildsCatOwnedFamiliarity(){
  WorldState s=state(500,640);double f=s.catSocial.familiarity,c=s.catSocial.comfort,w=s.catSocial.wariness;
  for(int i=0;i<5;i++)CatSocialEngine.tick(s,10000L+i*9000L);
  assertTrue(s.catSocial.calmEncounters>=4);assertTrue(s.catSocial.familiarity>f);assertTrue(s.catSocial.comfort>c);assertTrue(s.catSocial.wariness<w);
 }

 @Test public void defaultCalmCatCanProgressToVisibleApproachWithinTwoMinutes(){
  WorldState s=state(500,650);
  boolean moved=false;
  long now=10000L;
  for(int i=0;i<14;i++){
   moved=CatSocialEngine.tick(s,now);
   if(moved)break;
   now+=10000L;
  }
  assertTrue("calm default cat should not remain WATCH forever",moved);
  assertEquals("APPROACH",s.catSocial.mode);
  assertTrue(s.catTravel.active);
  assertTrue(s.catSocial.familiarity>=.35);
  assertTrue(s.catSocial.comfort>=.34);
 }

 @Test public void fastIntrusionRaisesWarinessAndCanTriggerRetreat(){
  WorldState s=state(500,660);s.catSocial.wariness=.44;s.catSocial.comfort=.24;
  CatSocialEngine.tick(s,10000L);
  s.haruX=590;
  boolean moving=CatSocialEngine.tick(s,11000L);
  assertTrue(s.catSocial.lastApproachPressure>.45);assertTrue(s.catSocial.wariness>.44);
  assertTrue(moving);assertEquals("RETREAT",s.catSocial.mode);assertTrue(s.catTravel.active);assertTrue(s.catTravel.segmentEndX>s.catState.x);
 }

 @Test public void familiarCatVoluntarilyApproachesButStopsShort(){
  WorldState s=state(300,540);s.catSocial.familiarity=.84;s.catSocial.comfort=.80;s.catSocial.wariness=.08;s.catSocial.curiosity=.66;
  boolean moving=CatSocialEngine.tick(s,20000L);
  assertTrue(moving);assertEquals("APPROACH",s.catSocial.mode);assertTrue(s.catTravel.active);
  float frozen=s.catSocial.frozenTargetX;assertTrue(frozen<s.catState.x);assertTrue(frozen>s.haruX);assertTrue(frozen-s.haruX>55);
  TravelEngine.advanceSeconds(s,s.catTravel,5.0,25000L);
  assertFalse(s.catTravel.active);assertTrue(s.catState.x<s.catX+1);assertEquals("",s.catSocial.activeTravelId);assertTrue(s.catSocial.approaches>=1);
  assertTrue(s.worldHistory.stream().anyMatch(e->"CAT_SOCIAL_RESPONSE_COMPLETED".equals(e.type)));
 }

 @Test public void recentPlayerActivitySuppressesAndCancelsAutonomousMovement(){
  WorldState s=state(300,540);s.catSocial.familiarity=.90;s.catSocial.comfort=.88;s.catSocial.wariness=.04;
  assertTrue(CatSocialEngine.tick(s,30000L));assertTrue(s.catTravel.active);
  CatSocialEngine.notePlayerActive(s,30500L);
  assertFalse(s.catTravel.active);assertEquals("",s.catSocial.activeTravelId);assertEquals("player_override",s.catTravel.interruption);
  float before=s.catState.x;assertFalse(CatSocialEngine.tick(s,32000L));assertEquals(before,s.catState.x,.001f);
  assertTrue(s.worldHistory.stream().anyMatch(e->"CAT_SOCIAL_RESPONSE_YIELDED".equals(e.type)));
 }

 @Test public void tiredComfortableCatCanSettleNearWithoutAttaching(){
  WorldState s=state(500,590);s.catState.energy=52;s.catState.sleepiness=62;s.catSocial.familiarity=.78;s.catSocial.comfort=.82;s.catSocial.wariness=.08;s.catSocial.curiosity=.4;
  boolean moving=CatSocialEngine.tick(s,40000L);
  assertFalse(moving);assertEquals("SETTLE_NEAR",s.catSocial.mode);assertEquals("",s.catState.attachedToEntity);assertFalse(s.catTravel.active);assertTrue(s.catSocial.settles>=1);
 }

 @Test public void hiddenHaruDoesNotDriveCatSocialResponse(){
  WorldState s=state(100,700);s.catSocial.familiarity=.90;s.catSocial.comfort=.90;s.catSocial.wariness=.05;
  assertFalse(CatSocialEngine.locallySeesGirl(s));assertFalse(CatSocialEngine.tick(s,50000L));
  assertFalse(s.catTravel.active);assertEquals("",s.catSocial.gazeTarget);
 }

 @Test public void activeAndOfflineKernelUseSameCatSocialPhysics(){
  WorldState active=state(300,540),offline=state(300,540);friendly(active);friendly(offline);
  LifeSimulationKernel.beginSlice(active,3.0,60000L,LifeSimulationKernel.Mode.ACTIVE);
  LifeSimulationKernel.beginSlice(offline,3.0,60000L,LifeSimulationKernel.Mode.OFFLINE);
  LifeSimulationKernel.endSlice(active,3.0,60000L,false,false,LifeSimulationKernel.Mode.ACTIVE);
  LifeSimulationKernel.endSlice(offline,3.0,60000L,false,false,LifeSimulationKernel.Mode.OFFLINE);
  assertEquals(active.catState.x,offline.catState.x,.001f);assertEquals(active.catSocial.mode,offline.catSocial.mode);assertEquals(active.catSocial.familiarity,offline.catSocial.familiarity,.000001);assertEquals(active.catSocial.comfort,offline.catSocial.comfort,.000001);
 }

 @Test public void catSocialStateSurvivesSaveRoundTrip() throws Exception{
  WorldState s=state(500,620);s.catSocial.familiarity=.73;s.catSocial.comfort=.66;s.catSocial.wariness=.19;s.catSocial.curiosity=.81;s.catSocial.mode="WATCH";s.catSocial.gazeTarget="girl";s.catSocial.lastObservedAt=70000L;s.catSocial.frozenTargetX=555;
  WorldState x=WorldState.fromJson(s.toJson());
  assertEquals(.73,x.catSocial.familiarity,.000001);assertEquals(.66,x.catSocial.comfort,.000001);assertEquals(.19,x.catSocial.wariness,.000001);assertEquals("girl",x.catSocial.gazeTarget);assertEquals(555,x.catSocial.frozenTargetX,.001f);
 }

 @Test public void sleepNearPreferenceUsesCatStateNotHarusPrivateRelationship(){
  WorldState comfortable=state(500,610),wary=state(500,610);
  comfortable.relationship.comfort=0;comfortable.relationship.trust=0;wary.relationship.comfort=100;wary.relationship.trust=100;
  comfortable.catSocial.familiarity=.90;comfortable.catSocial.comfort=.90;comfortable.catSocial.wariness=.03;
  wary.catSocial.familiarity=.08;wary.catSocial.comfort=.05;wary.catSocial.wariness=.92;
  double comfortableScore=withGirlScore(comfortable),waryScore=withGirlScore(wary);
  assertTrue(comfortableScore>waryScore);
 }

 private static double withGirlScore(WorldState s){for(CatSleepPlanner.Plan p:CatSleepPlanner.candidates(s))if("WITH_GIRL".equals(p.mode))return p.score;return -999;}
 private static void friendly(WorldState s){s.catSocial.familiarity=.86;s.catSocial.comfort=.82;s.catSocial.wariness=.06;s.catSocial.curiosity=.62;}
 private static WorldState state(float haru,float cat){
  WorldState s=WorldState.fresh();s.createdAt=100;s.lastOpenedAt=100;s.lastSimulatedAt=100;s.haruX=haru;s.catX=cat;s.world=new WorldModel();s.world.areas.add(new WorldArea("test","Test","test",0,1000,846,true,"test"));s.world.areas.add(new WorldArea("home_shelter","Home","home",1001,1500,846,false,"safe rest"));s.world.area("test").connections.add("home_shelter");s.world.area("home_shelter").connections.add("test");
  s.catState=CatState.fromJson(null,cat,0);s.catState.x=cat;s.catState.areaId="test";s.catState.awake=true;s.catState.attachedToEntity="";s.catState.carryKnownByGirl=false;s.catState.lastPlayerActiveAt=0;s.catState.energy=90;s.catState.sleepiness=10;
  s.catTravel=new TravelState();s.catSocial=new CatSocialState();s.catPhysics=PhysicsBodyState.fromJson(null,4.2);s.catPhysics.grounded=true;s.catPhysics.falling=false;s.catPhysics.balance=1;s.catPhysics.traction=1;s.catPhysics.velocityX=0;s.catNervous=new CatNervousState();s.visibility=1;
  s.girlTravel=new TravelState();s.planState=new PlanState();s.body.energy=90;s.body.sleepiness=10;s.body.pain=0;return s;
 }
}
