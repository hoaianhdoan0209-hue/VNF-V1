package com.aicharacter.v3;
import org.json.*;
import java.util.*;

/** Persistent intention execution state. Arrival, action, learning and review are distinct causal steps. */
public final class PlanState{
 public String planId="",intentionId="",goal="",status="IDLE",lastReason="",destination="",plannedAction="",lastDecision="",lastArrival="",lastAction="",lastOutcome="",pauseReason="",lastOutcomeReview="";
 public String origin="LEGACY",candidateGoalId="",triggerEvidenceId="",questionId="",reasoningHypothesisId="",predictionId="",causalAdaptationId="",adaptationPolicy="";
 public String outcomeMemoryId="";
 public double commitment=0;
 public long createdAt=0,lastReconsideredAt=0,pausedAt=0,lastProgressAt=0;
 public long arrivedAt=0,actionResolvedAt=0,outcomeLearnedAt=0,postOutcomeReviewedAt=0;
 public final List<String> steps=new ArrayList<>();
 public int stepIndex=0;

 public boolean active(){return "ACTIVE".equals(status)||"PAUSED".equals(status);}
 public boolean terminal(){return "COMPLETED".equals(status)||"FAILED".equals(status)||"ABANDONED".equals(status);}
 public static long transitionHoldMs(){return 12000L;}
 public boolean inTerminalTransition(long now){return terminal()&&lastProgressAt>0&&now>=lastProgressAt&&now-lastProgressAt<transitionHoldMs();}
 public boolean outcomeNeedsReview(){return terminal()&&outcomeLearnedAt>0&&postOutcomeReviewedAt<outcomeLearnedAt;}

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("planId",planId);j.put("intentionId",intentionId);j.put("goal",goal);j.put("status",status);j.put("lastReason",lastReason);j.put("origin",origin);j.put("candidateGoalId",candidateGoalId);j.put("triggerEvidenceId",triggerEvidenceId);j.put("questionId",questionId);j.put("reasoningHypothesisId",reasoningHypothesisId);j.put("predictionId",predictionId);j.put("causalAdaptationId",causalAdaptationId);j.put("adaptationPolicy",adaptationPolicy);
  j.put("destination",destination);j.put("plannedAction",plannedAction);j.put("lastDecision",lastDecision);j.put("lastArrival",lastArrival);
  j.put("lastAction",lastAction);j.put("lastOutcome",lastOutcome);j.put("pauseReason",pauseReason);j.put("lastOutcomeReview",lastOutcomeReview);j.put("outcomeMemoryId",outcomeMemoryId);
  j.put("commitment",commitment);j.put("createdAt",createdAt);j.put("lastReconsideredAt",lastReconsideredAt);j.put("pausedAt",pausedAt);
  j.put("lastProgressAt",lastProgressAt);j.put("arrivedAt",arrivedAt);j.put("actionResolvedAt",actionResolvedAt);j.put("outcomeLearnedAt",outcomeLearnedAt);
  j.put("postOutcomeReviewedAt",postOutcomeReviewedAt);j.put("steps",new JSONArray(steps));j.put("stepIndex",stepIndex);
 }catch(Exception ignored){}return j;}

 public static PlanState fromJson(JSONObject j){PlanState p=new PlanState();if(j==null)return p;
  p.planId=j.optString("planId","");p.intentionId=j.optString("intentionId","");p.goal=j.optString("goal","");p.status=j.optString("status","IDLE");p.origin=j.optString("origin","LEGACY");p.candidateGoalId=j.optString("candidateGoalId","");p.triggerEvidenceId=j.optString("triggerEvidenceId","");p.questionId=j.optString("questionId","");p.reasoningHypothesisId=j.optString("reasoningHypothesisId","");p.predictionId=j.optString("predictionId","");p.causalAdaptationId=j.optString("causalAdaptationId","");p.adaptationPolicy=j.optString("adaptationPolicy","");
  p.lastReason=j.optString("lastReason","");p.destination=j.optString("destination","");p.plannedAction=j.optString("plannedAction","");
  p.lastDecision=j.optString("lastDecision","");p.lastArrival=j.optString("lastArrival","");p.lastAction=j.optString("lastAction","");
  p.lastOutcome=j.optString("lastOutcome","");p.pauseReason=j.optString("pauseReason","");p.lastOutcomeReview=j.optString("lastOutcomeReview","");p.outcomeMemoryId=j.optString("outcomeMemoryId","");
  p.commitment=j.optDouble("commitment",0);p.createdAt=j.optLong("createdAt",0);p.lastReconsideredAt=j.optLong("lastReconsideredAt",0);
  p.pausedAt=j.optLong("pausedAt",0);p.lastProgressAt=j.optLong("lastProgressAt",p.createdAt);p.arrivedAt=j.optLong("arrivedAt",0);
  p.actionResolvedAt=j.optLong("actionResolvedAt",0);p.outcomeLearnedAt=j.optLong("outcomeLearnedAt",0);p.postOutcomeReviewedAt=j.optLong("postOutcomeReviewedAt",0);
  JSONArray a=j.optJSONArray("steps");if(a!=null)for(int i=0;i<a.length();i++)p.steps.add(a.optString(i));p.stepIndex=j.optInt("stepIndex",0);
  return p;
 }
}
