package com.aicharacter.v3;
import org.json.JSONObject;
/** Immune/lymphatic abstraction. It models readiness/inflammation/exposure pressure without inventing random diseases. */
public final class ImmuneState{
 public double readiness=.82,inflammation=.05,environmentalPressure=.03,recoverySupport=.80;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("readiness",readiness);j.put("inflammation",inflammation);j.put("environmentalPressure",environmentalPressure);j.put("recoverySupport",recoverySupport);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static ImmuneState fromJson(JSONObject j){ImmuneState i=new ImmuneState();if(j==null)return i;i.readiness=cl(j.optDouble("readiness",i.readiness));i.inflammation=cl(j.optDouble("inflammation",i.inflammation));i.environmentalPressure=cl(j.optDouble("environmentalPressure",i.environmentalPressure));i.recoverySupport=cl(j.optDouble("recoverySupport",i.recoverySupport));i.lastUpdatedAt=j.optLong("lastUpdatedAt");return i;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}