package com.aicharacter.v3;

import org.json.JSONObject;

/** One concrete offering. Yield is power only; offerings never manufacture knowledge. */
public final class DivineOfferingState {
 public String offeringId="",giverId="",speciesKey="",areaId="",kind="RESOURCE",status="OFFERED",requestedGiftKind="",provenance="";
 public double materialValue=0,devotionalMeaning=0,sacrificeCost=0,divineYield=0;
 public boolean voluntary=true,accepted=false;
 public long offeredAt=0,decidedAt=0;

 public void clamp(){if(offeringId==null)offeringId="";if(giverId==null)giverId="";if(speciesKey==null)speciesKey="";if(areaId==null)areaId="";if(kind==null)kind="RESOURCE";if(status==null)status="OFFERED";if(requestedGiftKind==null)requestedGiftKind="";if(provenance==null)provenance="";materialValue=unit(materialValue);devotionalMeaning=unit(devotionalMeaning);sacrificeCost=unit(sacrificeCost);divineYield=Math.max(0,Math.min(20,finite(divineYield,0)));if(offeredAt<0)offeredAt=0;if(decidedAt<0)decidedAt=0;}
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{j.put("offeringId",offeringId);j.put("giverId",giverId);j.put("speciesKey",speciesKey);j.put("areaId",areaId);j.put("kind",kind);j.put("status",status);j.put("requestedGiftKind",requestedGiftKind);j.put("provenance",provenance);j.put("materialValue",materialValue);j.put("devotionalMeaning",devotionalMeaning);j.put("sacrificeCost",sacrificeCost);j.put("divineYield",divineYield);j.put("voluntary",voluntary);j.put("accepted",accepted);j.put("offeredAt",offeredAt);j.put("decidedAt",decidedAt);}catch(Exception ignored){}return j;}
 public static DivineOfferingState fromJson(JSONObject j){DivineOfferingState s=new DivineOfferingState();if(j==null)return s;s.offeringId=j.optString("offeringId","");s.giverId=j.optString("giverId","");s.speciesKey=j.optString("speciesKey","");s.areaId=j.optString("areaId","");s.kind=j.optString("kind","RESOURCE");s.status=j.optString("status","OFFERED");s.requestedGiftKind=j.optString("requestedGiftKind","");s.provenance=j.optString("provenance","");s.materialValue=j.optDouble("materialValue",0);s.devotionalMeaning=j.optDouble("devotionalMeaning",0);s.sacrificeCost=j.optDouble("sacrificeCost",0);s.divineYield=j.optDouble("divineYield",0);s.voluntary=j.optBoolean("voluntary",true);s.accepted=j.optBoolean("accepted",false);s.offeredAt=j.optLong("offeredAt");s.decidedAt=j.optLong("decidedAt");s.clamp();return s;}
 private static double unit(double v){return Math.max(0,Math.min(1,finite(v,0)));}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
}
