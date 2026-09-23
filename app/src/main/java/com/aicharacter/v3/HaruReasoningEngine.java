package com.aicharacter.v3;

import java.util.*;

/**
 * Evidence-based reasoning for Haru.
 *
 * Pipeline:
 * perception -> self-question -> competing hypothesis -> prediction ->
 * bounded observation experiment -> causal outcome memory -> comparison ->
 * belief revision -> transferable general rule.
 *
 * This engine never writes World Truth and never treats an external reference
 * as lived evidence. Confidence only moves after Haru has a causal memory.
 */
public final class HaruReasoningEngine {
 private static final long MIN_CYCLE_MS=30_000L;
 private static final long REOBSERVE_MS=5L*60L*1000L;
 private static final String RULE_REVISIT="rule:safe_revisit_reduces_uncertainty";
 private HaruReasoningEngine(){}

 public static void observe(WorldState s,long now){
  if(s==null||s.world==null)return;ensure(s);
  HaruReasoningState rs=s.characterGod.reasoning;
  if(rs.lastCycleAt>0&&now>=rs.lastCycleAt&&now-rs.lastCycleAt<MIN_CYCLE_MS)return;
  rs.lastCycleAt=Math.max(rs.lastCycleAt,now);rs.cycleCount++;
  refreshCausalExplanations(s,now);

  HaruVisionEngine.Snapshot view=HaruVisionEngine.observe(s);
  for(OpenQuestionState q:s.characterGod.openQuestions.values()){
   if(q==null||q.aboutObjectId==null||q.aboutObjectId.isEmpty()||"RESOLVED".equals(q.status)||"REJECTED".equals(q.status))continue;
   if(isSeen(view,q.aboutObjectId))ensureHypothesis(s,q,view,now);
  }

  for(HypothesisState h:rs.hypotheses.values()){
   if(h==null||h.subjectId.isEmpty()||h.expectedAreaId.isEmpty())continue;
   if(!h.expectedAreaId.equals(view.areaId))continue;
   if(view.visibility<.65||view.attention<.50)continue;
   if(h.lastEvidenceAt>0&&now>=h.lastEvidenceAt&&now-h.lastEvidenceAt<REOBSERVE_MS)continue;
   boolean seen=isSeen(view,h.subjectId);
   MemoryEntry m=CognitionEngine.experience(s,now,"reasoning_observation",
     seen
       ?"She returned to the place connected with "+h.subjectId+" and could observe it again."
       :"She returned under clear enough conditions but could not observe "+h.subjectId+" where she expected it.",
     seen?.10:-.06,.46,"reasoning","hypothesis",h.id,h.subjectId,seen?"support":"contradiction");
   applyHypothesisEvidence(s,h,seen,seen?.62:.72,m,now);
  }
 }

 public static HypothesisState ensureHypothesisForQuestion(WorldState s,String questionId,long now){
  if(s==null||s.characterGod==null)return null;OpenQuestionState q=s.characterGod.openQuestions.get(questionId);if(q==null)return null;
  HaruVisionEngine.Snapshot view=HaruVisionEngine.observe(s);return ensureHypothesis(s,q,view,now);
 }

