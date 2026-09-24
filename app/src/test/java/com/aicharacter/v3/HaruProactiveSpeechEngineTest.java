package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class HaruProactiveSpeechEngineTest {

 @Test public void recentRealSocialEventCanMakeHaruSpeakFirst(){
  long now=100000L;WorldState s=state(now);
  s.worldHistory.add(new WorldHistoryEntry(now-100,"cat_settle_event","CAT_SOCIAL_SETTLE_NEAR","cat","Cat chose to settle near Haru."));
  assertTrue(HaruProactiveSpeechEngine.advance(s,now,LifeSimulationKernel.Mode.ACTIVE));
  assertTrue(s.haruSpeech.pending());assertTrue(s.haruSpeech.pendingText.contains("ở lại gần"));
  HaruProactiveSpeechEngine.Cue cue=HaruProactiveSpeechEngine.consume(s,now+20);
  assertNotNull(cue);assertEquals("cat_settle_event",cue.sourceId);assertFalse(s.haruSpeech.pending());assertEquals(1,s.haruSpeech.spokenCount);
 }

 @Test public void offlineSimulationNeverCreatesPlayerFacingSpeech(){
  long now=200000L;WorldState s=state(now);
  s.worldHistory.add(new WorldHistoryEntry(now-20,"cat_settle_offline","CAT_SOCIAL_SETTLE_NEAR","cat","settled"));
  assertFalse(HaruProactiveSpeechEngine.advance(s,now,LifeSimulationKernel.Mode.OFFLINE));
  assertFalse(s.haruSpeech.pending());assertEquals(0,s.haruSpeech.spokenCount);
 }

 @Test public void newAutonomousIntentionCanBeAnnouncedWithoutPlayerPrompt(){
  long now=300000L;WorldState s=state(now);s.currentIntention="observe_lake";s.intentionStartedAt=now-200;
  assertTrue(HaruProactiveSpeechEngine.advance(s,now,LifeSimulationKernel.Mode.ACTIVE));
  assertTrue(s.haruSpeech.pendingText.contains("mặt hồ"));
  assertTrue(s.haruSpeech.pendingSourceEventId.startsWith("intention:observe_lake:"));
 }

 @Test public void uncertaintyCanBecomeGroundedProactiveThought(){
  long now=400000L;WorldState s=state(now);s.thoughts.add(new ThoughtState("unknown pattern","recent observation","",.82,.71,now-500));
  assertTrue(HaruProactiveSpeechEngine.advance(s,now,LifeSimulationKernel.Mode.ACTIVE));
  assertTrue(s.haruSpeech.pendingText.contains("chưa chắc"));assertTrue(s.haruSpeech.pendingText.contains("quan sát thêm"));
 }

 @Test public void speakingHasCooldownAndDoesNotReplaySameCause(){
  long now=500000L;WorldState s=state(now);s.worldHistory.add(new WorldHistoryEntry(now-50,"evt_once","CAT_SOCIAL_RESPONSE_COMPLETED","cat","mode=APPROACH distance=90"));
  assertTrue(HaruProactiveSpeechEngine.advance(s,now,LifeSimulationKernel.Mode.ACTIVE));assertNotNull(HaruProactiveSpeechEngine.consume(s,now+10));
  assertFalse(HaruProactiveSpeechEngine.advance(s,now+1000,LifeSimulationKernel.Mode.ACTIVE));
  assertFalse(HaruProactiveSpeechEngine.advance(s,now+50000,LifeSimulationKernel.Mode.ACTIVE));
 }

 @Test public void proactiveSpeechStateSurvivesSaveRoundTrip()throws Exception{
  long now=600000L;WorldState s=state(now);s.worldHistory.add(new WorldHistoryEntry(now-40,"persist_evt","CAT_SOCIAL_SETTLE_NEAR","cat","settled"));
  assertTrue(HaruProactiveSpeechEngine.advance(s,now,LifeSimulationKernel.Mode.ACTIVE));
  WorldState loaded=WorldState.fromJson(s.toJson());
  assertNotNull(loaded.haruSpeech);assertTrue(loaded.haruSpeech.pending());assertEquals("persist_evt",loaded.haruSpeech.pendingSourceEventId);
  HaruProactiveSpeechEngine.Cue c=HaruProactiveSpeechEngine.consume(loaded,now+5);assertNotNull(c);
  WorldState loaded2=WorldState.fromJson(loaded.toJson());assertEquals("persist_evt",loaded2.haruSpeech.lastSourceEventId);assertEquals(1,loaded2.haruSpeech.spokenCount);assertFalse(loaded2.haruSpeech.pending());
 }

 @Test public void severeBodyNeedLineIsGroundedInActualBodyState(){
  long now=700000L;WorldState s=state(now);s.body.pain=48;
  assertTrue(HaruProactiveSpeechEngine.advance(s,now,LifeSimulationKernel.Mode.ACTIVE));
  assertTrue(s.haruSpeech.pendingText.contains("đau"));
 }

 private static WorldState state(long now){
  WorldState s=WorldState.fresh();s.createdAt=now-100000;s.lastOpenedAt=now;s.lastSimulatedAt=now;s.haruSpeech=new HaruProactiveSpeechState();
  s.currentIntention="";s.intentionStartedAt=0;s.haruActivity="standing quietly";s.body.energy=88;s.body.sleepiness=12;s.body.pain=0;s.body.health=100;
  s.environment.weather="CLEAR";s.environment.weatherIntensity=.1;s.world=new WorldModel();s.world.areas.add(new WorldArea("lakeside","Lake","lake",0,1200,850,true,"water,wet_margin,open"));s.haruX=500;s.catX=620;s.catState.x=620;s.catState.areaId="lakeside";s.catState.awake=true;
  s.thoughts.clear();s.worldHistory.clear();return s;
 }
}
