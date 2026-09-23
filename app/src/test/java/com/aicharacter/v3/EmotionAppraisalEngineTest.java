package com.aicharacter.v3;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public final class EmotionAppraisalEngineTest {

 @Test public void sameAbsenceFeelsDifferentWhenBeliefAboutReturnDiffers(){
  WorldState reassured=state(),uncertain=state();bond(reassured);bond(uncertain);
  reassured.beliefStates.put("cat_returns",new BeliefState("cat_returns","likely_returns",.92));
  uncertain.beliefStates.put("cat_returns",new BeliefState("cat_returns","likely_returns",.08));

  CognitionEngine.experience(reassured,1000L,"cat_absence","The cat has not returned yet.",-.38,.82,"cat","absence","social");
  CognitionEngine.experience(uncertain,1000L,"cat_absence","The cat has not returned yet.",-.38,.82,"cat","absence","social");

  EmotionEpisodeState calm=last(reassured),worried=last(uncertain);
  assertNotNull(calm);assertNotNull(worried);assertTrue(worried.fear>calm.fear);assertTrue(worried.sadness>=calm.sadness);
  assertTrue(calm.loneliness>0);assertTrue(worried.loneliness>0);
  assertEquals("cat",worried.targetId);assertTrue(EmotionAppraisalEngine.currentCause(uncertain,worried.primaryEmotion).contains("not returned"));
 }

 @Test public void oneEventCanCreateFearSadnessAndLonelinessTogether(){
  WorldState s=state();bond(s);s.beliefStates.put("cat_returns",new BeliefState("cat_returns","uncertain",.18));
  CognitionEngine.experience(s,1200L,"cat_absence_after_danger","The cat disappeared after a dangerous disturbance.",-.62,.94,"cat","absence","danger","loss","social");
  EmotionEpisodeState x=last(s);assertNotNull(x);
  assertTrue(x.fear>.20);assertTrue(x.sadness>.20);assertTrue(x.loneliness>.20);
  assertTrue(x.intensity>.20);assertTrue(Arrays.asList("afraid","sad","lonely").contains(x.primaryEmotion));
 }

 @Test public void bodyAlarmCreatesAttributedInteroceptiveEmotionAndCanResolve(){
  WorldState s=state();s.body.pain=72;s.endocrine.stressResponse=.78;s.respiration.breathingLoad=.58;s.nervous.protectiveReflex=.65;
  EmotionRegulationEngine.advance(s,60,2000L);
  EmotionEpisodeState x=last(s);assertNotNull(x);assertEquals("interoception",x.sourceKind);assertEquals("body",x.targetId);assertTrue(x.fear>.20);assertTrue(x.cause.length()>0);
  double before=s.emotion.fear;
  s.body.pain=0;s.endocrine.stressResponse=.05;s.respiration.breathingLoad=0;s.nervous.protectiveReflex=0;s.nervous.balanceAlarm=0;s.currentIntention="quiet_pause";
  EmotionRegulationEngine.advance(s,60*60,2000L+60*60*1000L);
  assertTrue(s.emotion.fear<before);assertTrue("RESOLVED".equals(x.status)||x.intensity<.08);
 }

 @Test public void mixedEmotionBiasesChoicesButDoesNotIssueCommands(){
  WorldState s=state();s.emotion.fear=.82;s.emotion.curiosity=.72;s.emotion.sadness=.20;s.emotion.calm=.08;
  List<LifeDecision> choices=new ArrayList<>();
  LifeDecision shelter=new LifeDecision(new Intention("seek_shelter",0,"safety","home_shelter","get safe",.5,9999L)).reason("base",5);
  LifeDecision explore=new LifeDecision(new Intention("explore_garden",0,"curiosity","garden_path","explore",.5,9999L)).reason("base",5);
  choices.add(shelter);choices.add(explore);EmotionalDecisionEngine.apply(s,choices,3000L);
  assertTrue(shelter.reasons.containsKey("mixed_emotion_bias"));assertTrue(explore.reasons.containsKey("mixed_emotion_bias"));
  assertTrue(shelter.intention.utility>explore.intention.utility);
  assertEquals("observe_lake",s.currentIntention);
  assertTrue(EmotionalDecisionEngine.attentionNarrowing(s)>0);
  assertTrue(EmotionalDecisionEngine.reflectiveSupport(s)<1);
 }

 @Test public void emotionalEpisodesSurviveSaveAndNonFiniteValuesAreContained() throws Exception{
  WorldState s=state();CognitionEngine.experience(s,4000L,"kind_social_event","A kind interaction felt unexpectedly warm.",.55,.72,"cat","kind","social");
  assertFalse(s.emotionEpisodes.isEmpty());s.emotion.joy=Double.NaN;s.mood.arousal=Double.POSITIVE_INFINITY;EmotionEpisodeState e=last(s);e.fear=Double.NaN;e.arousal=Double.POSITIVE_INFINITY;
  WorldState x=WorldState.fromJson(s.toJson());
  assertEquals(s.emotionEpisodes.size(),x.emotionEpisodes.size());assertTrue(Double.isFinite(x.emotion.joy));assertTrue(Double.isFinite(x.mood.arousal));
  EmotionEpisodeState xe=last(x);assertTrue(Double.isFinite(xe.fear));assertTrue(Double.isFinite(xe.arousal));assertEquals(e.sourceMemoryId,xe.sourceMemoryId);
 }

 @Test public void activeAndOfflineSlicesShareEmotionalClock(){
  WorldState active=state(),offline=state();bond(active);bond(offline);
  CognitionEngine.experience(active,5000L,"cat_absence","The cat is still away.",-.42,.82,"cat","absence","social");
  CognitionEngine.experience(offline,5000L,"cat_absence","The cat is still away.",-.42,.82,"cat","absence","social");
  long now=65000L;LifeSimulationKernel.beginSlice(active,60,now,LifeSimulationKernel.Mode.ACTIVE);LifeSimulationKernel.beginSlice(offline,60,now,LifeSimulationKernel.Mode.OFFLINE);
  assertEquals(active.emotion.fear,offline.emotion.fear,.000001);assertEquals(active.emotion.sadness,offline.emotion.sadness,.000001);assertEquals(active.emotion.loneliness,offline.emotion.loneliness,.000001);assertEquals(active.emotion.calm,offline.emotion.calm,.000001);
  assertEquals(active.emotionEpisodes.size(),offline.emotionEpisodes.size());assertEquals(active.emotionEpisodes.get(0).intensity,offline.emotionEpisodes.get(0).intensity,.000001);
 }

 private static EmotionEpisodeState last(WorldState s){return s.emotionEpisodes.isEmpty()?null:s.emotionEpisodes.get(s.emotionEpisodes.size()-1);}
 private static void bond(WorldState s){s.relationship.attachment=92;s.relationship.affection=86;s.relationship.trust=78;s.relationship.comfort=70;s.relationship.clamp();}
 private static WorldState state(){
  WorldState s=WorldState.fresh();s.createdAt=100;s.lastSavedAt=100;s.lastOpenedAt=100;s.lastSimulatedAt=100;s.haruX=10;s.catX=5;s.lastCatSeenAt=100;s.currentIntention="observe_lake";
  s.world=new WorldModel();s.world.areas.add(new WorldArea("test_area","Test","test place",0,100,0,true,"test"));s.body.energy=90;s.body.sleepiness=10;s.body.pain=0;s.endocrine.stressResponse=.08;s.respiration.breathingLoad=0;s.nervous.balanceAlarm=0;s.nervous.protectiveReflex=0;
  return s;
 }
}
