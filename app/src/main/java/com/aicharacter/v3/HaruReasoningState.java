package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/**
 * Persistent evidence-based cognition owned by Haru.
 * Hypotheses are possibilities, not World Truth. They may only gain confidence
 * from lived causal evidence and can be revised when later evidence disagrees.
 */
public final class HaruReasoningState {
 public final Map<String,HypothesisState> hypotheses=new LinkedHashMap<>();
 public final Map<String,PredictionState> predictions=new LinkedHashMap<>();
 public final Map<String,CausalExplanationState> causalExplanations=new LinkedHashMap<>();
 public final Map<String,CausalExperimentState> causalExperiments=new LinkedHashMap<>();
 public final Map<String,GeneralRuleState> rules=new LinkedHashMap<>();
 public MetacognitiveCalibrationState calibration=new MetacognitiveCalibrationState();
 public long lastCycleAt;
 public int cycleCount;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  JSONObject h=new JSONObject();for(Map.Entry<String,HypothesisState>e:hypotheses.entrySet())h.put(e.getKey(),e.getValue().toJson());j.put("hypotheses",h);
  JSONObject p=new JSONObject();for(Map.Entry<String,PredictionState>e:predictions.entrySet())p.put(e.getKey(),e.getValue().toJson());j.put("predictions",p);
  JSONObject x=new JSONObject();for(Map.Entry<String,CausalExplanationState>e:causalExplanations.entrySet())x.put(e.getKey(),e.getValue().toJson());j.put("causalExplanations",x);
  JSONObject ce=new JSONObject();for(Map.Entry<String,CausalExperimentState>e:causalExperiments.entrySet())ce.put(e.getKey(),e.getValue().toJson());j.put("causalExperiments",ce);
  JSONObject r=new JSONObject();for(Map.Entry<String,GeneralRuleState>e:rules.entrySet())r.put(e.getKey(),e.getValue().toJson());j.put("rules",r);
  j.put("calibration",calibration==null?new MetacognitiveCalibrationState().toJson():calibration.toJson());
  j.put("lastCycleAt",lastCycleAt);j.put("cycleCount",cycleCount);
 }catch(Exception ignored){}return j;}

 public static HaruReasoningState fromJson(JSONObject j){HaruReasoningState s=new HaruReasoningState();if(j==null)return s;
  read(j.optJSONObject("hypotheses"),(k,v)->s.hypotheses.put(k,HypothesisState.fromJson(k,v)));
  read(j.optJSONObject("predictions"),(k,v)->s.predictions.put(k,PredictionState.fromJson(k,v)));
  read(j.optJSONObject("causalExplanations"),(k,v)->s.causalExplanations.put(k,CausalExplanationState.fromJson(k,v)));
  read(j.optJSONObject("causalExperiments"),(k,v)->s.causalExperiments.put(k,CausalExperimentState.fromJson(k,v)));
  read(j.optJSONObject("rules"),(k,v)->s.rules.put(k,GeneralRuleState.fromJson(k,v)));
  s.calibration=MetacognitiveCalibrationState.fromJson(j.optJSONObject("calibration"));
  s.lastCycleAt=Math.max(0,j.optLong("lastCycleAt"));s.cycleCount=Math.max(0,j.optInt("cycleCount"));return s;
 }
 private interface Reader{void accept(String key,JSONObject value);}
 private static void read(JSONObject o,Reader r){if(o==null)return;Iterator<String>it=o.keys();while(it.hasNext()){String k=it.next();JSONObject v=o.optJSONObject(k);if(v!=null)r.accept(k,v);}}
}

final class HypothesisState {
 public String id="",questionId="",subjectId="",expectedAreaId="",proposition="",alternative="",status="ACTIVE";
 public double supportWeight,contradictionWeight,confidence=.5;
 public long createdAt,updatedAt,lastEvidenceAt;
 public int evidenceCount,revisionCount;
 public final List<String> evidenceMemoryIds=new ArrayList<>();

