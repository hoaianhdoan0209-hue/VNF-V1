package com.aicharacter.v3;
import org.json.JSONObject;

/** Persistent population field for a fictional species inside one authored world area. */
public final class SpeciesPopulationState {
 public String speciesKey="",areaId="";
 public double relativeAbundance=.22,carryingCapacity=.55,birthPressure=.18,recoveryPressure=.24,mortalityPressure=.04,competitionPressure=.08,migrationPressure=.06,resourcePressure=.18,weatherPressure=.06,seasonalInfluence=0;
 public long lastUpdatedAt;

 public void clamp(){
  relativeAbundance=cl(relativeAbundance);carryingCapacity=cl(carryingCapacity);birthPressure=cl(birthPressure);recoveryPressure=cl(recoveryPressure);mortalityPressure=cl(mortalityPressure);competitionPressure=cl(competitionPressure);migrationPressure=cl(migrationPressure);resourcePressure=cl(resourcePressure);weatherPressure=cl(weatherPressure);seasonalInfluence=Math.max(-1,Math.min(1,seasonalInfluence));
 }
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{j.put("speciesKey",speciesKey);j.put("areaId",areaId);j.put("relativeAbundance",relativeAbundance);j.put("carryingCapacity",carryingCapacity);j.put("birthPressure",birthPressure);j.put("recoveryPressure",recoveryPressure);j.put("mortalityPressure",mortalityPressure);j.put("competitionPressure",competitionPressure);j.put("migrationPressure",migrationPressure);j.put("resourcePressure",resourcePressure);j.put("weatherPressure",weatherPressure);j.put("seasonalInfluence",seasonalInfluence);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static SpeciesPopulationState fromJson(JSONObject j){SpeciesPopulationState p=new SpeciesPopulationState();if(j==null)return p;p.speciesKey=j.optString("speciesKey","");p.areaId=j.optString("areaId","");p.relativeAbundance=j.optDouble("relativeAbundance",p.relativeAbundance);p.carryingCapacity=j.optDouble("carryingCapacity",p.carryingCapacity);p.birthPressure=j.optDouble("birthPressure",p.birthPressure);p.recoveryPressure=j.optDouble("recoveryPressure",p.recoveryPressure);p.mortalityPressure=j.optDouble("mortalityPressure",p.mortalityPressure);p.competitionPressure=j.optDouble("competitionPressure",p.competitionPressure);p.migrationPressure=j.optDouble("migrationPressure",p.migrationPressure);p.resourcePressure=j.optDouble("resourcePressure",p.resourcePressure);p.weatherPressure=j.optDouble("weatherPressure",p.weatherPressure);p.seasonalInfluence=j.optDouble("seasonalInfluence",0);p.lastUpdatedAt=j.optLong("lastUpdatedAt");p.clamp();return p;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
