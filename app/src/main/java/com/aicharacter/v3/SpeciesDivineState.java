package com.aicharacter.v3;

import org.json.JSONObject;

/** Population-level relation to the one divine presence. Species identity never grants privileged devotion. */
public final class SpeciesDivineState {
 public String speciesKey="",areaId="";
 public double awareness=1.0,devotion=.18,reverence=.18,trust=.18,witnessInfluence=0;
 public long lastWitnessAt=0,lastUpdatedAt=0;
 public int witnessedDivineEvents=0;

 public void clamp(){
  if(speciesKey==null)speciesKey="";if(areaId==null)areaId="";
  // Ontology rule: living species know that Thần exists. Interpretation/intensity can differ, existence-awareness cannot.
  awareness=1.0;
  devotion=range(devotion,.18,.08,1);reverence=range(reverence,.18,.08,1);trust=unit(trust,.18);witnessInfluence=unit(witnessInfluence,0);
  if(lastWitnessAt<0)lastWitnessAt=0;if(lastUpdatedAt<0)lastUpdatedAt=0;if(witnessedDivineEvents<0)witnessedDivineEvents=0;
 }
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{
  j.put("speciesKey",speciesKey);j.put("areaId",areaId);j.put("awareness",awareness);j.put("devotion",devotion);j.put("reverence",reverence);j.put("trust",trust);j.put("witnessInfluence",witnessInfluence);j.put("lastWitnessAt",lastWitnessAt);j.put("lastUpdatedAt",lastUpdatedAt);j.put("witnessedDivineEvents",witnessedDivineEvents);
 }catch(Exception ignored){}return j;}
 public static SpeciesDivineState fromJson(JSONObject j){SpeciesDivineState s=new SpeciesDivineState();if(j==null){s.clamp();return s;}s.speciesKey=j.optString("speciesKey","");s.areaId=j.optString("areaId","");s.awareness=j.optDouble("awareness",1);s.devotion=j.optDouble("devotion",.18);s.reverence=j.optDouble("reverence",.18);s.trust=j.optDouble("trust",.18);s.witnessInfluence=j.optDouble("witnessInfluence",0);s.lastWitnessAt=j.optLong("lastWitnessAt");s.lastUpdatedAt=j.optLong("lastUpdatedAt");s.witnessedDivineEvents=j.optInt("witnessedDivineEvents");s.clamp();return s;}
 private static double unit(double v,double f){if(!Double.isFinite(v))v=f;return Math.max(0,Math.min(1,v));}
 private static double range(double v,double f,double lo,double hi){if(!Double.isFinite(v))v=f;return Math.max(lo,Math.min(hi,v));}
}
