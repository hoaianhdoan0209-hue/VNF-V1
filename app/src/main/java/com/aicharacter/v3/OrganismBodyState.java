package com.aicharacter.v3;
import org.json.JSONObject;
/** VNF-organism body state. These are fictional life variables, not Earth anatomy or lab values. */
public final class OrganismBodyState{
 public double vitalReserve=.78,fluidBalance=.72,structure=.94,rhythm=.42,strain=.08,surfaceIntegrity=.92;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("vitalReserve",vitalReserve);j.put("fluidBalance",fluidBalance);j.put("structure",structure);j.put("rhythm",rhythm);j.put("strain",strain);j.put("surfaceIntegrity",surfaceIntegrity);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static OrganismBodyState fromJson(JSONObject j){OrganismBodyState b=new OrganismBodyState();if(j==null)return b;b.vitalReserve=cl(j.optDouble("vitalReserve",b.vitalReserve));b.fluidBalance=cl(j.optDouble("fluidBalance",b.fluidBalance));b.structure=cl(j.optDouble("structure",b.structure));b.rhythm=cl(j.optDouble("rhythm",b.rhythm));b.strain=cl(j.optDouble("strain",b.strain));b.surfaceIntegrity=cl(j.optDouble("surfaceIntegrity",b.surfaceIntegrity));b.lastUpdatedAt=j.optLong("lastUpdatedAt");return b;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}