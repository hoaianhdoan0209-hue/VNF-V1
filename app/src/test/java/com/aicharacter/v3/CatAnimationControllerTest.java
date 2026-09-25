package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class CatAnimationControllerTest {

 @Test public void socialApproachAndRetreatMapToDifferentVisibleStates(){
  WorldState approach=state();approach.catSocial.mode="APPROACH";approach.catSocial.attention=.8;approach.catTravel.active=true;approach.catTravel.segmentEndX=approach.catState.x-80;
  CatAnimationController.Visual a=CatAnimationController.select(approach);
  assertEquals(CatAnimationController.State.APPROACH,a.state);assertFalse(a.facingRight);assertTrue(a.attention>.7);

  WorldState retreat=state();retreat.catSocial.mode="RETREAT";retreat.catSocial.wariness=.86;retreat.catTravel.active=true;retreat.catTravel.segmentEndX=retreat.catState.x+120;
  CatAnimationController.Visual r=CatAnimationController.select(retreat);
  assertEquals(CatAnimationController.State.RETREAT,r.state);assertTrue(r.facingRight);assertTrue(r.guardedness>.55);
 }

 @Test public void watchSettleSleepAndCarryRemainVisuallyDistinct(){
  WorldState watch=state();watch.catSocial.gazeTarget="girl";watch.catSocial.attention=.72;assertEquals(CatAnimationController.State.WATCH,CatAnimationController.select(watch).state);
  WorldState settle=state();settle.catSocial.mode="SETTLE_NEAR";assertEquals(CatAnimationController.State.SETTLE,CatAnimationController.select(settle).state);
  WorldState sleep=state();sleep.catState.awake=false;sleep.catState.sleepMode="HOME";assertEquals(CatAnimationController.State.SLEEP,CatAnimationController.select(sleep).state);
  WorldState carry=state();carry.catState.attachedToEntity="girl";assertEquals(CatAnimationController.State.ATTACHED,CatAnimationController.select(carry).state);
 }

 @Test public void physicalBalanceAlarmOverridesOptionalSocialPresentation(){
  WorldState s=state();s.catSocial.mode="APPROACH";s.catSocial.attention=.9;s.catNervous.balanceAlarm=.85;
  assertEquals(CatAnimationController.State.BRACE,CatAnimationController.select(s).state);
 }

 @Test public void HaruMotionStyleShowsDifferentApproachAndGuardedBodyLanguage(){
  WorldState toward=state();toward.currentIntention="social_adjust";toward.socialProximity.mode="APPROACH";toward.catState.x=toward.haruX+100;
  GirlAnimationController.Visual idle=GirlAnimationController.select(toward);HaruMotionStyleEngine.Style a=HaruMotionStyleEngine.derive(toward,idle,1.2f);
  WorldState guard=state();guard.currentIntention="social_adjust";guard.socialProximity.mode="GUARDED";guard.catState.x=guard.haruX+100;
  GirlAnimationController.Visual idle2=GirlAnimationController.select(guard);HaruMotionStyleEngine.Style g=HaruMotionStyleEngine.derive(guard,idle2,1.2f);
  assertTrue("approach should remain more cat-directed than guarded spacing",a.translateX>g.translateX);assertTrue("guarded spacing should be at least as settled",g.settle>=a.settle);
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("test","Test","test",0,1000,846,true,"test"));s.haruX=430;s.catX=560;s.catState=CatState.fromJson(null,560,0);s.catState.x=560;s.catState.areaId="test";s.catState.awake=true;s.catState.energy=85;s.catState.sleepiness=15;s.catState.attachedToEntity="";s.catTravel=new TravelState();s.catSocial=new CatSocialState();s.catNervous=new CatNervousState();s.catRig=new CatRigState();s.catPhysics=PhysicsBodyState.fromJson(null,4.2);s.catPhysics.grounded=true;s.catPhysics.falling=false;s.socialProximity=new SocialProximityState();s.girlTravel=new TravelState();s.girlPhysics=PhysicsBodyState.fromJson(null,52);s.girlPhysics.grounded=true;s.body.energy=90;s.body.sleepiness=10;s.environment.weather="CLEAR";s.environment.wind=0;s.lastOpenedAt=1000;s.lastSimulatedAt=1000;s.haruActivity="idle";return s;
 }
}
