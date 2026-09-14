package com.aicharacter.v3;
import org.json.JSONObject;
public final class BeliefChange { public final long timestamp; public final double oldConfidence,newConfidence; public final String evidenceId;
 public BeliefChange(long t,double o,double n,String e){timestamp=t;oldConfidence=o;newConfidence=n;evidenceId=e;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("timestamp",timestamp);j.put("oldConfidence",oldConfidence);j.put("newConfidence",newConfidence);j.put("evidenceId",evidenceId);}catch(Exception ignored){}return j;}
 public static BeliefChange fromJson(JSONObject j){return new BeliefChange(j.optLong("timestamp"),j.optDouble("oldConfidence"),j.optDouble("newConfidence"),j.optString("evidenceId"));}}
