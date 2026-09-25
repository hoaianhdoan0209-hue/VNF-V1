package com.aicharacter.v3;

/** Shared goal-selection entrypoint used by active and offline cognition. */
public final class HaruGoalSelectionEngine {
 private HaruGoalSelectionEngine(){}
 public static void selectAndBegin(WorldState s,long now){
  if(s==null)return;
  if(HaruVisibleAutonomyEngine.beginIfNeeded(s,now))return;
  if(HaruAffordanceEngine.beginPlanIfCompelling(s,now))return;
  LifeDecision d=LifeDecisionEngine.choose(s,now);
  OfflineLifeEngine.beginDecision(s,d,now);
 }
}
