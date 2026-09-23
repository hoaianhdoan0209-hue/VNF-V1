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
  s.girlTravel.currentPlanId=p.planId;s.girlTravel.travelMode="ROUTE";s.girlTravel.routeIndex=0;s.girlTravel.route.add("test_area");s.girlTravel.route.add("other_area");
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
  assertEquals("SUPPORTED",route.status);assertTrue(route.counterfactualChecks>=1);assertTrue(route.evidenceMemoryIds.contains(m2.memoryId));assertEquals("test_area->other_area",route.contextKey);
  WorldConnection learnedEdge=new WorldConnection("test_area","other_area",88,112,.05,.8,"walk");double neutralCost=WorldPathPlanner.cost(state(),learnedEdge,"girl"),learnedCost=WorldPathPlanner.cost(s,learnedEdge,"girl");assertTrue(learnedCost>neutralCost);assertTrue(HaruReasoningEngine.causalRoutePenalty(s,learnedEdge)>0);
  PlanState future=new PlanState();future.planId="future_retry";future.intentionId="affordance_inquiry";future.goal="learn from observation";future.destination="mystery_flora";future.origin="WORLD_AFFORDANCE";future.status="ACTIVE";future.commitment=.6;future.createdAt=1500L;future.steps.add("TRAVEL:test_area");HaruReasoningEngine.attachReasoningToPlan(s,future,1500L);
  assertEquals(route.id,future.causalAdaptationId);assertEquals("PREFER_ALTERNATE_ROUTE",future.adaptationPolicy);assertTrue(future.steps.stream().anyMatch(x->x.startsWith("ADAPT_CAUSE:")));
  s.planState=future;WorldState x=WorldState.fromJson(s.toJson());assertTrue(x.characterGod.reasoning.causalExplanations.containsKey(route.id));assertEquals(route.confidence,x.characterGod.reasoning.causalExplanations.get(route.id).confidence,.000001);assertEquals(route.testablePrediction,x.characterGod.reasoning.causalExplanations.get(route.id).testablePrediction);assertEquals(route.id,x.planState.causalAdaptationId);assertEquals("PREFER_ALTERNATE_ROUTE",x.planState.adaptationPolicy);
 }

 @Test public void causalExperimentChangesOneRealRouteVariableAndResolvesFromOutcome() throws Exception{
  WorldState s=experimentState();PlanState failed=reasoningPlan(s,"experiment_fail","test_target","",1000L);failed.origin="WORLD_AFFORDANCE";s.planState=failed;
  s.girlTravel.currentPlanId=failed.planId;s.girlTravel.travelMode="ROUTE";s.girlTravel.routeIndex=0;s.girlTravel.route.add("area_a");s.girlTravel.route.add("area_b");s.girlTravel.route.add("area_d");
  WorldEventBus.publishId(s,1100L,"evt_experiment_route_block","ROUTE_BLOCKED",failed.planId,"the original route became blocked");
  failed.status="FAILED";failed.lastOutcome="route invalid; no alternate path";
  MemoryEntry failure=CognitionEngine.experience(s,1200L,"travel_failed","route invalid; no alternate path",-.12,.55,"failure","plan_terminal");
  HaruReasoningEngine.reviewPlanOutcome(s,failed,failure,1200L);

  PredictionState pred=s.characterGod.reasoning.predictions.get(failed.predictionId);CausalExplanationState route=s.characterGod.reasoning.causalExplanations.get("cause_"+pred.id+"_ROUTE_CONSTRAINT");
  assertNotNull(route);assertEquals("area_a->area_b",route.contextKey);
  CausalExperimentState experiment=s.characterGod.reasoning.causalExperiments.values().stream().findFirst().orElse(null);
  assertNotNull(experiment);assertEquals("DESIGNED",experiment.status);assertEquals("ALTERNATE_ROUTE_RETRY",experiment.strategy);

  PlanState retry=experimentPlan("experiment_retry",1500L);HaruReasoningEngine.attachReasoningToPlan(s,retry,1500L);
  assertEquals(experiment.id,retry.causalExperimentId);assertEquals("ALTERNATE_ROUTE_RETRY",retry.experimentStrategy);assertEquals("RUNNING",experiment.status);assertTrue(experiment.manipulationVerified);
  assertFalse(experiment.testContextKey.contains("area_a->area_b"));
  assertEquals(Arrays.asList("area_a","area_c","area_d"),WorldPathPlanner.route(s,"girl","area_a","area_d"));

  retry.status="COMPLETED";retry.actionResolvedAt=1600L;MemoryEntry outcome=CognitionEngine.experience(s,1600L,"planned_action_outcome","the alternate-route retry reached the target",.12,.52,"success");
  HaruReasoningEngine.reviewPlanOutcome(s,retry,outcome,1600L);
  assertEquals("RESOLVED",experiment.status);assertEquals(route.id,experiment.favoredCauseId);assertEquals(outcome.memoryId,experiment.outcomeMemoryId);assertEquals("SUPPORTED",route.status);

  s.planState=retry;WorldState x=WorldState.fromJson(s.toJson());assertTrue(x.characterGod.reasoning.causalExperiments.containsKey(experiment.id));assertEquals(experiment.id,x.planState.causalExperimentId);assertEquals("RESOLVED",x.characterGod.reasoning.causalExperiments.get(experiment.id).status);
 }

 @Test public void causalExperimentDoesNotPretendToRunWithoutManipulation(){
  WorldState s=experimentState();PlanState failed=reasoningPlan(s,"no_manip_fail","test_target","",1000L);failed.origin="WORLD_AFFORDANCE";s.planState=failed;
  s.girlTravel.currentPlanId=failed.planId;s.girlTravel.travelMode="ROUTE";s.girlTravel.routeIndex=0;s.girlTravel.route.add("area_a");s.girlTravel.route.add("area_b");s.girlTravel.route.add("area_d");
  WorldEventBus.publishId(s,1100L,"evt_no_manip_block","ROUTE_BLOCKED",failed.planId,"the route was blocked");
  failed.status="FAILED";failed.lastOutcome="route invalid; no alternate path";MemoryEntry failure=CognitionEngine.experience(s,1200L,"travel_failed","route invalid; no alternate path",-.12,.55,"failure");
  HaruReasoningEngine.reviewPlanOutcome(s,failed,failure,1200L);
  CausalExperimentState experiment=s.characterGod.reasoning.causalExperiments.values().stream().findFirst().orElse(null);assertNotNull(experiment);

  s.world.area("area_a").connections.remove("area_c");
  PlanState retry=experimentPlan("same_route_retry",1500L);HaruReasoningEngine.attachReasoningToPlan(s,retry,1500L);
  assertTrue(retry.causalExperimentId.isEmpty());assertEquals("DESIGNED",experiment.status);assertFalse(experiment.manipulationVerified);
 }

 @Test public void dopamineOverdriveDelaysExecutiveCausalExperimentUntilControlRecovers(){
  WorldState s=experimentState();PlanState failed=reasoningPlan(s,"dopamine_exp_fail","test_target","",1000L);failed.origin="WORLD_AFFORDANCE";s.planState=failed;
  s.girlTravel.currentPlanId=failed.planId;s.girlTravel.travelMode="ROUTE";s.girlTravel.routeIndex=0;s.girlTravel.route.add("area_a");s.girlTravel.route.add("area_b");s.girlTravel.route.add("area_d");
  WorldEventBus.publishId(s,1100L,"evt_dopamine_route_block","ROUTE_BLOCKED",failed.planId,"the original route became blocked");
  failed.status="FAILED";failed.lastOutcome="route invalid; no alternate path";MemoryEntry failure=CognitionEngine.experience(s,1200L,"travel_failed","route invalid; no alternate path",-.12,.55,"failure");
  HaruReasoningEngine.reviewPlanOutcome(s,failed,failure,1200L);CausalExperimentState experiment=s.characterGod.reasoning.causalExperiments.values().stream().findFirst().orElse(null);assertNotNull(experiment);

  DopamineModulationEngine.pulse(s,1.0,"strong_reward",1300L);DopamineModulationEngine.pulse(s,1.0,"strong_reward",1301L);assertTrue(DopamineModulationEngine.logicalControl(s)<.58);
  PlanState impulsive=experimentPlan("impulsive_retry",1400L);HaruReasoningEngine.attachReasoningToPlan(s,impulsive,1400L);
  assertTrue(impulsive.causalExperimentId.isEmpty());assertEquals("DESIGNED",experiment.status);

  DopamineModulationEngine.advance(s,30*60.0,1400L+30*60*1000L);assertTrue(DopamineModulationEngine.logicalControl(s)>.58);
  PlanState deliberate=experimentPlan("deliberate_retry",1400L+30*60*1000L);HaruReasoningEngine.attachReasoningToPlan(s,deliberate,1400L+30*60*1000L);
  assertEquals(experiment.id,deliberate.causalExperimentId);assertEquals("RUNNING",experiment.status);
 }

 @Test public void dopamineOverdriveStoresEvidenceButDelaysEpistemicClosure(){
  WorldState s=state();HaruAffordanceEngine.observeQuestions(s,1000L);HaruReasoningEngine.observe(s,1000L);
  HypothesisState h=s.characterGod.reasoning.hypotheses.get("hyp_revisit_mystery_flora");OpenQuestionState q=s.characterGod.openQuestions.get("q_world_mystery_flora");assertNotNull(h);assertNotNull(q);
  DopamineModulationEngine.pulse(s,1.0,"high_reward",1100L);DopamineModulationEngine.pulse(s,1.0,"high_reward",1101L);assertTrue(DopamineModulationEngine.logicalControl(s)<.62);
  for(int i=0;i<5;i++){PlanState p=reasoningPlan(s,"overdrive_support_"+i,"mystery_flora",h.id,1200L+i*100);p.status="COMPLETED";MemoryEntry m=CognitionEngine.experience(s,1250L+i*100,"planned_action_outcome","support while overstimulated "+i,.10,.50,"reasoning");HaruReasoningEngine.reviewPlanOutcome(s,p,m,1250L+i*100);}
  assertTrue(h.confidence>.72);assertEquals("ACTIVE",h.status);assertEquals("PARTIAL",q.status);assertTrue(s.thoughts.stream().anyMatch(t->t.trigger.equals("metacognitive_hold:"+h.id)));

  DopamineModulationEngine.advance(s,30*60.0,30L*60L*1000L+2000L);assertTrue(DopamineModulationEngine.logicalControl(s)>.62);
  PlanState stable=reasoningPlan(s,"stable_support","mystery_flora",h.id,30L*60L*1000L+2100L);stable.status="COMPLETED";MemoryEntry m=CognitionEngine.experience(s,30L*60L*1000L+2200L,"planned_action_outcome","support after control recovered",.10,.50,"reasoning");HaruReasoningEngine.reviewPlanOutcome(s,stable,m,30L*60L*1000L+2200L);
  assertEquals("SUPPORTED",h.status);assertEquals("RESOLVED",q.status);
 }

 @Test public void HaruLearnsThatOverdriveCanMakeHerOverconfident() throws Exception{
  WorldState s=state();s.beliefStates.put("plan_outcome:affordance_inquiry",new BeliefState("plan_outcome:affordance_inquiry","usually_succeeds",.90));
  for(int i=0;i<3;i++){
   DopamineModulationEngine.pulse(s,1.0,"calibration_reward_"+i,2000L+i*200);DopamineModulationEngine.pulse(s,1.0,"calibration_reward_"+i,2001L+i*200);
   PlanState p=reasoningPlan(s,"calibration_miss_"+i,"mystery_flora","",2100L+i*200);PredictionState pred=s.characterGod.reasoning.predictions.get(p.predictionId);assertTrue(pred.overdriveAtPrediction>.20);assertTrue(pred.confidence>=.68);
   p.status="FAILED";MemoryEntry m=CognitionEngine.experience(s,2150L+i*200,"planned_action_outcome","overconfident prediction failed "+i,-.16,.55,"failure");HaruReasoningEngine.reviewPlanOutcome(s,p,m,2150L+i*200);
  }
  MetacognitiveCalibrationState cal=s.characterGod.reasoning.calibration;assertTrue(cal.learnedOverdriveRisk());assertTrue(cal.overdriveOverconfidentMisses>=3);assertTrue(cal.cautionAdjustment()>0);
  assertTrue(s.thoughts.stream().anyMatch(t->"self_calibration:dopamine_overdrive".equals(t.trigger)));

  WorldState naive=state();naive.beliefStates.put("plan_outcome:affordance_inquiry",new BeliefState("plan_outcome:affordance_inquiry","usually_succeeds",.90));DopamineModulationEngine.pulse(naive,1.0,"naive",5000L);DopamineModulationEngine.pulse(naive,1.0,"naive",5001L);
  PlanState naivePlan=reasoningPlan(naive,"naive_prediction","mystery_flora","",5100L);double naiveConfidence=naive.characterGod.reasoning.predictions.get(naivePlan.predictionId).confidence;
  DopamineModulationEngine.pulse(s,1.0,"learned_retry",5000L);DopamineModulationEngine.pulse(s,1.0,"learned_retry",5001L);PlanState calibrated=reasoningPlan(s,"calibrated_prediction","mystery_flora","",5100L);PredictionState cp=s.characterGod.reasoning.predictions.get(calibrated.predictionId);
  assertTrue(cp.confidence<naiveConfidence);assertTrue(cp.confidence>=.18);assertTrue(cp.dopamineAtPrediction>0);assertTrue(cp.logicalControlAtPrediction<1);

  WorldState x=WorldState.fromJson(s.toJson());assertTrue(x.characterGod.reasoning.calibration.learnedOverdriveRisk());PredictionState xp=x.characterGod.reasoning.predictions.get(cp.id);assertEquals(cp.overdriveAtPrediction,xp.overdriveAtPrediction,.000001);assertEquals(cp.logicalControlAtPrediction,xp.logicalControlAtPrediction,.000001);
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

 private static PlanState experimentPlan(String id,long now){
  PlanState p=new PlanState();p.planId=id;p.intentionId="affordance_inquiry";p.goal="learn from observation";p.destination="test_target";p.plannedAction="OBSERVE";p.origin="WORLD_AFFORDANCE";p.status="ACTIVE";p.commitment=.60;p.createdAt=now;p.lastProgressAt=now;p.steps.add("TRAVEL:area_d");p.steps.add("OBSERVE:test_target");return p;
 }

 private static WorldState experimentState(){
  WorldState s=state();s.haruX=10;s.world=new WorldModel();
  WorldArea a=new WorldArea("area_a","A","A",0,100,0,true,"test"),b=new WorldArea("area_b","B","B",120,220,0,true,"test"),cc=new WorldArea("area_c","C","C",120,220,0,true,"test"),d=new WorldArea("area_d","D","D",240,340,0,true,"test");
  a.connections.add("area_b");a.connections.add("area_c");b.connections.add("area_d");cc.connections.add("area_d");s.world.areas.add(a);s.world.areas.add(b);s.world.areas.add(cc);s.world.areas.add(d);
  WorldObject o=new WorldObject("test_target","flora","area_d","","mục tiêu thử nghiệm",270,0,10,10,"flora living resource");o.enabled=true;o.interactable=true;s.world.objects.add(o);return s;
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