 private static HypothesisState ensureHypothesis(WorldState s,OpenQuestionState q,HaruVisionEngine.Snapshot view,long now){
  ensure(s);String id="hyp_revisit_"+clean(q.aboutObjectId);HypothesisState existing=s.characterGod.reasoning.hypotheses.get(id);if(existing!=null)return existing;
  WorldObject o=s.world.object(q.aboutObjectId);if(o==null||!isSeen(view,o.id))return null;
  HypothesisState h=new HypothesisState();h.id=id;h.questionId=q.questionId;h.subjectId=o.id;h.expectedAreaId=view.areaId;
  h.proposition="Nếu mình quay lại "+view.areaId+" khi có thể quan sát rõ, mình có lẽ sẽ gặp lại "+label(o)+" và có thêm bằng chứng.";
  h.alternative="Lần thấy "+label(o)+" trước đó có thể chỉ là tình cờ, nên quay lại chưa chắc cho mình cùng loại bằng chứng.";
  h.createdAt=now;h.updatedAt=now;h.confidence=.5;s.characterGod.reasoning.hypotheses.put(h.id,h);
  MemoryEntry seed=CognitionEngine.experience(s,now,"self_question",
    "She compared two possibilities about "+o.id+": a repeatable local pattern versus an incidental sighting.",.04,.44,
    "reasoning","question",q.questionId,"hypothesis",h.id,o.id);
  h.evidenceMemoryIds.add(seed.memoryId);h.lastEvidenceAt=now;
  ThoughtState t=new ThoughtState("Maybe "+o.id+" belongs to a repeatable local pattern; maybe the sighting was only incidental.",
    "self_question:"+q.questionId,"affordance_inquiry",.50,.42,now);t.relatedMemories.add(seed.memoryId);s.thoughts.add(t);while(s.thoughts.size()>16)s.thoughts.remove(0);
  q.status="PARTIAL";q.lastRevisitedAt=now;q.revisits++;
  return h;
 }

 public static void attachReasoningToPlan(WorldState s,PlanState p,long now){
  if(s==null||p==null)return;ensure(s);
  if(p.questionId!=null&&!p.questionId.isEmpty()){
   HypothesisState h=ensureHypothesisForQuestion(s,p.questionId,now);
   if(h!=null)p.reasoningHypothesisId=h.id;
  }
  predictPlanOutcome(s,p,now);
  attachPlanningAdaptation(s,p,now);
  CausalExperimentEngine.attachIfTestable(s,p,now);
 }

 public static PredictionState predictPlanOutcome(WorldState s,PlanState p,long now){
  if(s==null||p==null||p.planId==null||p.planId.isEmpty())return null;ensure(s);
  if(p.predictionId!=null&&!p.predictionId.isEmpty()){
   PredictionState old=s.characterGod.reasoning.predictions.get(p.predictionId);if(old!=null)return old;
  }
  PredictionState x=new PredictionState();x.id="pred_"+clean(p.planId);x.planId=p.planId;x.hypothesisId=p.reasoningHypothesisId==null?"":p.reasoningHypothesisId;x.subjectId=p.destination==null?"":p.destination;
  double learned=AdaptiveBeliefEngine.planExpectation(s,p.intentionId);double base=.55+Math.max(-.22,Math.min(.22,learned*.22));
  HypothesisState h=x.hypothesisId.isEmpty()?null:s.characterGod.reasoning.hypotheses.get(x.hypothesisId);if(h!=null)base=(base+h.confidence)/2.0;
  base+=DopamineModulationEngine.predictionConfidenceBias(s);x.confidence=Math.max(.18,Math.min(.90,base));x.expectedOutcome=p.goal==null||p.goal.isEmpty()?"the planned action will produce useful evidence or satisfy its goal":p.goal;
  x.alternativeOutcome="the plan may be interrupted, fail, or produce evidence that changes the current expectation";x.createdAt=now;
  s.characterGod.reasoning.predictions.put(x.id,x);p.predictionId=x.id;return x;
 }

