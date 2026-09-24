package com.aicharacter.v3;

import java.util.*;

/** Bounded gift contract. World/Biology systems must explicitly consume a gift before it has an effect. */
public final class DivineGiftContract {
 private static final Set<String> KINDS=new LinkedHashSet<>(Arrays.asList("WEATHER_SENSE","DIVINE_SIGNAL","COLD_RESILIENCE","MINOR_RESTORATION","AREA_WARD","PRAYER_RELAY"));
 private DivineGiftContract(){}

 public static Result grant(WorldState s,String giftId,String bearerId,String kind,double intensity,double durationMinutes,String scopeType,String scopeTarget,String sourceEventId,long now){
  if(s==null||giftId==null||!giftId.matches("[A-Za-z0-9._-]{1,80}")||bearerId==null||bearerId.isEmpty())return new Result(false,"invalid gift",null);
  if(sourceEventId==null||!WorldEventBus.has(s,sourceEventId))return new Result(false,"gift lacks durable provenance",null);
  ensure(s);if(s.divineOntology.gifts.containsKey(giftId))return new Result(false,"gift already exists",s.divineOntology.gifts.get(giftId));
  DivineFollowerState f=DivineFollowerEngine.get(s,bearerId);if(f==null||!f.chosen)return new Result(false,"bearer is not chosen",null);
  String k=kind==null?"":kind.toUpperCase(Locale.ROOT);if(!KINDS.contains(k))return new Result(false,"unsupported gift kind",null);
  double in=unit(intensity),dur=Math.max(1,Math.min(1440,finite(durationMinutes,30))),cost=Math.max(.4,Math.min(20,.6+in*4.2+dur/1440.0*1.8));
  DivinePowerState p=s.divineOntology.power;p.clamp();if(p.available()+1e-9<cost)return new Result(false,"insufficient Divine Power",null);
  DivineGiftState g=new DivineGiftState();g.giftId=giftId;g.bearerId=bearerId;g.kind=k;g.intensity=in;g.durationMinutes=dur;g.scopeType="AREA".equals(scopeType)?"AREA":"ENTITY";g.scopeTarget=scopeTarget==null||scopeTarget.isEmpty()?bearerId:scopeTarget;g.provenance=sourceEventId;g.powerCost=cost;g.grantedAt=now;g.expiresAt=now+(long)(dur*60000.0);g.status="GRANTED";g.clamp();
  p.reservedForGifts=Math.min(p.reserve,p.reservedForGifts+cost);p.clamp();s.divineOntology.gifts.put(giftId,g);f.giftCount++;f.clamp();
  WorldEventBus.publishId(s,now,"divine_gift_grant_"+safe(giftId)+"_"+Long.toHexString(now),"DIVINE_GIFT_GRANTED",bearerId,"Gift="+k+" scope="+g.scopeType+":"+g.scopeTarget+" intensity="+fmt(in)+" durationMin="+fmt(dur)+" status=GRANTED.");
  return new Result(true,"gift granted",g);
 }

 public static boolean activate(WorldState s,String giftId,String consumerToken,long now){
  ensure(s);DivineGiftState g=s.divineOntology.gifts.get(giftId);if(g==null||!"GRANTED".equals(g.status)||now>g.expiresAt)return false;DivinePowerState p=s.divineOntology.power;p.clamp();if(p.reserve+1e-9<g.powerCost)return false;
  p.reservedForGifts=Math.max(0,p.reservedForGifts-g.powerCost);p.reserve=Math.max(0,p.reserve-g.powerCost);p.clamp();g.status="ACTIVE";g.consumerToken=consumerToken==null?"":consumerToken;g.activatedAt=now;g.clamp();WorldEventBus.publishId(s,now,"divine_gift_active_"+safe(giftId)+"_"+Long.toHexString(now),"DIVINE_GIFT_ACTIVATED",g.bearerId,"Gift="+g.kind+" activated by bounded consumer="+g.consumerToken+".");return true;
 }
 public static boolean rollback(WorldState s,String giftId,String consumerToken,long now){
  ensure(s);DivineGiftState g=s.divineOntology.gifts.get(giftId);if(g==null||(!"GRANTED".equals(g.status)&&!"ACTIVE".equals(g.status)))return false;if(!g.consumerToken.isEmpty()&&consumerToken!=null&&!consumerToken.isEmpty()&&!g.consumerToken.equals(consumerToken))return false;
  if("GRANTED".equals(g.status)){s.divineOntology.power.reservedForGifts=Math.max(0,s.divineOntology.power.reservedForGifts-g.powerCost);s.divineOntology.power.clamp();}
  g.status="ROLLED_BACK";g.rolledBackAt=now;g.clamp();WorldEventBus.publishId(s,now,"divine_gift_rollback_"+safe(giftId)+"_"+Long.toHexString(now),"DIVINE_GIFT_ROLLED_BACK",g.bearerId,"Gift="+g.kind+" rolled back.");return true;
 }
 public static void tick(WorldState s,long now){if(s==null||s.divineOntology==null)return;for(DivineGiftState g:s.divineOntology.gifts.values()){if(g==null)continue;g.clamp();if(("GRANTED".equals(g.status)||"ACTIVE".equals(g.status))&&g.expiresAt>0&&now>=g.expiresAt){if("GRANTED".equals(g.status)){s.divineOntology.power.reservedForGifts=Math.max(0,s.divineOntology.power.reservedForGifts-g.powerCost);s.divineOntology.power.clamp();}g.status="EXPIRED";g.clamp();WorldEventBus.publishId(s,now,"divine_gift_expire_"+safe(g.giftId)+"_"+Long.toHexString(now),"DIVINE_GIFT_EXPIRED",g.bearerId,"Gift="+g.kind+" expired.");}}}
 public static DivineGiftState get(WorldState s,String id){ensure(s);DivineGiftState g=s.divineOntology.gifts.get(id);if(g!=null)g.clamp();return g;}
 private static void ensure(WorldState s){if(s.divineOntology==null)s.divineOntology=new DivineOntologyState();s.divineOntology.clamp();}
 private static String safe(String s){return s==null?"unknown":s.replaceAll("[^A-Za-z0-9._-]","_");}
 private static String fmt(double d){return String.format(Locale.US,"%.2f",finite(d,0));}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
 private static double unit(double v){return Math.max(0,Math.min(1,finite(v,0)));}
 public static final class Result{public final boolean ok;public final String message;public final DivineGiftState gift;Result(boolean ok,String m,DivineGiftState g){this.ok=ok;message=m;gift=g;}}
}
