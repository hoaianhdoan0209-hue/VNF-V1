package com.aicharacter.v3;

/**
 * Reviews a finished plan only after its outcome memory has already been learned.
 * This phase records causal availability of evidence; it never selects or starts
 * a new intention.
 */
public final class PlanOutcomeReviewEngine {
 private PlanOutcomeReviewEngine(){}

 public static boolean reviewIfReady(WorldState s,long now){
  if(s==null||s.planState==null)return false;
  PlanState p=s.planState;
  if(!p.outcomeNeedsReview())return false;
  MemoryEntry m=PlanCausalAudit.findMemory(s,p.outcomeMemoryId);
  if(m==null){PlanCausalAudit.reportIfInvalid(s,now);return false;}
  double expectation=AdaptiveBeliefEngine.planExpectation(s,p.intentionId);
  double preference=0;
  PreferenceState pref=s.preferences.get("activity:"+activityKey(p.intentionId));
  if(pref!=null)preference=pref.value;
  p.lastOutcomeReview="POST_OUTCOME_REVIEW: memory="+m.memoryId+
   " valence="+fmt(m.valence)+" expectation="+fmt(expectation)+" preference="+fmt(preference);
  PlanCausalAudit.reviewed(s,p,now,p.lastOutcomeReview);
  return true;
 }

 private static String activityKey(String id){
  if(id==null)return "";
  if(id.equals("sleep")||id.equals("seek_shelter")||id.equals("recover"))return "rest";
  if(id.equals("find_cat"))return "search";
  if(id.equals("explore_garden"))return "explore";
  if(id.equals("observe_lake")||id.equals("watch_reedling")||id.equals("study_ecology")||id.equals("compare_concept"))return "observe";
  if(id.equals("quiet_pause")||id.equals("seek_solitude"))return "solitude";
  if(id.equals("reflect"))return "reflect";
  return id;
 }
 private static String fmt(double v){return String.format(java.util.Locale.US,"%.3f",v);}
}
