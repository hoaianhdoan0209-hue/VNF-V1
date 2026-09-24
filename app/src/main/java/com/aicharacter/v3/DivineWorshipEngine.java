package com.aicharacter.v3;

import java.util.*;

/**
 * Causal worship field shared by ACTIVE/OFFLINE simulation.
 * Species are never preselected; only abundance, witnessed divine influence and history change devotion.
 */
public final class DivineWorshipEngine {
 private static final double BASE_DEVOTION=.18;
 private static final double DEVOTION_FLOOR=.08;
 private static final double HISTORY_TAU_MIN=30.0*1440.0;
 private DivineWorshipEngine(){}

 public static void advance(WorldState s,double minutes,long now){
  if(s==null||!Double.isFinite(minutes)||minutes<=0)return;
  if(s.livingWorld==null)s.livingWorld=new LivingWorldState();
  if(s.divineOntology==null)s.divineOntology=new DivineOntologyState();
  DivineOntologyState o=s.divineOntology;o.clamp();

  // Every known population field receives the same divine baseline independent of species identity.
  for(SpeciesPopulationState p:s.livingWorld.populations.values()){
   if(p==null)continue;p.clamp();
   SpeciesDivineState d=s.livingWorld.divine(p.speciesKey,p.areaId);
   if(d.lastUpdatedAt<=0)d.lastUpdatedAt=now;
  }

  consumeNewDivineInfluence(s,now);

  for(SpeciesDivineState d:s.livingWorld.divineStates.values()){
   if(d==null)continue;d.clamp();
   d.witnessInfluence=follow(d.witnessInfluence,0,minutes,10*1440.0);
   double targetDev=Math.max(DEVOTION_FLOOR,Math.min(1,BASE_DEVOTION+d.witnessInfluence*.58));
   double targetRev=Math.max(DEVOTION_FLOOR,Math.min(1,BASE_DEVOTION+d.witnessInfluence*.66));
   double targetTrust=Math.max(0,Math.min(1,BASE_DEVOTION+d.witnessInfluence*.50));
   d.devotion=follow(d.devotion,targetDev,minutes,3*1440.0);
   d.reverence=follow(d.reverence,targetRev,minutes,4*1440.0);
   d.trust=follow(d.trust,targetTrust,minutes,5*1440.0);
   d.ritualization=follow(d.ritualization,.02,minutes,60*1440.0);
   d.offeringTradition=follow(d.offeringTradition,0,minutes,120*1440.0);
   d.lastUpdatedAt=now;d.clamp();
  }

  // Haru and the player-cat know of Thần as living beings too. No contact is fabricated.
  o.haruDevotion=follow(o.haruDevotion,BASE_DEVOTION,minutes,7*1440.0);
  o.catDevotion=follow(o.catDevotion,BASE_DEVOTION,minutes,7*1440.0);

  double weighted=0,weight=0;
  for(Map.Entry<String,SpeciesPopulationState> e:s.livingWorld.populations.entrySet()){
   SpeciesPopulationState p=e.getValue();if(p==null)continue;p.clamp();if(p.relativeAbundance<=0)continue;
   SpeciesDivineState d=s.livingWorld.divineStates.get(e.getKey());if(d==null)continue;d.clamp();
   double w=Math.max(.001,p.relativeAbundance);weighted+=d.devotion*w;weight+=w;
  }
  // Individuals remain small contributors relative to a populated living world.
  weighted+=o.haruDevotion*.18+o.catDevotion*.18;weight+=.36;
  double current=weight<=0?BASE_DEVOTION:weighted/weight;
  o.currentWorship=Math.max(DEVOTION_FLOOR,Math.min(1,finite(current,BASE_DEVOTION)));

  // Historical worship is monotonic: lower worship reduces current bandwidth, not learned knowledge.
  double k=1-Math.exp(-Math.max(0,minutes)*o.currentWorship/HISTORY_TAU_MIN);
  o.accumulatedWorship=Math.max(o.accumulatedWorship,Math.min(1,o.accumulatedWorship+(1-o.accumulatedWorship)*k));
  o.lastUpdatedAt=now;o.clamp();
 }

 private static void consumeNewDivineInfluence(WorldState s,long now){
  if(s.worldHistory==null||s.worldHistory.isEmpty())return;
  DivineOntologyState o=s.divineOntology;
  for(WorldHistoryEntry e:s.worldHistory){
   if(e==null||e.eventId==null||e.eventId.isEmpty()||o.processedInfluenceEventIds.contains(e.eventId))continue;
   if(!"GOD_WORLD_CONDITION_APPLIED".equals(e.type))continue;
   WorldConditionProposalState p=GodWorldConditionContract.get(s,e.entity);
   if(p==null||!"APPLIED".equals(p.status))continue;
   double impact=.10+.24*unit(p.intensity,0);
   for(SpeciesPopulationState pop:s.livingWorld.populations.values()){
    if(pop==null)continue;pop.clamp();if(pop.relativeAbundance<=0||!withinScope(pop.areaId,p))continue;
    SpeciesDivineState d=s.livingWorld.divine(pop.speciesKey,pop.areaId);
    d.witnessInfluence=unit(d.witnessInfluence+impact,0);
    d.devotion=Math.max(DEVOTION_FLOOR,Math.min(1,d.devotion+.035+.055*unit(p.intensity,0)));
    d.reverence=Math.max(DEVOTION_FLOOR,Math.min(1,d.reverence+.045+.060*unit(p.intensity,0)));
    d.trust=unit(d.trust+.020+.035*unit(p.intensity,0),0);
    d.ritualization=unit(d.ritualization+.025+.055*unit(p.intensity,0),0);
    d.lastWitnessAt=e.time;d.witnessedDivineEvents++;d.clamp();
   }
   if(individualInScope(s,s.haruX,null,p))o.haruDevotion=Math.min(1,o.haruDevotion+.03+.05*unit(p.intensity,0));
   if(individualInScope(s,s.catState==null?s.catX:s.catState.x,s.catState==null?null:s.catState.areaId,p))o.catDevotion=Math.min(1,o.catDevotion+.03+.05*unit(p.intensity,0));
   o.processedInfluenceEventIds.add(e.eventId);
  }
  o.clamp();
 }

 private static boolean withinScope(String areaId,WorldConditionProposalState p){
  if(p==null)return false;if("GLOBAL".equals(p.scopeType))return true;return "AREA".equals(p.scopeType)&&p.scopeTarget!=null&&p.scopeTarget.equals(areaId);
 }
 private static boolean individualInScope(WorldState s,float x,String knownArea,WorldConditionProposalState p){
  if(p==null)return false;if("GLOBAL".equals(p.scopeType))return true;if(!"AREA".equals(p.scopeType))return false;
  String area=knownArea;if((area==null||area.isEmpty())&&s.world!=null){WorldArea a=s.world.areaAt(x);area=a==null?"":a.id;}
  return p.scopeTarget!=null&&p.scopeTarget.equals(area);
 }
 private static double follow(double v,double target,double minutes,double tau){
  v=finite(v,target);target=finite(target,v);if(minutes<=0)return v;double k=1-Math.exp(-minutes/Math.max(.05,tau));return v+(target-v)*k;
 }
 private static double finite(double v,double fallback){return Double.isFinite(v)?v:fallback;}
 private static double unit(double v,double fallback){v=finite(v,fallback);return Math.max(0,Math.min(1,v));}
}