 public void apply(boolean supports,double weight,String memoryId,long now){
  if(memoryId!=null&&!memoryId.isEmpty()&&evidenceMemoryIds.contains(memoryId))return;
  double w=Double.isFinite(weight)?Math.max(.02,Math.min(1.5,weight)):.1;
  double before=confidence;String prior=status;if(supports)supportWeight+=w;else contradictionWeight+=w;
  confidence=clamp((1.0+supportWeight)/(2.0+supportWeight+contradictionWeight));
  evidenceCount++;updatedAt=now;lastEvidenceAt=now;
  if(memoryId!=null&&!memoryId.isEmpty()){evidenceMemoryIds.add(memoryId);while(evidenceMemoryIds.size()>24)evidenceMemoryIds.remove(0);}
  boolean revised=("SUPPORTED".equals(prior)&&!supports&&confidence<.68)||("DISFAVORED".equals(prior)&&supports&&confidence>.32);
  if(revised){status="REVISED";revisionCount++;}
  else if("REVISED".equals(prior)&&confidence>.32&&confidence<.72)status="REVISED";
  else if(confidence>=.72&&supportWeight>=1.0)status="SUPPORTED";
  else if(confidence<=.32&&contradictionWeight>=1.0)status="DISFAVORED";
  else status="ACTIVE";
  if(Math.abs(confidence-before)>.0001)updatedAt=now;
 }
 public double informationNeed(){return 1.0-Math.min(1.0,Math.abs(confidence-.5)*2.0);}

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("id",id);j.put("questionId",questionId);j.put("subjectId",subjectId);j.put("expectedAreaId",expectedAreaId);j.put("proposition",proposition);j.put("alternative",alternative);j.put("status",status);
  j.put("supportWeight",supportWeight);j.put("contradictionWeight",contradictionWeight);j.put("confidence",confidence);j.put("createdAt",createdAt);j.put("updatedAt",updatedAt);j.put("lastEvidenceAt",lastEvidenceAt);j.put("evidenceCount",evidenceCount);j.put("revisionCount",revisionCount);j.put("evidenceMemoryIds",new JSONArray(evidenceMemoryIds));
 }catch(Exception ignored){}return j;}
 public static HypothesisState fromJson(String key,JSONObject j){HypothesisState h=new HypothesisState();if(j==null){h.id=key;return h;}h.id=j.optString("id",key);h.questionId=j.optString("questionId","");h.subjectId=j.optString("subjectId","");h.expectedAreaId=j.optString("expectedAreaId","");h.proposition=j.optString("proposition","");h.alternative=j.optString("alternative","");h.status=j.optString("status","ACTIVE");h.supportWeight=finite(j.optDouble("supportWeight"),0);h.contradictionWeight=finite(j.optDouble("contradictionWeight"),0);h.confidence=clamp(finite(j.optDouble("confidence",.5),.5));h.createdAt=Math.max(0,j.optLong("createdAt"));h.updatedAt=Math.max(0,j.optLong("updatedAt"));h.lastEvidenceAt=Math.max(0,j.optLong("lastEvidenceAt"));h.evidenceCount=Math.max(0,j.optInt("evidenceCount"));h.revisionCount=Math.max(0,j.optInt("revisionCount"));JSONArray a=j.optJSONArray("evidenceMemoryIds");if(a!=null)for(int i=0;i<a.length();i++){String x=a.optString(i,"");if(!x.isEmpty())h.evidenceMemoryIds.add(x);}return h;}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}private static double clamp(double v){return Math.max(0,Math.min(1,v));}
}

