package com.aicharacter.v3;
import org.json.JSONObject;
/** Persistent living state for authored VNF flora. Plants sense and respond without human-like cognition. */
public final class FloraLifeState{
 public OrganismBodyState body=new OrganismBodyState();public OrganismSenseState sense=new OrganismSenseState();public double rootGrip=.84,opening=.45,swayDrive=.12,growthPulse=.35;public String lastStimulus="";public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("body",body.toJson());j.put("sense",sense.toJson());j.put("rootGrip",rootGrip);j.put("opening",opening);j.put("swayDrive",swayDrive);j.put("growthPulse",growthPulse);j.put("lastStimulus",lastStimulus);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static FloraLifeState fromJson(JSONObject j){FloraLifeState f=new FloraLifeState();if(j==null)return f;f.body=OrganismBodyState.fromJson(j.optJSONObject("body"));f.sense=OrganismSenseState.fromJson(j.optJSONObject("sense"));f.rootGrip=cl(j.optDouble("rootGrip",f.rootGrip));f.opening=cl(j.optDouble("opening",f.opening));f.swayDrive=cl(j.optDouble("swayDrive",f.swayDrive));f.growthPulse=cl(j.optDouble("growthPulse",f.growthPulse));f.lastStimulus=j.optString("lastStimulus","");f.lastUpdatedAt=j.optLong("lastUpdatedAt");return f;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}