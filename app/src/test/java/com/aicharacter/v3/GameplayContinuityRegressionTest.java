package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class GameplayContinuityRegressionTest {
 private static final long T0=1_780_000_000_000L;

 @Test public void interruptedActivePlanRecoversWithoutWaitingForPlayer(){
  WorldState s=state();
  s.haruX=120f;
  s.planState=new PlanState();
  s.planState.planId="plan_test";
  s.planState.intentionId="observe_lake";
  s.planState.goal="observe destination";
  s.planState.status="ACTIVE";
  s.planState.destination="b";
  s.planState.plannedAction="OBSERVE";
  s.planState.createdAt=T0;
  s.planState.lastProgressAt=T0;
  s.planState.lastReconsideredAt=T0-PlanState.transitionHoldMs()-1;
  s.currentIntention="observe_lake";
  s.girlTravel.active=false;

  HaruAutonomyEngine.tickDecision(s,T0+PlanState.transitionHoldMs()+10);
  assertTrue("active plan must restore physical travel",s.girlTravel.active);
  assertEquals("plan_test",s.girlTravel.currentPlanId);
  assertEquals("b",s.girlTravel.destinationArea);
 }

 @Test public void invalidSavedActorPositionsAreRepairedIntoWorld(){
  WorldState s=state();
  s.haruX=Float.NaN;
  s.catState.x=Float.POSITIVE_INFINITY;
  s.catX=Float.NEGATIVE_INFINITY;
  StateInvariantChecker.normalize(s,T0);
  assertTrue(Float.isFinite(s.haruX));
  assertTrue(Float.isFinite(s.catState.x));
  assertTrue(Float.isFinite(s.catX));
  assertNotNull("Haru must remain in a world area",s.world.areaAt(s.haruX));
  assertNotNull("cat must remain in a world area",s.world.areaAt(s.catState.x));
  assertEquals(s.catState.x,s.catX,0.0001f);
 }

 @Test public void shortOfflineCatchupKeepsHaruAndCatSpatiallyValid(){
  WorldState s=state();
  s.lastOpenedAt=T0;
  s.lastSimulatedAt=T0;
  s.lastSavedAt=T0;
  OfflineLifeEngine.reconstruct(s,T0+5L*60000L);
  assertTrue(Float.isFinite(s.haruX));
  assertTrue(Float.isFinite(s.catState.x));
  assertNotNull(s.world.areaAt(s.haruX));
  assertNotNull(s.world.areaAt(s.catState.x));
  assertEquals(T0+5L*60000L,s.lastSimulatedAt);
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();
  s.createdAt=T0;s.lastOpenedAt=T0;s.lastSimulatedAt=T0;s.lastSavedAt=T0;
  s.world=world();
  s.haruX=120f;s.catX=160f;s.catState.x=160f;s.catState.areaId="a";
  s.girlPhysics.grounded=true;s.girlPhysics.falling=false;s.girlPhysics.groundClearanceM=0;
  s.catPhysics.grounded=true;s.catPhysics.falling=false;s.catPhysics.groundClearanceM=0;
  return s;
 }

 private static WorldModel world(){
  WorldModel w=new WorldModel();
  WorldArea a=new WorldArea("a","A","A",0,500,846,true,"safe,vegetation");
  WorldArea b=new WorldArea("b","B","B",500,1000,846,true,"lake,vegetation");
  a.connections.add("b");b.connections.add("a");
  w.areas.add(a);w.areas.add(b);
  return w;
 }
}
