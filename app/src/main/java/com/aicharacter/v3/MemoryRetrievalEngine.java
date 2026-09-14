package com.aicharacter.v3;import java.util.*;
/** V0.97 associative retrieval: relevance, emotion, belief/intention context, recency and importance. */
public final class MemoryRetrievalEngine {private MemoryRetrievalEngine(){}
 public static List<MemoryEntry> retrieve(WorldState s,String location,String participant,String tag,int limit){return retrieve(s,location,participant,tag,s.currentIntention,limit,System.currentTimeMillis());}
 public static List<MemoryEntry> retrieve(WorldState s,String location,String participant,String tag,String intention,int limit,long now){List<MemoryEntry> all=new ArrayList<>(s.memories);all.sort((a,b)->Double.compare(score(b,now,location,participant,tag,intention,s),score(a,now,location,participant,tag,intention,s)));return all.subList(0,Math.min(limit,all.size()));}
 public static double score(MemoryEntry m,long now,String loc,String participant,String tag,WorldState s){return score(m,now,loc,participant,tag,s.currentIntention,s);}
 public static double score(MemoryEntry m,long now,String loc,String participant,String tag,String intention,WorldState s){
  double v=m.retrievalWeight(now);
  if(loc!=null&&!loc.isEmpty()&&loc.equals(m.location))v+=.46*Math.max(.2,m.importance);
  if(participant!=null&&m.participants.contains(participant))v+=.30;
  if(tag!=null&&m.hasTag(tag))v+=.34;
  if(intention!=null&&!intention.isEmpty()&&(m.hasTag(intention)||semanticMatch(intention,m)))v+=.22;
  double mood=s.mood.pleasantness; if(m.valence*mood>0)v+=.12*Math.abs(m.valence)*Math.min(1,Math.abs(mood)+.25);
  for(BeliefState b:s.beliefStates.values())for(BeliefEvidence e:b.evidence)if(m.memoryId.equals(e.sourceMemoryId))v+=.18*Math.abs(e.weight);
  return v;
 }
 private static boolean semanticMatch(String i,MemoryEntry m){if(i.contains("cat")&&(m.hasTag("cat")||m.participants.contains("cat")))return true;if(i.contains("reflect")&&(m.hasTag("reflect")||m.hasTag("rest")))return true;if(i.contains("shelter")&&(m.hasTag("rain")||m.hasTag("safety")))return true;return false;}
}