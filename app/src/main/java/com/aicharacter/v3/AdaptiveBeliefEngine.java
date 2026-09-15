package com.aicharacter.v3;
/** Learns revisable beliefs from experienced plan outcomes. No network, no scripted life choices. */
public final class AdaptiveBeliefEngine {
 private AdaptiveBeliefEngine(){}
 public static void observePlanOutcome(WorldState s,PlanState p,boolean success,MemoryEntry source){
  if(s==null||p==null||p.intentionId==null||p.intentionId.isEmpty()||source==null)return;
  String subject="plan_outcome:"+p.intentionId;
  String observed=success?"usually_succeeds":"often_fails";
  double weight=Math.max(.45,Math.min(.9,.45+source.importance*.45));
  BeliefState b=s.beliefStates.get(subject);
  if(b==null){b=new BeliefState(subject,observed,.48);s.beliefStates.put(subject,b);}
  b.considerAlternative(observed,new BeliefEvidence(IdFactory.next("evidence"),source.memoryId,"experienced_outcome",observed.equals(b.value)?1:-1,weight,source.time,source.summary));
 }
 public static double planExpectation(WorldState s,String intentionId){
  if(s==null||intentionId==null)return 0;BeliefState b=s.beliefStates.get("plan_outcome:"+intentionId);if(b==null)return 0;
  double direction="usually_succeeds".equals(b.value)?1:"often_fails".equals(b.value)?-1:0;
  return direction*b.confidence;
 }
}
