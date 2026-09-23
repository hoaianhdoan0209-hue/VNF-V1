package com.aicharacter.v3;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public final class HaruReasoningEngineTest {

 @Test public void selfQuestionCreatesCompetingHypothesisAndPrediction(){
  WorldState s=state();HaruAffordanceEngine.observeQuestions(s,1000L);
  assertTrue(s.characterGod.openQuestions.containsKey("q_world_mystery_flora"));
  HaruReasoningEngine.observe(s,1000L);
  HypothesisState h=s.characterGod.reasoning.hypotheses.get("hyp_revisit_mystery_flora");
  assertNotNull(h);assertFalse(h.proposition.isEmpty());assertFalse(h.alternative.isEmpty());assertNotEquals(h.proposition,h.alternative);assertTrue(HaruReasoningEngine.informationGainBias(s,"mystery_flora")>0);
  assertFalse(h.evidenceMemoryIds.isEmpty());
  assertTrue(HaruAffordanceEngine.beginPlanIfCompelling(s,1100L));
  assertEquals(h.id,s.planState.reasoningHypothesisId);assertFalse(s.planState.predictionId.isEmpty());
  PredictionState p=s.characterGod.reasoning.predictions.get(s.planState.predictionId);assertNotNull(p);assertEquals("PENDING",p.status);
  assertTrue(s.planState.steps.stream().anyMatch(x->x.startsWith("COMPARE_EVIDENCE:")));
 }

 @Test public void causalOutcomesConfirmPredictionAndGeneralizeSafeInquiry(){
  WorldState s=state();HaruAffordanceEngine.observeQuestions(s,1000L);HaruReasoningEngine.observe(s,1000L);
  HypothesisState h=s.characterGod.reasoning.hypotheses.get("hyp_revisit_mystery_flora");

  PlanState first=reasoningPlan(s,"p1","mystery_flora",h.id,1100L);
  MemoryEntry m1=CognitionEngine.experience(s,1200L,"planned_action_outcome","first observation succeeded",.1,.5,"reasoning");
  first.status="COMPLETED";HaruReasoningEngine.reviewPlanOutcome(s,first,m1,1200L);
  assertEquals("CONFIRMED",s.characterGod.reasoning.predictions.get(first.predictionId).status);
  assertTrue(h.supportWeight>0);

  PlanState second=reasoningPlan(s,"p2","new_subject_a","",1300L);second.status="COMPLETED";
  MemoryEntry m2=CognitionEngine.experience(s,1400L,"planned_action_outcome","second observation succeeded",.1,.5,"reasoning");
  HaruReasoningEngine.reviewPlanOutcome(s,second,m2,1400L);
  PlanState third=reasoningPlan(s,"p3","new_subject_b","",1500L);third.status="COMPLETED";
  MemoryEntry m3=CognitionEngine.experience(s,1600L,"planned_action_outcome","third observation succeeded",.1,.5,"reasoning");
  HaruReasoningEngine.reviewPlanOutcome(s,third,m3,1600L);

  GeneralRuleState rule=s.characterGod.reasoning.rules.get("rule:safe_revisit_reduces_uncertainty");
  assertNotNull(rule);assertEquals("GENERALIZED",rule.status);assertTrue(rule.subjects.size()>=2);assertTrue(HaruReasoningEngine.inquiryStrategyBias(s)>0);
 }

 @Test public void contradictoryLivedEvidenceMakesHaruReviseEarlierExpectation(){
  WorldState s=state();HaruAffordanceEngine.observeQuestions(s,1000L);HaruReasoningEngine.observe(s,1000L);
  HypothesisState h=s.characterGod.reasoning.hypotheses.get("hyp_revisit_mystery_flora");
  for(int i=0;i<3;i++){
   PlanState p=reasoningPlan(s,"support"+i,"mystery_flora",h.id,2000L+i*100);
   p.status="COMPLETED";MemoryEntry m=CognitionEngine.experience(s,2100L+i*100,"planned_action_outcome","repeat observation "+i,.1,.5,"reasoning");
   HaruReasoningEngine.reviewPlanOutcome(s,p,m,2100L+i*100);
  }
  assertEquals("SUPPORTED",h.status);assertTrue(HaruReasoningEngine.informationGainBias(s,"mystery_flora")<0);double before=h.confidence;
  s.world.object("mystery_flora").enabled=false;
  long later=h.lastEvidenceAt+6L*60L*1000L;HaruReasoningEngine.observe(s,later);
  assertTrue(h.confidence<before);assertEquals("REVISED",h.status);assertTrue(h.revisionCount>=1);
  assertTrue(s.memories.stream().anyMatch(m->"reasoning_observation".equals(m.kind)&&m.summary.contains("could not observe")));
 }

 @Test public void predictionErrorCreatesANewQuestionInsteadOfBeingIgnored(){
  WorldState s=state();PlanState p=new PlanState();p.planId="failed_plan";p.intentionId="observe_lake";p.goal="observe the target";p.destination="mystery_flora";p.origin="LEGACY";
  HaruReasoningEngine.predictPlanOutcome(s,p,1000L);p.status="FAILED";
  MemoryEntry m=CognitionEngine.experience(s,1200L,"planned_action_outcome","route was blocked",-.1,.5,"failure");
  HaruReasoningEngine.reviewPlanOutcome(s,p,m,1200L);
  assertEquals("DISCONFIRMED",s.characterGod.reasoning.predictions.get(p.predictionId).status);
  assertTrue(s.characterGod.openQuestions.keySet().stream().anyMatch(x->x.startsWith("q_prediction_")));
  assertTrue(s.thoughts.stream().anyMatch(t->t.trigger.startsWith("prediction_error:")));
  assertTrue(HaruReasoningEngine.currentReasoningSummary(s).contains("kết quả thực tế không khớp"));
 }

 @Test public void preActionFailureStillClosesPredictionLoopWithoutInventingKnowledge(){
  WorldState s=state();PlanState p=new PlanState();p.planId="blocked_before_action";p.intentionId="affordance_inquiry";p.goal="observe an unfamiliar target";p.destination="missing_target";p.plannedAction="OBSERVE";p.origin="WORLD_AFFORDANCE";p.status="PAUSED";p.createdAt=900L;p.pausedAt=1000L;
  HaruReasoningEngine.predictPlanOutcome(s,p,950L);s.planState=p;
  assertFalse(PlanExecutor.resume(s,2000L));
  assertEquals("FAILED",p.status);assertFalse(p.outcomeMemoryId.isEmpty());assertTrue(p.outcomeNeedsReview());assertTrue(PlanCausalAudit.valid(s,p));
  assertTrue(PlanOutcomeReviewEngine.reviewIfReady(s,2001L));
  PredictionState pred=s.characterGod.reasoning.predictions.get(p.predictionId);assertNotNull(pred);assertEquals("DISCONFIRMED",pred.status);
  assertTrue(s.characterGod.openQuestions.keySet().stream().anyMatch(x->x.startsWith("q_prediction_")));
  assertFalse(s.characterGod.conceptKnowledge.containsKey("world:missing_target"));
  assertTrue(PlanCausalAudit.valid(s,p));
 }

 @Test public void predictionErrorBuildsGroundedCompetingCauseExplanations() throws Exception{
  WorldState s=state();PlanState p=reasoningPlan(s,"route_cause","mystery_flora","",1000L);p.origin="WORLD_AFFORDANCE";s.planState=p;
  WorldEventBus.publishId(s,1100L,"evt_route_block","ROUTE_BLOCKED",p.planId,"current connection invalid/topology changed");
  p.status="FAILED";p.lastOutcome="route invalid; no alternate path";
  MemoryEntry m=CognitionEngine.experience(s,1200L,"travel_failed","route invalid; no alternate path",-.12,.55,"failure","plan_terminal");
  HaruReasoningEngine.reviewPlanOutcome(s,p,m,1200L);
  PredictionState pred=s.characterGod.reasoning.predictions.get(p.predictionId);assertEquals("DISCONFIRMED",pred.status);
  CausalExplanationState route=s.characterGod.reasoning.causalExplanations.get("cause_"+pred.id+"_ROUTE_CONSTRAINT");
  CausalExplanationState unknown=s.characterGod.reasoning.causalExplanations.get("cause_"+pred.id+"_UNKNOWN_FACTOR");
  assertNotNull(route);assertNotNull(unknown);assertEquals("TENTATIVE",route.status);assertTrue(route.confidence>unknown.confidence);assertFalse(route.testablePrediction.isEmpty());
  assertTrue(route.evidenceEventIds.stream().anyMatch(x->x.startsWith("evt_route_block|")));
  assertTrue(HaruReasoningEngine.currentReasoningSummary(s).contains("đường đi"));
  assertFalse(s.characterGod.reasoning.rules.containsKey("rule:safe_revisit_reduces_uncertainty"));
  PlanState retry=reasoningPlan(s,"route_retry","mystery_flora","",1300L);retry.origin="WORLD_AFFORDANCE";retry.status="COMPLETED";retry.actionResolvedAt=1400L;
  MemoryEntry m2=CognitionEngine.experience(s,1400L,"planned_action_outcome","retry reached the target without a route block",.12,.50,"success");
  HaruReasoningEngine.reviewPlanOutcome(s,retry,m2,1400L);
  assertEquals("SUPPORTED",route.status);assertTrue(route.counterfactualChecks>=1);assertTrue(route.evidenceMemoryIds.contains(m2.memoryId));
  WorldState x=WorldState.fromJson(s.toJson());assertTrue(x.characterGod.reasoning.causalExplanations.containsKey(route.id));assertEquals(route.confidence,x.characterGod.reasoning.causalExplanations.get(route.id).confidence,.000001);assertEquals(route.testablePrediction,x.characterGod.reasoning.causalExplanations.get(route.id).testablePrediction);
 }

 @Test public void reasoningSurvivesSaveRoundTrip() throws Exception{
  WorldState s=state();HaruAffordanceEngine.observeQuestions(s,1000L);HaruReasoningEngine.observe(s,1000L);
  HypothesisState h=s.characterGod.reasoning.hypotheses.get("hyp_revisit_mystery_flora");
  PlanState p=reasoningPlan(s,"save_plan","mystery_flora",h.id,1100L);p.status="COMPLETED";
  MemoryEntry m=CognitionEngine.experience(s,1200L,"planned_action_outcome","observed again",.1,.5,"reasoning");HaruReasoningEngine.reviewPlanOutcome(s,p,m,1200L);
  WorldState x=WorldState.fromJson(s.toJson());
  assertNotNull(x.characterGod.reasoning);assertTrue(x.characterGod.reasoning.hypotheses.containsKey(h.id));assertTrue(x.characterGod.reasoning.predictions.containsKey(p.predictionId));
  assertEquals(h.confidence,x.characterGod.reasoning.hypotheses.get(h.id).confidence,.000001);
 }

 @Test public void activeAndOfflineSlicesUseSameReasoningBoundary(){
  WorldState active=state(),offline=state();HaruAffordanceEngine.observeQuestions(active,1000L);HaruAffordanceEngine.observeQuestions(offline,1000L);
  LifeSimulationKernel.beginSlice(active,60,2000L,LifeSimulationKernel.Mode.ACTIVE);
  LifeSimulationKernel.beginSlice(offline,60,2000L,LifeSimulationKernel.Mode.OFFLINE);
  assertEquals(active.characterGod.reasoning.hypotheses.keySet(),offline.characterGod.reasoning.hypotheses.keySet());
  assertEquals(active.characterGod.openQuestions.keySet(),offline.characterGod.openQuestions.keySet());
  HypothesisState a=active.characterGod.reasoning.hypotheses.get("hyp_revisit_mystery_flora"),b=offline.characterGod.reasoning.hypotheses.get("hyp_revisit_mystery_flora");
  assertNotNull(a);assertNotNull(b);assertEquals(a.confidence,b.confidence,.000001);assertEquals(a.evidenceCount,b.evidenceCount);
 }

 private static PlanState reasoningPlan(WorldState s,String id,String subject,String hypothesis,long now){
  PlanState p=new PlanState();p.planId=id;p.intentionId="affordance_inquiry";p.goal="learn from observation";p.destination=subject;p.origin="WORLD_AFFORDANCE";p.reasoningHypothesisId=hypothesis;p.status="ACTIVE";p.createdAt=now;
  HaruReasoningEngine.predictPlanOutcome(s,p,now);return p;
 }

 private static WorldState state(){
  WorldState s=new WorldState();s.createdAt=100L;s.lastSavedAt=100L;s.lastOpenedAt=100L;s.lastSimulatedAt=100L;s.worldMinutes=8*60;s.haruX=10;s.catX=5;s.age=15;s.brainGrowth=5.0;s.haruMood="calm";s.haruActivity="idle";s.currentIntention="observe_lake";
  s.relationship=RelationshipState.fresh(new Random(7));s.body.energy=95;s.body.sleepiness=5;s.body.pain=0;s.body.health=100;s.emotion.calm=.9;s.personality.curiosity=.92;s.personality.patience=.9;s.catState=CatState.fromJson(null,s.catX,100L);
  s.world=new WorldModel();s.world.areas.add(new WorldArea("test_area","Test","test place",0,100,0,true,"test"));
  WorldObject o=new WorldObject("mystery_flora","flora","test_area","","rêu bạc",14,0,10,10,"flora living resource");o.enabled=true;o.interactable=true;s.world.objects.add(o);
  return s;
 }
}
