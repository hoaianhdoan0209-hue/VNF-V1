package com.aicharacter.v3;
import org.json.JSONObject;
/** Persistent automatic body responses. These are physiology, not chosen intentions or dialogue. */
public final class BodyInstinctState{
 public double shiver,sweat,breathDrive,guarding,fatigueDroop;public String posture="NEUTRAL";public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("shiver",shiver);j.put("sweat",sweat);j.put("breathDrive",breathDrive);j.put("guarding",guarding);j.put("fatigueDroop",fatigueDroop);j.put("posture",posture);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static BodyInstinctState fromJson(JSONObject j){BodyInstinctState x=new BodyInstinctState();if(j==null)return x;x.shiver=c(j.optDouble("shiver"));x.sweat=c(j.optDouble("sweat"));x.breathDrive=c(j.optDouble("breathDrive"));x.guarding=c(j.optDouble("guarding"));x.fatigueDroop=c(j.optDouble("fatigueDroop"));x.posture=j.optString("posture","NEUTRAL");x.lastUpdatedAt=j.optLong("lastUpdatedAt");return x;}private static double c(double v){return Math.max(0,Math.min(1,v));}
}