 public static void reviewPlanOutcome(WorldState s,PlanState p,MemoryEntry outcome,long now){
  if(s==null||p==null||outcome==null)return;ensure(s);
  PredictionState pred=p.predictionId==null?null:s.characterGod.reasoning.predictions.get(p.predictionId);
  boolean success="COMPLETED".equals(p.status);
  if(pred!=null&&"PENDING".equals(pred.status)){
   pred.status=success?"CONFIRMED":"DISCONFIRMED";pred.outcomeMemoryId=outcome.memoryId;pred.resolvedAt=now;
   if(!success&&!pred.subjectId.isEmpty()){
    String qid="q_prediction_"+clean(pred.id);if(!s.characterGod.openQuestions.containsKey(qid)){OpenQuestionState q=new OpenQuestionState();q.questionId=qid;q.topic="prediction:"+pred.planId;q.aboutObjectId=s.world!=null&&s.world.object(pred.subjectId)!=null?pred.subjectId:"";q.question="Vì sao kết quả thực tế khác với điều mình vừa dự đoán?";q.reason="a prediction did not match the causal outcome";q.createdAt=now;q.lastRevisitedAt=now;s.characterGod.openQuestions.put(qid,q);}
    inferCausalExplanations(s,p,pred,outcome,now);
    ThoughtState t=new ThoughtState("Mình đã dự đoán một kết quả nhưng trải nghiệm thật lại khác. Mình cần tìm nguyên nhân thay vì giữ nguyên giả định cũ.","prediction_error:"+pred.id,"reflect",.78,.58,now);t.relatedMemories.add(outcome.memoryId);s.thoughts.add(t);while(s.thoughts.size()>16)s.thoughts.remove(0);
   }
  }
  if(p.causalExperimentId==null||p.causalExperimentId.isEmpty())reviewPriorCausalExplanations(s,p,pred,outcome,success,now);
  CausalExperimentEngine.review(s,p,outcome,now);

  if("WORLD_AFFORDANCE".equals(p.origin)){
   HypothesisState h=p.reasoningHypothesisId==null?null:s.characterGod.reasoning.hypotheses.get(p.reasoningHypothesisId);
   if(success&&h!=null)applyHypothesisEvidence(s,h,true,.58,outcome,now);
   if(success||p.actionResolvedAt>0){GeneralRuleState rule=rule(s,RULE_REVISIT,"When a cautious revisit repeatedly produces useful observations, using a revisit as a low-risk inquiry strategy can transfer to a new unfamiliar subject.");
   rule.record(p.destination,success,now);}
  }
 }

 public static double inquiryStrategyBias(WorldState s){
  if(s==null||s.characterGod==null||s.characterGod.reasoning==null)return 0;
  GeneralRuleState r=s.characterGod.reasoning.rules.get(RULE_REVISIT);
  if(r==null||!"GENERALIZED".equals(r.status))return 0;
  return Math.max(0,Math.min(.16,(r.confidence-.65)*.34+.04));
 }

 /** Expected information gain: uncertainty creates a reason to test; resolved hypotheses stop consuming attention. */
 public static double informationGainBias(WorldState s,String subjectId){
  if(s==null||subjectId==null||s.characterGod==null||s.characterGod.reasoning==null)return 0;
  HypothesisState h=s.characterGod.reasoning.hypotheses.get("hyp_revisit_"+clean(subjectId));
  if(h==null)return 0;
  if("SUPPORTED".equals(h.status)||"DISFAVORED".equals(h.status))return -.10;
  return Math.max(0,Math.min(.22,h.informationNeed()*.22*DopamineModulationEngine.logicalControl(s)));
 }

 public static String currentReasoningSummary(WorldState s){
  if(s==null||s.characterGod==null||s.characterGod.reasoning==null)return"";
  HaruReasoningState r=s.characterGod.reasoning;HypothesisState best=null;for(HypothesisState h:r.hypotheses.values())if(h!=null&&(best==null||h.updatedAt>best.updatedAt))best=h;
  PredictionState miss=null;for(PredictionState p:r.predictions.values())if(p!=null&&"DISCONFIRMED".equals(p.status)&&(miss==null||p.resolvedAt>miss.resolvedAt))miss=p;
  if(miss!=null&&(best==null||miss.resolvedAt>=best.updatedAt)){CausalExplanationState cause=bestCause(r,miss.id);if(cause!=null&&"SUPPORTED".equals(cause.status))return "Mình vừa dự đoán rằng "+miss.expectedOutcome+", nhưng kết quả thực tế không khớp. Có bằng chứng trực tiếp khiến mình nghiêng về nguyên nhân: "+cause.claim+" Mình vẫn giữ nó là một giải thích có thể sửa, không phải sự thật tuyệt đối.";if(cause!=null)return "Mình vừa dự đoán rằng "+miss.expectedOutcome+", nhưng kết quả thực tế không khớp. Mình đang cân nhắc khả năng: "+cause.claim+" nhưng chưa có đủ bằng chứng để chốt.";return "Mình vừa dự đoán rằng "+miss.expectedOutcome+", nhưng kết quả thực tế không khớp. Mình chưa muốn bịa lý do; mình đang giữ câu hỏi đó mở để tìm thêm bằng chứng."; }
  if(best==null)return"";String certainty=best.confidence>=.72?"nghiêng về khả năng đầu":best.confidence<=.32?"nghiêng về khả năng thứ hai":"chưa đủ bằng chứng để chọn";
  return "Mình đang so hai khả năng: "+best.proposition+" Hoặc "+best.alternative+" Hiện mình "+certainty+", nên mình muốn kiểm tra bằng trải nghiệm thật.";
 }

