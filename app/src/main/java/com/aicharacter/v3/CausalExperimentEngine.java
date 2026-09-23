package com.aicharacter.v3;

import java.util.*;

/**
 * Bounded active causal learning.
 *
 * An experiment is only RUNNING when Haru can verify that one relevant condition
 * differs from the failed attempt. Otherwise it remains DESIGNED/INCONCLUSIVE.
 * This engine updates Haru-owned causal explanations only; it never writes World Truth.
 */
public final class CausalExperimentEngine {
 private CausalExperimentEngine(){}

 public static CausalExperimentState designForPrediction(WorldState s,PlanState failedPlan,PredictionState pred,long now){
  if(s==null||failedPlan==null||pred==null||s.characterGod==null||s.characterGod.reasoning==null||!"DISCONFIRMED".equals(pred.status))return null;
  HaruReasoningState r=s.characterGod.reasoning;CausalExplanationState a=bestSpecificCause(r,pred.id),b=bestAlternative(r,pred.id,a);
  if(a==null)return null;String strategy=strategyFor(a);
  if(strategy.isEmpty())return null;
  String id="cexp_"+clean(pred.id)+"_"+clean(a.causeType);CausalExperimentState old=r.causalExperiments.get(id);if(old!=null)return old;
  CausalExperimentState x=new CausalExperimentState();x.id=id;x.sourcePredictionId=pred.id;x.causeAId=a.id;x.causeBId=b==null?"":b.id;x.intentionId=failedPlan.intentionId==null?"":failedPlan.intentionId;x.subjectId=pred.subjectId==null?"":pred.subjectId;x.strategy=strategy;x.manipulatedVariable=manipulatedVariable(strategy);x.expectedIfA=expectedIfA(a,strategy);x.expectedIfB=expectedIfB(b,strategy);x.baselineContextKey=a.contextKey==null?"":a.contextKey;x.createdAt=now;r.causalExperiments.put(id,x);
  ThoughtState t=new ThoughtState("Mình có hơn một cách giải thích cho lần thất bại. Nếu có thể thay đổi đúng một điều kiện, mình có thể kiểm tra xem nguyên nhân nào hợp lý hơn.","causal_experiment_designed:"+id,"reflect",.42,.52,now);if(!a.evidenceMemoryIds.isEmpty())t.relatedMemories.add(a.evidenceMemoryIds.get(a.evidenceMemoryIds.size()-1));s.thoughts.add(t);trimThoughts(s);
  return x;
 }

 public static boolean attachIfTestable(WorldState s,PlanState p,long now){
  if(s==null||p==null||s.world==null||s.characterGod==null||s.characterGod.reasoning==null)return false;HaruReasoningState r=s.characterGod.reasoning;CausalExperimentState best=null;
  for(CausalExperimentState x:r.causalExperiments.values()){
   if(x==null||!("DESIGNED".equals(x.status)||"INCONCLUSIVE".equals(x.status))||x.attempts>=3)continue;
   boolean sameSubject=!x.subjectId.isEmpty()&&x.subjectId.equals(p.destination),sameIntention=!x.intentionId.isEmpty()&&x.intentionId.equals(p.intentionId);if(!sameSubject&&!sameIntention)continue;
   if(best==null||x.createdAt>best.createdAt)best=x;
  }
  if(best==null)return false;CausalExplanationState a=r.causalExplanations.get(best.causeAId);if(a==null)return false;
  String context="";boolean verified=false;
  if("ALTERNATE_ROUTE_RETRY".equals(best.strategy)){
   List<String> route=prospectiveRoute(s,p);if(route.size()<1)return false;context=routeContext(route);verified=!best.baselineContextKey.isEmpty()&&!containsEdge(route,best.baselineContextKey);
  }else if("VERIFY_TARGET_RETRY".equals(best.strategy)){
   WorldObject o=s.world.object(p.destination);verified=o!=null&&o.enabled;context=verified?"target_available:"+o.id:"";
  }
  if(!verified)return false;
  best.status="RUNNING";best.activePlanId=p.planId;best.startedAt=now;best.attempts++;best.manipulationVerified=true;best.testContextKey=context;p.causalExperimentId=best.id;p.experimentStrategy=best.strategy;
  String step="CAUSAL_EXPERIMENT:"+best.id+":"+best.strategy;if(!p.steps.contains(step))p.steps.add(0,step);
  ThoughtState t=new ThoughtState("Mình đang thử lại nhưng đã thay đổi một điều kiện có liên quan, để xem lời giải thích nào còn đứng vững.","causal_experiment_started:"+best.id,p.intentionId,.38,.48,now);s.thoughts.add(t);trimThoughts(s);
  WorldEventBus.publishId(s,now,"causal_experiment_start_"+clean(best.id)+"_"+clean(p.planId),"HARU_CAUSAL_EXPERIMENT_STARTED",p.planId,"experiment="+best.id+" strategy="+best.strategy+" baseline="+best.baselineContextKey+" test="+best.testContextKey);
  return true;
 }

