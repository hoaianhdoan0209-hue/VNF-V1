package com.aicharacter.v3;import org.json.JSONObject;
/** Effective circulation; normalized values are internal physiology, never player meters. */
public final class CirculationState{public double cardiacDrive=.28,perfusion=1,oxygenDelivery=1;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("cardiacDrive",cardiacDrive);j.put("perfusion",perfusion);j.put("oxygenDelivery",oxygenDelivery);}catch(Exception ignored){}return j;}
 public static CirculationState fromJson(JSONObject j){CirculationState x=new CirculationState();if(j==null)return x;x.cardiacDrive=cl(j.optDouble("cardiacDrive",.28));x.perfusion=cl(j.optDouble("perfusion",1));x.oxygenDelivery=cl(j.optDouble("oxygenDelivery",1));return x;}private static double cl(double v){return Math.max(0,Math.min(1,v));}}
