package com.aicharacter.v3;
import org.json.JSONObject;
/** Fictional life-cycle rhythm shared by non-human VNF organisms. This is not Earth aging/reproduction. */
public final class OrganismCycleState{
 public String phase="QUIET";public double activityDebt=.10,recoveryDrive=.18,growthReserve=.42,adaptation=.20,decline=.02;public long phaseSince,lastUpdatedAt;public int completedCycles;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("phase",phase);j.put("activityDebt",activityDebt);j.put("recoveryDrive",recoveryDrive);j.put("growthReserve",growthReserve);j.put("adaptation",adaptation);j.put("decline",decline);j.put("phaseSince",phaseSince);j.put("lastUpdatedAt",lastUpdatedAt);j.put("completedCycles",completedCycles);}catch(Exception ignored){}return j;}
 public static OrganismCycleState fromJson(JSONObject j){OrganismCycleState c=new OrganismCycleState();if(j==null)return c;c.phase=j.optString("phase",c.phase);c.activityDebt=cl(j.optDouble("activityDebt",c.activityDebt));c.recoveryDrive=cl(j.optDouble("recoveryDrive",c.recoveryDrive));c.growthReserve=cl(j.optDouble("growthReserve",c.growthReserve));c.adaptation=cl(j.optDouble("adaptation",c.adaptation));c.decline=cl(j.optDouble("decline",c.decline));c.phaseSince=j.optLong("phaseSince");c.lastUpdatedAt=j.optLong("lastUpdatedAt");c.completedCycles=Math.max(0,j.optInt("completedCycles"));return c;}
 public void setPhase(String next,long now){if(next==null||next.equals(phase))return;phase=next;phaseSince=now;if("QUIET".equals(next)||"REST".equals(next))completedCycles++;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}