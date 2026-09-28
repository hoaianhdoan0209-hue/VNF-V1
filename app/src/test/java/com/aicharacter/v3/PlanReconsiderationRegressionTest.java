package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PlanReconsiderationRegressionTest {
 private static final long T0=1_800_000_000_000L;

 @Test public void lifeReplanStopsOldGoalInsteadOfRestartingSameDestination(){
  WorldState s=WorldState.fresh();
  PlanState p=new PlanState();
  p.planId="old_lake_plan";
  p.status="ACTIVE";
  p.intentionId="observe_lake";
  p.destination="bench_lake_01";
  p.plannedAction="OBSERVE";
  p.createdAt=T0-30000L;
  p.lastProgressAt=T0-5000L;
  s.planState=p;
  s.currentIntention=p.intentionId;
  s.persistentIntentionTarget=p.destination;
  s.intentionStartedAt=p.createdAt;
  s.girlTravel.active=true;
  s.girlTravel.currentPlanId=p.planId;
  s.girlTravel.destinationArea="lakeside";

  PlanReconsiderationEngine.Result r=new PlanReconsiderationEngine.Result(
    PlanReconsiderationEngine.Decision.REPLAN,
    "new evidence makes another life choice worth evaluating",
    .74,0,"","new evidence");

  PlanExecutor.applyDecision(s,r,T0);

  assertEquals("ABANDONED",p.status);
  assertFalse("life-level REPLAN must not restart travel to the old destination",s.girlTravel.active);
  assertEquals("",s.currentIntention);
  assertEquals("",s.persistentIntentionTarget);
  assertEquals("changing course after new evidence",s.haruActivity);
  assertTrue(s.worldHistory.stream().anyMatch(e->"PLAN_RECONSIDERED".equals(e.type)&&p.planId.equals(e.entity)));
 }

 @Test public void genuineReconsiderationCanBeSpokenWithoutInventingACause(){
  WorldState s=WorldState.fresh();
  s.haruSpeech=new HaruProactiveSpeechState();
  WorldEventBus.publishId(s,T0,"reconsider_test","PLAN_RECONSIDERED","plan_x","reason=body pressure changed pressure=0.74");

  assertTrue(HaruProactiveSpeechEngine.advance(s,T0,LifeSimulationKernel.Mode.ACTIVE));
  assertEquals("reconsider_test",s.haruSpeech.pendingSourceEventId);
  assertTrue(s.haruSpeech.pendingText.contains("đổi hướng"));
 }
 @Test public void visibleCatCanTriggerReconsiderationEvenWhenHaruIsNotTravelling(){
  WorldState s=WorldState.fresh();
  s.world=new WorldModel();
  s.world.areas.add(new WorldArea("home_shelter","Nhà","nơi trú",0,1000,846,true,"interior,shelter,home,dry,quiet"));
  s.haruX=500f;
  s.catX=520f;
  s.catState.x=520f;
  s.catState.areaId="home_shelter";
  s.relationship.attachment=100;
  s.body.energy=95;
  s.body.sleepiness=5;
  s.body.pain=0;
  s.body.health=100;
  s.digestive.stomachFood=.82;
  s.digestive.nutrientReserve=.88;
  s.hydration.hydration=.95;
  s.hydration.bladderFill=.05;
  s.environment.weather="CLEAR";
  s.girlTravel.active=false;
  s.girlTravel.interruption="";

  PlanState p=new PlanState();
  p.planId="quiet_observation";
  p.status="ACTIVE";
  p.intentionId="observe_lake";
  p.destination="lakeside";
  p.plannedAction="OBSERVE";
  p.createdAt=T0-30000L;
  p.lastProgressAt=T0-5000L;
  p.commitment=0;
  s.planState=p;
  s.currentIntention=p.intentionId;

  PlanReconsiderationEngine.Result r=PlanReconsiderationEngine.evaluate(s,T0);

  assertTrue("the cat is locally visible through HaruVision",HaruVisionEngine.observe(s).catVisible);
  assertEquals("a highly relevant nearby cat should make a low-commitment leisure plan genuinely reconsider",PlanReconsiderationEngine.Decision.REPLAN,r.decision);
  assertTrue(r.contradictory.contains("cat"));
 }

}