final class PredictionState {
 public String id="",planId="",hypothesisId="",subjectId="",expectedOutcome="",alternativeOutcome="",status="PENDING",outcomeMemoryId="";
 public double confidence=.5,dopamineAtPrediction,logicalControlAtPrediction=1,overdriveAtPrediction,calibrationError;
 public long createdAt,resolvedAt;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("planId",planId);j.put("hypothesisId",hypothesisId);j.put("subjectId",subjectId);j.put("expectedOutcome",expectedOutcome);j.put("alternativeOutcome",alternativeOutcome);j.put("status",status);j.put("outcomeMemoryId",outcomeMemoryId);j.put("confidence",confidence);j.put("dopamineAtPrediction",dopamineAtPrediction);j.put("logicalControlAtPrediction",logicalControlAtPrediction);j.put("overdriveAtPrediction",overdriveAtPrediction);j.put("calibrationError",calibrationError);j.put("createdAt",createdAt);j.put("resolvedAt",resolvedAt);}catch(Exception ignored){}return j;}
 public static PredictionState fromJson(String key,JSONObject j){PredictionState p=new PredictionState();if(j==null){p.id=key;return p;}p.id=j.optString("id",key);p.planId=j.optString("planId","");p.hypothesisId=j.optString("hypothesisId","");p.subjectId=j.optString("subjectId","");p.expectedOutcome=j.optString("expectedOutcome","");p.alternativeOutcome=j.optString("alternativeOutcome","");p.status=j.optString("status","PENDING");p.outcomeMemoryId=j.optString("outcomeMemoryId","");double c=j.optDouble("confidence",.5);p.confidence=finite01(c,.5);p.dopamineAtPrediction=finite01(j.optDouble("dopamineAtPrediction",0),0);p.logicalControlAtPrediction=finite01(j.optDouble("logicalControlAtPrediction",1),1);p.overdriveAtPrediction=finite01(j.optDouble("overdriveAtPrediction",0),0);double err=j.optDouble("calibrationError",0);p.calibrationError=Double.isFinite(err)?Math.max(-1,Math.min(1,err)):0;p.createdAt=Math.max(0,j.optLong("createdAt"));p.resolvedAt=Math.max(0,j.optLong("resolvedAt"));return p;}private static double finite01(double v,double f){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):f;}
}

final class CausalExplanationState {
 public String id="",predictionId="",planId="",intentionId="",subjectId="",contextKey="",causeType="",claim="",testablePrediction="",status="TENTATIVE";
 public double supportWeight,contradictionWeight,confidence=.5;
 public long createdAt,updatedAt;
 public int counterfactualChecks;
 public final List<String> evidenceEventIds=new ArrayList<>(),evidenceMemoryIds=new ArrayList<>();

 public void apply(boolean supports,double weight,String eventId,String memoryId,long now){
  String key=(eventId==null?"":eventId)+"|"+(memoryId==null?"":memoryId);for(String x:evidenceEventIds)if(x.equals(key))return;
  double w=Double.isFinite(weight)?Math.max(.02,Math.min(1.5,weight)):.1;if(supports)supportWeight+=w;else contradictionWeight+=w;
  confidence=Math.max(.02,Math.min(.98,(1.0+supportWeight)/(2.0+supportWeight+contradictionWeight)));
  if(!key.equals("|")){evidenceEventIds.add(key);while(evidenceEventIds.size()>24)evidenceEventIds.remove(0);}
  if(memoryId!=null&&!memoryId.isEmpty()&&!evidenceMemoryIds.contains(memoryId)){evidenceMemoryIds.add(memoryId);while(evidenceMemoryIds.size()>24)evidenceMemoryIds.remove(0);}
  if(confidence>=.68&&supportWeight>=.5)status="SUPPORTED";else if(confidence<=.32&&contradictionWeight>=.5)status="DISFAVORED";else status="TENTATIVE";updatedAt=now;
 }
 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("id",id);j.put("predictionId",predictionId);j.put("planId",planId);j.put("intentionId",intentionId);j.put("subjectId",subjectId);j.put("contextKey",contextKey);j.put("causeType",causeType);j.put("claim",claim);j.put("testablePrediction",testablePrediction);j.put("status",status);j.put("supportWeight",supportWeight);j.put("contradictionWeight",contradictionWeight);j.put("confidence",confidence);j.put("createdAt",createdAt);j.put("updatedAt",updatedAt);j.put("counterfactualChecks",counterfactualChecks);j.put("evidenceEventIds",new JSONArray(evidenceEventIds));j.put("evidenceMemoryIds",new JSONArray(evidenceMemoryIds));
 }catch(Exception ignored){}return j;}
 public static CausalExplanationState fromJson(String key,JSONObject j){CausalExplanationState x=new CausalExplanationState();if(j==null){x.id=key;return x;}x.id=j.optString("id",key);x.predictionId=j.optString("predictionId","");x.planId=j.optString("planId","");x.intentionId=j.optString("intentionId","");x.subjectId=j.optString("subjectId","");x.contextKey=j.optString("contextKey","");x.causeType=j.optString("causeType","");x.claim=j.optString("claim","");x.testablePrediction=j.optString("testablePrediction","");x.status=j.optString("status","TENTATIVE");x.supportWeight=finite(j.optDouble("supportWeight"),0);x.contradictionWeight=finite(j.optDouble("contradictionWeight"),0);x.confidence=Math.max(.02,Math.min(.98,finite(j.optDouble("confidence",.5),.5)));x.createdAt=Math.max(0,j.optLong("createdAt"));x.updatedAt=Math.max(0,j.optLong("updatedAt"));x.counterfactualChecks=Math.max(0,j.optInt("counterfactualChecks"));strings(j.optJSONArray("evidenceEventIds"),x.evidenceEventIds);strings(j.optJSONArray("evidenceMemoryIds"),x.evidenceMemoryIds);return x;}
 private static void strings(JSONArray a,List<String>out){if(a!=null)for(int i=0;i<a.length();i++){String v=a.optString(i,"");if(!v.isEmpty())out.add(v);}}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
}

