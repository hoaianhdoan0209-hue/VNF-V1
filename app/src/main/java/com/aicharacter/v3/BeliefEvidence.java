package com.aicharacter.v3;
import org.json.JSONObject;
public final class BeliefEvidence {
 public final String evidenceId,sourceMemoryId,type,context; public final double polarity,weight; public final long timestamp;
 public BeliefEvidence(String id,String memoryId,String type,double polarity,double weight,long timestamp,String context){this.evidenceId=id;this.sourceMemoryId=memoryId;this.type=type;this.polarity=polarity;this.weight=weight;this.timestamp=timestamp;this.context=context==null?"":context;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("evidenceId",evidenceId);j.put("sourceMemoryId",sourceMemoryId);j.put("type",type);j.put("polarity",polarity);j.put("weight",weight);j.put("timestamp",timestamp);j.put("context",context);}catch(Exception ignored){}return j;}
 public static BeliefEvidence fromJson(JSONObject j){return new BeliefEvidence(j.optString("evidenceId",IdFactory.next("evidence")),j.optString("sourceMemoryId",""),j.optString("type","legacy"),j.optDouble("polarity",1),j.optDouble("weight",.25),j.optLong("timestamp",System.currentTimeMillis()),j.optString("context",""));}
}
