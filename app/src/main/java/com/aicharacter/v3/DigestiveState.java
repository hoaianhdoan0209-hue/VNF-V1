package com.aicharacter.v3;
import org.json.JSONObject;
/** Haru digestion authority. Stomach contents are converted into BodyState energy over time; this state never directly chooses behavior. */
public final class DigestiveState{
 public double stomachFood=.45,nutrientReserve=.70,digestionLoad=.15;public long lastMealAt,lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("stomachFood",stomachFood);j.put("nutrientReserve",nutrientReserve);j.put("digestionLoad",digestionLoad);j.put("lastMealAt",lastMealAt);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static DigestiveState fromJson(JSONObject j){DigestiveState d=new DigestiveState();if(j==null)return d;d.stomachFood=cl(j.optDouble("stomachFood",d.stomachFood));d.nutrientReserve=cl(j.optDouble("nutrientReserve",d.nutrientReserve));d.digestionLoad=cl(j.optDouble("digestionLoad",d.digestionLoad));d.lastMealAt=j.optLong("lastMealAt");d.lastUpdatedAt=j.optLong("lastUpdatedAt");return d;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}