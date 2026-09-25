package com.aicharacter.v3;
/** Active autonomy evaluates at meaningful intervals, then executes a persistent plan instead of rerolling every frame. */
public final class HaruAutonomyEngine {private static final long IDLE_DECISION_INTERVAL_MS=3000L,NON_TRAVEL_PLAN_REVIEW_MS=6000L,TRAVEL_RECONSIDER_MS=PlanState.transitionHoldMs();private HaruAutonomyEngine(){}
 public static void tick(WorldState s,float dt){tick(s,dt,System.currentTimeMillis());}
 public static void tick(WorldState s,float dt,long now){if(s==null)return;advanceMindBody(s,dt,now);tickDecision(s,now);}

 /**
  * Shared V3 body/mind clock. The simulation kernel calls this for both active
  * and offline slices so fatigue, sleep pressure and affect obey one timeline.
  */
 static void advanceMindBody(WorldState s,double seconds,long now){
  if(s==null||seconds<=0)return;
  BodyRhythmEngine.advanceSeconds(s,seconds);
  if(s.environment!=null)s.environment.updateForTime(s.worldMinutes);
  if(s.mood!=null)s.mood.decay(Math.pow(.9997,seconds));
  DopamineModulationEngine.advance(s,seconds,now);
  EmotionRegulationEngine.advance(s,seconds,now);
  // Same causal learning boundary in ACTIVE and OFFLINE slices. God only
  // creates the external offer; Haru owns whether it becomes lived learning.
  HaruTeachingOpportunityEngine.observe(s,now);
  HaruAffordanceEngine.observeQuestions(s,now);
  HaruReasoningEngine.observe(s,now);
 }

 /**
  * Decision-only phase. Time-dependent physiology is intentionally excluded;
  * callers must advance it once before reaching this method.
  */
 static void tickDecision(WorldState s,long now){
  if(s==null)return;
  if(!BodyRhythmEngine.isSleeping(s)&&BodyRhythmEngine.shouldCollapse(s)){BodyRhythmEngine.collapse(s,now);return;}
  if(BodyRhythmEngine.shouldRemainAsleep(s))return;
  if(BodyRhythmEngine.isSleeping(s)){if(!BodyRhythmEngine.recoveredEnoughToAct(s))return;s.haruActivity="waking slowly";s.currentIntention="";s.persistentIntentionTarget="";s.intentionStartedAt=now;}
  if(s.planState==null)s.planState=new PlanState();
  if(s.girlTravel!=null&&s.girlTravel.active){
   long travelAnchor=s.planState.lastReconsideredAt;
   if(travelAnchor<=0||now-travelAnchor>=TRAVEL_RECONSIDER_MS){
    PlanIntegrityChecker.check(s,now);
    PlanReconsiderationEngine.Result travelDecision=PlanReconsiderationEngine.evaluate(s,now);
    s.planState.lastReconsideredAt=now;
    if(travelDecision.reconsider())PlanExecutor.applyDecision(s,travelDecision,now);
   }
   return;
  }
  long anchor=s.planState.lastReconsideredAt;
  long interval=s.planState.active()?NON_TRAVEL_PLAN_REVIEW_MS:IDLE_DECISION_INTERVAL_MS;
  if(anchor>0&&now-anchor<interval)return;
  if("PAUSED".equals(s.planState.status)){s.planState.lastReconsideredAt=now;if(PlanExecutor.resume(s,now))return;}
  if(s.planState.inTerminalTransition(now))return;
  if(s.planState.terminal())PlanOutcomeReviewEngine.reviewIfReady(s,now);
  PlanIntegrityChecker.check(s,now);
  PlanReconsiderationEngine.Result rr=PlanReconsiderationEngine.evaluate(s,now);
  s.planState.lastReconsideredAt=now;
  if(s.planState.active()&&!rr.reconsider())return;
  if(rr.reconsider()){PlanExecutor.applyDecision(s,rr,now);if(s.planState.active())return;}
  HaruGoalSelectionEngine.selectAndBegin(s,now);
  if(s.mood!=null)s.haruMood=s.mood.label();
 }
}
