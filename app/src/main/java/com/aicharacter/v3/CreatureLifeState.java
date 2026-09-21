package com.aicharacter.v3;
import org.json.JSONObject;
/** Persistent body/senses/motion for every authored non-human VNF creature. No Earth anatomy assumptions. */
public final class CreatureLifeState{
 public OrganismBodyState body=new OrganismBodyState();public OrganismSenseState sense=new OrganismSenseState();
 public float x;public String areaId="",activity="rest";public double hunger=.30,locomotionDrive=.20,avoidance=.10,curiosity=.22;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("body",body.toJson());j.put("sense",sense.toJson());j.put("x",x);j.put("areaId",areaId);j.put("activity",activity);j.put("hunger",hunger);j.put("locomotionDrive",locomotionDrive);j.put("avoidance",avoidance);j.put("curiosity",curiosity);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static CreatureLifeState fromJson(JSONObject j){CreatureLifeState c=new CreatureLifeState();if(j==null)return c;c.body=OrganismBodyState.fromJson(j.optJSONObject("body"));c.sense=OrganismSenseState.fromJson(j.optJSONObject("sense"));c.x=(float)j.optDouble("x");c.areaId=j.optString("areaId","");c.activity=j.optString("activity","rest");c.hunger=cl(j.optDouble("hunger",c.hunger));c.locomotionDrive=cl(j.optDouble("locomotionDrive",c.locomotionDrive));c.avoidance=cl(j.optDouble("avoidance",c.avoidance));c.curiosity=cl(j.optDouble("curiosity",c.curiosity));c.lastUpdatedAt=j.optLong("lastUpdatedAt");return c;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}