final class CausalExperimentState {
 public String id="",sourcePredictionId="",causeAId="",causeBId="",intentionId="",subjectId="",strategy="",manipulatedVariable="",expectedIfA="",expectedIfB="",status="DESIGNED",activePlanId="",favoredCauseId="",outcomeMemoryId="",resultSummary="",baselineContextKey="",testContextKey="";
 public long createdAt,startedAt,resolvedAt;
 public int attempts;
 public boolean manipulationVerified;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("id",id);j.put("sourcePredictionId",sourcePredictionId);j.put("causeAId",causeAId);j.put("causeBId",causeBId);j.put("intentionId",intentionId);j.put("subjectId",subjectId);j.put("strategy",strategy);j.put("manipulatedVariable",manipulatedVariable);j.put("expectedIfA",expectedIfA);j.put("expectedIfB",expectedIfB);j.put("status",status);j.put("activePlanId",activePlanId);j.put("favoredCauseId",favoredCauseId);j.put("outcomeMemoryId",outcomeMemoryId);j.put("resultSummary",resultSummary);j.put("baselineContextKey",baselineContextKey);j.put("testContextKey",testContextKey);j.put("createdAt",createdAt);j.put("startedAt",startedAt);j.put("resolvedAt",resolvedAt);j.put("attempts",attempts);j.put("manipulationVerified",manipulationVerified);
 }catch(Exception ignored){}return j;}

 public static CausalExperimentState fromJson(String key,JSONObject j){CausalExperimentState x=new CausalExperimentState();if(j==null){x.id=key;return x;}
  x.id=j.optString("id",key);x.sourcePredictionId=j.optString("sourcePredictionId","");x.causeAId=j.optString("causeAId","");x.causeBId=j.optString("causeBId","");x.intentionId=j.optString("intentionId","");x.subjectId=j.optString("subjectId","");x.strategy=j.optString("strategy","");x.manipulatedVariable=j.optString("manipulatedVariable","");x.expectedIfA=j.optString("expectedIfA","");x.expectedIfB=j.optString("expectedIfB","");x.status=j.optString("status","DESIGNED");x.activePlanId=j.optString("activePlanId","");x.favoredCauseId=j.optString("favoredCauseId","");x.outcomeMemoryId=j.optString("outcomeMemoryId","");x.resultSummary=j.optString("resultSummary","");x.baselineContextKey=j.optString("baselineContextKey","");x.testContextKey=j.optString("testContextKey","");x.createdAt=Math.max(0,j.optLong("createdAt"));x.startedAt=Math.max(0,j.optLong("startedAt"));x.resolvedAt=Math.max(0,j.optLong("resolvedAt"));x.attempts=Math.max(0,j.optInt("attempts"));x.manipulationVerified=j.optBoolean("manipulationVerified",false);return x;
 }
}

final class MetacognitiveCalibrationState {
 public int stablePredictions,overdrivePredictions,overdriveOverconfidentMisses;
 public double stableAbsErrorSum,overdriveAbsErrorSum,overdriveBias;
 public long updatedAt;

