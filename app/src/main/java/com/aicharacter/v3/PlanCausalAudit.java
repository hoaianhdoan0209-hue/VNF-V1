package com.aicharacter.v3;

/**
 * V3 causal ledger for one plan lifecycle.
 * Arrival -> physical action -> learned outcome -> post-outcome review.
 */
public final class PlanCausalAudit {
 private PlanCausalAudit(){}

 public static void arrival(WorldState s,PlanState p,long now,String detail){
  if(s==null||p==null)return;
  if(p.arrivedAt<=0)p.arrivedAt=now;
  WorldEventBus.publishId(s,now,id(p,"arrival"),"PLAN_ARRIVAL_CONFIRMED",p.planId,detail==null?"arrival confirmed":detail);
 }

 public static void actionResolved(WorldState s,PlanState p,long now,boolean ok,String outcome){
  if(s==null||p==null)return;
  p.actionResolvedAt=now;
  WorldEventBus.publishId(s,now,id(p,"action"),"PLAN_ACTION_RESOLVED",p.planId,(ok?"success: ":"failure: ")+(outcome==null?"":outcome));
 }

 public static void outcomeLearned(WorldState s,PlanState p,MemoryEntry memory,long now){
  if(s==null||p==null||memory==null)return;
  p.outcomeMemoryId=memory.memoryId;
  p.outcomeLearnedAt=Math.max(now,memory.time);
  WorldEventBus.publishId(s,p.outcomeLearnedAt,id(p,"learned"),"PLAN_OUTCOME_LEARNED",p.planId,
   "memory="+memory.memoryId+" kind="+memory.kind);
 }

 public static void reviewed(WorldState s,PlanState p,long now,String detail){
  if(s==null||p==null)return;
  p.postOutcomeReviewedAt=now;
  WorldEventBus.publishId(s,now,id(p,"review"),"PLAN_POST_OUTCOME_REVIEWED",p.planId,detail==null?"outcome reviewed":detail);
 }

 public static boolean valid(WorldState s,PlanState p){
  if(p==null)return true;
  if(p.actionResolvedAt>0&&p.arrivedAt<=0)return false;
  if(p.outcomeLearnedAt>0&&p.actionResolvedAt<=0)return false;
  if(p.postOutcomeReviewedAt>0&&p.outcomeLearnedAt<=0)return false;
  if(p.arrivedAt>0&&p.actionResolvedAt>0&&p.actionResolvedAt<p.arrivedAt)return false;
  if(p.actionResolvedAt>0&&p.outcomeLearnedAt>0&&p.outcomeLearnedAt<p.actionResolvedAt)return false;
  if(p.outcomeLearnedAt>0&&p.postOutcomeReviewedAt>0&&p.postOutcomeReviewedAt<p.outcomeLearnedAt)return false;
  if(p.outcomeLearnedAt>0&&!p.outcomeMemoryId.isEmpty()&&findMemory(s,p.outcomeMemoryId)==null)return false;
  return true;
 }

 public static boolean reportIfInvalid(WorldState s,long now){
  PlanState p=s==null?null:s.planState;
  if(p==null||valid(s,p))return false;
  String marker="plan="+p.planId;
  if(s.developerReports!=null){
   for(int i=s.developerReports.size()-1;i>=0&&i>=s.developerReports.size()-24;i--){
    DeveloperReport r=s.developerReports.get(i);
    if(r!=null&&"PLAN_CAUSAL_AUDIT".equals(r.category)&&r.context!=null&&r.context.contains(marker))return true;
   }
   s.developerReports.add(new DeveloperReport(
    now,"PLAN_CAUSAL_AUDIT","ERROR","PlanCausalAudit",
    "Plan lifecycle phases are missing or out of causal order.",
    "arrival/action/learning/review ordering violation",
    marker+" arrival="+p.arrivedAt+" action="+p.actionResolvedAt+" learned="+p.outcomeLearnedAt+" review="+p.postOutcomeReviewedAt+" memory="+p.outcomeMemoryId,
    "Do not auto-repair cognition. Preserve the save and inspect the lifecycle integration."));
   while(s.developerReports.size()>80)s.developerReports.remove(0);
  }
  return true;
 }

 public static String diagnostic(WorldState s){
  PlanState p=s==null?null:s.planState;
  if(p==null)return "PLAN CAUSAL AUDIT V3 <no plan>";
  return "PLAN CAUSAL AUDIT V3\n"+
   "plan="+p.planId+" intention="+p.intentionId+" status="+p.status+"\n"+
   "arrival="+p.arrivedAt+" -> action="+p.actionResolvedAt+" -> learned="+p.outcomeLearnedAt+" -> review="+p.postOutcomeReviewedAt+"\n"+
   "memory="+(p.outcomeMemoryId.isEmpty()?"<none>":p.outcomeMemoryId)+" valid="+valid(s,p)+"\n"+
   "contract=arrival precedes action; action precedes learning; learned evidence precedes next-life review";
 }

 public static MemoryEntry findMemory(WorldState s,String id){
  if(s==null||id==null||id.isEmpty()||s.memories==null)return null;
  for(int i=s.memories.size()-1;i>=0;i--){MemoryEntry m=s.memories.get(i);if(m!=null&&id.equals(m.memoryId))return m;}
  return null;
 }

 private static String id(PlanState p,String phase){return "plan_causal_"+safe(p==null?"":p.planId)+"_"+phase;}
 private static String safe(String x){return x==null?"unknown":x.replaceAll("[^A-Za-z0-9._-]","_");}
}
