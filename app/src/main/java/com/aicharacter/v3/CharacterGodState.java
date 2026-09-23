package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/** Persistent Character/God state. Kept separate from God memory, Haru episodic memory and authored World Truth. */
public final class CharacterGodState {
 public final Map<String,LessonState> lessons=new LinkedHashMap<>();
 public final Map<String,ConceptKnowledgeState> conceptKnowledge=new LinkedHashMap<>();
 public final Map<String,OpenQuestionState> openQuestions=new LinkedHashMap<>();
 public final Map<String,WorldConditionProposalState> worldConditions=new LinkedHashMap<>();
 public HaruReasoningState reasoning=new HaruReasoningState();

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  JSONObject ls=new JSONObject();for(Map.Entry<String,LessonState>e:lessons.entrySet())ls.put(e.getKey(),e.getValue().toJson());j.put("lessons",ls);
  JSONObject ck=new JSONObject();for(Map.Entry<String,ConceptKnowledgeState>e:conceptKnowledge.entrySet())ck.put(e.getKey(),e.getValue().toJson());j.put("conceptKnowledge",ck);
  JSONObject oq=new JSONObject();for(Map.Entry<String,OpenQuestionState>e:openQuestions.entrySet())oq.put(e.getKey(),e.getValue().toJson());j.put("openQuestions",oq);
  JSONObject wc=new JSONObject();for(Map.Entry<String,WorldConditionProposalState>e:worldConditions.entrySet())wc.put(e.getKey(),e.getValue().toJson());j.put("worldConditions",wc);j.put("reasoning",reasoning.toJson());
 }catch(Exception ignored){}return j;}

 public static CharacterGodState fromJson(JSONObject j){CharacterGodState s=new CharacterGodState();if(j==null)return s;
  read(j.optJSONObject("lessons"),(k,v)->s.lessons.put(k,LessonState.fromJson(v)));
  read(j.optJSONObject("conceptKnowledge"),(k,v)->s.conceptKnowledge.put(k,ConceptKnowledgeState.fromJson(k,v)));
  read(j.optJSONObject("openQuestions"),(k,v)->s.openQuestions.put(k,OpenQuestionState.fromJson(k,v)));
  read(j.optJSONObject("worldConditions"),(k,v)->s.worldConditions.put(k,WorldConditionProposalState.fromJson(k,v)));
  s.reasoning=HaruReasoningState.fromJson(j.optJSONObject("reasoning"));
  return s;
 }
 private interface Reader{void accept(String key,JSONObject value);}
 private static void read(JSONObject o,Reader r){if(o==null)return;Iterator<String>it=o.keys();while(it.hasNext()){String k=it.next();JSONObject v=o.optJSONObject(k);if(v!=null)r.accept(k,v);}}
}

final class LessonState {
 public String lessonId="",topic="",mode="KNOWLEDGE",status="OFFERED",lastResponse="",claim="";
 public String sourceLayer="REAL_REFERENCE",sourceFamily="",sourceRef="",retrievedAt="",provenance="",limits="";
 public double sourceConfidence=-1,difficulty=.5;
 public long offeredAt,updatedAt,nextReviewAt;
 public int attempts;
 public final List<String> concepts=new ArrayList<>(),prerequisites=new ArrayList<>(),evidence=new ArrayList<>();

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("lessonId",lessonId);j.put("topic",topic);j.put("mode",mode);j.put("status",status);j.put("lastResponse",lastResponse);j.put("claim",claim);
  j.put("sourceLayer",sourceLayer);j.put("sourceFamily",sourceFamily);j.put("sourceRef",sourceRef);j.put("retrievedAt",retrievedAt);j.put("provenance",provenance);j.put("limits",limits);
  j.put("sourceConfidence",sourceConfidence);j.put("difficulty",difficulty);j.put("offeredAt",offeredAt);j.put("updatedAt",updatedAt);j.put("nextReviewAt",nextReviewAt);j.put("attempts",attempts);
  j.put("concepts",new JSONArray(concepts));j.put("prerequisites",new JSONArray(prerequisites));j.put("evidence",new JSONArray(evidence));
 }catch(Exception ignored){}return j;}
 public static LessonState fromJson(JSONObject j){LessonState x=new LessonState();if(j==null)return x;
  x.lessonId=j.optString("lessonId","");x.topic=j.optString("topic","");x.mode=j.optString("mode","KNOWLEDGE");x.status=j.optString("status","OFFERED");x.lastResponse=j.optString("lastResponse","");x.claim=j.optString("claim","");
  x.sourceLayer=j.optString("sourceLayer","REAL_REFERENCE");x.sourceFamily=j.optString("sourceFamily","");x.sourceRef=j.optString("sourceRef","");x.retrievedAt=j.optString("retrievedAt","");x.provenance=j.optString("provenance","");x.limits=j.optString("limits","");
  x.sourceConfidence=finite(j.optDouble("sourceConfidence",-1),-1);x.difficulty=unit(j.optDouble("difficulty",.5));x.offeredAt=j.optLong("offeredAt");x.updatedAt=j.optLong("updatedAt",x.offeredAt);x.nextReviewAt=j.optLong("nextReviewAt");x.attempts=Math.max(0,j.optInt("attempts"));
  strings(j.optJSONArray("concepts"),x.concepts);strings(j.optJSONArray("prerequisites"),x.prerequisites);strings(j.optJSONArray("evidence"),x.evidence);return x;
 }
 static void strings(JSONArray a,List<String>out){if(a!=null)for(int i=0;i<a.length();i++){String v=a.optString(i,"").trim();if(!v.isEmpty())out.add(v);}}
 static double unit(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):.5;} static double finite(double v,double f){return Double.isFinite(v)?v:f;}
}

