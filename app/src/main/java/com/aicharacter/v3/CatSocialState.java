package com.aicharacter.v3;

import org.json.JSONObject;

/** Cat-owned social learning. Separate from Haru's relationship state. */
public final class CatSocialState {
 public double familiarity=.22,comfort=.20,wariness=.28,curiosity=.46,attention;
 public double lastObservedDistance,lastApproachPressure,lastCalmExposure;
 public String mode="WATCH",reason="",activeTravelId="",gazeTarget="";
 public float frozenTargetX=Float.NaN;
 public long lastObservedAt,lastResponseAt,lastLearningAt,lastPlayerOverrideAt;
 public int calmEncounters,approaches,retreats,settles;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("familiarity",cl(familiarity));j.put("comfort",cl(comfort));j.put("wariness",cl(wariness));j.put("curiosity",cl(curiosity));j.put("attention",cl(attention));
  j.put("lastObservedDistance",nonneg(lastObservedDistance));j.put("lastApproachPressure",cl(lastApproachPressure));j.put("lastCalmExposure",cl(lastCalmExposure));
  j.put("mode",safe(mode,"WATCH"));j.put("reason",safe(reason,""));j.put("activeTravelId",safe(activeTravelId,""));j.put("gazeTarget",safe(gazeTarget,""));
  if(Float.isFinite(frozenTargetX))j.put("frozenTargetX",frozenTargetX);
  j.put("lastObservedAt",time(lastObservedAt));j.put("lastResponseAt",time(lastResponseAt));j.put("lastLearningAt",time(lastLearningAt));j.put("lastPlayerOverrideAt",time(lastPlayerOverrideAt));
  j.put("calmEncounters",Math.max(0,calmEncounters));j.put("approaches",Math.max(0,approaches));j.put("retreats",Math.max(0,retreats));j.put("settles",Math.max(0,settles));
 }catch(Exception ignored){}return j;}

 public static CatSocialState fromJson(JSONObject j){CatSocialState s=new CatSocialState();if(j==null)return s;
  s.familiarity=cl(j.optDouble("familiarity",.22));s.comfort=cl(j.optDouble("comfort",.20));s.wariness=cl(j.optDouble("wariness",.28));s.curiosity=cl(j.optDouble("curiosity",.46));s.attention=cl(j.optDouble("attention",0));
  s.lastObservedDistance=nonneg(j.optDouble("lastObservedDistance",0));s.lastApproachPressure=cl(j.optDouble("lastApproachPressure",0));s.lastCalmExposure=cl(j.optDouble("lastCalmExposure",0));
  s.mode=j.optString("mode","WATCH");s.reason=j.optString("reason","");s.activeTravelId=j.optString("activeTravelId","");s.gazeTarget=j.optString("gazeTarget","");
  double tx=j.optDouble("frozenTargetX",Double.NaN);s.frozenTargetX=Double.isFinite(tx)?(float)tx:Float.NaN;
  s.lastObservedAt=time(j.optLong("lastObservedAt"));s.lastResponseAt=time(j.optLong("lastResponseAt"));s.lastLearningAt=time(j.optLong("lastLearningAt"));s.lastPlayerOverrideAt=time(j.optLong("lastPlayerOverrideAt"));
  s.calmEncounters=Math.max(0,j.optInt("calmEncounters"));s.approaches=Math.max(0,j.optInt("approaches"));s.retreats=Math.max(0,j.optInt("retreats"));s.settles=Math.max(0,j.optInt("settles"));return s;
 }
 public void normalize(){familiarity=cl(familiarity);comfort=cl(comfort);wariness=cl(wariness);curiosity=cl(curiosity);attention=cl(attention);lastObservedDistance=nonneg(lastObservedDistance);lastApproachPressure=cl(lastApproachPressure);lastCalmExposure=cl(lastCalmExposure);if(mode==null||mode.isEmpty())mode="WATCH";if(reason==null)reason="";if(activeTravelId==null)activeTravelId="";if(gazeTarget==null)gazeTarget="";}
 private static String safe(String s,String d){return s==null?d:s;}private static long time(long v){return Math.max(0,v);}
 private static double nonneg(double v){return Double.isFinite(v)?Math.max(0,v):0;}private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
