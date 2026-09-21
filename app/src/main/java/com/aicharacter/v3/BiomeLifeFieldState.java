package com.aicharacter.v3;
import org.json.JSONObject;
/** Population-level living field for dense background VNF flora/micro-life that are not authored as individual objects. */
public final class BiomeLifeFieldState{
 public OrganismBodyState body=new OrganismBodyState();public OrganismSenseState sense=new OrganismSenseState();public double motionDrive=.12,densityVitality=.76;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("body",body.toJson());j.put("sense",sense.toJson());j.put("motionDrive",motionDrive);j.put("densityVitality",densityVitality);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static BiomeLifeFieldState fromJson(JSONObject j){BiomeLifeFieldState f=new BiomeLifeFieldState();if(j==null)return f;f.body=OrganismBodyState.fromJson(j.optJSONObject("body"));f.sense=OrganismSenseState.fromJson(j.optJSONObject("sense"));f.motionDrive=cl(j.optDouble("motionDrive",f.motionDrive));f.densityVitality=cl(j.optDouble("densityVitality",f.densityVitality));f.lastUpdatedAt=j.optLong("lastUpdatedAt");return f;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}