final class ConceptKnowledgeState {
 public String concept="",claim="";
 public double confidence;
 public long updatedAt;
 public final List<KnowledgeEvidence> evidence=new ArrayList<>();

 public void applyEvidence(String newClaim,double polarity,double weight,String sourceLayer,String sourceRef,String evidenceId,long now){
  String c=newClaim==null||newClaim.trim().isEmpty()?concept:newClaim.trim();double p=Double.isFinite(polarity)?Math.max(-1,Math.min(1,polarity)):0,w=Double.isFinite(weight)?Math.max(.01,Math.min(1,weight)):.1;
  for(KnowledgeEvidence e:evidence)if(evidenceId!=null&&!evidenceId.isEmpty()&&evidenceId.equals(e.evidenceId))return;
  evidence.add(new KnowledgeEvidence(evidenceId,c,p,w,sourceLayer,sourceRef,now));while(evidence.size()>32)evidence.remove(0);recompute(now);
 }
 private void recompute(long now){
  Map<String,Double>score=new LinkedHashMap<>();double total=0;
  for(KnowledgeEvidence e:evidence){double age=Math.max(0,(now-e.time)/86400000.0),rec=.55+.45*Math.exp(-age/60.0),s=e.polarity*e.weight*rec;score.put(e.claim,score.getOrDefault(e.claim,0.0)+s);total+=Math.abs(s);}
  String best=claim;double bestScore=score.getOrDefault(best,Double.NEGATIVE_INFINITY);
  for(Map.Entry<String,Double>e:score.entrySet())if(e.getValue()>bestScore){best=e.getKey();bestScore=e.getValue();}
  if(best!=null&&!best.isEmpty()&&(claim.isEmpty()||bestScore>score.getOrDefault(claim,0.0)+.20))claim=best;
  double own=Math.max(0,score.getOrDefault(claim,0.0));confidence=Math.max(0,Math.min(.98,total<=.001?0:.08+.88*own/(total+.35)));updatedAt=now;
 }
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("concept",concept);j.put("claim",claim);j.put("confidence",confidence);j.put("updatedAt",updatedAt);JSONArray a=new JSONArray();for(KnowledgeEvidence e:evidence)a.put(e.toJson());j.put("evidence",a);}catch(Exception ignored){}return j;}
 public static ConceptKnowledgeState fromJson(String key,JSONObject j){ConceptKnowledgeState c=new ConceptKnowledgeState();c.concept=j==null?key:j.optString("concept",key);if(j==null)return c;c.claim=j.optString("claim","");c.confidence=LessonState.unit(j.optDouble("confidence",0));c.updatedAt=j.optLong("updatedAt");JSONArray a=j.optJSONArray("evidence");if(a!=null)for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null)c.evidence.add(KnowledgeEvidence.fromJson(x));}return c;}
}

