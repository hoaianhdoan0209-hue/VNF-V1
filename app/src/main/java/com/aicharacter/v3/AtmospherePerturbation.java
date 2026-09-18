package com.aicharacter.v3;
import org.json.JSONObject;
/** Persistent bounded atmospheric disturbance. It is a cause, never a scripted weather outcome. */
public final class AtmospherePerturbation{
 public String id="",areaId="";public double intensity,durationMinutes,evolution;public long startedAt;public boolean active;
 public static AtmospherePerturbation fromProposal(GodWorldEventProposal p,long now){AtmospherePerturbation a=new AtmospherePerturbation();a.id=p.id;a.areaId=p.areaId;a.intensity=p.intensity;a.durationMinutes=p.durationMinutes;a.evolution=p.evolution;a.startedAt=now;a.active=true;return a;}
 public double influenceAt(long now){if(!active||now<startedAt)return 0;double age=(now-startedAt)/60000.0;if(age>=durationMinutes)return 0;double phase=durationMinutes<=0?1:age/durationMinutes;return Math.max(0,Math.min(1,intensity+evolution*phase*.45))*(1-phase*.35);}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("areaId",areaId);j.put("intensity",intensity);j.put("durationMinutes",durationMinutes);j.put("evolution",evolution);j.put("startedAt",startedAt);j.put("active",active);}catch(Exception ignored){}return j;}
 public static AtmospherePerturbation fromJson(JSONObject j){AtmospherePerturbation a=new AtmospherePerturbation();if(j==null)return a;a.id=j.optString("id","");a.areaId=j.optString("areaId","");a.intensity=Math.max(0,Math.min(1,j.optDouble("intensity",0)));a.durationMinutes=Math.max(1,Math.min(360,j.optDouble("durationMinutes",30)));a.evolution=Math.max(-1,Math.min(1,j.optDouble("evolution",0)));a.startedAt=j.optLong("startedAt",0);a.active=j.optBoolean("active",false);return a;}
}