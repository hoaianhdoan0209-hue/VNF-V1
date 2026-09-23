package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SocialProximityEngineTest {

 @Test public void warmRelationshipCreatesBoundedApproachOnlyAfterAutonomySelectsIt(){
  WorldState s=state(500,610);warm(s);
  SocialProximityEngine.Assessment a=SocialProximityEngine.assess(s,1000L);
  assertTrue(a.perceived);assertTrue(a.adjustmentNeeded);assertEquals("APPROACH",a.mode);assertTrue(a.targetX>s.haruX);assertTrue(a.targetX<s.catState.x);
  assertFalse(s.planState.active());assertFalse(s.girlTravel.active);

  LifeDecision d=socialDecision(1000L);OfflineLifeEngine.beginDecision(s,d,1000L);
  assertEquals("social_adjust",s.planState.intentionId);assertEquals("SOCIAL_MICRO",s.planState.origin);assertTrue(s.girlTravel.active);assertEquals(s.planState.planId,s.girlTravel.currentPlanId);
  float target=s.girlTravel.segmentEndX;assertEquals(a.targetX,target,.001f);
  TravelEngine.advanceSeconds(s,s.girlTravel,3.0,4000L);
  assertEquals("COMPLETED",s.planState.status);assertFalse(s.girlTravel.active);assertTrue(s.haruX>500);assertTrue(s.haruX<610);
  assertTrue(s.memories.stream().anyMatch(m->m.hasTag("social_adjust")&&m.hasTag("cat")&&m.hasTag("relationship")));
 }

 @Test public void hurtAngerAndFearCreateGuardedRetreatWithoutHiddenTracking(){
  WorldState s=state(500,555);guarded(s);
  SocialProximityEngine.Assessment a=SocialProximityEngine.assess(s,2000L);
  assertTrue(a.perceived);assertTrue(a.adjustmentNeeded);assertEquals("GUARDED",a.mode);assertTrue(a.desiredMin>120);assertTrue(a.targetX<s.haruX);
  double observed=a.distance;

  OfflineLifeEngine.beginDecision(s,socialDecision(2000L),2000L);float frozen=s.girlTravel.segmentEndX;
  s.catState.x=820;s.catX=820; // no longer locally perceived; target must not chase this hidden movement
  assertFalse(SocialProximityEngine.shouldOrientToCat(s));assertEquals(frozen,s.girlTravel.segmentEndX,.001f);
  TravelEngine.advanceSeconds(s,s.girlTravel,4.0,6000L);
  assertEquals("COMPLETED",s.planState.status);assertEquals(observed,s.socialProximity.lastObservedDistance,.001);
  assertTrue(s.worldHistory.stream().anyMatch(e->"HARU_SOCIAL_SPACING_COMPLETED".equals(e.type)&&e.summary.contains("catNoLongerLocallyPerceived=true")));
 }

 @Test public void urgentBodyStateSuppressesSocialAdjustment(){
  WorldState s=state(500,610);warm(s);s.body.energy=15;s.body.pain=42;
  SocialProximityEngine.Assessment a=SocialProximityEngine.assess(s,3000L);
  assertTrue(a.perceived);assertFalse(a.adjustmentNeeded);assertEquals(0,a.utility,.000001);
 }

 @Test public void warmEyeContactIsLongerWhileGuardedStateLooksDownMore(){
  WorldState warm=state(500,590);warm(warm);warm.currentIntention="social_adjust";
  WorldState guard=state(500,590);guarded(guard);guard.currentIntention="social_adjust";
  HaruExpressionEngine.Visual w=HaruExpressionEngine.derive(warm,4000L),g=HaruExpressionEngine.derive(guard,4000L);
  assertTrue(w.socialFocus);assertTrue(g.socialFocus);assertTrue(Math.abs(w.gazeX)>Math.abs(g.gazeX));assertTrue(g.gazeY>w.gazeY);
 }

 @Test public void bodyTurnsTowardCatOnLeftAndMirrorsStableEmotionPose(){
  WorldState idle=state(500,420);warm(idle);idle.haruActivity="idle";
  GirlAnimationController.Visual iv=GirlAnimationController.select(idle);
  assertEquals(GirlAnimationController.State.IDLE,iv.state);assertEquals("girl_idle_left",iv.asset);

  WorldState joyful=state(500,420);warm(joyful);joyful.haruActivity="idle";joyful.emotion.joy=.84;EmotionEpisodeState e=new EmotionEpisodeState();e.id="social_joy";e.primaryEmotion="joyful";e.intensity=.82;e.joy=.88;e.targetId="cat";e.socialRelevance=.9;e.status="ACTIVE";e.createdAt=5000;e.updatedAt=5000;joyful.emotionEpisodes.add(e);joyful.lastSimulatedAt=5000;joyful.lastOpenedAt=5000;
  GirlAnimationController.Visual jv=GirlAnimationController.select(joyful);
  assertEquals(GirlAnimationController.State.REACT,jv.state);assertTrue(jv.flipX);
 }

 @Test public void socialSpacingStateSurvivesSaveRoundTrip() throws Exception{
  WorldState s=state(500,610);warm(s);SocialProximityEngine.Assessment a=SocialProximityEngine.assess(s,6000L);
  s.socialProximity.lastAdjustmentAt=6000L;s.socialProximity.activePlanId="example";
  WorldState x=WorldState.fromJson(s.toJson());
  assertEquals(a.mode,x.socialProximity.mode);assertEquals(a.desiredMin,x.socialProximity.desiredMin,.000001);assertEquals(a.desiredMax,x.socialProximity.desiredMax,.000001);assertEquals("example",x.socialProximity.activePlanId);assertTrue(Float.isFinite(x.socialProximity.lastTargetX));
 }

 private static LifeDecision socialDecision(long now){return new LifeDecision(new Intention("social_adjust",0,"connection","cat","settle at a self-chosen social distance",.20,now+1800000)).reason("test",10);}
 private static void warm(WorldState s){s.relationship.affection=92;s.relationship.trust=91;s.relationship.attachment=86;s.relationship.comfort=94;s.relationship.hurt=2;s.relationship.irritation=2;s.relationship.clamp();s.emotion.joy=.25;s.emotion.fear=.02;s.emotion.anger=.01;s.emotion.loneliness=.22;}
 private static void guarded(WorldState s){s.relationship.affection=35;s.relationship.trust=24;s.relationship.attachment=52;s.relationship.comfort=18;s.relationship.hurt=92;s.relationship.irritation=84;s.relationship.clamp();s.emotion.fear=.72;s.emotion.anger=.56;s.emotion.sadness=.32;}
 private static WorldState state(float haru,float cat){
  WorldState s=WorldState.fresh();s.createdAt=100;s.lastOpenedAt=100;s.lastSimulatedAt=100;s.haruX=haru;s.catX=cat;s.world=new WorldModel();s.world.areas.add(new WorldArea("test","Test","test",0,1000,846,true,"test"));s.catState=CatState.fromJson(null,cat,100);s.catState.x=cat;s.catState.areaId="test";s.catState.awake=true;s.catState.attachedToEntity="";s.catState.carryKnownByGirl=false;s.body.energy=90;s.body.sleepiness=8;s.body.pain=0;s.girlPhysics.grounded=true;s.girlPhysics.groundClearanceM=0;s.currentIntention="";s.haruActivity="idle";s.planState=new PlanState();s.girlTravel=new TravelState();s.socialProximity=new SocialProximityState();s.emotion.joy=0;s.emotion.fear=0;s.emotion.sadness=0;s.emotion.anger=0;s.emotion.curiosity=0;s.emotion.loneliness=0;s.emotion.calm=.65;return s;
 }
}
