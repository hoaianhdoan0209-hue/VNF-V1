package com.aicharacter.v3;

import org.json.JSONObject;

/** Bounded divine gift contract. A grant is not a direct physics/cognition write. */
public final class DivineGiftState {
 public String giftId="",bearerId="",kind="",scopeType="ENTITY",scopeTarget="",status="GRANTED",provenance="",rollbackPolicy="REMOVE_GIFT_EFFECT",consumerToken="";
 public double intensity=0,durationMinutes=0,powerCost=0;
 public long grantedAt=0,activatedAt=0,expiresAt=0,rolledBackAt=0;

 public void clamp(){if(giftId==null)giftId="";if(bearerId==null)bearerId="";if(kind==null)kind="";if(scopeType==null)scopeType="ENTITY";if(scopeTarget==null)scopeTarget="";if(status==null)status="GRANTED";if(provenance==null)provenance="";if(rollbackPolicy==null)rollbackPolicy="REMOVE_GIFT_EFFECT";if(consumerToken==null)consumerToken="";intensity=unit(intensity);durationMinutes=Math.max(1,Math.min(1440,finite(durationMinutes,30)));powerCost=Math.max(0,Math.min(20,finite(powerCost,0)));if(grantedAt<0)grantedAt=0;if(activatedAt<0)activatedAt=0;if(expiresAt<0)expiresAt=0;if(rolledBackAt<0)rolledBackAt=0;}
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{j.put("giftId",giftId);j.put("bearerId",bearerId);j.put("kind",kind);j.put("scopeType",scopeType);j.put("scopeTarget",scopeTarget);j.put("status",status);j.put("provenance",provenance);j.put("rollbackPolicy",rollbackPolicy);j.put("consumerToken",consumerToken);j.put("intensity",intensity);j.put("durationMinutes",durationMinutes);j.put("powerCost",powerCost);j.put("grantedAt",grantedAt);j.put("activatedAt",activatedAt);j.put("expiresAt",expiresAt);j.put("rolledBackAt",rolledBackAt);}catch(Exception ignored){}return j;}
 public static DivineGiftState fromJson(JSONObject j){DivineGiftState s=new DivineGiftState();if(j==null)return s;s.giftId=j.optString("giftId","");s.bearerId=j.optString("bearerId","");s.kind=j.optString("kind","");s.scopeType=j.optString("scopeType","ENTITY");s.scopeTarget=j.optString("scopeTarget","");s.status=j.optString("status","GRANTED");s.provenance=j.optString("provenance","");s.rollbackPolicy=j.optString("rollbackPolicy","REMOVE_GIFT_EFFECT");s.consumerToken=j.optString("consumerToken","");s.intensity=j.optDouble("intensity",0);s.durationMinutes=j.optDouble("durationMinutes",30);s.powerCost=j.optDouble("powerCost",0);s.grantedAt=j.optLong("grantedAt");s.activatedAt=j.optLong("activatedAt");s.expiresAt=j.optLong("expiresAt");s.rolledBackAt=j.optLong("rolledBackAt");s.clamp();return s;}
 private static double unit(double v){return Math.max(0,Math.min(1,finite(v,0)));}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
}