 public static String diagnostic(WorldState s){
  if(s==null||s.characterGod==null||s.characterGod.reasoning==null)return"HARU REASONING unavailable";
  HaruReasoningState r=s.characterGod.reasoning;StringBuilder b=new StringBuilder("HARU REASONING\n");
  for(HypothesisState h:r.hypotheses.values())b.append("hyp ").append(h.id).append(" conf=").append(fmt(h.confidence)).append(" status=").append(h.status).append(" evidence=").append(h.evidenceCount).append(" revisions=").append(h.revisionCount).append('\n');
  for(PredictionState p:r.predictions.values())b.append("pred ").append(p.id).append(" conf=").append(fmt(p.confidence)).append(" status=").append(p.status).append('\n');
  for(CausalExplanationState x:r.causalExplanations.values())b.append("cause ").append(x.id).append(" type=").append(x.causeType).append(" conf=").append(fmt(x.confidence)).append(" status=").append(x.status).append('\n');
  for(CausalExperimentState x:r.causalExperiments.values())b.append("experiment ").append(x.id).append(" status=").append(x.status).append(" strategy=").append(x.strategy).append(" attempts=").append(x.attempts).append('\n');
  for(GeneralRuleState g:r.rules.values())b.append("rule ").append(g.id).append(" conf=").append(fmt(g.confidence)).append(" status=").append(g.status).append(" subjects=").append(g.subjects.size()).append('\n');
  return b.toString();
 }

 private static void applyHypothesisEvidence(WorldState s,HypothesisState h,boolean support,double weight,MemoryEntry m,long now){
  if(m==null||!hasMemory(s,m.memoryId))return;double control=DopamineModulationEngine.logicalControl(s);String priorStatus=h.status;
  double interpretedWeight=weight*(.62+.38*control);h.apply(support,interpretedWeight,m.memoryId,now);
  boolean hold=control<.62&&!isSettled(priorStatus)&&("SUPPORTED".equals(h.status)||"DISFAVORED".equals(h.status));if(hold)h.status="ACTIVE";
  OpenQuestionState q=s.characterGod.openQuestions.get(h.questionId);if(q!=null){q.lastRevisitedAt=now;q.revisits++;if(!hold&&control>=.62&&h.evidenceCount>=3&&(h.confidence>=.76||h.confidence<=.24)){q.status="RESOLVED";q.resolvedAt=now;}else q.status="PARTIAL";}
  if(hold)addMetacognitiveHoldThought(s,h,m,now);
  if("REVISED".equals(h.status)){
   ThoughtState t=new ThoughtState("My earlier expectation about "+h.subjectId+" no longer fits the newer evidence.",
     "contradiction:"+h.id,"compare_concept",1.0-h.informationNeed(),.58,now);t.relatedMemories.add(m.memoryId);s.thoughts.add(t);while(s.thoughts.size()>16)s.thoughts.remove(0);
  }
 }

