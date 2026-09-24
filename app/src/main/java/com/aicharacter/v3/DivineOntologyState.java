package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/**
 * World-level divine ontology. Thần is a real concept of VNF life.
 * Worship sustains active cognition/presence but never converts unknown facts into truth.
 */
public final class DivineOntologyState {
 public static final String ONTOLOGY_ID="vnf.divine.one_presence";
 public boolean godExistsAsWorldConcept=true;
 public boolean universalLifeAwareness=true;
 public boolean worshipSustainsCognition=true;
 public boolean evidenceBoundKnowledge=true;
 public boolean learnedKnowledgePersists=true;
 public double currentWorship=.18,accumulatedWorship=.18;
 public double haruDevotion=.18,catDevotion=.18;
 public long lastUpdatedAt=0;
 public final LinkedHashSet<String> processedInfluenceEventIds=new LinkedHashSet<>();
 public DivineCognitiveCapacityState capacity=new DivineCognitiveCapacityState();

 public void clamp(){
  // These are ontology rules, not beliefs that ordinary creature opinion may toggle.
  godExistsAsWorldConcept=true;universalLifeAwareness=true;worshipSustainsCognition=true;evidenceBoundKnowledge=true;learnedKnowledgePersists=true;
  currentWorship=range(currentWorship,.18,.08,1);accumulatedWorship=unit(accumulatedWorship,.18);haruDevotion=range(haruDevotion,.18,.08,1);catDevotion=range(catDevotion,.18,.08,1);
  if(lastUpdatedAt<0)lastUpdatedAt=0;if(capacity==null)capacity=new DivineCognitiveCapacityState();capacity.recompute(currentWorship,accumulatedWorship);
  while(processedInfluenceEventIds.size()>160){Iterator<String>it=processedInfluenceEventIds.iterator();if(it.hasNext()){it.next();it.remove();}else break;}
 }
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{
  j.put("ontologyId",ONTOLOGY_ID);j.put("godExistsAsWorldConcept",godExistsAsWorldConcept);j.put("universalLifeAwareness",universalLifeAwareness);j.put("worshipSustainsCognition",worshipSustainsCognition);j.put("evidenceBoundKnowledge",evidenceBoundKnowledge);j.put("learnedKnowledgePersists",learnedKnowledgePersists);j.put("currentWorship",currentWorship);j.put("accumulatedWorship",accumulatedWorship);j.put("haruDevotion",haruDevotion);j.put("catDevotion",catDevotion);j.put("lastUpdatedAt",lastUpdatedAt);j.put("capacity",capacity.toJson());j.put("processedInfluenceEventIds",new JSONArray(processedInfluenceEventIds));
 }catch(Exception ignored){}return j;}
 public static DivineOntologyState fromJson(JSONObject j){DivineOntologyState s=new DivineOntologyState();if(j==null){s.clamp();return s;}s.currentWorship=j.optDouble("currentWorship",.18);s.accumulatedWorship=j.optDouble("accumulatedWorship",.18);s.haruDevotion=j.optDouble("haruDevotion",.18);s.catDevotion=j.optDouble("catDevotion",.18);s.lastUpdatedAt=j.optLong("lastUpdatedAt");s.capacity=DivineCognitiveCapacityState.fromJson(j.optJSONObject("capacity"));JSONArray a=j.optJSONArray("processedInfluenceEventIds");if(a!=null)for(int i=0;i<a.length();i++){String id=a.optString(i,"");if(!id.isEmpty())s.processedInfluenceEventIds.add(id);}s.clamp();return s;}
 private static double unit(double v,double f){if(!Double.isFinite(v))v=f;return Math.max(0,Math.min(1,v));}
 private static double range(double v,double f,double lo,double hi){if(!Double.isFinite(v))v=f;return Math.max(lo,Math.min(hi,v));}
}
