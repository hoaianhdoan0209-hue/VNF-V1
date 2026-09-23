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
 public final Map<String,GeneralRuleState> rules=new LinkedHashMap<>();
 public long lastCycleAt;
 public int cycleCount;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  JSONObject h=new JSONObject();for(Map.Entry<String,HypothesisState>e:hypotheses.entrySet())h.put(e.getKey(),e.getValue().toJson());j.put("hypotheses",h);
  JSONObject p=new JSONObject();for(Map.Entry<String,PredictionState>e:predictions.entrySet())p.put(e.getKey(),e.getValue().toJson());j.put("predictions",p);
  JSONObject x=new JSONObject();for(Map.Entry<String,CausalExplanationState>e:causalExplanations.entrySet())x.put(e.getKey(),e.getValue().toJson());j.put("causalExplanations",x);
  JSONObject r=new JSONObject();for(Map.Entry<String,GeneralRuleState>e:rules.entrySet())r.put(e.getKey(),e.getValue().toJson());j.put("rules",r);
  j.put("lastCycleAt",lastCycleAt);j.put("cycleCount",cycleCount);
 }catch(Exception ignored){}return j;}

 public static HaruReasoningState fromJson(JSONObject j){HaruReasoningState s=new HaruReasoningState();if(j==null)return s;
  read(j.optJSONObject("hypotheses"),(k,v)->s.hypotheses.put(k,HypothesisState.fromJson(k,v)));
  read(j.optJSONObject("predictions"),(k,v)->s.predictions.put(k,PredictionState.fromJson(k,v)));
  read(j.optJSONObject("causalExplanations"),(k,v)->s.causalExplanations.put(k,CausalExplanationState.fromJson(k,v)));
  read(j.optJSONObject("rules"),(k,v)->s.rules.put(k,GeneralRuleState.fromJson(k,v)));
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
 public double confidence=.5;
 public long createdAt,resolvedAt;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("planId",planId);j.put("hypothesisId",hypothesisId);j.put("subjectId",subjectId);j.put("expectedOutcome",expectedOutcome);j.put("alternativeOutcome",alternativeOutcome);j.put("status",status);j.put("outcomeMemoryId",outcomeMemoryId);j.put("confidence",confidence);j.put("createdAt",createdAt);j.put("resolvedAt",resolvedAt);}catch(Exception ignored){}return j;}
 public static PredictionState fromJson(String key,JSONObject j){PredictionState p=new PredictionState();if(j==null){p.id=key;return p;}p.id=j.optString("id",key);p.planId=j.optString("planId","");p.hypothesisId=j.optString("hypothesisId","");p.subjectId=j.optString("subjectId","");p.expectedOutcome=j.optString("expectedOutcome","");p.alternativeOutcome=j.optString("alternativeOutcome","");p.status=j.optString("status","PENDING");p.outcomeMemoryId=j.optString("outcomeMemoryId","");double c=j.optDouble("confidence",.5);p.confidence=Double.isFinite(c)?Math.max(0,Math.min(1,c)):.5;p.createdAt=Math.max(0,j.optLong("createdAt"));p.resolvedAt=Math.max(0,j.optLong("resolvedAt"));return p;}
}

final class CausalExplanationState {
 public String id="",predictionId="",planId="",intentionId="",subjectId="",causeType="",claim="",testablePrediction="",status="TENTATIVE";
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
  j.put("id",id);j.put("predictionId",predictionId);j.put("planId",planId);j.put("intentionId",intentionId);j.put("subjectId",subjectId);j.put("causeType",causeType);j.put("claim",claim);j.put("testablePrediction",testablePrediction);j.put("status",status);j.put("supportWeight",supportWeight);j.put("contradictionWeight",contradictionWeight);j.put("confidence",confidence);j.put("createdAt",createdAt);j.put("updatedAt",updatedAt);j.put("counterfactualChecks",counterfactualChecks);j.put("evidenceEventIds",new JSONArray(evidenceEventIds));j.put("evidenceMemoryIds",new JSONArray(evidenceMemoryIds));
 }catch(Exception ignored){}return j;}
 public static CausalExplanationState fromJson(String key,JSONObject j){CausalExplanationState x=new CausalExplanationState();if(j==null){x.id=key;return x;}x.id=j.optString("id",key);x.predictionId=j.optString("predictionId","");x.planId=j.optString("planId","");x.intentionId=j.optString("intentionId","");x.subjectId=j.optString("subjectId","");x.causeType=j.optString("causeType","");x.claim=j.optString("claim","");x.testablePrediction=j.optString("testablePrediction","");x.status=j.optString("status","TENTATIVE");x.supportWeight=finite(j.optDouble("supportWeight"),0);x.contradictionWeight=finite(j.optDouble("contradictionWeight"),0);x.confidence=Math.max(.02,Math.min(.98,finite(j.optDouble("confidence",.5),.5)));x.createdAt=Math.max(0,j.optLong("createdAt"));x.updatedAt=Math.max(0,j.optLong("updatedAt"));x.counterfactualChecks=Math.max(0,j.optInt("counterfactualChecks"));strings(j.optJSONArray("evidenceEventIds"),x.evidenceEventIds);strings(j.optJSONArray("evidenceMemoryIds"),x.evidenceMemoryIds);return x;}
 private static void strings(JSONArray a,List<String>out){if(a!=null)for(int i=0;i<a.length();i++){String v=a.optString(i,"");if(!v.isEmpty())out.add(v);}}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
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
