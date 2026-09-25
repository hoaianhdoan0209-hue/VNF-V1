package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class HaruEmotionPresentationTest {
 @Test public void strongLiveFearChangesPoseWithoutNeedingEpisodeObject(){
  WorldState s=state();
  s.emotion.fear=.72;s.emotion.anger=.08;
  s.emotionEpisodes.clear();
  GirlAnimationController.Visual v=GirlAnimationController.select(s);
  assertEquals(GirlAnimationController.State.REACT,v.state);
  assertTrue(v.reason.contains("fear"));
 }

 @Test public void liveSadnessClosesPosture(){
  WorldState s=state();
  s.emotion.sadness=.61;s.emotion.loneliness=.22;
  s.emotionEpisodes.clear();
  GirlAnimationController.Visual v=GirlAnimationController.select(s);
  assertEquals(GirlAnimationController.State.SIT,v.state);
 }

 @Test public void curiosityShowsThinkingOnlyWhenGroundedInCurrentInquiry(){
  WorldState s=state();
  s.emotion.curiosity=.78;s.currentIntention="explore_world";
  s.emotionEpisodes.clear();
  GirlAnimationController.Visual v=GirlAnimationController.select(s);
  assertEquals(GirlAnimationController.State.THINK,v.state);
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();
  s.world=new WorldModel();
  WorldArea home=new WorldArea("home_shelter","home","home",0,900,846,true,"home,shelter,quiet");
  s.world.areas.add(home);s.haruX=315;s.catX=420;s.catState.x=420;s.catState.areaId="home_shelter";
  s.environment.weather="CLEAR";s.environment.weatherIntensity=0;s.environment.wind=0;
  s.haruActivity="standing quietly";s.currentIntention="";
  s.girlTravel=new TravelState();s.girlPhysics=PhysicsBodyState.fromJson(null,50);s.catSearch=new GirlCatSearchEngine.SearchState();
  s.body.energy=90;s.body.sleepiness=10;s.body.pain=0;
  s.relationship.hurt=0;s.relationship.irritation=0;
  return s;
 }
}