 private static void inferCausalExplanations(WorldState s,PlanState p,PredictionState pred,MemoryEntry outcome,long now){
  if(s==null||p==null||pred==null||outcome==null)return;boolean grounded=false;long start=Math.max(0,pred.createdAt-1000L),end=Math.max(now,outcome.time)+1000L;
  if(s.worldHistory!=null)for(int i=Math.max(0,s.worldHistory.size()-48);i<s.worldHistory.size();i++){WorldHistoryEntry e=s.worldHistory.get(i);if(e==null||e.time<start||e.time>end||!p.planId.equals(e.entity))continue;String type=causeTypeForEvent(e);if(type.isEmpty())continue;CausalExplanationState x=ensureCause(s,p,pred,type,claimFor(type,e.summary),now);x.apply(true,causeEvidenceWeight(e),e.eventId,outcome.memoryId,now);grounded=true;}
  if(!grounded){String type=causeTypeFromText((p.lastOutcome==null?"":p.lastOutcome)+" "+outcome.summary);if(!type.isEmpty()){CausalExplanationState x=ensureCause(s,p,pred,type,claimFor(type,outcome.summary),now);x.apply(true,.35,"",outcome.memoryId,now);}}
  ensureCause(s,p,pred,"UNKNOWN_FACTOR","Một yếu tố khác mà mình chưa quan sát được có thể đã làm kế hoạch lệch khỏi dự đoán.",now);
  CausalExperimentEngine.designForPrediction(s,p,pred,now);
 }

 private static void refreshCausalExplanations(WorldState s,long now){
  if(s==null||s.characterGod==null||s.characterGod.reasoning==null||s.worldHistory==null)return;HaruReasoningState r=s.characterGod.reasoning;
  for(CausalExplanationState x:r.causalExplanations.values()){if(x==null||"UNKNOWN_FACTOR".equals(x.causeType)||"DISFAVORED".equals(x.status))continue;PredictionState p=r.predictions.get(x.predictionId);if(p==null||!"DISCONFIRMED".equals(p.status))continue;long start=Math.max(0,p.createdAt-1000L),end=Math.max(now,p.resolvedAt)+1000L;
   for(int i=Math.max(0,s.worldHistory.size()-64);i<s.worldHistory.size();i++){WorldHistoryEntry e=s.worldHistory.get(i);if(e==null||e.time<start||e.time>end||!x.planId.equals(e.entity)||!eventSupports(x.causeType,e))continue;x.apply(true,causeEvidenceWeight(e),e.eventId,p.outcomeMemoryId,now);}
  }
 }

