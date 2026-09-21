package com.aicharacter.v3;
import org.json.JSONObject;
/** Fictional sensory field shared by non-human VNF life. A species may weight these channels differently. */
public final class OrganismSenseState{
 public double lumenSense=.5,moistureSense=.5,vibrationSense=.05,thermalSense=.5,proximitySense=.05,alertness=.25;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("lumenSense",lumenSense);j.put("moistureSense",moistureSense);j.put("vibrationSense",vibrationSense);j.put("thermalSense",thermalSense);j.put("proximitySense",proximitySense);j.put("alertness",alertness);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static OrganismSenseState fromJson(JSONObject j){OrganismSenseState s=new OrganismSenseState();if(j==null)return s;s.lumenSense=cl(j.optDouble("lumenSense",s.lumenSense));s.moistureSense=cl(j.optDouble("moistureSense",s.moistureSense));s.vibrationSense=cl(j.optDouble("vibrationSense",s.vibrationSense));s.thermalSense=cl(j.optDouble("thermalSense",s.thermalSense));s.proximitySense=cl(j.optDouble("proximitySense",s.proximitySense));s.alertness=cl(j.optDouble("alertness",s.alertness));s.lastUpdatedAt=j.optLong("lastUpdatedAt");return s;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}