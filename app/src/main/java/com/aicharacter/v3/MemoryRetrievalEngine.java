package com.aicharacter.v3;import java.util.*;
/** Associative retrieval with bounded emotional diversity. Opposite valence is recall diversity, never belief counter-evidence. */
public final class MemoryRetrievalEngine {private MemoryRetrievalEngine(){}
 public static List<MemoryEntry> retrieve(WorldState s,String location,String participant,String tag,int limit){return retrieve(s,location,participant,tag,s.currentIntention,limit,System.currentTimeMillis());}
 public static List<MemoryEntry> retrieve(WorldState s,String location,String participant,String tag,String intention,int limit,long now){List<MemoryEntry> all=new ArrayList<>(s.memories);all.sort((a,b)->Double.compare(score(b,now,location,participant,tag,intention,s),score(a,now,location,participant,tag,intention,s)));int n=Math.min(limit,all.size());if(n<=0)return new ArrayList<>();List<MemoryEntry> out=new ArrayList<>(all.subList(0,n));MemoryEntry counter=bestDiverseMemory(all,out,now,location,participant,tag,intention,s);if(counter!=null&&n>1)out.set(n-1,counter);return out;}
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
 private static MemoryEntry bestDiverseMemory(List<MemoryEntry> all,List<MemoryEntry> chosen,long now,String loc,String participant,String tag,String intention,WorldState s){if(chosen.isEmpty())return null;double dominant=0;for(MemoryEntry m:chosen)dominant+=m.valence;if(Math.abs(dominant)<.35)return null;int sign=dominant>0?1:-1;MemoryEntry best=null;double bestScore=.34;for(MemoryEntry m:all){if(chosen.contains(m)||m.valence*sign>=-.08||!contextRelevant(m,loc,participant,tag,intention))continue;double sc=score(m,now,loc,participant,tag,intention,s)+Math.abs(m.valence)*.18;if(sc>bestScore){bestScore=sc;best=m;}}return best;}
 private static boolean contextRelevant(MemoryEntry m,String loc,String participant,String tag,String intention){if(loc!=null&&!loc.isEmpty()&&loc.equals(m.location))return true;if(participant!=null&&!participant.isEmpty()&&m.participants.contains(participant))return true;if(tag!=null&&!tag.isEmpty()&&m.hasTag(tag))return true;return intention!=null&&!intention.isEmpty()&&(m.hasTag(intention)||semanticMatch(intention,m));}
 private static boolean semanticMatch(String i,MemoryEntry m){if(i.contains("cat")&&(m.hasTag("cat")||m.participants.contains("cat")))return true;if(i.contains("reflect")&&(m.hasTag("reflect")||m.hasTag("rest")))return true;if(i.contains("shelter")&&(m.hasTag("rain")||m.hasTag("safety")))return true;return false;}
}