 private static CausalExplanationState ensureCause(WorldState s,PlanState plan,PredictionState pred,String type,String claim,long now){
  String id="cause_"+clean(pred.id)+"_"+clean(type);CausalExplanationState x=s.characterGod.reasoning.causalExplanations.get(id);if(x==null){x=new CausalExplanationState();x.id=id;x.predictionId=pred.id;x.planId=pred.planId;x.intentionId=plan==null?"":plan.intentionId;x.subjectId=pred.subjectId;x.contextKey=routeContext(s,plan);x.causeType=type;x.claim=claim;x.testablePrediction=testablePrediction(type);x.createdAt=now;x.updatedAt=now;s.characterGod.reasoning.causalExplanations.put(id,x);}else if((x.claim==null||x.claim.isEmpty())&&claim!=null)x.claim=claim;return x;
 }
 private static void reviewPriorCausalExplanations(WorldState s,PlanState plan,PredictionState current,MemoryEntry outcome,boolean success,long now){
  if(s==null||plan==null||outcome==null||!success)return;HaruReasoningState r=s.characterGod.reasoning;
  for(CausalExplanationState x:r.causalExplanations.values()){if(x==null||"UNKNOWN_FACTOR".equals(x.causeType)||"DISFAVORED".equals(x.status))continue;if(current!=null&&current.id.equals(x.predictionId))continue;
   boolean sameIntention=!x.intentionId.isEmpty()&&x.intentionId.equals(plan.intentionId),sameSubject=!x.subjectId.isEmpty()&&x.subjectId.equals(plan.destination);if(!sameIntention&&!sameSubject)continue;
   long start=current==null?Math.max(0,plan.createdAt):Math.max(0,current.createdAt);if(hasCauseEvent(s,plan.planId,start,now,x.causeType))continue;
   double before=x.confidence;x.apply(true,.55,"counterfactual:"+plan.planId,outcome.memoryId,now);x.counterfactualChecks++;
   if(before<.68&&x.confidence>=.68){ThoughtState t=new ThoughtState("Một lần thử khác đã thành công khi dấu hiệu cản trở trước đó không xuất hiện. Điều này làm lời giải thích cũ đáng tin hơn, nhưng vẫn có thể sửa.","causal_counterfactual:"+x.id,"reflect",.68,.52,now);t.relatedMemories.add(outcome.memoryId);s.thoughts.add(t);while(s.thoughts.size()>16)s.thoughts.remove(0);}
  }
 }
 private static boolean hasCauseEvent(WorldState s,String planId,long start,long end,String causeType){if(s==null||s.worldHistory==null)return false;for(int i=Math.max(0,s.worldHistory.size()-64);i<s.worldHistory.size();i++){WorldHistoryEntry e=s.worldHistory.get(i);if(e!=null&&e.time>=start&&e.time<=end&&planId.equals(e.entity)&&eventSupports(causeType,e))return true;}return false;}
 private static double causeEvidenceWeight(WorldHistoryEntry e){if(e==null||e.type==null)return.25;if("TRAVEL_OBJECT_BLOCKED".equals(e.type)||"TRAVEL_TERRAIN_BLOCKED".equals(e.type))return 1.25;if("ROUTE_BLOCKED".equals(e.type))return.65;if("PLAN_INTEGRITY_FAILED".equals(e.type))return.55;if("PLAN_ACTION_RESOLVED".equals(e.type))return.35;return.25;}
 private static String testablePrediction(String type){if("ROUTE_CONSTRAINT".equals(type))return"Nếu trở ngại đường đi là nguyên nhân chính, một lần thử tương tự khi không còn dấu hiệu route bị chặn nên có cơ hội hoàn tất tốt hơn.";if("OBJECT_OBSTRUCTION".equals(type))return"Nếu vật cản là nguyên nhân chính, cùng mục tiêu sẽ dễ hoàn tất hơn khi đường vật lý thông thoáng.";if("TERRAIN_CONSTRAINT".equals(type))return"Nếu địa hình là nguyên nhân chính, kết quả sẽ khác khi chọn lối có độ cao vượt qua được.";if("TARGET_OR_ROUTE_CHANGED".equals(type)||"TARGET_UNAVAILABLE".equals(type))return"Nếu mục tiêu/đường đi thay đổi là nguyên nhân, kế hoạch tương tự chỉ nên thành công khi mục tiêu và đường đi thực sự khả dụng.";if("ACTION_CONSTRAINT".equals(type))return"Nếu chính điều kiện thực hiện hành động gây thất bại, cùng hành động trong điều kiện khả dụng hơn nên cho kết quả khác.";return"Mình chưa có dự đoán kiểm chứng cụ thể cho nguyên nhân này.";}

 public static double causalRoutePenalty(WorldState s,WorldConnection c){
  if(s==null||c==null||s.characterGod==null||s.characterGod.reasoning==null)return 0;double best=0;
  for(CausalExplanationState x:s.characterGod.reasoning.causalExplanations.values()){
   if(x==null||x.contextKey==null||!c.id.equals(x.contextKey)||"DISFAVORED".equals(x.status)||"UNKNOWN_FACTOR".equals(x.causeType))continue;
   if(!("ROUTE_CONSTRAINT".equals(x.causeType)||"OBJECT_OBSTRUCTION".equals(x.causeType)||"TERRAIN_CONSTRAINT".equals(x.causeType)||"TARGET_OR_ROUTE_CHANGED".equals(x.causeType)))continue;
   double p="SUPPORTED".equals(x.status)?.20+.55*Math.max(0,x.confidence-.55):.10+.30*Math.max(0,x.confidence-.50);best=Math.max(best,Math.min(.75,p));
  }
  return best;
 }

