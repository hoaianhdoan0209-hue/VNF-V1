package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ProceduralAudioEngineTest {

 @Test public void rainWindLakeAndBodyStateReachAudioSceneWithoutMutation(){
  WorldState s=state();s.environment.weather="RAIN";s.environment.weatherIntensity=.82;s.environment.wind=.61;s.worldWetness=.74;s.respiration.ventilationDrive=.78;s.respiration.breathingLoad=.44;s.emotion.fear=.69;s.girlTravel.active=true;s.girlTravel.lastSpeed=96;s.catTravel.active=true;s.catTravel.lastSpeed=48;s.catSocial.mode="APPROACH";s.catSocial.attention=.73;
  double wet=s.worldWetness,breath=s.respiration.ventilationDrive,fear=s.emotion.fear;
  AudioSceneFrame f=AudioSceneEmitter.derive(s,.66);
  assertEquals("lakeside",f.areaId);assertEquals("RAIN",f.weather);assertEquals(.82,f.rain,.000001);assertEquals(.61,f.wind,.000001);assertEquals(.74,f.wetness,.000001);assertEquals(1,f.water,.000001);assertTrue(f.haruWalking);assertTrue(f.catMoving);assertEquals("APPROACH",f.catMode);assertTrue(f.haruBreathing>.4);assertTrue(f.haruStress>.6);assertEquals(.66,f.divinePresence,.000001);assertTrue(f.finite());
  assertEquals(wet,s.worldWetness,.000001);assertEquals(breath,s.respiration.ventilationDrive,.000001);assertEquals(fear,s.emotion.fear,.000001);
 }

 @Test public void clearHomeSceneStaysQuietAndFinite(){
  WorldState s=state();s.haruX=100;s.catState.x=130;s.catX=130;s.environment.weather="CLEAR";s.environment.wind=Double.NaN;s.worldWetness=Double.NaN;s.respiration.ventilationDrive=Double.NaN;s.emotion.fear=Double.NaN;
  AudioSceneFrame f=AudioSceneEmitter.derive(s,Double.POSITIVE_INFINITY);
  assertTrue(f.finite());assertEquals(0,f.rain,.000001);assertEquals(0,f.wind,.000001);assertEquals(0,f.wetness,.000001);assertEquals(0,f.divinePresence,.000001);
 }

 @Test public void semanticMomentProfilesAreDistinctAndBounded(){
  ProceduralAudioEngine.CueProfile reunion=ProceduralAudioEngine.profileFor("social_reunion");
  ProceduralAudioEngine.CueProfile retreat=ProceduralAudioEngine.profileFor("social_retreat");
  ProceduralAudioEngine.CueProfile divine=ProceduralAudioEngine.profileFor("divine_presence_pulse");
  ProceduralAudioEngine.CueProfile unknown=ProceduralAudioEngine.profileFor("something_new");
  assertTrue(reunion.frequencyA>retreat.frequencyA);assertTrue(retreat.noiseMix>reunion.noiseMix);assertTrue(divine.frequencyA<reunion.frequencyA);assertTrue(reunion.gain>0&&reunion.gain<=.35);assertTrue(retreat.durationSeconds>0);assertTrue(unknown.frequencyA>0);
 }

 @Test public void quietFrameAndMalformedSoundAreAlwaysFinite(){
  AudioSceneFrame q=AudioSceneFrame.quiet();assertTrue(q.finite());
  SoundEvent e=new SoundEvent("bad","social_reunion",SoundEvent.Layer.MOMENT,Double.NaN,Double.NaN,Double.NaN,Double.POSITIVE_INFINITY,Double.NaN,true,-1);
  assertTrue(e.valid());ProceduralAudioEngine.CueProfile p=ProceduralAudioEngine.profileFor(e.semanticId);assertTrue(Double.isFinite(p.frequencyA));assertTrue(Double.isFinite(p.gain));assertTrue(Double.isFinite(p.durationSeconds));
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("home_shelter","Home","home",0,300,846,false,"home"));s.world.areas.add(new WorldArea("lakeside","Lake","lakeside",301,1000,846,true,"water"));s.haruX=520;s.catX=620;s.catState=CatState.fromJson(null,620,0);s.catState.x=620;s.catState.areaId="lakeside";s.catState.awake=true;s.girlTravel=new TravelState();s.catTravel=new TravelState();s.catSocial=new CatSocialState();s.respiration=new RespirationState();s.environment=new EnvironmentState();s.worldMinutes=13*60;s.lastOpenedAt=1000;s.lastSimulatedAt=1000;return s;
 }
}
