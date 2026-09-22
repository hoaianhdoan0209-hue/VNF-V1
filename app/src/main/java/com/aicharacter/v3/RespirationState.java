package com.aicharacter.v3;
import org.json.JSONObject;
/** Coarse physiology coupling: air limitation is separate from ventilation demand so exertion never masquerades as hypoxia. */
public final class RespirationState{
 public double oxygenSaturation=.98,breathingLoad=.08,ventilationDrive=.18;public long lastUpdatedAt;
 public void normalize(){oxygenSaturation=finiteClamp(oxygenSaturation,.98,.5,1);breathingLoad=finiteClamp(breathingLoad,.08,0,1);ventilationDrive=finiteClamp(ventilationDrive,.18,0,1);if(lastUpdatedAt<0)lastUpdatedAt=0;}
 public JSONObject toJson(){normalize();JSONObject j=new JSONObject();try{j.put("oxygenSaturation",oxygenSaturation);j.put("breathingLoad",breathingLoad);j.put("ventilationDrive",ventilationDrive);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static RespirationState fromJson(JSONObject j){RespirationState r=new RespirationState();if(j==null)return r;r.oxygenSaturation=j.optDouble("oxygenSaturation",.98);r.breathingLoad=j.optDouble("breathingLoad",.08);r.ventilationDrive=j.optDouble("ventilationDrive",r.ventilationDrive);r.lastUpdatedAt=j.optLong("lastUpdatedAt",0);r.normalize();return r;}
 private static double finiteClamp(double v,double fallback,double lo,double hi){if(!Double.isFinite(v))v=fallback;return Math.max(lo,Math.min(hi,v));}
}
