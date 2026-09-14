package com.aicharacter.v3;
import org.json.JSONObject;
public final class PreferenceState {
 public String key; public double value; public int evidenceCount; public long lastUpdated;
 public PreferenceState(String key,double value){this.key=key;this.value=clamp(value);}
 public void learn(double valence,double significance,long now){double rate=.035*Math.max(.15,significance);value=clamp(value+valence*rate);evidenceCount++;lastUpdated=now;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("key",key);j.put("value",value);j.put("evidenceCount",evidenceCount);j.put("lastUpdated",lastUpdated);}catch(Exception ignored){}return j;}
 public static PreferenceState fromJson(String key,JSONObject j){PreferenceState p=new PreferenceState(key,j==null?0:j.optDouble("value",0));if(j!=null){p.evidenceCount=j.optInt("evidenceCount",0);p.lastUpdated=j.optLong("lastUpdated",0);}return p;}
 private static double clamp(double v){return Math.max(-1,Math.min(1,v));}
}
