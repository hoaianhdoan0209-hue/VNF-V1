package com.aicharacter.v3;
import org.json.JSONObject;
/** Effective muscle/joint system: enough mechanics to constrain motion without simulating individual fibers. */
public final class MusculoskeletalState{
 public double legForceCapacity=1,coreForceCapacity=1,armForceCapacity=1,legFatigue,coreFatigue,jointMobility=1,impactLoad,terrainEffort,brakingLoad,stepDemand;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("legForceCapacity",legForceCapacity);j.put("coreForceCapacity",coreForceCapacity);j.put("armForceCapacity",armForceCapacity);j.put("legFatigue",legFatigue);j.put("coreFatigue",coreFatigue);j.put("jointMobility",jointMobility);j.put("impactLoad",impactLoad);j.put("terrainEffort",terrainEffort);j.put("brakingLoad",brakingLoad);j.put("stepDemand",stepDemand);}catch(Exception ignored){}return j;}
 public static MusculoskeletalState fromJson(JSONObject j){MusculoskeletalState m=new MusculoskeletalState();if(j==null)return m;m.legForceCapacity=cl(j.optDouble("legForceCapacity",1));m.coreForceCapacity=cl(j.optDouble("coreForceCapacity",1));m.armForceCapacity=cl(j.optDouble("armForceCapacity",1));m.legFatigue=cl(j.optDouble("legFatigue"));m.coreFatigue=cl(j.optDouble("coreFatigue"));m.jointMobility=cl(j.optDouble("jointMobility",1));m.impactLoad=Math.max(0,j.optDouble("impactLoad"));m.terrainEffort=cl(j.optDouble("terrainEffort"));m.brakingLoad=cl(j.optDouble("brakingLoad"));m.stepDemand=cl(j.optDouble("stepDemand"));return m;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}