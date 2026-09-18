package com.aicharacter.v3;
import java.util.*;
/** Slow offline personal development derived only from lived evidence. It never scripts choices. */
public final class PersonalDevelopmentEngine {
 private PersonalDevelopmentEngine(){}
 public static void learn(WorldState s,MemoryEntry m){
  if(s==null||m==null)return;
  double sig=Math.max(.15,m.importance),v=m.valence;
  if(hasAny(m,"explore","explore_garden","observe","observe_lake","observe_creature","watch_reedling","novel"))develop(s,"explore",v>=-.1?1:-1,sig);
  if(m.hasTag("danger")||m.hasTag("failure")||m.hasTag("failed_search"))develop(s,"danger",1,sig);
  if(m.hasTag("social")||m.hasTag("reunion"))develop(s,"social",v>=0?1:-1,sig);
  if(hasAny(m,"solitude","alone","seek_solitude","quiet_pause"))develop(s,"alone",v>=0?1:-1,sig);
  if(m.hasTag("wait")||m.hasTag("reconsidered"))develop(s,"wait",v>=-.1?1:-1,sig);
 }
 private static boolean hasAny(MemoryEntry m,String...tags){for(String t:tags)if(m.hasTag(t))return true;return false;}
 private static void develop(WorldState s,String axis,double direction,double sig){
  s.personality.slowlyLearn(axis,direction,Math.min(1.5,sig));
 }
 public static double expression(WorldState s,String intentionId){
  if(s==null||s.personality==null)return 0;
  PersonalityState p=s.personality; String id=intentionId==null?"":intentionId;
  if("explore".equals(id)||"explore_garden".equals(id)||"observe".equals(id)||"observe_lake".equals(id)||"observe_creature".equals(id)||"watch_reedling".equals(id))return (p.curiosity-.5)*7;
  if("seek_solitude".equals(id)||"quiet_pause".equals(id))return (p.independence-.5)*7;
  if("find_cat".equals(id))return (p.sociability-.5)*5+(p.patience-.5)*2;
  if("seek_shelter".equals(id)||"sleep".equals(id)||"recover".equals(id))return (p.caution-.5)*5;
  if("reflect".equals(id))return (p.patience-.5)*5;
  return 0;
 }
}