package com.aicharacter.v3;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public final class DopamineModulationEngineTest {

 @Test public void strongPhasicDopamineRaisesRewardAndInstinctWhileReducingExecutiveControl(){
  WorldState s=WorldState.fresh();double before=DopamineModulationEngine.logicalControl(s);
  DopamineModulationEngine.pulse(s,.98,"test_reward",1000L);
  assertTrue(s.neuroModulation.dopamine()>.62);
  assertTrue(DopamineModulationEngine.overdrive(s)>0);
  assertTrue(s.neuroModulation.rewardSalience>.45);
  assertTrue(DopamineModulationEngine.instinctBias(s)>0);
  assertTrue(DopamineModulationEngine.logicalControl(s)<before);
 }

 @Test public void phasicDopamineDecaysAndJudgmentRecovers(){
  WorldState s=WorldState.fresh();DopamineModulationEngine.pulse(s,1.0,"test_reward",1000L);
  double spike=s.neuroModulation.dopaminePhasic,control=DopamineModulationEngine.logicalControl(s);
  DopamineModulationEngine.advance(s,20*60.0,1000L+20*60*1000L);
  assertTrue(s.neuroModulation.dopaminePhasic<spike);
  assertTrue(DopamineModulationEngine.logicalControl(s)>control);
 }

 @Test public void dopamineBiasesDeliberationButDoesNotRewriteBeliefsOrPersonality(){
  WorldState low=decisionState(),high=decisionState();
  high.beliefStates.put("stable_test",new BeliefState("stable_test","unchanged",.73));
  double curiosity=high.personality.curiosity,caution=high.personality.caution;
  DopamineModulationEngine.pulse(high,1.0,"reward_spike",2000L);

  List<LifeDecision> lowChoices=choices(2000L),highChoices=choices(2000L);
  DeliberationEngine.apply(low,lowChoices,2000L);DeliberationEngine.apply(high,highChoices,2000L);

  assertFalse(lowChoices.get(0).reasons.containsKey("dopamine_reward_salience"));
  assertTrue(highChoices.get(0).reasons.containsKey("dopamine_reward_salience"));
  assertTrue(highChoices.get(0).reasons.containsKey("dopamine_executive_noise"));
  assertEquals(.73,high.beliefStates.get("stable_test").confidence,.000001);
  assertEquals(curiosity,high.personality.curiosity,.000001);assertEquals(caution,high.personality.caution,.000001);
 }

 @Test public void livedRewardPulsesDopamineThroughCognitionPipeline(){
  WorldState s=decisionState();double before=s.neuroModulation.dopaminePhasic;
  CognitionEngine.experience(s,5000L,"social_reward","a real rewarding interaction happened",.55,.72,"success","curiosity");
  assertTrue(s.neuroModulation.dopaminePhasic>before);
  assertTrue(s.neuroModulation.lastPulseSource.startsWith("experience:"));
 }

 @Test public void plannedOutcomeWaitsForPredictionErrorReviewInsteadOfDoublePulsing(){
  WorldState s=decisionState();double before=s.neuroModulation.dopaminePhasic;
  CognitionEngine.experience(s,5100L,"planned_action_outcome","planned success",.55,.72,"success");
  assertEquals(before,s.neuroModulation.dopaminePhasic,.0000001);
 }

 @Test public void unexpectedSuccessCreatesLargerPositiveRewardPredictionError(){
  WorldState unexpected=decisionState(),expected=decisionState();
  PlanState pu=plan("u",true),pe=plan("e",true);PredictionState pru=prediction("pu",.20),pre=prediction("pe",.88);
  MemoryEntry mu=new MemoryEntry(6000L,"planned_action_outcome","unexpected success",.6,.30,.9,"test_area",null,Arrays.asList("success"));
  MemoryEntry me=new MemoryEntry(6000L,"planned_action_outcome","expected success",.6,.30,.9,"test_area",null,Arrays.asList("success"));
  double ru=DopamineModulationEngine.onPlanOutcome(unexpected,pu,pru,mu,6000L),re=DopamineModulationEngine.onPlanOutcome(expected,pe,pre,me,6000L);
  assertTrue(ru>re);assertTrue(ru>0);assertTrue(unexpected.neuroModulation.dopaminePhasic>expected.neuroModulation.dopaminePhasic);
 }

 @Test public void confidentFailureCreatesNegativeRewardPredictionErrorAndDopamineDip(){
  WorldState s=decisionState();DopamineModulationEngine.pulse(s,.80,"preexisting_reward",6500L);double before=s.neuroModulation.dopaminePhasic;
  PlanState p=plan("fail",false);PredictionState prediction=prediction("pfail",.92);
  MemoryEntry m=new MemoryEntry(6600L,"planned_action_outcome","confident plan failed",.7,-.35,.9,"test_area",null,Arrays.asList("failure"));
  double rpe=DopamineModulationEngine.onPlanOutcome(s,p,prediction,m,6600L);
  assertTrue(rpe<0);assertTrue(s.neuroModulation.dopaminePhasic<before);assertEquals(rpe,s.neuroModulation.lastRewardPredictionError,.0000001);
  assertTrue(s.neuroModulation.lastPulseSource.startsWith("negative_reward_prediction_error:"));
 }

 @Test public void postOutcomeReviewComputesRpeBeforeReasoningClosesPrediction(){
  WorldState s=decisionState();PlanState p=new PlanState();p.planId="review_rpe";p.intentionId="observe_lake";p.status="COMPLETED";p.origin="LEGACY";p.predictionId="pred_review_rpe";p.outcomeLearnedAt=7000L;p.lastProgressAt=7000L;
  PredictionState pred=prediction(p.predictionId,.22);pred.planId=p.planId;pred.expectedOutcome="observe the target";s.characterGod.reasoning.predictions.put(pred.id,pred);
  MemoryEntry m=CognitionEngine.experience(s,7000L,"planned_action_outcome","an unexpectedly successful observation",.30,.60,"success");assertEquals(0,s.neuroModulation.dopaminePhasic,.0000001);
  p.outcomeMemoryId=m.memoryId;s.planState=p;
  assertTrue(PlanOutcomeReviewEngine.reviewIfReady(s,7100L));
  assertTrue(s.neuroModulation.lastRewardPredictionError>0);assertTrue(s.neuroModulation.dopaminePhasic>0);assertEquals("CONFIRMED",pred.status);assertTrue(p.postOutcomeReviewedAt>=7100L);
  assertTrue(p.lastOutcomeReview.contains("rewardPredictionError="));
 }

 @Test public void activeAndOfflineSlicesShareTheSameDopamineClock(){
  WorldState active=decisionState(),offline=decisionState();long now=System.currentTimeMillis()+60_000L;
  DopamineModulationEngine.pulse(active,.92,"parity",now-60_000L);DopamineModulationEngine.pulse(offline,.92,"parity",now-60_000L);
  LifeSimulationKernel.beginSlice(active,60,now,LifeSimulationKernel.Mode.ACTIVE);
  LifeSimulationKernel.beginSlice(offline,60,now,LifeSimulationKernel.Mode.OFFLINE);
  assertEquals(active.neuroModulation.dopaminePhasic,offline.neuroModulation.dopaminePhasic,.0000001);
  assertEquals(active.neuroModulation.rewardSalience,offline.neuroModulation.rewardSalience,.0000001);
  assertEquals(DopamineModulationEngine.logicalControl(active),DopamineModulationEngine.logicalControl(offline),.0000001);
  assertEquals(DopamineModulationEngine.instinctBias(active),DopamineModulationEngine.instinctBias(offline),.0000001);
 }

 @Test public void neuromodulationSurvivesSaveAndRejectsNonFiniteValues() throws Exception{
  WorldState s=WorldState.fresh();DopamineModulationEngine.pulse(s,.86,"save_test",3000L);s.neuroModulation.dopamineTonic=Double.NaN;s.neuroModulation.executiveNoise=Double.POSITIVE_INFINITY;
  WorldState x=WorldState.fromJson(s.toJson());
  assertTrue(Double.isFinite(x.neuroModulation.dopamineTonic));assertTrue(Double.isFinite(x.neuroModulation.executiveNoise));
  assertTrue(x.neuroModulation.dopaminePhasic>0);assertEquals("save_test",x.neuroModulation.lastPulseSource);assertTrue(Double.isFinite(x.neuroModulation.lastRewardPredictionError));
 }

 private static PlanState plan(String id,boolean success){
  PlanState p=new PlanState();p.planId=id;p.intentionId="observe_lake";p.status=success?"COMPLETED":"FAILED";return p;
 }
 private static PredictionState prediction(String id,double confidence){
  PredictionState p=new PredictionState();p.id=id;p.planId=id;p.confidence=confidence;return p;
 }

 private static WorldState decisionState(){
  WorldState s=WorldState.fresh();s.haruX=10;s.world=new WorldModel();s.world.areas.add(new WorldArea("test_area","Test","test",0,100,0,true,"test"));return s;
 }

 private static List<LifeDecision> choices(long now){
  List<LifeDecision> x=new ArrayList<>();
  x.add(new LifeDecision(new Intention("explore_garden",0,"curiosity","garden_path","notice something different",.32,now+7200000)).reason("base",10));
  x.add(new LifeDecision(new Intention("reflect",0,"curiosity","bench_lake_01","reflect",.30,now+7200000)).reason("base",10));
  return x;
 }
}
