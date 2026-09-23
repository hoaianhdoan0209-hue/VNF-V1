package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class EmotionCameraDirectorTest {

 @Test public void strongStationaryEmotionMovesFromWideToClose(){
  WorldState s=state();EmotionCameraDirector d=new EmotionCameraDirector();
  EmotionCameraDirector.Frame base=d.direct(s,1000L);assertEquals(EmotionCameraDirector.Shot.WIDE,base.shot);assertEquals(1f,base.zoom,.0001f);
  s.emotion.fear=.78;EmotionEpisodeState e=episode("afraid",.78,1100L);e.fear=.82;e.threat=.80;s.emotionEpisodes.add(e);
  EmotionCameraDirector.Frame close=d.direct(s,1900L);
  assertTrue(close.shot==EmotionCameraDirector.Shot.CLOSE||close.shot==EmotionCameraDirector.Shot.INTIMATE_CLOSE);
  assertTrue(close.zoom>1.25f);assertTrue(close.focusBlend>.70f);
 }

 @Test public void socialEmotionCanUseIntimateTwoSubjectFraming(){
  WorldState s=state();s.haruX=500;s.catX=610;s.emotion.joy=.86;s.reunionContext="cat returned";
  EmotionEpisodeState e=episode("joyful",.88,2000L);e.targetId="cat";e.socialRelevance=.92;e.joy=.90;s.emotionEpisodes.add(e);
  EmotionCameraDirector d=new EmotionCameraDirector();EmotionCameraDirector.Frame f=d.direct(s,2000L);
  assertEquals(EmotionCameraDirector.Shot.INTIMATE_CLOSE,f.shot);assertTrue(f.zoom>=1.50f);
  assertTrue(f.focusX>s.haruX&&f.focusX<s.catX);assertTrue(f.focusBlend<1f);
 }

 @Test public void travelKeepsContextEvenWhenEmotionIsStrong(){
  WorldState s=state();s.girlTravel.active=true;s.emotion.fear=.82;EmotionEpisodeState e=episode("afraid",.82,3000L);e.fear=.86;e.threat=.80;s.emotionEpisodes.add(e);
  EmotionCameraDirector.Frame f=new EmotionCameraDirector().direct(s,3000L);
  assertEquals(EmotionCameraDirector.Shot.MEDIUM,f.shot);assertTrue(f.zoom<1.25f);
 }

 @Test public void downgradeWaitsForMinimumHoldInsteadOfCameraChatter(){
  WorldState s=state();s.emotion.sadness=.82;EmotionEpisodeState e=episode("sad",.84,4000L);e.sadness=.88;s.emotionEpisodes.add(e);
  EmotionCameraDirector d=new EmotionCameraDirector();EmotionCameraDirector.Frame close=d.direct(s,4000L);assertEquals(EmotionCameraDirector.Shot.INTIMATE_CLOSE,close.shot);
  s.emotion.sadness=0;s.emotionEpisodes.clear();
  EmotionCameraDirector.Frame held=d.direct(s,5000L);assertEquals(EmotionCameraDirector.Shot.INTIMATE_CLOSE,held.shot);
  EmotionCameraDirector.Frame wide=d.direct(s,8000L);assertEquals(EmotionCameraDirector.Shot.WIDE,wide.shot);
 }

 @Test public void projectionActuallyMakesHaruLargerAndReducesWorldViewport(){
  int w=1920,h=1080;float wideW=EmotionalCameraProjection.viewportW(w,h,1f),closeW=EmotionalCameraProjection.viewportW(w,h,1.56f);
  float logicalH=HaruVisualRenderer.authoredFrameHeight()*HaruVisualRenderer.fixedBodyScale();
  float wideH=EmotionalCameraProjection.projectedHeightPx(w,h,logicalH,1f),closeH=EmotionalCameraProjection.projectedHeightPx(w,h,logicalH,1.56f);
  assertTrue(closeW<wideW*.70f);assertTrue(closeH>wideH*1.50f);
  assertTrue(EmotionalCameraProjection.verticalEmotionOffset(700,780,.56f,1.56f)<0);
 }

 private static EmotionEpisodeState episode(String primary,double intensity,long now){EmotionEpisodeState e=new EmotionEpisodeState();e.id="e"+now;e.primaryEmotion=primary;e.intensity=intensity;e.status="ACTIVE";e.createdAt=now;e.updatedAt=now;return e;}
 private static WorldState state(){WorldState s=WorldState.fresh();s.haruX=500;s.catX=900;s.lastSimulatedAt=1000;s.world=new WorldModel();s.world.areas.add(new WorldArea("test","Test","test",0,1200,0,true,"test"));s.emotion.joy=0;s.emotion.fear=0;s.emotion.sadness=0;s.emotion.anger=0;s.emotion.curiosity=0;s.emotion.loneliness=0;s.emotion.calm=.65;return s;}
}
