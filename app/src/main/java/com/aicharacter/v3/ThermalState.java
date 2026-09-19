package com.aicharacter.v3;
import org.json.JSONObject;
/** Coarse persistent thermoregulation; enough for causal comfort/body effects without simulating organs. */
public final class ThermalState{
 public double coreTemperatureC=37.0,skinWetness=0,coldLoad=0,heatLoad=0;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("coreTemperatureC",coreTemperatureC);j.put("skinWetness",skinWetness);j.put("coldLoad",coldLoad);j.put("heatLoad",heatLoad);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static ThermalState fromJson(JSONObject j){ThermalState t=new ThermalState();if(j==null)return t;t.coreTemperatureC=clamp(j.optDouble("coreTemperatureC",37),34,40);t.skinWetness=clamp(j.optDouble("skinWetness",0),0,1);t.coldLoad=clamp(j.optDouble("coldLoad",0),0,1);t.heatLoad=clamp(j.optDouble("heatLoad",0),0,1);t.lastUpdatedAt=j.optLong("lastUpdatedAt",0);return t;}
 private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
}