 public void record(PredictionState p,boolean success,long now){
  if(p==null)return;double actual=success?1:0,error=actual-p.confidence,abs=Math.abs(error);
  p.calibrationError=Double.isFinite(error)?Math.max(-1,Math.min(1,error)):0;
  boolean over=p.overdriveAtPrediction>=.22||p.logicalControlAtPrediction<.70;
  if(over){overdrivePredictions++;overdriveAbsErrorSum+=abs;if(!success&&p.confidence>=.68)overdriveOverconfidentMisses++;}
  else{stablePredictions++;stableAbsErrorSum+=abs;}
  double overErr=overdrivePredictions==0?0:overdriveAbsErrorSum/overdrivePredictions,stableErr=stablePredictions==0?overErr:stableAbsErrorSum/stablePredictions;
  double missRate=overdrivePredictions==0?0:(double)overdriveOverconfidentMisses/overdrivePredictions;
  overdriveBias=clamp((overErr-stableErr)*1.35+missRate*.55);updatedAt=Math.max(updatedAt,now);
 }
 public double cautionAdjustment(){return overdrivePredictions<3?0:Math.min(.22,overdriveBias*.22);}
 public boolean learnedOverdriveRisk(){return overdrivePredictions>=3&&overdriveBias>=.18;}

 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("stablePredictions",stablePredictions);j.put("overdrivePredictions",overdrivePredictions);j.put("overdriveOverconfidentMisses",overdriveOverconfidentMisses);j.put("stableAbsErrorSum",finite(stableAbsErrorSum));j.put("overdriveAbsErrorSum",finite(overdriveAbsErrorSum));j.put("overdriveBias",finite(overdriveBias));j.put("updatedAt",Math.max(0,updatedAt));}catch(Exception ignored){}return j;}
 public static MetacognitiveCalibrationState fromJson(JSONObject j){MetacognitiveCalibrationState x=new MetacognitiveCalibrationState();if(j==null)return x;x.stablePredictions=Math.max(0,j.optInt("stablePredictions"));x.overdrivePredictions=Math.max(0,j.optInt("overdrivePredictions"));x.overdriveOverconfidentMisses=Math.max(0,j.optInt("overdriveOverconfidentMisses"));x.stableAbsErrorSum=nonneg(j.optDouble("stableAbsErrorSum"));x.overdriveAbsErrorSum=nonneg(j.optDouble("overdriveAbsErrorSum"));x.overdriveBias=clamp(j.optDouble("overdriveBias"));x.updatedAt=Math.max(0,j.optLong("updatedAt"));return x;}
 private static double finite(double v){return Double.isFinite(v)?v:0;}private static double nonneg(double v){return Double.isFinite(v)?Math.max(0,v):0;}private static double clamp(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}

final class GeneralRuleState {
 public String id="",statement="",status="TENTATIVE";
 public double confidence=.5;
 public int successfulApplications,failedApplications;
 public long updatedAt;
 public final Set<String> subjects=new LinkedHashSet<>();

 public void record(String subject,boolean success,long now){
  if(success)successfulApplications++;else failedApplications++;
  if(subject!=null&&!subject.isEmpty())subjects.add(subject);
  confidence=Math.max(0,Math.min(.97,(1.0+successfulApplications)/(2.0+successfulApplications+failedApplications)));
  status=successfulApplications>=3&&subjects.size()>=2&&confidence>=.70?"GENERALIZED":"TENTATIVE";updatedAt=now;
 }
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("statement",statement);j.put("status",status);j.put("confidence",confidence);j.put("successfulApplications",successfulApplications);j.put("failedApplications",failedApplications);j.put("updatedAt",updatedAt);j.put("subjects",new JSONArray(subjects));}catch(Exception ignored){}return j;}
 public static GeneralRuleState fromJson(String key,JSONObject j){GeneralRuleState r=new GeneralRuleState();if(j==null){r.id=key;return r;}r.id=j.optString("id",key);r.statement=j.optString("statement","");r.status=j.optString("status","TENTATIVE");double c=j.optDouble("confidence",.5);r.confidence=Double.isFinite(c)?Math.max(0,Math.min(1,c)):.5;r.successfulApplications=Math.max(0,j.optInt("successfulApplications"));r.failedApplications=Math.max(0,j.optInt("failedApplications"));r.updatedAt=Math.max(0,j.optLong("updatedAt"));JSONArray a=j.optJSONArray("subjects");if(a!=null)for(int i=0;i<a.length();i++){String x=a.optString(i,"");if(!x.isEmpty())r.subjects.add(x);}return r;}
}
