package com.aicharacter.v3;
import org.json.JSONObject;
public final class HabitState {
 public String key,context,action,targetId; public double strength; public int repetitions; public long lastPerformed;
 public HabitState(String key,String context,String action,String targetId){this.key=key;this.context=context;this.action=action;this.targetId=targetId;}
 public void reinforce(double significance,long now){repetitions++;strength=Math.min(1,strength+.035*Math.max(.2,significance));lastPerformed=now;}public void weaken(double evidence,long now){strength=Math.max(0,strength-.05*Math.max(.15,evidence));lastPerformed=now;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("key",key);j.put("context",context);j.put("action",action);j.put("targetId",targetId);j.put("strength",strength);j.put("repetitions",repetitions);j.put("lastPerformed",lastPerformed);}catch(Exception ignored){}return j;}
 public static HabitState fromJson(String key,JSONObject j){HabitState h=new HabitState(key,j.optString("context"),j.optString("action"),j.optString("targetId"));h.strength=j.optDouble("strength");h.repetitions=j.optInt("repetitions");h.lastPerformed=j.optLong("lastPerformed");return h;}
}
