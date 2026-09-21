package com.aicharacter.v3;
import org.json.JSONObject;
/** Effective feline work/supply state: enough biology to constrain locomotion without pretending to model every cell. */
public final class CatMetabolismState{public double demand=.14,availableEnergy=.78,oxygenDebt,metabolicHeat=.1,fatigue;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("demand",demand);j.put("availableEnergy",availableEnergy);j.put("oxygenDebt",oxygenDebt);j.put("metabolicHeat",metabolicHeat);j.put("fatigue",fatigue);}catch(Exception ignored){}return j;}
 public static CatMetabolismState fromJson(JSONObject j){CatMetabolismState x=new CatMetabolismState();if(j==null)return x;x.demand=cl(j.optDouble("demand",.14));x.availableEnergy=cl(j.optDouble("availableEnergy",.78));x.oxygenDebt=cl(j.optDouble("oxygenDebt"));x.metabolicHeat=cl(j.optDouble("metabolicHeat",.1));x.fatigue=cl(j.optDouble("fatigue"));return x;}private static double cl(double v){return Math.max(0,Math.min(1,v));}
}