 private static void attachPlanningAdaptation(WorldState s,PlanState p,long now){
  if(s==null||p==null||s.characterGod==null||s.characterGod.reasoning==null)return;CausalExplanationState best=null;
  for(CausalExplanationState x:s.characterGod.reasoning.causalExplanations.values()){
   if(x==null||"DISFAVORED".equals(x.status)||"UNKNOWN_FACTOR".equals(x.causeType)||x.confidence<.54)continue;
   boolean sameSubject=!x.subjectId.isEmpty()&&x.subjectId.equals(p.destination),sameIntention=!x.intentionId.isEmpty()&&x.intentionId.equals(p.intentionId);if(!sameSubject&&!sameIntention)continue;
   if(best==null||x.confidence>best.confidence)best=x;
  }
  if(best==null)return;p.causalAdaptationId=best.id;p.adaptationPolicy=adaptationPolicy(best.causeType);
  String step="ADAPT_CAUSE:"+best.id+":"+p.adaptationPolicy;if(!p.steps.contains(step))p.steps.add(0,step);
  if("CAUTIOUS_RETRY".equals(p.adaptationPolicy))p.commitment=Math.max(.20,p.commitment-.08);
  ThoughtState t=new ThoughtState("Lần thử trước có một nguyên nhân khả dĩ. Mình sẽ thay đổi cách thử thay vì lặp lại y hệt.","planning_adaptation:"+best.id,p.intentionId,.56,.46,now);if(!best.evidenceMemoryIds.isEmpty())t.relatedMemories.add(best.evidenceMemoryIds.get(best.evidenceMemoryIds.size()-1));s.thoughts.add(t);while(s.thoughts.size()>16)s.thoughts.remove(0);
 }

 private static String adaptationPolicy(String type){if("ROUTE_CONSTRAINT".equals(type)||"OBJECT_OBSTRUCTION".equals(type)||"TERRAIN_CONSTRAINT".equals(type)||"TARGET_OR_ROUTE_CHANGED".equals(type))return"PREFER_ALTERNATE_ROUTE";if("TARGET_UNAVAILABLE".equals(type))return"VERIFY_TARGET_AVAILABLE";if("ACTION_CONSTRAINT".equals(type))return"CAUTIOUS_RETRY";return"RECHECK_CONDITIONS";}
 private static String routeContext(WorldState s,PlanState p){if(s==null||p==null||s.girlTravel==null)return"";TravelState t=s.girlTravel;if(!p.planId.equals(t.currentPlanId)||!"ROUTE".equals(t.travelMode)||t.routeIndex<0||t.routeIndex>=t.route.size()-1)return"";return t.route.get(t.routeIndex)+"->"+t.route.get(t.routeIndex+1);}

