package com.aicharacter.v3;

import java.util.*;

/** Converts real, provenance-backed offerings into Divine Power. It never creates knowledge or guarantees a gift. */
public final class DivineOfferingEngine {
 private static final Set<String> KINDS=new LinkedHashSet<>(Arrays.asList("RESOURCE","RELIC","LABOR","KNOWLEDGE_TRIBUTE","LIFE_COST"));
 private DivineOfferingEngine(){}

 public static Result submit(WorldState s,String id,String giverId,String speciesKey,String areaId,String kind,double materialValue,double devotionalMeaning,double sacrificeCost,boolean voluntary,String requestedGiftKind,String sourceEventId,long now){
  if(s==null||id==null||!id.matches("[A-Za-z0-9._-]{1,80}")||giverId==null||giverId.isEmpty())return new Result(false,"invalid offering",null);
  if(sourceEventId==null||!WorldEventBus.has(s,sourceEventId))return new Result(false,"offering lacks durable surrender evidence",null);
  ensure(s);if(s.divineOntology.offerings.containsKey(id))return new Result(false,"offering already recorded",s.divineOntology.offerings.get(id));
  String k=kind==null?"RESOURCE":kind.toUpperCase(Locale.ROOT);if(!KINDS.contains(k))return new Result(false,"unsupported offering kind",null);
  DivineOfferingState o=new DivineOfferingState();o.offeringId=id;o.giverId=giverId;o.speciesKey=speciesKey==null?"":speciesKey;o.areaId=areaId==null?"":areaId;o.kind=k;o.materialValue=unit(materialValue);o.devotionalMeaning=unit(devotionalMeaning);o.sacrificeCost=unit(sacrificeCost);o.voluntary=voluntary;o.requestedGiftKind=requestedGiftKind==null?"":requestedGiftKind;o.provenance=sourceEventId;o.offeredAt=now;

  DivineFollowerState f=DivineFollowerEngine.get(s,giverId);double devotion=f==null?.18:f.devotionSnapshot,reliability=f==null?.5:f.reliability;
  double commitment=o.materialValue+o.sacrificeCost;
  boolean acceptable=voluntary&&o.devotionalMeaning>=.20&&commitment>=.12;
  if("LIFE_COST".equals(k))acceptable=acceptable&&o.devotionalMeaning>=.55&&reliability>=.45;
  if(acceptable){
   double raw=o.materialValue*.25+o.devotionalMeaning*.45+o.sacrificeCost*.20+devotion*.10;
   if("LIFE_COST".equals(k))raw*=.80; // living cost is never mechanically more valuable than safer offerings.
   o.divineYield=Math.max(.05,Math.min(8,raw*8));o.accepted=true;o.status="ACCEPTED";o.decidedAt=now;
   DivinePowerState p=s.divineOntology.power;p.reserve=Math.min(100,p.reserve+o.divineYield);p.lifetimeYield=Math.min(100000,p.lifetimeYield+o.divineYield);p.acceptedOfferings++;p.lastOfferingAt=now;p.clamp();
   s.divineOntology.offerings.put(id,o);
   WorldHistoryEntry e=WorldEventBus.publishId(s,now,"divine_offering_accept_"+safe(id)+"_"+Long.toHexString(now),"DIVINE_OFFERING_ACCEPTED",giverId,"Offering accepted as divine power; kind="+k+" requestedGift="+o.requestedGiftKind+".");
   DivineFollowerEngine.recordOffering(s,giverId,e.eventId,unit(.08+o.devotionalMeaning*.10+o.sacrificeCost*.06),now);
   if(s.livingWorld!=null&&!o.speciesKey.isEmpty()&&!o.areaId.isEmpty()){SpeciesDivineState culture=s.livingWorld.divine(o.speciesKey,o.areaId);culture.ritualization=unit(culture.ritualization+.02+.035*o.devotionalMeaning);culture.offeringTradition=unit(culture.offeringTradition+.04+.08*o.devotionalMeaning);culture.clamp();}
   return new Result(true,"offering accepted",o);
  }
  o.accepted=false;o.status="REFUSED";o.divineYield=0;o.decidedAt=now;s.divineOntology.power.refusedOfferings++;s.divineOntology.power.lastOfferingAt=now;s.divineOntology.power.clamp();s.divineOntology.offerings.put(id,o);
  WorldEventBus.publishId(s,now,"divine_offering_refuse_"+safe(id)+"_"+Long.toHexString(now),"DIVINE_OFFERING_REFUSED",giverId,"Offering was not accepted; no Divine Power was created.");
  return new Result(true,"offering refused",o);
 }
 private static void ensure(WorldState s){if(s.divineOntology==null)s.divineOntology=new DivineOntologyState();s.divineOntology.clamp();}
 private static String safe(String s){return s==null?"unknown":s.replaceAll("[^A-Za-z0-9._-]","_");}
 private static double unit(double v){if(!Double.isFinite(v))return 0;return Math.max(0,Math.min(1,v));}
 public static final class Result{public final boolean ok;public final String message;public final DivineOfferingState offering;Result(boolean ok,String m,DivineOfferingState o){this.ok=ok;message=m;offering=o;}}
}
