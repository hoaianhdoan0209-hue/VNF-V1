package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class EmotionalVisualExpressionTest {

 @Test public void fearUsesReactionVisualWhenStationary(){
  WorldState s=state();EmotionEpisodeState e=episode("afraid",.72);e.fear=.82;s.emotionEpisodes.add(e);
  GirlAnimationController.Visual v=GirlAnimationController.select(s);
  assertEquals(GirlAnimationController.State.REACT,v.state);assertTrue(v.reason.contains("afraid"));
 }

 @Test public void sadnessUsesSettledPostureWhenStationary(){
  WorldState s=state();EmotionEpisodeState e=episode("sad",.66);e.sadness=.78;s.emotionEpisodes.add(e);
  GirlAnimationController.Visual v=GirlAnimationController.select(s);
  assertEquals(GirlAnimationController.State.SIT,v.state);assertTrue(v.reason.contains("sad"));
 }

 @Test public void curiosityUsesThinkVisual(){
  WorldState s=state();EmotionEpisodeState e=episode("curious",.64);e.curiosity=.82;s.emotionEpisodes.add(e);
  GirlAnimationController.Visual v=GirlAnimationController.select(s);
  assertEquals(GirlAnimationController.State.THINK,v.state);
 }

 @Test public void physicalTravelStillOverridesEmotionPresentation(){
  WorldState s=state();EmotionEpisodeState e=episode("sad",.90);e.sadness=.92;s.emotionEpisodes.add(e);s.girlTravel.active=true;s.girlTravel.segmentEndX=s.haruX+100;s.girlTravel.lastSpeed=40;
  GirlAnimationController.Visual v=GirlAnimationController.select(s);
  assertTrue(v.state==GirlAnimationController.State.WALK_RIGHT||v.state==GirlAnimationController.State.WALK_LEFT);
 }

 private static EmotionEpisodeState episode(String p,double intensity){EmotionEpisodeState e=new EmotionEpisodeState();e.id="visual_"+p;e.primaryEmotion=p;e.intensity=intensity;e.status="ACTIVE";e.createdAt=1000;e.updatedAt=1000;return e;}
 private static WorldState state(){WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("test","Test","test",0,1200,0,true,"test"));s.haruX=500;s.catX=900;s.lastOpenedAt=1000;s.lastSimulatedAt=1000;s.haruActivity="idle";s.body.energy=90;s.body.sleepiness=10;s.body.pain=0;s.environment.weather="CLEAR";s.environment.wind=0;s.girlTravel.active=false;return s;}
}
