package com.aicharacter.v3;
import org.json.JSONObject;
/** Persistent physical air state. Normal Earth-like air is intentionally stable; weather alone cannot consume oxygen. */
public final class AtmosphereState{
 public double oxygenFraction=.2095,pressureKPa=101.325,temperatureC=24.0,relativeHumidity=.62,airQuality=1.0;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("oxygenFraction",oxygenFraction);j.put("pressureKPa",pressureKPa);j.put("temperatureC",temperatureC);j.put("relativeHumidity",relativeHumidity);j.put("airQuality",airQuality);}catch(Exception ignored){}return j;}
 public static AtmosphereState fromJson(JSONObject j){AtmosphereState a=new AtmosphereState();if(j==null)return a;a.oxygenFraction=clamp(j.optDouble("oxygenFraction",.2095),.15,.24);a.pressureKPa=clamp(j.optDouble("pressureKPa",101.325),70,110);a.temperatureC=clamp(j.optDouble("temperatureC",24),-20,50);a.relativeHumidity=clamp(j.optDouble("relativeHumidity",.62),0,1);a.airQuality=clamp(j.optDouble("airQuality",1),0,1);return a;}
 private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
}