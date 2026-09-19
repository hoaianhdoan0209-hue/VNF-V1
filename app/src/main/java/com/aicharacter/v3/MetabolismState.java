package com.aicharacter.v3;import org.json.JSONObject;
public final class MetabolismState{public double demand=.18,availableEnergy=1,metabolicHeat=.12,oxygenDebt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("demand",demand);j.put("availableEnergy",availableEnergy);j.put("metabolicHeat",metabolicHeat);j.put("oxygenDebt",oxygenDebt);}catch(Exception ignored){}return j;}
 public static MetabolismState fromJson(JSONObject j){MetabolismState x=new MetabolismState();if(j==null)return x;x.demand=cl(j.optDouble("demand",.18));x.availableEnergy=cl(j.optDouble("availableEnergy",1));x.metabolicHeat=cl(j.optDouble("metabolicHeat",.12));x.oxygenDebt=cl(j.optDouble("oxygenDebt"));return x;}private static double cl(double v){return Math.max(0,Math.min(1,v));}}
