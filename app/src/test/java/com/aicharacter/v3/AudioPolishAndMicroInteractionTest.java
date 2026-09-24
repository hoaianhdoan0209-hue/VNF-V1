package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class AudioPolishAndMicroInteractionTest {

 @Test public void authoredAreasProduceDistinctFootstepMaterials(){
  WorldState s=state(100,150,10000L);
  assertEquals(SurfaceAcoustics.Surface.WOOD,SurfaceAcoustics.surfaceAt(s,100));
  assertEquals(SurfaceAcoustics.Surface.ROOTMAT,SurfaceAcoustics.surfaceAt(s,500));
  assertEquals(SurfaceAcoustics.Surface.WET_BANK,SurfaceAcoustics.surfaceAt(s,900));
  assertEquals(SurfaceAcoustics.Surface.LEAF_FLOOR,SurfaceAcoustics.surfaceAt(s,1300));
  SurfaceAcoustics.Profile wood=ProceduralAudioEngine.surfaceProfile("WOOD"),leaf=ProceduralAudioEngine.surfaceProfile("LEAF_FLOOR");
  assertTrue(wood.lowTone>leaf.lowTone);assertTrue(leaf.noiseMix>wood.noiseMix);assertTrue(wood.damping<leaf.damping);
 }

 @Test public void shelterOccludesRainWhileOpenLakeDoesNot(){
  WorldState home=state(100,150,11000L);home.environment.weather="RAIN";home.environment.weatherIntensity=.88;home.environment.wind=.62;
  AudioSceneFrame h=AudioSceneEmitter.derive(home,0);
  assertEquals("home_shelter",h.areaId);assertEquals(.88,h.rain,.000001);assertTrue(h.weatherExposure<.05);assertEquals("WOOD",h.haruSurface);

  WorldState lake=state(900,950,11000L);lake.environment.weather="RAIN";lake.environment.weatherIntensity=.88;lake.environment.wind=.62;
  AudioSceneFrame l=AudioSceneEmitter.derive(lake,0);
  assertEquals("lakeside",l.areaId);assertTrue(l.weatherExposure>.95);assertEquals("WET_BANK",l.haruSurface);
 }

 @Test public void warmCloseCatCanPurrAndRubWithoutChangingSimulation(){
  long now=20000L;WorldState s=state(900,952,now);warm(s);s.worldHistory.add(new WorldHistoryEntry(now-100,"warm","CAT_SOCIAL_RESPONSE_COMPLETED","cat","mode=APPROACH distance=52"));
  float hx=s.haruX,cx=s.catState.x;double comfort=s.catSocial.comfort;int history=s.worldHistory.size();

  MicroInteractionDirector.Cue cue=MicroInteractionDirector.derive(s,now);
  assertTrue(cue.active);assertEquals(MicroInteractionDirector.Kind.CAT_RUB,cue.kind);
  assertEquals(CatAnimationController.State.RUB,CatAnimationController.select(s).state);
  assertEquals(GirlAnimationController.State.CROUCH,GirlAnimationController.select(s).state);
  AudioSceneFrame audio=AudioSceneEmitter.derive(s,0);
  assertTrue(audio.catPurr>.35);

  assertEquals(hx,s.haruX,.0001f);assertEquals(cx,s.catState.x,.0001f);assertEquals(comfort,s.catSocial.comfort,.000001);assertEquals(history,s.worldHistory.size());
 }

 @Test public void microInteractionTransitionsToHaruLookDownThenExpires(){
  long eventAt=30000L;WorldState s=state(900,950,eventAt);warm(s);s.worldHistory.add(new WorldHistoryEntry(eventAt,"warm2","CAT_SOCIAL_SETTLE_NEAR","cat","settled"));
  MicroInteractionDirector.Cue later=MicroInteractionDirector.derive(s,eventAt+3000L);
  assertTrue(later.active);assertEquals(MicroInteractionDirector.Kind.HARU_LOOK_DOWN,later.kind);
  assertFalse(MicroInteractionDirector.derive(s,eventAt+5000L).active);
 }

 @Test public void travelPainOrDistanceImmediatelySuppressesContactPerformance(){
  long now=40000L;WorldState s=state(900,950,now);warm(s);s.worldHistory.add(new WorldHistoryEntry(now-50,"warm3","CAT_SOCIAL_RESPONSE_COMPLETED","cat","mode=APPROACH"));
  assertTrue(MicroInteractionDirector.derive(s,now).active);
  s.girlTravel.active=true;assertFalse(MicroInteractionDirector.derive(s,now).active);
  s.girlTravel.active=false;s.body.pain=50;assertFalse(MicroInteractionDirector.derive(s,now).active);
  s.body.pain=0;s.catState.x=1100;s.catX=1100;assertFalse(MicroInteractionDirector.derive(s,now).active);assertEquals(0,AudioSceneEmitter.derive(s,0).catPurr,.000001);
 }

 @Test public void malformedSurfaceFallsBackSafely(){
  SurfaceAcoustics.Profile p=ProceduralAudioEngine.surfaceProfile("NOT_A_SURFACE");
  assertEquals(SurfaceAcoustics.Surface.SOFT_GROUND,p.surface);assertTrue(Double.isFinite(p.lowTone));assertTrue(Double.isFinite(p.noiseMix));
 }

 private static void warm(WorldState s){s.catSocial.familiarity=.84;s.catSocial.comfort=.88;s.catSocial.wariness=.06;s.catSocial.attention=.78;s.catSocial.mode="WATCH";s.catSocial.gazeTarget="girl";s.relationship.hurt=0;s.relationship.irritation=0;}
 private static WorldState state(float haru,float cat,long now){
  WorldState s=WorldState.fresh();s.world=new WorldModel();
  s.world.areas.add(new WorldArea("home_shelter","Home","home",0,300,846,false,"shelter,home,interior,dry,quiet"));
  s.world.areas.add(new WorldArea("garden_path","Garden","garden",301,700,842,true,"path,vegetation,rootmat,mist"));
  s.world.areas.add(new WorldArea("lakeside","Lake","lake",701,1100,850,true,"water,wet_margin,open"));
  s.world.areas.add(new WorldArea("quiet_grove","Grove","grove",1101,1500,844,true,"grove,veilroot,shade,vegetation"));
  s.haruX=haru;s.catX=cat;s.catState=CatState.fromJson(null,cat,0);s.catState.x=cat;s.catState.areaId=s.world.areaAt(cat)==null?"":s.world.areaAt(cat).id;s.catState.awake=true;s.catState.attachedToEntity="";s.catState.energy=88;s.catState.sleepiness=18;
  s.catSocial=new CatSocialState();s.girlTravel=new TravelState();s.catTravel=new TravelState();s.girlPhysics=PhysicsBodyState.fromJson(null,52);s.catPhysics=PhysicsBodyState.fromJson(null,4.2);s.girlPhysics.grounded=true;s.girlPhysics.falling=false;s.catPhysics.grounded=true;s.catPhysics.falling=false;
  s.body.energy=90;s.body.sleepiness=8;s.body.pain=0;s.environment=new EnvironmentState();s.environment.weather="CLEAR";s.environment.wind=.12;s.respiration=new RespirationState();s.lastOpenedAt=now;s.lastSimulatedAt=now;s.haruActivity="idle";s.currentIntention="";s.emotion.joy=0;s.emotion.fear=0;s.emotion.sadness=0;s.emotion.anger=0;s.emotion.curiosity=0;s.emotion.loneliness=0;s.emotion.calm=.7;return s;
 }

 @Test public void ambienceAndRainFollowPlayerCatAreaNotHarusArea(){
  WorldState s=state(900,100,50000L);s.environment.weather="RAIN";s.environment.weatherIntensity=.9;s.environment.wind=.6;
  AudioSceneFrame home=AudioSceneEmitter.derive(s,0);
  assertEquals("home_shelter",home.areaId);assertTrue("sheltered cat should hear muffled rain",home.weatherExposure<.5);assertEquals(0.0,home.water,.0001);
  s.catState.x=900f;s.catX=900f;
  AudioSceneFrame lake=AudioSceneEmitter.derive(s,0);
  assertEquals("lakeside",lake.areaId);assertTrue(lake.weatherExposure>.5);assertTrue(lake.water>.5);
 }

}
