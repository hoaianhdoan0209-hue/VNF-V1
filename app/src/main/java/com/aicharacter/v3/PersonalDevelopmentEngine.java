package com.aicharacter.v3;
import java.util.*;

/** Slow personal development derived only from lived evidence. It never scripts choices. */
public final class PersonalDevelopmentEngine {
 private PersonalDevelopmentEngine(){}
 public static void learn(WorldState s,MemoryEntry m){
  if(s==null||m==null)return;if(s.personality==null)s.personality=new PersonalityState();
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
 /**
  * Contextual expression pressure retained for diagnostics/future realization.
  * It is deliberately not consumed as action utility by LifeDecisionEngine.
  */
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
 public static String diagnostic(WorldState s,long now){
  if(s==null||s.personality==null)return "PERSONALITY V3 <no state>";
  PersonalityState p=s.personality;
  BehaviorExpressionEngine.Snapshot x=BehaviorExpressionEngine.observe(s,now);
  return "PERSONALITY V3\n"+
   "curiosity="+fmt(p.curiosity)+"[e="+p.curiosityEvidence+",opp="+fmt(p.curiosityOpposition)+"] caution="+fmt(p.caution)+"[e="+p.cautionEvidence+",opp="+fmt(p.cautionOpposition)+"]\n"+
   "sociability="+fmt(p.sociability)+"[e="+p.sociabilityEvidence+",opp="+fmt(p.sociabilityOpposition)+"] independence="+fmt(p.independence)+"[e="+p.independenceEvidence+",opp="+fmt(p.independenceOpposition)+"] patience="+fmt(p.patience)+"[e="+p.patienceEvidence+",opp="+fmt(p.patienceOpposition)+"]\n"+
   "context="+(s.currentIntention==null?"":s.currentIntention)+" expressionPressure="+fmt(expression(s,s.currentIntention))+"\n"+
   "visible="+(x.stageDirection().isEmpty()?"<none>":x.stageDirection())+"\n"+
   "contract=lived evidence -> contradiction buffer -> slow traits -> contextual expression; no direct action control";
 }
 private static String fmt(double v){return String.format(Locale.US,"%.3f",v);}
}