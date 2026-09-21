package com.aicharacter.v3;
import org.json.JSONObject;
/** Haru water/renal authority. Hydration and bladder fill are hidden physiology, not player meters. */
public final class HydrationState{
 public double hydration=.78,bladderFill=.18,renalLoad=.12;public long lastDrinkAt,lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("hydration",hydration);j.put("bladderFill",bladderFill);j.put("renalLoad",renalLoad);j.put("lastDrinkAt",lastDrinkAt);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static HydrationState fromJson(JSONObject j){HydrationState h=new HydrationState();if(j==null)return h;h.hydration=cl(j.optDouble("hydration",h.hydration));h.bladderFill=cl(j.optDouble("bladderFill",h.bladderFill));h.renalLoad=cl(j.optDouble("renalLoad",h.renalLoad));h.lastDrinkAt=j.optLong("lastDrinkAt");h.lastUpdatedAt=j.optLong("lastUpdatedAt");return h;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}