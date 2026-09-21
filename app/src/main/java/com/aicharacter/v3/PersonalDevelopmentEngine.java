package com.aicharacter.v3;
import java.util.*;

/** Slow personal development derived only from lived evidence. It never scripts choices. */
public final class PersonalDevelopmentEngine {
 private static final long REPETITION_WINDOW_MS=12L*60L*60L*1000L;
 private PersonalDevelopmentEngine(){}

 public static void learn(WorldState s,MemoryEntry m){
  if(s==null||m==null)return;if(s.personality==null)s.personality=new PersonalityState();
  double sig=evidenceWeight(s,m),v=m.valence;
  if(hasAny(m,"explore","explore_garden","observe","observe_lake","observe_creature","watch_reedling","novel"))develop(s,"explore",v>=-.1?1:-1,sig);
  if(m.hasTag("danger")||m.hasTag("failure")||m.hasTag("failed_search"))develop(s,"danger",1,sig);
  if(m.hasTag("social")||m.hasTag("reunion"))develop(s,"social",v>=0?1:-1,sig);
  if(hasAny(m,"solitude","alone","seek_solitude","quiet_pause"))develop(s,"alone",v>=0?1:-1,sig);
  if(m.hasTag("wait")||m.hasTag("reconsidered"))develop(s,"wait",v>=-.1?1:-1,sig);
 }

 static double evidenceWeight(WorldState s,MemoryEntry m){
  if(m==null)return 0;
  double importance=Math.max(.08,Math.min(1,Math.abs(m.importance)));
  double confidence=Math.max(0,Math.min(1,m.confidence));
  double base=importance*(.48+.52*confidence);
  if(isMajor(m))base*=1.28;
  int repeats=similarRecentEvidence(s,m,8);
  double novelty=1.0/(1.0+repeats*.42);
  if(isMajor(m))novelty=Math.max(.68,novelty);
  return Math.max(.08,Math.min(1.5,base*novelty));
 }

 private static int similarRecentEvidence(WorldState s,MemoryEntry current,int limit){
  if(s==null||s.memories==null||current==null)return 0;
  int n=0;
  for(int i=s.memories.size()-1;i>=0&&n<limit;i--){
   MemoryEntry old=s.memories.get(i);if(old==null||old==current||old.memoryId.equals(current.memoryId))continue;
   long age=Math.abs(current.time-old.time);if(age>REPETITION_WINDOW_MS)continue;
   if(similar(old,current))n++;
  }
  return n;
 }

 private static boolean similar(MemoryEntry a,MemoryEntry b){
  if(a==null||b==null)return false;
  boolean sameKind=a.kind!=null&&a.kind.equals(b.kind);
  boolean samePlace=a.location!=null&&!a.location.isEmpty()&&a.location.equals(b.location);
  int shared=0;
  for(String t:a.tags)if(isDevelopmentTag(t)&&b.hasTag(t))shared++;
  return (sameKind&&shared>0)||(samePlace&&shared>1);
 }

 private static boolean isDevelopmentTag(String t){
  return "explore".equals(t)||"explore_garden".equals(t)||"observe".equals(t)||"observe_lake".equals(t)||
   "observe_creature".equals(t)||"watch_reedling".equals(t)||"novel".equals(t)||"danger".equals(t)||
   "failure".equals(t)||"failed_search".equals(t)||"social".equals(t)||"reunion".equals(t)||
   "solitude".equals(t)||"alone".equals(t)||"seek_solitude".equals(t)||"quiet_pause".equals(t)||
   "wait".equals(t)||"reconsidered".equals(t);
 }

 private static boolean isMajor(MemoryEntry m){
  return m!=null&&(m.importance>=.82||m.hasTag("reunion")||m.hasTag("danger")||m.hasTag("failure")||
   m.hasTag("body_collapse")||m.hasTag("injury")||m.hasTag("loss"));
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
   "contract=lived evidence -> repetition damping -> contradiction buffer -> slow traits -> contextual expression; no direct action control";
 }
 private static String fmt(double v){return String.format(Locale.US,"%.3f",v);}
}
