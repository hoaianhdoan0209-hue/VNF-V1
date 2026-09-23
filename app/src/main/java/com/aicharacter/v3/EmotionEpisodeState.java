package com.aicharacter.v3;

import org.json.JSONObject;

/**
 * One Haru-owned emotional episode grounded in a lived memory.
 * It records Haru's appraisal, not World Truth and not a scripted emotion label.
 */
public final class EmotionEpisodeState {
 public String id="",sourceMemoryId="",sourceKind="",targetId="",primaryEmotion="calm",cause="",status="ACTIVE";
 public double intensity,valence,arousal,goalCongruence,threat,loss,frustration,uncertainty,socialRelevance,bodilyLoad,perceivedControl=.5;
 public double joy,fear,sadness,anger,curiosity,loneliness,relief;
 public long createdAt,updatedAt,resolvedAt;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("id",id);j.put("sourceMemoryId",sourceMemoryId);j.put("sourceKind",sourceKind);j.put("targetId",targetId);j.put("primaryEmotion",primaryEmotion);j.put("cause",cause);j.put("status",status);
  j.put("intensity",f01(intensity));j.put("valence",fs(valence));j.put("arousal",f01(arousal));j.put("goalCongruence",fs(goalCongruence));j.put("threat",f01(threat));j.put("loss",f01(loss));j.put("frustration",f01(frustration));j.put("uncertainty",f01(uncertainty));j.put("socialRelevance",f01(socialRelevance));j.put("bodilyLoad",f01(bodilyLoad));j.put("perceivedControl",f01(perceivedControl));j.put("joy",f01(joy));j.put("fear",f01(fear));j.put("sadness",f01(sadness));j.put("anger",f01(anger));j.put("curiosity",f01(curiosity));j.put("loneliness",f01(loneliness));j.put("relief",f01(relief));j.put("createdAt",Math.max(0,createdAt));j.put("updatedAt",Math.max(0,updatedAt));j.put("resolvedAt",Math.max(0,resolvedAt));
 }catch(Exception ignored){}return j;}

 public static EmotionEpisodeState fromJson(JSONObject j){EmotionEpisodeState e=new EmotionEpisodeState();if(j==null)return e;
  e.id=j.optString("id","");e.sourceMemoryId=j.optString("sourceMemoryId","");e.sourceKind=j.optString("sourceKind","");e.targetId=j.optString("targetId","");e.primaryEmotion=j.optString("primaryEmotion","calm");e.cause=j.optString("cause","");e.status=j.optString("status","ACTIVE");
  e.intensity=f01(j.optDouble("intensity"));e.valence=fs(j.optDouble("valence"));e.arousal=f01(j.optDouble("arousal"));e.goalCongruence=fs(j.optDouble("goalCongruence"));e.threat=f01(j.optDouble("threat"));e.loss=f01(j.optDouble("loss"));e.frustration=f01(j.optDouble("frustration"));e.uncertainty=f01(j.optDouble("uncertainty"));e.socialRelevance=f01(j.optDouble("socialRelevance"));e.bodilyLoad=f01(j.optDouble("bodilyLoad"));e.perceivedControl=f01(j.optDouble("perceivedControl",.5));e.joy=f01(j.optDouble("joy"));e.fear=f01(j.optDouble("fear"));e.sadness=f01(j.optDouble("sadness"));e.anger=f01(j.optDouble("anger"));e.curiosity=f01(j.optDouble("curiosity"));e.loneliness=f01(j.optDouble("loneliness"));e.relief=f01(j.optDouble("relief"));e.createdAt=Math.max(0,j.optLong("createdAt"));e.updatedAt=Math.max(0,j.optLong("updatedAt"));e.resolvedAt=Math.max(0,j.optLong("resolvedAt"));return e;
 }
 private static double f01(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
 private static double fs(double v){return Double.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
}
