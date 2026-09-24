package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class MomentDirectorTest {
 private static final long T0=1_800_000_000_000L;

 @Test public void reunionGetsStrongCloseBeatAndSemanticSound(){
  long now=10000L;WorldState s=state();s.worldHistory.add(new WorldHistoryEntry(now-80,"evt_reunion","CAT_REUNION","cat","cat returned"));
  MomentDirector.Cue c=new MomentDirector().direct(s,now);
  assertTrue(c.active);assertEquals(MomentDirector.Kind.REUNION,c.kind);assertTrue(c.minZoom>=1.50f);assertTrue(c.focusX>s.haruX&&c.focusX<s.catState.x);
  assertNotNull(c.sound);assertEquals("social_reunion",c.sound.semanticId);assertEquals(SoundEvent.Layer.MOMENT,c.sound.layer);assertTrue(c.sound.valid());
 }

 @Test public void retreatKeepsMoreEnvironmentalContext(){
  long now=20000L;WorldState s=state();s.worldHistory.add(new WorldHistoryEntry(now-60,"evt_retreat","CAT_SOCIAL_RESPONSE_STARTED","cat","mode=RETREAT observedDistance=62"));
  MomentDirector.Cue c=new MomentDirector().direct(s,now);
  assertEquals(MomentDirector.Kind.SOCIAL_RETREAT,c.kind);assertTrue(c.minZoom<1.30f);assertTrue(c.focusBlend<.70f);assertEquals("social_retreat",c.sound.semanticId);
 }

 @Test public void insightIsQuietNotIntimate(){
  long now=30000L;WorldState s=state();s.worldHistory.add(new WorldHistoryEntry(now-40,"evt_insight","HARU_CAUSAL_EXPERIMENT_RESOLVED","girl","favoredCause=blocked_route"));
  MomentDirector.Cue c=new MomentDirector().direct(s,now);
  assertEquals(MomentDirector.Kind.INSIGHT,c.kind);assertTrue(c.minZoom>=1.15f&&c.minZoom<1.30f);assertTrue(c.visualIntensity<.40f);assertEquals("insight_soft",c.sound.semanticId);
 }

 @Test public void staleEventDoesNotCreateMoment(){
  long now=50000L;WorldState s=state();s.worldHistory.add(new WorldHistoryEntry(now-12000,"evt_old","CAT_REUNION","cat","old reunion"));
  assertFalse(new MomentDirector().direct(s,now).active);
 }

 @Test public void consumedMomentDoesNotBacktrackIntoOlderEvents(){
  long now=60000L;WorldState s=state();
  s.worldHistory.add(new WorldHistoryEntry(now-1000,"evt_old_approach","CAT_SOCIAL_RESPONSE_STARTED","cat","mode=APPROACH"));
  s.worldHistory.add(new WorldHistoryEntry(now-50,"evt_new_reunion","CAT_REUNION","cat","returned"));
  MomentDirector d=new MomentDirector();MomentDirector.Cue first=d.direct(s,now);assertEquals(MomentDirector.Kind.REUNION,first.kind);
  MomentDirector.Cue after=d.direct(s,first.endsAt+1);assertFalse(after.active);
 }

 @Test public void newerEventCanFollowAfterPreviousMomentEnds(){
  long now=70000L;WorldState s=state();MomentDirector d=new MomentDirector();
  s.worldHistory.add(new WorldHistoryEntry(now-20,"evt1","CAT_SOCIAL_SETTLE_NEAR","cat","settled"));
  MomentDirector.Cue a=d.direct(s,now);assertTrue(a.active);
  long next=a.endsAt+5;s.worldHistory.add(new WorldHistoryEntry(next-2,"evt2","HARU_CAUSAL_EXPERIMENT_RESOLVED","girl","resolved"));
  MomentDirector.Cue b=d.direct(s,next);assertTrue(b.active);assertEquals(MomentDirector.Kind.INSIGHT,b.kind);
 }

 @Test public void directorNeverMutatesSimulationState(){
  long now=80000L;WorldState s=state();s.emotion.joy=.41;float hx=s.haruX,cx=s.catState.x;int history=1;s.worldHistory.add(new WorldHistoryEntry(now-10,"evt","CAT_SOCIAL_SETTLE_NEAR","cat","settled"));history=s.worldHistory.size();
  new MomentDirector().direct(s,now);
  assertEquals(hx,s.haruX,.0001f);assertEquals(cx,s.catState.x,.0001f);assertEquals(.41,s.emotion.joy,.000001);assertEquals(history,s.worldHistory.size());assertFalse(s.girlTravel.active);assertFalse(s.catTravel.active);
 }

 @Test public void soundEventContainsFiniteBoundedContract(){
  SoundEvent e=new SoundEvent(null,"test",SoundEvent.Layer.FOLEY,Double.NaN,Double.POSITIVE_INFINITY,9,-3,99,true,-4);
  assertTrue(e.valid());assertEquals(0,e.intensity,.000001);assertEquals(0,e.spatialX,.000001);assertEquals(1,e.panHint,.000001);assertEquals(.5,e.pitchHint,.000001);assertEquals(30,e.durationSeconds,.000001);assertEquals(0,e.createdAt);
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("test","Test","test",0,1000,846,true,"test"));s.haruX=430;s.catX=560;s.catState=CatState.fromJson(null,560,0);s.catState.x=560;s.catState.areaId="test";s.catState.awake=true;s.catState.attachedToEntity="";s.girlTravel=new TravelState();s.catTravel=new TravelState();s.lastOpenedAt=1;s.lastSimulatedAt=1;return s;
 }

 @Test public void retreatCanActuallyWidenACompetingCloseEmotionFrame(){
  WorldState s=state();long now=T0+5000;
  s.emotion.fear=.92;
  EmotionCameraDirector emotionDirector=new EmotionCameraDirector();
  EmotionCameraDirector.Frame emotional=emotionDirector.direct(s,now);
  s.worldHistory.add(new WorldHistoryEntry(now-50,"retreat_real","CAT_SOCIAL_RESPONSE_STARTED","cat","mode=RETREAT distance=220"));
  MomentDirector.Cue retreat=new MomentDirector().direct(s,now);
  assertEquals(MomentDirector.Kind.SOCIAL_RETREAT,retreat.kind);
  float composed=CameraCompositionPolicy.targetZoom(emotional,retreat);
  assertTrue("retreat must cap close emotional framing",composed<=retreat.minZoom+.0001f);
  assertTrue(composed<=emotional.zoom+.0001f);
 }

}
