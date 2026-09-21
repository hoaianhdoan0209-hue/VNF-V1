package com.aicharacter.v3;

/**
 * Presentation-only bridge from durable cognition/history to short visible cues.
 * It never mutates simulation state and never creates intentions.
 */
public final class HaruVisibleBehaviorBridge {
 public enum Mode{NONE,THINK,REACT,SETTLE}
 public static final class Cue{
  public final Mode mode;public final String reason;public final long sourceAt;
  Cue(Mode m,String r,long at){mode=m;reason=r;sourceAt=at;}
  public boolean active(){return mode!=Mode.NONE;}
 }
 private static final long POST_OUTCOME_WINDOW_MS=7000L;
 private HaruVisibleBehaviorBridge(){}

 public static Cue observe(WorldState s,long now){
  if(s==null||s.worldHistory==null)return none();
  WorldHistoryEntry review=latest(s,"PLAN_POST_OUTCOME_REVIEWED",now,POST_OUTCOME_WINDOW_MS);
  if(review==null)return none();
  String intention=field(review.summary,"intention");
  String status=field(review.summary,"status");
  String memoryId=field(review.summary,"memory");
  MemoryEntry m=PlanCausalAudit.findMemory(s,memoryId);
  boolean failed="FAILED".equals(status)||(m!=null&&m.hasTag("failure"));
  if(failed)return new Cue(Mode.REACT,"recent learned action failure is still visibly settling",review.time);
  if(isReflective(intention))return new Cue(Mode.THINK,"recent completed observation/reflection remains briefly visible",review.time);
  if(isBodySettling(intention))return new Cue(Mode.SETTLE,"recent completed body-care action is still settling",review.time);
  return new Cue(Mode.REACT,"recent completed action produced a brief state-driven transition",review.time);
 }

 public static String diagnostic(WorldState s,long now){
  Cue c=observe(s,now);
  return "VISIBLE BEHAVIOR BRIDGE V3\nmode="+c.mode+"\nsourceAt="+c.sourceAt+"\nreason="+c.reason+
   "\ncontract=durable cognition/history -> short presentation cue; presentation never writes cognition";
 }

 private static WorldHistoryEntry latest(WorldState s,String type,long now,long maxAge){
  for(int i=s.worldHistory.size()-1;i>=0;i--){
   WorldHistoryEntry e=s.worldHistory.get(i);if(e==null)continue;
   if(now>=e.time&&now-e.time>maxAge)break;
   if(type.equals(e.type)&&now>=e.time&&now-e.time<=maxAge)return e;
  }
  return null;
 }
 private static String field(String summary,String key){
  if(summary==null||key==null)return "";
  String token=key+"=";int i=summary.indexOf(token);if(i<0)return "";
  int start=i+token.length(),end=summary.indexOf(' ',start);
  return end<0?summary.substring(start):summary.substring(start,end);
 }
 private static boolean isReflective(String id){
  return "reflect".equals(id)||"quiet_pause".equals(id)||"observe_lake".equals(id)||"explore_garden".equals(id)||
   "watch_reedling".equals(id)||"study_ecology".equals(id)||"compare_concept".equals(id)||"study_herb".equals(id);
 }
 private static boolean isBodySettling(String id){
  return "sleep".equals(id)||"seek_shelter".equals(id)||"recover".equals(id)||"eat".equals(id)||"drink".equals(id)||"toilet".equals(id)||
   "prepare_herb".equals(id)||"try_herb".equals(id);
 }
 private static Cue none(){return new Cue(Mode.NONE,"no recent reviewed outcome requires a visible bridge",0);}
}