 public static void review(WorldState s,PlanState p,MemoryEntry outcome,long now){
  if(s==null||p==null||outcome==null||p.causalExperimentId==null||p.causalExperimentId.isEmpty()||s.characterGod==null||s.characterGod.reasoning==null)return;HaruReasoningState r=s.characterGod.reasoning;CausalExperimentState x=r.causalExperiments.get(p.causalExperimentId);if(x==null||!"RUNNING".equals(x.status)||!p.planId.equals(x.activePlanId))return;
  CausalExplanationState a=r.causalExplanations.get(x.causeAId),b=r.causalExplanations.get(x.causeBId);if(a==null||!x.manipulationVerified){inconclusive(s,x,outcome,now,"planned manipulation was not verified");return;}
  boolean success="COMPLETED".equals(p.status),aEvent=hasCauseEvent(s,p.planId,Math.max(0,p.createdAt),now,a.causeType);String evidenceId="experiment:"+x.id+":"+p.planId;
  if("ALTERNATE_ROUTE_RETRY".equals(x.strategy)){
   if(success&&!aEvent){
    a.apply(true,.68,evidenceId,outcome.memoryId,now);if(b!=null&&!"UNKNOWN_FACTOR".equals(b.causeType))b.apply(false,.28,evidenceId+":alt",outcome.memoryId,now);
    resolve(s,x,a.id,outcome,now,"The retry succeeded after avoiding the earlier route context; this supports the route-related explanation over its alternative.");
   }else if(!success&&!aEvent){
    a.apply(false,.62,evidenceId,outcome.memoryId,now);if(b!=null&&!"UNKNOWN_FACTOR".equals(b.causeType)){b.apply(true,.34,evidenceId+":alt",outcome.memoryId,now);resolve(s,x,b.id,outcome,now,"The failure repeated after the earlier route context was removed, weakening the route explanation relative to the alternative.");}
    else inconclusive(s,x,outcome,now,"The failure repeated despite avoiding the earlier route context; the known route explanation weakened but the alternative is still unspecified.");
   }else inconclusive(s,x,outcome,now,"The supposedly changed trial still encountered the same causal event, so it was not a clean comparison.");
  }else if("VERIFY_TARGET_RETRY".equals(x.strategy)){
   if(success){a.apply(true,.55,evidenceId,outcome.memoryId,now);resolve(s,x,a.id,outcome,now,"The target was verified available and the retry succeeded, supporting prior target unavailability as a transient cause.");}
   else{a.apply(false,.55,evidenceId,outcome.memoryId,now);if(b!=null&&!"UNKNOWN_FACTOR".equals(b.causeType)){b.apply(true,.30,evidenceId+":alt",outcome.memoryId,now);resolve(s,x,b.id,outcome,now,"The target was available but the plan still failed, weakening target unavailability relative to the alternative.");}else inconclusive(s,x,outcome,now,"The target was available but failure persisted, so another cause remains open.");}
  }
 }

 public static String diagnostic(WorldState s){
  if(s==null||s.characterGod==null||s.characterGod.reasoning==null)return"CAUSAL EXPERIMENT unavailable";StringBuilder b=new StringBuilder("CAUSAL EXPERIMENTS\n");
  for(CausalExperimentState x:s.characterGod.reasoning.causalExperiments.values())b.append(x.id).append(" ").append(x.status).append(" strategy=").append(x.strategy).append(" attempts=").append(x.attempts).append(" favored=").append(x.favoredCauseId).append("\n");return b.toString();
 }

 private static void resolve(WorldState s,CausalExperimentState x,String favored,MemoryEntry outcome,long now,String summary){x.status="RESOLVED";x.favoredCauseId=favored==null?"":favored;x.outcomeMemoryId=outcome.memoryId;x.resultSummary=summary;x.resolvedAt=now;ThoughtState t=new ThoughtState("Thử nghiệm nhỏ vừa cho mình thêm bằng chứng để phân biệt hai cách giải thích.","causal_experiment_resolved:"+x.id,"reflect",.32,.58,now);t.relatedMemories.add(outcome.memoryId);s.thoughts.add(t);trimThoughts(s);WorldEventBus.publishId(s,now,"causal_experiment_result_"+clean(x.id),"HARU_CAUSAL_EXPERIMENT_RESOLVED",x.id,summary);}
 private static void inconclusive(WorldState s,CausalExperimentState x,MemoryEntry outcome,long now,String summary){x.status=x.attempts>=3?"ABANDONED":"INCONCLUSIVE";x.favoredCauseId="";x.outcomeMemoryId=outcome.memoryId;x.resultSummary=summary;x.resolvedAt=now;WorldEventBus.publishId(s,now,"causal_experiment_inconclusive_"+clean(x.id)+"_"+x.attempts,"HARU_CAUSAL_EXPERIMENT_INCONCLUSIVE",x.id,summary);}

