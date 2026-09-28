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
}
