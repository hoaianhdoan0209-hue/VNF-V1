package com.aicharacter.v3;

import java.util.*;

/** Evidence-bound follower selection. Species identity is metadata only and never contributes to the score. */
public final class DivineFollowerEngine {
 private DivineFollowerEngine(){}

 public static void observe(WorldState s,long now){
  if(s==null)return;ensure(s);
  syncCandidate(s,"haru","haru_kind",areaAt(s,s.haruX,""),s.divineOntology.haruDevotion);
  syncCandidate(s,"player_cat","cat_kind",areaAt(s,s.catState==null?s.catX:s.catState.x,s.catState==null?"":s.catState.areaId),s.divineOntology.catDevotion);
  if(s.world!=null){
   for(WorldObject o:s.world.objects){
    if(o==null||!o.enabled||!"creature".equals(o.type))continue;
    String species=FantasyEcologyDictionary.keyForObject(o);if(species.isEmpty())species="unclassified";
    SpeciesDivineState d=s.livingWorld==null?null:s.livingWorld.divineStates.get(LivingWorldState.populationKey(species,o.areaId));
    double devotion=d==null?.18:d.devotion;
    syncCandidate(s,o.id,species,o.areaId,devotion);
   }
  }
  if(s.reedling!=null&&s.reedling.objectId!=null&&!s.reedling.objectId.isEmpty()){
   WorldObject o=s.world==null?null:s.world.object(s.reedling.objectId);String species=o==null?"reedling":FantasyEcologyDictionary.keyForObject(o);if(species.isEmpty())species="reedling";
   String area=s.reedling.areaId==null?"":s.reedling.areaId;SpeciesDivineState d=s.livingWorld.divineStates.get(LivingWorldState.populationKey(species,area));syncCandidate(s,s.reedling.objectId,species,area,d==null?.18:d.devotion);
  }

  ArrayList<DivineFollowerState> ranked=new ArrayList<>(s.divineOntology.followers.values());
  for(DivineFollowerState f:ranked){f.clamp();f.candidateScore=score(f);f.lastEvaluatedAt=now;if(!f.chosen)f.rank=f.candidateScore>=.55?"CANDIDATE":"WORSHIPPER";}
  ranked.sort((a,b)->Double.compare(b.candidateScore,a.candidateScore));
  int chosen=0;for(DivineFollowerState f:ranked)if(f.chosen)chosen++;
  int maxChosen=1+(int)Math.floor(s.divineOntology.accumulatedWorship*4.0);maxChosen=Math.max(1,Math.min(5,maxChosen));
  for(DivineFollowerState f:ranked){
   if(chosen>=maxChosen)break;if(f.chosen)continue;
   double livedEvidence=f.influence+f.service+f.offeringCommitment;
   if(f.candidateScore<.72||f.devotionSnapshot<.48||livedEvidence<.55)continue;
   f.chosen=true;f.rank="CHOSEN";f.chosenAt=now;chosen++;
   WorldHistoryEntry e=WorldEventBus.publishId(s,now,"divine_chosen_"+safe(f.entityId)+"_"+Long.toHexString(now),"DIVINE_FOLLOWER_CHOSEN",f.entityId,"Thần selected an individual follower from evidence of devotion, influence, reliability and service; species="+f.speciesKey+".");
   f.lastEvidenceEventId=e.eventId;f.evidenceEventIds.add(e.eventId);f.clamp();
  }
 }

 public static boolean recordEvidence(WorldState s,String entityId,String sourceEventId,double influence,double service,double reliabilityDelta,long now){
  if(s==null||entityId==null||entityId.isEmpty()||sourceEventId==null||!WorldEventBus.has(s,sourceEventId))return false;ensure(s);
  DivineFollowerState f=s.divineOntology.followers.computeIfAbsent(entityId,k->{DivineFollowerState x=new DivineFollowerState();x.entityId=k;return x;});
  f.clamp();if(f.evidenceEventIds.contains(sourceEventId))return false;
  f.influence=unit(f.influence+Math.max(0,finite(influence,0)));f.service=unit(f.service+Math.max(0,finite(service,0)));f.reliability=unit(f.reliability+finite(reliabilityDelta,0));f.lastEvidenceEventId=sourceEventId;f.evidenceEventIds.add(sourceEventId);f.lastEvaluatedAt=now;f.clamp();return true;
 }
 static boolean recordOffering(WorldState s,String entityId,String sourceEventId,double commitment,long now){
  if(!recordEvidence(s,entityId,sourceEventId,0,0,0,now))return false;
  DivineFollowerState f=s.divineOntology.followers.get(entityId);f.offeringCommitment=unit(f.offeringCommitment+Math.max(0,finite(commitment,0)));f.clamp();return true;
 }
 public static DivineFollowerState get(WorldState s,String entityId){ensure(s);DivineFollowerState f=s.divineOntology.followers.get(entityId);if(f!=null)f.clamp();return f;}
 public static double score(DivineFollowerState f){if(f==null)return 0;f.clamp();return unit(f.devotionSnapshot*.38+f.influence*.24+f.reliability*.18+f.service*.12+f.offeringCommitment*.08);}
 private static void syncCandidate(WorldState s,String id,String species,String area,double devotion){if(id==null||id.isEmpty())return;DivineFollowerState f=s.divineOntology.followers.computeIfAbsent(id,k->{DivineFollowerState x=new DivineFollowerState();x.entityId=k;return x;});f.speciesKey=species==null?"":species;f.areaId=area==null?"":area;f.devotionSnapshot=unit(devotion);f.clamp();}
 private static String areaAt(WorldState s,float x,String known){if(known!=null&&!known.isEmpty())return known;if(s.world==null)return"";WorldArea a=s.world.areaAt(x);return a==null?"":a.id;}
 private static void ensure(WorldState s){if(s.divineOntology==null)s.divineOntology=new DivineOntologyState();if(s.livingWorld==null)s.livingWorld=new LivingWorldState();s.divineOntology.clamp();}
 private static String safe(String s){return s==null?"unknown":s.replaceAll("[^A-Za-z0-9._-]","_");}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
 private static double unit(double v){return Math.max(0,Math.min(1,finite(v,0)));}
}
