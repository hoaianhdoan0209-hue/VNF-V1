package com.aicharacter.v3;

/** Shared goal-selection entrypoint used by active and offline cognition. */
public final class HaruGoalSelectionEngine {
 private HaruGoalSelectionEngine(){}
 public static void selectAndBegin(WorldState s,long now){
  if(s==null)return;
  if(HaruVisibleAutonomyEngine.beginIfNeeded(s,now))return;
  if(HaruVisibleAutonomyEngine.reserveStartupVisibleChoice(s,now)){s.haruActivity="looking around and deciding what to inspect";return;}
  if(HaruAffordanceEngine.beginPlanIfCompelling(s,now))return;
  LifeDecision d=LifeDecisionEngine.choose(s,now);
  OfflineLifeEngine.beginDecision(s,d,now);
 }
}