 private static CausalExplanationState bestCause(HaruReasoningState r,String predictionId){CausalExplanationState best=null;for(CausalExplanationState x:r.causalExplanations.values())if(x!=null&&predictionId.equals(x.predictionId)&&!"UNKNOWN_FACTOR".equals(x.causeType)&&(best==null||x.confidence>best.confidence))best=x;return best;}
 private static String causeTypeForEvent(WorldHistoryEntry e){if(e==null||e.type==null)return"";if("ROUTE_BLOCKED".equals(e.type))return"ROUTE_CONSTRAINT";if("TRAVEL_OBJECT_BLOCKED".equals(e.type))return"OBJECT_OBSTRUCTION";if("TRAVEL_TERRAIN_BLOCKED".equals(e.type))return"TERRAIN_CONSTRAINT";if("PLAN_INTEGRITY_FAILED".equals(e.type))return"TARGET_OR_ROUTE_CHANGED";if("PLAN_ACTION_RESOLVED".equals(e.type)&&e.summary!=null&&e.summary.startsWith("failure:"))return"ACTION_CONSTRAINT";return"";}
 private static boolean eventSupports(String causeType,WorldHistoryEntry e){return causeType!=null&&causeType.equals(causeTypeForEvent(e));}
 private static String causeTypeFromText(String text){String x=text==null?"":text.toLowerCase(Locale.ROOT);if(x.contains("route")||x.contains("destination unreachable")||x.contains("no valid path"))return"ROUTE_CONSTRAINT";if(x.contains("target")&&x.contains("exist"))return"TARGET_UNAVAILABLE";if(x.contains("terrain"))return"TERRAIN_CONSTRAINT";if(x.contains("blocked")||x.contains("collision"))return"OBJECT_OBSTRUCTION";if(x.contains("action")||x.contains("interaction")||x.contains("unavailable"))return"ACTION_CONSTRAINT";return"";}
 private static String claimFor(String type,String detail){String d=detail==null?"":detail.trim();if("ROUTE_CONSTRAINT".equals(type))return"đường đi hoặc kết nối tới mục tiêu đã bị chặn/thay đổi"+suffix(d);if("OBJECT_OBSTRUCTION".equals(type))return"một vật cản vật lý đã chặn đường thực tế"+suffix(d);if("TERRAIN_CONSTRAINT".equals(type))return"địa hình thực tế vượt quá khả năng đi qua ở thời điểm đó"+suffix(d);if("TARGET_OR_ROUTE_CHANGED".equals(type))return"mục tiêu hoặc đường tới nó không còn như lúc lập kế hoạch"+suffix(d);if("TARGET_UNAVAILABLE".equals(type))return"mục tiêu cần thiết không còn khả dụng"+suffix(d);if("ACTION_CONSTRAINT".equals(type))return"hành động dự kiến không thể hoàn tất trong điều kiện thực tế"+suffix(d);return"có một nguyên nhân chưa xác định"+suffix(d);}
 private static String suffix(String d){return d==null||d.isEmpty()?".":" ("+d+").";}

 private static boolean isSettled(String status){return "SUPPORTED".equals(status)||"DISFAVORED".equals(status);}
 private static void addMetacognitiveHoldThought(WorldState s,HypothesisState h,MemoryEntry m,long now){
  String trigger="metacognitive_hold:"+h.id;for(int i=Math.max(0,s.thoughts.size()-8);i<s.thoughts.size();i++){ThoughtState old=s.thoughts.get(i);if(old!=null&&trigger.equals(old.trigger))return;}
  ThoughtState t=new ThoughtState("Mình đang bị cuốn mạnh bởi cảm giác/phần thưởng lúc này. Bằng chứng vẫn đáng nhớ, nhưng mình chưa nên chốt kết luận cho tới khi đầu óc ổn định hơn.",trigger,"reflect",.76,.50,now);t.relatedMemories.add(m.memoryId);s.thoughts.add(t);while(s.thoughts.size()>16)s.thoughts.remove(0);
 }

 private static GeneralRuleState rule(WorldState s,String id,String statement){
  GeneralRuleState r=s.characterGod.reasoning.rules.get(id);if(r==null){r=new GeneralRuleState();r.id=id;r.statement=statement;s.characterGod.reasoning.rules.put(id,r);}return r;
 }
 private static boolean isSeen(HaruVisionEngine.Snapshot v,String id){if(v==null||id==null)return false;for(HaruVisionEngine.Seen x:v.seen)if(id.equals(x.id))return true;return false;}
 private static boolean hasMemory(WorldState s,String id){if(s==null||id==null)return false;for(MemoryEntry m:s.memories)if(m!=null&&id.equals(m.memoryId))return true;return false;}
 private static String label(WorldObject o){String x=o==null?"":o.haruDescription;return x==null||x.trim().isEmpty()?(o==null?"something":o.id):x.trim();}
 private static String clean(String s){return(s==null?"unknown":s).replaceAll("[^A-Za-z0-9._-]","_");}
 private static String fmt(double v){return String.format(Locale.US,"%.3f",v);}
 private static void ensure(WorldState s){if(s.characterGod==null)s.characterGod=new CharacterGodState();if(s.characterGod.reasoning==null)s.characterGod.reasoning=new HaruReasoningState();}
}
