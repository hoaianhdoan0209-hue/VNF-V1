package com.aicharacter.v3;
import org.json.JSONObject;
/** Skin barrier authority: surface injury, irritation and environmental exposure. */
public final class SkinState{
 public double barrierIntegrity=.98,surfaceDamage=.01,irritation=.03,moistureExposure=.08,thermoregulationStrain=.08;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("barrierIntegrity",barrierIntegrity);j.put("surfaceDamage",surfaceDamage);j.put("irritation",irritation);j.put("moistureExposure",moistureExposure);j.put("thermoregulationStrain",thermoregulationStrain);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static SkinState fromJson(JSONObject j){SkinState s=new SkinState();if(j==null)return s;s.barrierIntegrity=cl(j.optDouble("barrierIntegrity",s.barrierIntegrity));s.surfaceDamage=cl(j.optDouble("surfaceDamage",s.surfaceDamage));s.irritation=cl(j.optDouble("irritation",s.irritation));s.moistureExposure=cl(j.optDouble("moistureExposure",s.moistureExposure));s.thermoregulationStrain=cl(j.optDouble("thermoregulationStrain",s.thermoregulationStrain));s.lastUpdatedAt=j.optLong("lastUpdatedAt");return s;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}