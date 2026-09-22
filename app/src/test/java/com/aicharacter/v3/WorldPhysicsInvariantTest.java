package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class WorldPhysicsInvariantTest {
 private static final long T0=1700000000000L;

 private static WorldState flat(){
  WorldState s=WorldState.fresh();
  s.world=new WorldModel();
  BiomeProfile b=new BiomeProfile();
  b.id="test";b.label="test";b.baseMoisture=.48;b.baseTemperatureC=24;b.waterRegime="dry";
  s.world.biomes.put(b.id,b);
  WorldArea a=new WorldArea("field","field","field",0,1200,846,true,"plain");
  a.biomeId=b.id;
  s.world.areas.add(a);
  s.haruX=240;s.catX=160;s.catState.x=160;s.catState.areaId=a.id;
  s.haruActivity="idle";
  s.environment.weather="CLEAR";s.environment.cloudCover=.15;s.environment.wind=.10;s.environment.weatherIntensity=.10;
  s.environment.weatherSince=T0;s.environment.lastAtmosphereUpdateAt=T0;
  s.lastSimulatedAt=T0;s.lastOpenedAt=T0;s.lastSavedAt=T0;s.worldMinutes=12*60;
  return s;
 }

 @Test public void unsupportedFallUsesGravity(){
  WorldState s=flat();
  WholeBodyPhysicsEngine.loseGroundSupport(s,"girl",1.0,0,T0);
  WholeBodyPhysicsEngine.prepare(s,"girl",.10,T0+100);
  assertFalse(s.girlPhysics.grounded);
  assertTrue(s.girlPhysics.falling);
  assertEquals(.981,s.girlPhysics.velocityY,.015);
  assertTrue(s.girlPhysics.groundClearanceM<1.0);
 }

 @Test public void groundLandingRestoresContact(){
  WorldState s=flat();
  s.girlPhysics.grounded=false;s.girlPhysics.falling=true;s.girlPhysics.groundClearanceM=.03;s.girlPhysics.velocityY=2.0;
  WholeBodyPhysicsEngine.prepare(s,"girl",.05,T0+50);
  assertTrue(s.girlPhysics.grounded);
  assertFalse(s.girlPhysics.falling);
  assertEquals(0,s.girlPhysics.groundClearanceM,1e-9);
  assertTrue(s.girlPhysics.groundReaction>0);
 }

 @Test public void jumpRequiresGroundSupport(){
  WorldState s=flat();
  assertTrue(WholeBodyPhysicsEngine.beginJump(s,"girl",3.0,T0));
  assertFalse(s.girlPhysics.grounded);
  assertFalse(s.girlPhysics.falling);
  double before=s.girlPhysics.velocityY;
  WholeBodyPhysicsEngine.prepare(s,"girl",.10,T0+100);
  assertTrue(s.girlPhysics.velocityY>before);
  assertTrue(s.girlPhysics.groundClearanceM>0);
  assertFalse(WholeBodyPhysicsEngine.beginJump(s,"girl",3.0,T0+101));
 }

 @Test public void collisionStopsAtSolidWorldGeometry(){
  WorldState s=flat();
  WorldObject wall=new WorldObject("wall","prop","field","","",300,846,40,100,"solid");
  wall.collision=true;s.world.objects.add(wall);
  double limited=WholeBodyPhysicsEngine.limitHorizontalMove(s,"girl",200,400);
  assertTrue(limited<300);
  assertTrue(WholeBodyPhysicsEngine.blockedBetween(s,"girl",200,400));
 }

 @Test public void slopeAndWetnessReduceTraction(){
  WorldState s=flat();
  s.world.areas.clear();
  WorldArea low=new WorldArea("low","low","low",0,200,800,true,"plain");
  WorldArea high=new WorldArea("high","high","high",200,400,900,true,"water,wet_margin");
  low.biomeId="test";high.biomeId="test";s.world.areas.add(low);s.world.areas.add(high);s.haruX=200;
  assertTrue(Math.abs(GroundGeometry.slope(s,200))>.01);
  s.worldWetness=0;WholeBodyPhysicsEngine.prepare(s,"girl",.02,T0+20);double dry=s.girlPhysics.traction;
  s.worldWetness=.9;WholeBodyPhysicsEngine.prepare(s,"girl",.02,T0+40);
  assertTrue(s.girlPhysics.traction<dry);
 }

 @Test public void cameraHorizontalFollowIsSmoothedAndBounded(){
  WorldState s=flat();
  CatCameraDirector.resetForTest();
  CatCameraDirector.Frame first=CatCameraDirector.direct(s);
  s.haruX=900;s.catX=820;s.catState.x=820;
  CatCameraDirector.Frame second=CatCameraDirector.direct(s);
  assertTrue(second.lookX>first.lookX);
  assertTrue(second.lookX>=s.haruX-220f);
  assertTrue(second.lookX<=s.haruX+220f);
 }

 @Test public void atmosphereBoundsAndDensityStayPhysical(){
  AtmosphereState a=new AtmosphereState();
  a.oxygenFraction=.40;a.carbonDioxideFraction=.40;a.pressureKPa=10;a.temperatureC=100;a.relativeHumidity=2;a.airQuality=-2;
  a.syncDerived();
  assertTrue(a.oxygenFraction>=.15&&a.oxygenFraction<=.24);
  assertTrue(a.carbonDioxideFraction>=0&&a.carbonDioxideFraction<=.02);
  assertEquals(1.0,a.oxygenFraction+a.inertGasFraction+a.carbonDioxideFraction,1e-9);
  assertTrue(a.pressureKPa>=55&&a.pressureKPa<=115);
  assertTrue(a.relativeHumidity>=0&&a.relativeHumidity<=1);
  assertTrue(a.airDensityKgM3>=.55&&a.airDensityKgM3<=1.60);
 }

 @Test public void activeOfflineKernelParity(){
  WorldState active=flat(),offline=flat();
  long end=T0+60000L;
  LifeSimulationKernel.beginSlice(active,60,end,LifeSimulationKernel.Mode.ACTIVE);
  LifeSimulationKernel.endSlice(active,60,end,false,false,LifeSimulationKernel.Mode.ACTIVE);
  LifeSimulationKernel.beginSlice(offline,60,end,LifeSimulationKernel.Mode.OFFLINE);
  LifeSimulationKernel.endSlice(offline,60,end,false,false,LifeSimulationKernel.Mode.OFFLINE);
  assertEquals(active.atmosphere.temperatureC,offline.atmosphere.temperatureC,1e-9);
  assertEquals(active.atmosphere.relativeHumidity,offline.atmosphere.relativeHumidity,1e-9);
  assertEquals(active.environment.cloudCover,offline.environment.cloudCover,1e-9);
  assertEquals(active.girlPhysics.grounded,offline.girlPhysics.grounded);
  assertEquals(active.worldWetness,offline.worldWetness,1e-9);
 }

 @Test public void atmosphereSameTimestampIsIdempotent(){
  WorldState s=flat();
  long end=T0+60000L;
  AtmosphereEvolutionEngine.advance(s,end);
  double t=s.atmosphere.temperatureC,h=s.atmosphere.relativeHumidity,p=s.atmosphere.pressureKPa,c=s.environment.cloudCover,w=s.environment.wind;
  AtmosphereEvolutionEngine.advance(s,end);
  assertEquals(t,s.atmosphere.temperatureC,1e-12);
  assertEquals(h,s.atmosphere.relativeHumidity,1e-12);
  assertEquals(p,s.atmosphere.pressureKPa,1e-12);
  assertEquals(c,s.environment.cloudCover,1e-12);
  assertEquals(w,s.environment.wind,1e-12);
 }
}
