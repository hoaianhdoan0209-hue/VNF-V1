package com.aicharacter.v3;
import org.json.JSONObject;

/** Persistent population field for a fictional species inside one authored world area. */
public final class SpeciesPopulationState {
 public String speciesKey="",areaId="";
 public double relativeAbundance=0,carryingCapacity=0,birthPressure=0,recoveryPressure=0,mortalityPressure=0,competitionPressure=0,migrationPressure=0,resourcePressure=0,weatherPressure=0,seasonalInfluence=0;
 public long lastUpdatedAt;

 public void clamp(){
  if(speciesKey==null)speciesKey="";if(areaId==null)areaId="";
  relativeAbundance=unit(relativeAbundance,0);carryingCapacity=unit(carryingCapacity,0);birthPressure=unit(birthPressure,0);recoveryPressure=unit(recoveryPressure,0);mortalityPressure=unit(mortalityPressure,0);competitionPressure=unit(competitionPressure,0);migrationPressure=unit(migrationPressure,0);resourcePressure=unit(resourcePressure,0);weatherPressure=unit(weatherPressure,0);seasonalInfluence=signed(seasonalInfluence);if(lastUpdatedAt<0)lastUpdatedAt=0;
 }
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{j.put("speciesKey",speciesKey);j.put("areaId",areaId);j.put("relativeAbundance",relativeAbundance);j.put("carryingCapacity",carryingCapacity);j.put("birthPressure",birthPressure);j.put("recoveryPressure",recoveryPressure);j.put("mortalityPressure",mortalityPressure);j.put("competitionPressure",competitionPressure);j.put("migrationPressure",migrationPressure);j.put("resourcePressure",resourcePressure);j.put("weatherPressure",weatherPressure);j.put("seasonalInfluence",seasonalInfluence);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static SpeciesPopulationState fromJson(JSONObject j){SpeciesPopulationState p=new SpeciesPopulationState();if(j==null)return p;p.speciesKey=j.optString("speciesKey","");p.areaId=j.optString("areaId","");p.relativeAbundance=j.optDouble("relativeAbundance",0);p.carryingCapacity=j.optDouble("carryingCapacity",0);p.birthPressure=j.optDouble("birthPressure",0);p.recoveryPressure=j.optDouble("recoveryPressure",0);p.mortalityPressure=j.optDouble("mortalityPressure",0);p.competitionPressure=j.optDouble("competitionPressure",0);p.migrationPressure=j.optDouble("migrationPressure",0);p.resourcePressure=j.optDouble("resourcePressure",0);p.weatherPressure=j.optDouble("weatherPressure",0);p.seasonalInfluence=j.optDouble("seasonalInfluence",0);p.lastUpdatedAt=j.optLong("lastUpdatedAt");p.clamp();return p;}
 private static double unit(double v,double fallback){if(!Double.isFinite(v))v=fallback;return Math.max(0,Math.min(1,v));}
 private static double signed(double v){if(!Double.isFinite(v))return 0;return Math.max(-1,Math.min(1,v));}
}
