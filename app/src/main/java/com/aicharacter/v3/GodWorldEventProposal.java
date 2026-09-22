package com.aicharacter.v3;
import org.json.JSONObject;
import java.util.Locale;

/** Bounded God request for a current/future world condition. A proposal is not history or an applied physical fact. */
public final class GodWorldEventProposal{
 public final String id,type,areaId,reason;
 public final String condition,desiredValue,scopeType,scopeTarget,provenanceLayer,sourceRef,rollbackPolicy;
 public final double intensity,durationMinutes,evolution,sourceConfidence;
 public final long requestedAt;

 private GodWorldEventProposal(String id,String type,String areaId,String reason,String condition,String desiredValue,
                               String scopeType,String scopeTarget,double intensity,double durationMinutes,double evolution,
                               String provenanceLayer,String sourceRef,double sourceConfidence,String rollbackPolicy,long requestedAt){
  this.id=id;this.type=type;this.areaId=areaId;this.reason=reason;this.condition=condition;this.desiredValue=desiredValue;
  this.scopeType=scopeType;this.scopeTarget=scopeTarget;this.intensity=intensity;this.durationMinutes=durationMinutes;this.evolution=evolution;
  this.provenanceLayer=provenanceLayer;this.sourceRef=sourceRef;this.sourceConfidence=sourceConfidence;this.rollbackPolicy=rollbackPolicy;this.requestedAt=requestedAt;
 }

 public static GodWorldEventProposal fromJson(JSONObject j,long now){
  if(j==null)return null;
  String id=safe(j.optString("id","god_event"),80);
  String type=safe(j.optString("type","WORLD_CONDITION"),40).toUpperCase(Locale.ROOT);
  String condition=safe(j.optString("condition",""),40).toUpperCase(Locale.ROOT);
  String desired=safe(j.optString("desiredValue",""),64).toUpperCase(Locale.ROOT);
  if(condition.isEmpty()){
   if("ATMOSPHERE_PERTURBATION".equals(type))condition="ATMOSPHERE_PERTURBATION";
   else if(type.startsWith("WEATHER_")){condition="WEATHER";desired=type.substring("WEATHER_".length());}
  }
  String area=safe(j.optString("areaId",""),80);
  String scopeType=safe(j.optString("scopeType",area.isEmpty()?"GLOBAL":"AREA"),20).toUpperCase(Locale.ROOT);
  String scopeTarget=safe(j.optString("scopeTarget",area),80);
  String reason=safe(j.optString("reason",""),240);
  double intensity=unit(j.optDouble("intensity",.5)),duration=Math.max(1,Math.min(360,finite(j.optDouble("durationMinutes",30),30))),evolution=signed(j.optDouble("evolution",0));
  String provenance=safe(j.optString("provenanceLayer","GOD_PROPOSAL"),32).toUpperCase(Locale.ROOT);
  String sourceRef=safe(j.optString("sourceRef","god:proposal"),160);
  double sourceConfidence=j.has("sourceConfidence")?unit(j.optDouble("sourceConfidence",0)):-1;
  String rollback=safe(j.optString("rollbackPolicy","RESTORE_PREVIOUS_BASELINE"),48).toUpperCase(Locale.ROOT);
  long requested=j.has("requestedAt")?j.optLong("requestedAt",now):now;
  return condition.isEmpty()?null:new GodWorldEventProposal(id,type,area,reason,condition,desired,scopeType,scopeTarget,intensity,duration,evolution,provenance,sourceRef,sourceConfidence,rollback,requested);
 }

 public static Result validate(WorldState s,GodWorldEventProposal p,long now){
  if(s==null||p==null)return new Result(false,"missing proposal");
  if(p.id.isEmpty()||!p.id.matches("[A-Za-z0-9._-]{1,80}"))return new Result(false,"invalid proposal id");
  if(p.requestedAt>now+5L*60L*1000L||p.requestedAt<now-5L*60L*1000L)return new Result(false,"requestedAt must be present-time bounded");
  if(s.committedGodEventIds.contains(p.id)||WorldEventBus.hasPrefix(s,"god_world_"+p.id+"_"))return new Result(false,"God event proposal already committed");
  if(!("GLOBAL".equals(p.scopeType)||"AREA".equals(p.scopeType)))return new Result(false,"unsupported scope");
  if("AREA".equals(p.scopeType)&&(p.scopeTarget.isEmpty()||s.world==null||s.world.area(p.scopeTarget)==null))return new Result(false,"unknown world area");
  if(p.durationMinutes<1||p.durationMinutes>360||p.intensity<0||p.intensity>1)return new Result(false,"proposal bounds invalid");
  if(!("RESTORE_PREVIOUS_BASELINE".equals(p.rollbackPolicy)||"DISSIPATE_TO_BASELINE".equals(p.rollbackPolicy)))return new Result(false,"unsupported rollback policy");
  if("REAL_REFERENCE".equals(p.provenanceLayer)&&(p.sourceRef.isEmpty()||p.sourceConfidence<0))return new Result(false,"reference provenance incomplete");
  return new Result(true,"bounded world condition proposal");
 }

 public static GodWorldEventProposal fromSignedPayload(JSONObject j,long receivedAt){
  if(j==null)return null;String kind=j.optString("kind","");
  if(!("god-world-event-v1".equals(kind)||"god-world-condition-v2".equals(kind)))return null;
  return fromJson(j,receivedAt);
 }
 public static final class Result{public final boolean ok;public final String reason;Result(boolean ok,String reason){this.ok=ok;this.reason=reason;}}
 private static String safe(String s,int n){if(s==null)return"";s=s.replace('\n',' ').replace('\r',' ').trim();return s.length()>n?s.substring(0,n):s;}
 private static double unit(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):.5;}
 private static double signed(double v){return Double.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
 private static double finite(double v,double fallback){return Double.isFinite(v)?v:fallback;}
}