 private static CausalExplanationState bestSpecificCause(HaruReasoningState r,String predId){CausalExplanationState best=null;for(CausalExplanationState x:r.causalExplanations.values())if(x!=null&&predId.equals(x.predictionId)&&!"UNKNOWN_FACTOR".equals(x.causeType)&&!"DISFAVORED".equals(x.status)&&(best==null||x.confidence>best.confidence))best=x;return best;}
 private static CausalExplanationState bestAlternative(HaruReasoningState r,String predId,CausalExplanationState a){CausalExplanationState best=null;for(CausalExplanationState x:r.causalExplanations.values())if(x!=null&&x!=a&&predId.equals(x.predictionId)&&!"DISFAVORED".equals(x.status)&&(best==null||("UNKNOWN_FACTOR".equals(best.causeType)&&!"UNKNOWN_FACTOR".equals(x.causeType))||x.confidence>best.confidence))best=x;return best;}
 private static String strategyFor(CausalExplanationState a){if(a==null)return"";if(("ROUTE_CONSTRAINT".equals(a.causeType)||"OBJECT_OBSTRUCTION".equals(a.causeType)||"TERRAIN_CONSTRAINT".equals(a.causeType)||"TARGET_OR_ROUTE_CHANGED".equals(a.causeType))&&a.contextKey!=null&&!a.contextKey.isEmpty())return"ALTERNATE_ROUTE_RETRY";if("TARGET_UNAVAILABLE".equals(a.causeType)||"TARGET_OR_ROUTE_CHANGED".equals(a.causeType))return"VERIFY_TARGET_RETRY";return"";}
 private static String manipulatedVariable(String strategy){if("ALTERNATE_ROUTE_RETRY".equals(strategy))return"route_context";if("VERIFY_TARGET_RETRY".equals(strategy))return"target_availability";return"";}
 private static String expectedIfA(CausalExplanationState a,String strategy){if("ALTERNATE_ROUTE_RETRY".equals(strategy))return"Nếu nguyên nhân A đúng, tránh đúng route context cũ sẽ làm khả năng hoàn tất tăng lên.";if("VERIFY_TARGET_RETRY".equals(strategy))return"Nếu nguyên nhân A đúng, khi mục tiêu thực sự khả dụng thì kết quả nên khác lần thất bại.";return a==null?"":a.testablePrediction;}
 private static String expectedIfB(CausalExplanationState b,String strategy){if(b==null||"UNKNOWN_FACTOR".equals(b.causeType))return"Nếu kết quả vẫn thất bại sau khi đã đổi điều kiện A, một nguyên nhân khác vẫn còn mở.";return"Nếu nguyên nhân B đúng hơn, thay đổi riêng điều kiện của A sẽ không đủ để khôi phục kết quả.";}
 private static List<String> prospectiveRoute(WorldState s,PlanState p){ArrayList<String> none=new ArrayList<>();if(s==null||s.world==null||p==null)return none;WorldArea start=s.world.areaAt(s.haruX);if(start==null)return none;WorldObject o=s.world.object(p.destination);String dest=o==null?p.destination:o.areaId;if(s.world.area(dest)==null)return none;return WorldPathPlanner.route(s,"girl",start.id,dest);}
 private static String routeContext(List<String> r){StringBuilder b=new StringBuilder();for(int i=0;i+1<r.size();i++){if(b.length()>0)b.append("|");b.append(r.get(i)).append("->").append(r.get(i+1));}return b.toString();}
 private static boolean containsEdge(List<String> r,String edge){if(edge==null||edge.isEmpty())return false;for(int i=0;i+1<r.size();i++)if(edge.equals(r.get(i)+"->"+r.get(i+1)))return true;return false;}
 private static boolean hasCauseEvent(WorldState s,String planId,long start,long end,String causeType){if(s==null||s.worldHistory==null)return false;for(WorldHistoryEntry e:s.worldHistory)if(e!=null&&e.time>=start&&e.time<=end&&planId.equals(e.entity)&&causeType.equals(causeTypeForEvent(e)))return true;return false;}
 private static String causeTypeForEvent(WorldHistoryEntry e){if(e==null||e.type==null)return"";if("ROUTE_BLOCKED".equals(e.type))return"ROUTE_CONSTRAINT";if("TRAVEL_OBJECT_BLOCKED".equals(e.type))return"OBJECT_OBSTRUCTION";if("TRAVEL_TERRAIN_BLOCKED".equals(e.type))return"TERRAIN_CONSTRAINT";if("PLAN_INTEGRITY_FAILED".equals(e.type))return"TARGET_OR_ROUTE_CHANGED";if("PLAN_ACTION_RESOLVED".equals(e.type)&&e.summary!=null&&e.summary.startsWith("failure:"))return"ACTION_CONSTRAINT";return"";}
 private static void trimThoughts(WorldState s){while(s.thoughts.size()>16)s.thoughts.remove(0);}
 private static String clean(String s){return(s==null?"unknown":s).replaceAll("[^A-Za-z0-9._-]","_");}
}
