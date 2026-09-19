package com.aicharacter.v3;
import org.json.JSONObject;
/** Coarse physiology coupling: tracks breathing consequences, not individual breaths. */
public final class RespirationState{
 public double oxygenSaturation=.98,breathingLoad=.08;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("oxygenSaturation",oxygenSaturation);j.put("breathingLoad",breathingLoad);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static RespirationState fromJson(JSONObject j){RespirationState r=new RespirationState();if(j==null)return r;r.oxygenSaturation=Math.max(.5,Math.min(1,j.optDouble("oxygenSaturation",.98)));r.breathingLoad=Math.max(0,Math.min(1,j.optDouble("breathingLoad",.08)));r.lastUpdatedAt=j.optLong("lastUpdatedAt",0);return r;}
}