final class KnowledgeEvidence {
 public final String evidenceId,claim,sourceLayer,sourceRef;public final double polarity,weight;public final long time;
 KnowledgeEvidence(String id,String claim,double polarity,double weight,String layer,String ref,long time){this.evidenceId=id==null?"":id;this.claim=claim==null?"":claim;this.polarity=polarity;this.weight=weight;this.sourceLayer=layer==null?"":layer;this.sourceRef=ref==null?"":ref;this.time=time;}
 JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("evidenceId",evidenceId);j.put("claim",claim);j.put("polarity",polarity);j.put("weight",weight);j.put("sourceLayer",sourceLayer);j.put("sourceRef",sourceRef);j.put("time",time);}catch(Exception ignored){}return j;}
 static KnowledgeEvidence fromJson(JSONObject j){return new KnowledgeEvidence(j.optString("evidenceId",""),j.optString("claim",""),j.optDouble("polarity"),j.optDouble("weight"),j.optString("sourceLayer",""),j.optString("sourceRef",""),j.optLong("time"));}
}

final class OpenQuestionState {
 public String questionId="",topic="",aboutObjectId="",question="",reason="",status="OPEN";public long createdAt,lastRevisitedAt,resolvedAt;public int revisits;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("questionId",questionId);j.put("topic",topic);j.put("aboutObjectId",aboutObjectId);j.put("question",question);j.put("reason",reason);j.put("status",status);j.put("createdAt",createdAt);j.put("lastRevisitedAt",lastRevisitedAt);j.put("resolvedAt",resolvedAt);j.put("revisits",revisits);}catch(Exception ignored){}return j;}
 public static OpenQuestionState fromJson(String key,JSONObject j){OpenQuestionState q=new OpenQuestionState();q.questionId=j.optString("questionId",key);q.topic=j.optString("topic","");q.aboutObjectId=j.optString("aboutObjectId","");q.question=j.optString("question","");q.reason=j.optString("reason","");q.status=j.optString("status","OPEN");q.createdAt=j.optLong("createdAt");q.lastRevisitedAt=j.optLong("lastRevisitedAt");q.resolvedAt=j.optLong("resolvedAt");q.revisits=Math.max(0,j.optInt("revisits"));return q;}
}

final class WorldConditionProposalState {
 public String id="",condition="",desiredValue="",scopeType="GLOBAL",scopeTarget="",provenanceLayer="GOD_PROPOSAL",sourceRef="",rollbackPolicy="RESTORE_PREVIOUS_BASELINE",status="PROPOSED",consumerToken="";
 public double intensity,durationMinutes,sourceConfidence=-1;
 public long proposedAt,appliedAt,rollbackRequestedAt,rolledBackAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("condition",condition);j.put("desiredValue",desiredValue);j.put("scopeType",scopeType);j.put("scopeTarget",scopeTarget);j.put("provenanceLayer",provenanceLayer);j.put("sourceRef",sourceRef);j.put("rollbackPolicy",rollbackPolicy);j.put("status",status);j.put("consumerToken",consumerToken);j.put("intensity",intensity);j.put("durationMinutes",durationMinutes);j.put("sourceConfidence",sourceConfidence);j.put("proposedAt",proposedAt);j.put("appliedAt",appliedAt);j.put("rollbackRequestedAt",rollbackRequestedAt);j.put("rolledBackAt",rolledBackAt);}catch(Exception ignored){}return j;}
 public static WorldConditionProposalState fromJson(String key,JSONObject j){WorldConditionProposalState p=new WorldConditionProposalState();p.id=j.optString("id",key);p.condition=j.optString("condition","");p.desiredValue=j.optString("desiredValue","");p.scopeType=j.optString("scopeType","GLOBAL");p.scopeTarget=j.optString("scopeTarget","");p.provenanceLayer=j.optString("provenanceLayer","GOD_PROPOSAL");p.sourceRef=j.optString("sourceRef","");p.rollbackPolicy=j.optString("rollbackPolicy","RESTORE_PREVIOUS_BASELINE");p.status=j.optString("status","PROPOSED");p.consumerToken=j.optString("consumerToken","");p.intensity=LessonState.unit(j.optDouble("intensity",0));p.durationMinutes=Math.max(1,Math.min(360,j.optDouble("durationMinutes",30)));p.sourceConfidence=LessonState.finite(j.optDouble("sourceConfidence",-1),-1);p.proposedAt=j.optLong("proposedAt");p.appliedAt=j.optLong("appliedAt");p.rollbackRequestedAt=j.optLong("rollbackRequestedAt");p.rolledBackAt=j.optLong("rolledBackAt");return p;}
}
