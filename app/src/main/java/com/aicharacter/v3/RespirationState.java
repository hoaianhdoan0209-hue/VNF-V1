package com.aicharacter.v3;
import org.json.JSONObject;
/** Coarse physiology coupling: air limitation is separate from ventilation demand so exertion never masquerades as hypoxia. */
public final class RespirationState{
 public double oxygenSaturation=.98,breathingLoad=.08,ventilationDrive=.18;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("oxygenSaturation",oxygenSaturation);j.put("breathingLoad",breathingLoad);j.put("ventilationDrive",ventilationDrive);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static RespirationState fromJson(JSONObject j){RespirationState r=new RespirationState();if(j==null)return r;r.oxygenSaturation=clamp(j.optDouble("oxygenSaturation",.98),.5,1);r.breathingLoad=clamp(j.optDouble("breathingLoad",.08),0,1);r.ventilationDrive=clamp(j.optDouble("ventilationDrive",r.ventilationDrive),0,1);r.lastUpdatedAt=j.optLong("lastUpdatedAt",0);return r;}
 private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
}
