package com.aicharacter.v3;
import org.json.JSONObject;
/** Coarse feline respiratory response coupled to physical work and atmospheric oxygen availability. */
public final class CatRespirationState{public double breathingDrive=.08,oxygenDelivery=.96,strain;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("breathingDrive",breathingDrive);j.put("oxygenDelivery",oxygenDelivery);j.put("strain",strain);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static CatRespirationState fromJson(JSONObject j){CatRespirationState x=new CatRespirationState();if(j==null)return x;x.breathingDrive=cl(j.optDouble("breathingDrive",.08));x.oxygenDelivery=cl(j.optDouble("oxygenDelivery",.96));x.strain=cl(j.optDouble("strain"));x.lastUpdatedAt=j.optLong("lastUpdatedAt");return x;}private static double cl(double v){return Math.max(0,Math.min(1,v));}
}