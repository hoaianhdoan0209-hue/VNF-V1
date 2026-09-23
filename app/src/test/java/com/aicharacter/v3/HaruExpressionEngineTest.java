package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class HaruExpressionEngineTest {

 @Test public void joyProducesSmileAndWarmCheeks(){
  WorldState s=state();s.emotion.joy=.86;EmotionEpisodeState e=episode("joyful",.82);e.joy=.90;e.relief=.25;s.emotionEpisodes.add(e);
  HaruExpressionEngine.Visual v=HaruExpressionEngine.derive(s,2200L);
  assertTrue(v.mouthCurve>.45);assertTrue(v.cheekWarmth>.35);assertTrue(v.eyeOpen>0);assertEquals("joyful",v.emotion);
 }

 @Test public void sadnessDropsHeadLooksDownAndFrowns(){
  WorldState s=state();s.emotion.sadness=.88;s.emotion.loneliness=.55;EmotionEpisodeState e=episode("sad",.84);e.sadness=.92;e.loneliness=.60;s.emotionEpisodes.add(e);
  HaruExpressionEngine.Visual v=HaruExpressionEngine.derive(s,2300L);
  assertTrue(v.mouthCurve<-.45);assertTrue(v.headDropPx>2);assertTrue(v.gazeY>.20);assertTrue(v.eyeOpen<.62);
 }

 @Test public void angerPinchesBrowsWithoutBecomingACommand(){
  WorldState s=state();s.emotion.anger=.90;EmotionEpisodeState e=episode("angry",.86);e.anger=.92;s.emotionEpisodes.add(e);String intention=s.currentIntention;
  HaruExpressionEngine.Visual v=HaruExpressionEngine.derive(s,2400L);
  assertTrue(v.browPinch>.65);assertTrue(v.mouthCurve<0);assertEquals(intention,s.currentIntention);assertEquals(1,s.emotionEpisodes.size());
 }

 @Test public void socialGazeTracksCatOnEitherSide(){
  WorldState right=state();right.haruX=500;right.catX=620;right.catState.x=620;right.catState.awake=true;right.currentIntention="find_cat";EmotionEpisodeState er=episode("relieved",.70);er.targetId="cat";er.socialRelevance=.9;er.relief=.8;right.emotionEpisodes.add(er);
  HaruExpressionEngine.Visual vr=HaruExpressionEngine.derive(right,2500L);assertTrue(vr.socialFocus);assertTrue(vr.gazeX>.30);

  WorldState left=state();left.haruX=500;left.catX=380;left.catState.x=380;left.catState.awake=true;left.currentIntention="find_cat";EmotionEpisodeState el=episode("relieved",.70);el.targetId="cat";el.socialRelevance=.9;el.relief=.8;left.emotionEpisodes.add(el);
  HaruExpressionEngine.Visual vl=HaruExpressionEngine.derive(left,2500L);assertTrue(vl.socialFocus);assertTrue(vl.gazeX<-.30);
 }

 @Test public void blinkAndExpressionUseSimulationTimeDeterministically(){
  WorldState s=state();s.createdAt=101L;s.emotion.curiosity=.7;EmotionEpisodeState e=episode("curious",.62);e.curiosity=.78;s.emotionEpisodes.add(e);
  HaruExpressionEngine.Visual a=HaruExpressionEngine.derive(s,2600L),b=HaruExpressionEngine.derive(s,2600L);
  assertEquals(a.blink,b.blink);assertEquals(a.gazeX,b.gazeX,.000001);assertEquals(a.eyeOpen,b.eyeOpen,.000001);assertEquals(a.headTiltDeg,b.headTiltDeg,.000001);
 }

 @Test public void overlayOnlySupportsStableFacePoses(){
  assertTrue(HaruExpressionEngine.supports(GirlAnimationController.State.IDLE));
  assertTrue(HaruExpressionEngine.supports(GirlAnimationController.State.REACT));
  assertTrue(HaruExpressionEngine.supports(GirlAnimationController.State.THINK));
  assertTrue(HaruExpressionEngine.supports(GirlAnimationController.State.SIT));
  assertTrue(HaruExpressionEngine.supports(GirlAnimationController.State.CROUCH));
  assertFalse(HaruExpressionEngine.supports(GirlAnimationController.State.WALK_LEFT));
  assertFalse(HaruExpressionEngine.supports(GirlAnimationController.State.SEARCH_RIGHT));
  assertFalse(HaruExpressionEngine.supports(GirlAnimationController.State.SLEEP));
 }

 private static EmotionEpisodeState episode(String p,double intensity){EmotionEpisodeState e=new EmotionEpisodeState();e.id="expr_"+p;e.primaryEmotion=p;e.intensity=intensity;e.status="ACTIVE";e.createdAt=2000L;e.updatedAt=2000L;return e;}
 private static WorldState state(){WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("test","Test","test",0,1200,0,true,"test"));s.haruX=500;s.catX=900;s.createdAt=100;s.lastOpenedAt=2000;s.lastSimulatedAt=2000;s.currentIntention="observe_lake";s.emotion.joy=0;s.emotion.fear=0;s.emotion.sadness=0;s.emotion.anger=0;s.emotion.curiosity=0;s.emotion.loneliness=0;s.emotion.calm=.6;return s;}
}
