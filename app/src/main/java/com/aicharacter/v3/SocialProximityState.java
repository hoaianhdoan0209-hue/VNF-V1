package com.aicharacter.v3;

import org.json.JSONObject;

/** Persistent social-spacing memory for Haru's relationship with the nearby cat. */
public final class SocialProximityState {
 public String mode="NEUTRAL",reason="",activePlanId="";
 public double desiredMin=82,desiredMax=132,lastObservedDistance,lastWarmth,lastGuardedness,lastUtility;
 public float lastTargetX=Float.NaN;
 public long lastEvaluatedAt,lastAdjustmentAt,lastCompletedAt;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("mode",mode==null?"NEUTRAL":mode);j.put("reason",reason==null?"":reason);j.put("activePlanId",activePlanId==null?"":activePlanId);
  j.put("desiredMin",finite(desiredMin,82));j.put("desiredMax",finite(desiredMax,132));j.put("lastObservedDistance",finite(lastObservedDistance,0));j.put("lastWarmth",cl(lastWarmth));j.put("lastGuardedness",cl(lastGuardedness));j.put("lastUtility",finite(lastUtility,0));
  j.put("lastTargetX",Float.isFinite(lastTargetX)?lastTargetX:JSONObject.NULL);j.put("lastEvaluatedAt",Math.max(0,lastEvaluatedAt));j.put("lastAdjustmentAt",Math.max(0,lastAdjustmentAt));j.put("lastCompletedAt",Math.max(0,lastCompletedAt));
 }catch(Exception ignored){}return j;}

 public static SocialProximityState fromJson(JSONObject j){SocialProximityState s=new SocialProximityState();if(j==null)return s;
  s.mode=j.optString("mode","NEUTRAL");s.reason=j.optString("reason","");s.activePlanId=j.optString("activePlanId","");
  s.desiredMin=Math.max(24,finite(j.optDouble("desiredMin",82),82));s.desiredMax=Math.max(s.desiredMin+8,finite(j.optDouble("desiredMax",132),132));s.lastObservedDistance=Math.max(0,finite(j.optDouble("lastObservedDistance",0),0));s.lastWarmth=cl(j.optDouble("lastWarmth",0));s.lastGuardedness=cl(j.optDouble("lastGuardedness",0));s.lastUtility=finite(j.optDouble("lastUtility",0),0);
  double tx=j.optDouble("lastTargetX",Double.NaN);s.lastTargetX=Double.isFinite(tx)?(float)tx:Float.NaN;s.lastEvaluatedAt=Math.max(0,j.optLong("lastEvaluatedAt"));s.lastAdjustmentAt=Math.max(0,j.optLong("lastAdjustmentAt"));s.lastCompletedAt=Math.max(0,j.optLong("lastCompletedAt"));return s;
 }
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
