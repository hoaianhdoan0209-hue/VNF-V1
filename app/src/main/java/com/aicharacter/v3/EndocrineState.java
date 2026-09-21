package com.aicharacter.v3;
import org.json.JSONObject;
/** Hidden endocrine regulation. Values are normalized regulatory signals, not literal lab hormone concentrations. */
public final class EndocrineState{
 public double stressResponse=.18,circadianSleepSignal=.28,metabolicSupport=.78,recoverySignal=.70;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("stressResponse",stressResponse);j.put("circadianSleepSignal",circadianSleepSignal);j.put("metabolicSupport",metabolicSupport);j.put("recoverySignal",recoverySignal);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static EndocrineState fromJson(JSONObject j){EndocrineState e=new EndocrineState();if(j==null)return e;e.stressResponse=cl(j.optDouble("stressResponse",e.stressResponse));e.circadianSleepSignal=cl(j.optDouble("circadianSleepSignal",e.circadianSleepSignal));e.metabolicSupport=cl(j.optDouble("metabolicSupport",e.metabolicSupport));e.recoverySignal=cl(j.optDouble("recoverySignal",e.recoverySignal));e.lastUpdatedAt=j.optLong("lastUpdatedAt");return e;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}