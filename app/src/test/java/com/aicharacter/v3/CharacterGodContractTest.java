package com.aicharacter.v3;

import org.json.JSONObject;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public final class CharacterGodContractTest {

 @Test public void capabilityModelContainsRequiredBoundaries() throws Exception{
  for(GodCapabilityModel.Capability c:GodCapabilityModel.Capability.values())assertTrue(GodCapabilityModel.supports(c));
  assertEquals(8,GodCapabilityModel.toJson().length());
 }

 @Test public void godCannotRewriteHaruMindOrPastHistory() throws Exception{
  WorldState s=state();WorldEventBus.publishId(s,500L,"seed_event","WEATHER","environment","It rained.");
  String prior=s.worldHistory.get(0).toJson().toString();int memories=s.memories.size(),knowledge=s.knowledge.size(),skills=s.skills.size(),actions=s.learnedActions.size();double curiosity=s.personality.curiosity,calm=s.emotion.calm,trust=s.relationship.trust;String intention=s.currentIntention;

  String lesson=GodTeachingGateway.offerLesson(s,"gravity",Collections.singletonList("gravity"),"unsupported bodies fall",
          Collections.emptyList(),Collections.singletonList("reference explanation"),.25,
          "REAL_REFERENCE","open science","ref:gravity","2026-09-22T00:00:00Z",.9,"Reference only.","KNOWLEDGE",1000L);

  assertFalse(lesson.isEmpty());assertEquals(prior,s.worldHistory.get(0).toJson().toString());
  assertEquals(memories,s.memories.size());assertEquals(knowledge,s.knowledge.size());assertEquals(skills,s.skills.size());assertEquals(actions,s.learnedActions.size());
  assertEquals(curiosity,s.personality.curiosity,0);assertEquals(calm,s.emotion.calm,0);assertEquals(trust,s.relationship.trust,0);assertEquals(intention,s.currentIntention);
  assertEquals(2,s.worldHistory.size());assertEquals(HaruTeachingOpportunityEngine.OFFER_TYPE,s.worldHistory.get(1).type);
 }

 @Test public void godObservesMultipleSubsystemsWithoutRawMindScores() throws Exception{
  WorldState s=state();s.environment.weather="RAIN";s.environment.wind=.7;s.atmosphere.temperatureC=31;s.atmosphere.relativeHumidity=.8;s.livingWorld.flora("flora_1");s.livingWorld.creature("fauna_1");
  s.planState.goal="understand silver moss";s.planState.status="ACTIVE";s.planState.origin="WORLD_AFFORDANCE";s.planState.steps.add("OBSERVE:mystery_flora");
  WorldEventBus.publishId(s,800L,"cause_1","WORLD_TEST","test_area","A causal test event happened.");
  JSONObject j=GodObservationSnapshot.build(s);
  assertTrue(j.has("environment"));assertTrue(j.has("atmosphere"));assertTrue(j.has("livingWorld"));assertTrue(j.has("recentCausalHistory"));assertTrue(j.has("haruVisibleCondition"));assertTrue(j.has("currentPlanOutcome"));assertTrue(j.has("abnormalities"));
  assertEquals(1,j.getJSONObject("livingWorld").getInt("trackedFlora"));assertEquals(1,j.getJSONObject("livingWorld").getInt("trackedFauna"));assertTrue(j.getJSONObject("livingWorld").has("populations"));
  assertFalse(j.getJSONObject("haruVisibleCondition").has("energyRaw"));assertFalse(j.getJSONObject("haruVisibleCondition").has("personality"));
 }

 @Test public void knowledgeResultsPreserveLayerAndProvenance() throws Exception{
  WorldState s=state();s.world.knowledgeAnchors.add(new WorldKnowledgeAnchor("physics.gravity","physics","gravity reference","fantasy gravity transform","open science","ref:gravity","2026-09-22T00:00:00Z",.91,"Broad reference only.",Collections.singletonList("gravity")));
  List<GodKnowledgeQueryEngine.Result> r=GodKnowledgeQueryEngine.query(s,"gravity",5);
  assertFalse(r.isEmpty());GodKnowledgeQueryEngine.Result x=r.get(r.size()-1);
  assertEquals("REAL_REFERENCE",x.layer);assertEquals("ref:gravity",x.sourceRef);assertEquals(.91,x.confidence,0.0001);
  assertFalse(s.knowledge.containsKey("physics.gravity"));assertTrue(s.memories.isEmpty());
 }

 @Test public void boundedWorldProposalHasScopeDurationRollbackAndNoDoubleApply() throws Exception{
  WorldState s=state();long now=2000L;JSONObject j=new JSONObject();
  j.put("kind","god-world-condition-v2");j.put("id","wind-1");j.put("condition","WIND");j.put("desiredValue","BREEZE");j.put("scopeType","AREA");j.put("scopeTarget","test_area");j.put("intensity",.55);j.put("durationMinutes",45);j.put("provenanceLayer","GOD_PROPOSAL");j.put("sourceRef","god:proposal");j.put("rollbackPolicy","RESTORE_PREVIOUS_BASELINE");j.put("requestedAt",now);
  GodWorldEventProposal p=GodWorldEventProposal.fromJson(j,now);assertNotNull(p);
  assertTrue(GodWorldEventExecutor.apply(s,p,now).ok);assertFalse(GodWorldEventExecutor.apply(s,p,now+1).ok);
  WorldConditionProposalState saved=s.characterGod.worldConditions.get("wind-1");assertNotNull(saved);assertEquals("AREA",saved.scopeType);assertEquals("test_area",saved.scopeTarget);assertEquals(45,saved.durationMinutes,0);assertEquals(.55,saved.intensity,0);assertEquals("PROPOSED",saved.status);
  assertTrue(GodWorldConditionContract.markApplied(s,"wind-1","WorldPhysicsTest",now+2));assertFalse(GodWorldConditionContract.markApplied(s,"wind-1","WorldPhysicsTest",now+3));
  assertTrue(GodWorldEventExecutor.requestRollback(s,"wind-1",now+4).ok);assertEquals("ROLLBACK_REQUESTED",saved.status);assertTrue(GodWorldConditionContract.markRolledBack(s,"wind-1","WorldPhysicsTest",now+5));assertEquals("ROLLED_BACK",saved.status);
 }

 @Test public void lessonCanBeListenedTo() throws Exception{
  WorldState s=state();prepareEngaged(s);s.currentIntention="gravity inquiry";
  String id=offer(s,"gravity","gravity",.15,.95,Collections.emptyList(),1000L);
  assertTrue(HaruTeachingOpportunityEngine.observe(s,1100L));LessonState l=s.characterGod.lessons.get(id);
  assertEquals(HaruTeachingOpportunityEngine.Response.LISTEN.name(),l.lastResponse);assertEquals("LEARNED",l.status);assertNotNull(s.characterGod.conceptKnowledge.get("gravity"));assertEquals(1,s.memories.size());
 }

 @Test public void lessonCanBeDeferredByCurrentBodyContext() throws Exception{
  WorldState s=state();prepareEngaged(s);s.body.energy=10;
  String id=offer(s,"gravity","gravity",.15,.95,Collections.emptyList(),1000L);
  assertTrue(HaruTeachingOpportunityEngine.observe(s,1100L));assertEquals(HaruTeachingOpportunityEngine.Response.DEFER.name(),s.characterGod.lessons.get(id).lastResponse);assertTrue(s.memories.isEmpty());assertFalse(s.characterGod.conceptKnowledge.containsKey("gravity"));
 }

 @Test public void lessonCanTriggerQuestionWhenPrerequisiteMissing() throws Exception{
  WorldState s=state();prepareEngaged(s);
  String id=offer(s,"orbital motion","orbit",.35,.9,Collections.singletonList("gravity"),1000L);
  assertTrue(HaruTeachingOpportunityEngine.observe(s,1100L));assertEquals(HaruTeachingOpportunityEngine.Response.QUESTION.name(),s.characterGod.lessons.get(id).lastResponse);assertTrue(s.characterGod.openQuestions.containsKey("q_lesson_"+id));assertTrue(s.memories.isEmpty());
 }

 @Test public void lessonCanBeRejectedWhenLowTrustConflictsWithEvidence() throws Exception{
  WorldState s=state();prepareEngaged(s);ConceptKnowledgeState k=new ConceptKnowledgeState();k.concept="rain";k.claim="rain is wet";k.confidence=.8;s.characterGod.conceptKnowledge.put("rain",k);
  String id=offer(s,"rain claim","rain",.25,.1,Collections.emptyList(),1000L);
  s.characterGod.lessons.get(id).claim="rain is dry";
  assertTrue(HaruTeachingOpportunityEngine.observe(s,1100L));assertEquals(HaruTeachingOpportunityEngine.Response.REJECT.name(),s.characterGod.lessons.get(id).lastResponse);assertTrue(s.memories.isEmpty());
 }

 @Test public void partialLearningDoesNotBecomeFullyKnown() throws Exception{
  WorldState s=state();prepareEngaged(s);s.currentIntention="complex topic";s.personality.curiosity=.55;s.personality.patience=.55;s.emotion.calm=.55;s.body.energy=70;
  String id=offer(s,"complex topic","complex_topic",1.0,.8,Collections.emptyList(),1000L);
  assertTrue(HaruTeachingOpportunityEngine.observe(s,1100L));LessonState l=s.characterGod.lessons.get(id);assertEquals(HaruTeachingOpportunityEngine.Response.PARTIALLY_UNDERSTAND.name(),l.lastResponse);
  ConceptKnowledgeState k=s.characterGod.conceptKnowledge.get("complex_topic");assertNotNull(k);assertTrue(k.confidence<.75);assertFalse(s.knowledge.containsKey("complex_topic"));
 }

 @Test public void contradictoryCausalEvidenceCanCorrectKnowledgeBelief() throws Exception{
  WorldState s=state();
  MemoryEntry a=CognitionEngine.experience(s,1000L,"observation","first observation",.1,.5,"evidence");
  assertTrue(HaruKnowledgeRevisionEngine.applyEvidence(s,"sky_state","blue",1,.85,"VNF_WORLD_TRUTH","memory:"+a.memoryId,a,1000L));
  MemoryEntry b=CognitionEngine.experience(s,2000L,"observation","contradiction",-.1,.6,"evidence");
  assertTrue(HaruKnowledgeRevisionEngine.applyEvidence(s,"sky_state","blue",-1,1.0,"VNF_WORLD_TRUTH","memory:"+b.memoryId,b,2000L));
  MemoryEntry c=CognitionEngine.experience(s,3000L,"observation","alternative evidence",.1,.7,"evidence");
  assertTrue(HaruKnowledgeRevisionEngine.applyEvidence(s,"sky_state","red",1,1.0,"VNF_WORLD_TRUTH","memory:"+c.memoryId,c,3000L));
  assertEquals("red",s.characterGod.conceptKnowledge.get("sky_state").claim);
 }

 @Test public void skillDoesNotIncreaseWithoutPracticeOutcome() throws Exception{
  WorldState s=state();prepareEngaged(s);s.currentIntention="skill_theory:sketch";
  int size=s.skills.size(),actions=s.learnedActions.size();double before=s.skills.getOrDefault("sketch",0.0);
  String id=GodTeachingGateway.offerLesson(s,"sketch theory",Collections.singletonList("skill_theory:sketch"),"safe sketching theory",Collections.emptyList(),Collections.singletonList("explanation"),.15,"REAL_REFERENCE","art reference","ref:sketch","2026-09-22T00:00:00Z",.9,"Theory only.","SKILL_THEORY",1000L);
  assertTrue(HaruTeachingOpportunityEngine.observe(s,1100L));assertTrue(Arrays.asList("LISTEN","PARTIALLY_UNDERSTAND").contains(s.characterGod.lessons.get(id).lastResponse));
  assertEquals(size,s.skills.size());assertEquals(actions,s.learnedActions.size());assertEquals(before,s.skills.getOrDefault("sketch",0.0),0);
 }

 @Test public void haruFormsMultiStepPlanFromVisibleWorldAffordance() throws Exception{
  WorldState s=state();prepareEngaged(s);HaruAffordanceEngine.observeQuestions(s,1000L);
  HaruAffordanceEngine.Candidate candidate=HaruAffordanceEngine.bestCandidate(s,1000L);assertNotNull(candidate);assertEquals("mystery_flora",candidate.objectId);
  assertTrue(HaruAffordanceEngine.beginPlanIfCompelling(s,1000L));assertEquals("WORLD_AFFORDANCE",s.planState.origin);assertEquals("affordance_inquiry",s.planState.intentionId);assertEquals("mystery_flora",s.planState.destination);assertTrue(s.planState.steps.size()>=4);assertTrue(s.planState.steps.get(2).startsWith("COMPARE_EVIDENCE:"));assertFalse(s.planState.candidateGoalId.isEmpty());
 }

 @Test public void saveRoundTripPreservesAllNewCharacterGodState() throws Exception{
  WorldState s=state();prepareEngaged(s);String id=offer(s,"gravity","gravity",.2,.9,Collections.emptyList(),1000L);HaruTeachingOpportunityEngine.observe(s,1100L);HaruAffordanceEngine.observeQuestions(s,1200L);
  JSONObject pjson=new JSONObject();pjson.put("id","mist-save");pjson.put("condition","HUMIDITY_MIST");pjson.put("desiredValue","LIGHT_MIST");pjson.put("scopeType","AREA");pjson.put("scopeTarget","test_area");pjson.put("intensity",.3);pjson.put("durationMinutes",25);pjson.put("requestedAt",1300L);pjson.put("rollbackPolicy","DISSIPATE_TO_BASELINE");
  assertTrue(GodWorldConditionContract.propose(s,GodWorldEventProposal.fromJson(pjson,1300L),1300L).ok);
  s.planState.origin="WORLD_AFFORDANCE";s.planState.candidateGoalId="affordance:mystery_flora";s.planState.triggerEvidenceId="visible_object:mystery_flora";s.planState.questionId="q_world_mystery_flora";
  WorldState x=WorldState.fromJson(s.toJson());
  assertTrue(x.characterGod.lessons.containsKey(id));assertTrue(x.characterGod.conceptKnowledge.containsKey("gravity"));assertFalse(x.characterGod.openQuestions.isEmpty());assertTrue(x.characterGod.worldConditions.containsKey("mist-save"));
  assertEquals("WORLD_AFFORDANCE",x.planState.origin);assertEquals("affordance:mystery_flora",x.planState.candidateGoalId);assertEquals("visible_object:mystery_flora",x.planState.triggerEvidenceId);assertEquals("q_world_mystery_flora",x.planState.questionId);
 }

 @Test public void activeOfflineModesUseSameCharacterCausalPipeline() throws Exception{
  WorldState active=state(),offline=state();prepareEngaged(active);prepareEngaged(offline);active.currentIntention=offline.currentIntention="gravity inquiry";
  offer(active,"gravity","gravity",.2,.9,Collections.emptyList(),1600L);offer(offline,"gravity","gravity",.2,.9,Collections.emptyList(),1600L);
  LifeSimulationKernel.beginSlice(active,60,1700L,LifeSimulationKernel.Mode.ACTIVE);
  LifeSimulationKernel.beginSlice(offline,60,1700L,LifeSimulationKernel.Mode.OFFLINE);
  HaruGoalSelectionEngine.selectAndBegin(active,1700L);HaruGoalSelectionEngine.selectAndBegin(offline,1700L);
  assertEquals(active.characterGod.lessons.values().iterator().next().lastResponse,offline.characterGod.lessons.values().iterator().next().lastResponse);
  assertEquals(active.characterGod.conceptKnowledge.keySet(),offline.characterGod.conceptKnowledge.keySet());assertEquals(active.characterGod.openQuestions.keySet(),offline.characterGod.openQuestions.keySet());
  assertEquals(active.planState.origin,offline.planState.origin);assertEquals(active.planState.destination,offline.planState.destination);assertEquals(active.planState.steps,offline.planState.steps);
 }

 @Test public void provenanceOnlyCompleteWhenRealMetadataIsPresent() throws Exception{
  WorldKnowledgeAnchor incomplete=new WorldKnowledgeAnchor("physics.gravity","physics","real reference","fictional transform","open science","gravity","UNKNOWN_BUNDLED_REFERENCE",-1,"reference only",Collections.singletonList("gravity"));
  assertFalse(incomplete.hasCompleteProvenance());
  WorldKnowledgeAnchor complete=new WorldKnowledgeAnchor("physics.gravity.verified","physics","real reference","fictional transform","open science","ref:gravity","2026-09-22T00:00:00Z",.85,"Known limits are documented.",Collections.singletonList("gravity"));
  assertTrue(complete.hasCompleteProvenance());
 }

 private static String offer(WorldState s,String topic,String concept,double difficulty,double sourceConfidence,List<String>prereq,long now){
  return GodTeachingGateway.offerLesson(s,topic,Collections.singletonList(concept),topic,prereq,Collections.singletonList("bounded evidence"),difficulty,"REAL_REFERENCE","test source","ref:"+concept,"2026-09-22T00:00:00Z",sourceConfidence,"Test provenance limits.","KNOWLEDGE",now);
 }

 private static void prepareEngaged(WorldState s){s.body.energy=95;s.body.sleepiness=5;s.body.pain=0;s.body.health=100;s.personality.curiosity=.92;s.personality.patience=.90;s.emotion.calm=.90;}

 private static WorldState state(){
  WorldState s=new WorldState();s.createdAt=100L;s.lastSavedAt=100L;s.lastOpenedAt=100L;s.lastSimulatedAt=100L;s.worldMinutes=8*60;s.haruX=10;s.catX=5;s.age=15;s.brainGrowth=5.0;s.haruMood="calm";s.haruActivity="idle";s.currentIntention="observe_lake";
  s.relationship=RelationshipState.fresh(new Random(7));s.body.energy=88;s.body.sleepiness=12;s.body.pain=0;s.body.health=100;s.emotion.calm=.82;s.personality.curiosity=.78;s.personality.patience=.72;s.catState=CatState.fromJson(null,s.catX,100L);
  s.world=new WorldModel();s.world.areas.add(new WorldArea("test_area","Test","test place",0,100,0,true,"test"));
  WorldObject o=new WorldObject("mystery_flora","flora","test_area","","silver moss",14,0,10,10,"flora living resource");o.enabled=true;o.interactable=true;s.world.objects.add(o);
  return s;
 }
}
