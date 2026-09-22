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

 private static WorldState twoBiome(){
  WorldState s=flat();
  s.world=new WorldModel();
  BiomeProfile warm=new BiomeProfile();warm.id="warm";warm.baseTemperatureC=29;warm.baseMoisture=.35;
  BiomeProfile cool=new BiomeProfile();cool.id="cool";cool.baseTemperatureC=18;cool.baseMoisture=.78;
  s.world.biomes.put(warm.id,warm);s.world.biomes.put(cool.id,cool);
  WorldArea a=new WorldArea("warm_area","warm","warm",0,600,846,true,"dry");
  WorldArea b=new WorldArea("cool_area","cool","cool",600,1200,846,true,"water,wet_margin");
  a.biomeId=warm.id;b.biomeId=cool.id;a.elevationM=20;b.elevationM=220;
  s.world.areas.add(a);s.world.areas.add(b);
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

 @Test public void atmosphereAndPhysicsRepairNaNInfinity(){
  WorldState s=flat();
  s.atmosphere.oxygenFraction=Double.NaN;
  s.atmosphere.carbonDioxideFraction=Double.POSITIVE_INFINITY;
  s.atmosphere.pressureKPa=Double.NEGATIVE_INFINITY;
  s.atmosphere.temperatureC=Double.NaN;
  s.atmosphere.relativeHumidity=Double.POSITIVE_INFINITY;
  s.atmosphere.airQuality=Double.NaN;
  s.atmosphere.airDensityKgM3=Double.NaN;
  s.girlPhysics.massKg=Double.NaN;
  s.girlPhysics.velocityX=Double.POSITIVE_INFINITY;
  s.girlPhysics.velocityY=Double.NaN;
  s.girlPhysics.centerOfMassX=Double.NaN;
  s.girlPhysics.centerOfMassY=Double.NEGATIVE_INFINITY;
  s.girlPhysics.groundClearanceM=Double.NaN;
  s.girlPhysics.groundReaction=Double.POSITIVE_INFINITY;
  s.girlPhysics.traction=Double.NaN;
  s.girlPhysics.balance=Double.POSITIVE_INFINITY;
  s.girlPhysics.supportLeft=Double.NaN;
  s.girlPhysics.supportRight=Double.NEGATIVE_INFINITY;
  s.girlPhysics.slipVelocity=Double.NaN;
  s.girlPhysics.slipSeverity=Double.POSITIVE_INFINITY;
  s.girlPhysics.fallBodyHeightM=Double.NaN;
  StateInvariantChecker.normalize(s,T0);
  assertFiniteAtmosphere(s.atmosphere);
  assertFinitePhysics(s.girlPhysics);
  WholeBodyPhysicsEngine.prepare(s,"girl",.1,T0+100);
  assertFinitePhysics(s.girlPhysics);
 }

 @Test public void globalAtmosphereDoesNotFollowHaruBiome(){
  WorldState left=twoBiome(),right=twoBiome();
  left.haruX=100;right.haruX=1000;
  long end=T0+10L*60000L;
  LifeSimulationKernel.syncClock(left,end);LifeSimulationKernel.syncClock(right,end);
  AtmosphereEvolutionEngine.advance(left,end);AtmosphereEvolutionEngine.advance(right,end);
  assertEquals(left.atmosphere.temperatureC,right.atmosphere.temperatureC,1e-12);
  assertEquals(left.atmosphere.relativeHumidity,right.atmosphere.relativeHumidity,1e-12);
  assertEquals(left.atmosphere.pressureKPa,right.atmosphere.pressureKPa,1e-12);
  assertEquals(left.environment.cloudCover,right.environment.cloudCover,1e-12);
  assertNotEquals(EcologyEngine.localTemperatureC(left,left.world.area("warm_area")),
                  EcologyEngine.localTemperatureC(left,left.world.area("cool_area")),1e-6);
 }

 @Test public void activeOfflinePartitionParityWithWeatherWetnessAndFall(){
  WorldState active=flat(),offline=flat();
  configureRainAndFall(active);configureRainAndFall(offline);

  for(int i=1;i<=12;i++){
   long now=T0+i*5000L;
   LifeSimulationKernel.beginSlice(active,5,now,LifeSimulationKernel.Mode.ACTIVE);
   LifeSimulationKernel.endSlice(active,5,now,false,false,LifeSimulationKernel.Mode.ACTIVE);
  }
  for(int i=1;i<=3;i++){
   long now=T0+i*20000L;
   LifeSimulationKernel.beginSlice(offline,20,now,LifeSimulationKernel.Mode.OFFLINE);
   LifeSimulationKernel.endSlice(offline,20,now,false,false,LifeSimulationKernel.Mode.OFFLINE);
  }

  assertEquals(active.atmosphere.temperatureC,offline.atmosphere.temperatureC,.12);
  assertEquals(active.atmosphere.relativeHumidity,offline.atmosphere.relativeHumidity,.025);
  assertEquals(active.atmosphere.pressureKPa,offline.atmosphere.pressureKPa,.02);
  assertEquals(active.environment.cloudCover,offline.environment.cloudCover,.035);
  assertEquals(active.environment.wind,offline.environment.wind,.035);
  assertEquals(active.worldWetness,offline.worldWetness,.035);
  assertEquals(active.girlPhysics.grounded,offline.girlPhysics.grounded);
  assertEquals(active.girlPhysics.groundClearanceM,offline.girlPhysics.groundClearanceM,.01);
  assertEquals(active.girlPhysics.velocityY,offline.girlPhysics.velocityY,.02);
  assertTrue(active.worldWetness>.20);
  assertTrue(active.girlPhysics.grounded);
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

 private static void configureRainAndFall(WorldState s){
  s.environment.weather="RAIN";s.environment.weatherIntensity=.85;s.environment.cloudCover=.92;s.environment.wind=.25;s.environment.weatherSince=T0;s.environment.lastAtmosphereUpdateAt=T0;
  s.atmosphere.relativeHumidity=.88;s.atmosphere.temperatureC=22.5;s.atmosphere.syncDerived();
  s.worldWetness=.20;
  WholeBodyPhysicsEngine.loseGroundSupport(s,"girl",.65,.18,T0);
 }

 private static void assertFiniteAtmosphere(AtmosphereState a){
  assertTrue(Double.isFinite(a.oxygenFraction));assertTrue(Double.isFinite(a.inertGasFraction));assertTrue(Double.isFinite(a.carbonDioxideFraction));
  assertTrue(Double.isFinite(a.pressureKPa));assertTrue(Double.isFinite(a.temperatureC));assertTrue(Double.isFinite(a.relativeHumidity));
  assertTrue(Double.isFinite(a.airQuality));assertTrue(Double.isFinite(a.airDensityKgM3));
 }

 private static void assertFinitePhysics(PhysicsBodyState p){
  assertTrue(Double.isFinite(p.massKg));assertTrue(Double.isFinite(p.velocityX));assertTrue(Double.isFinite(p.velocityY));
  assertTrue(Double.isFinite(p.centerOfMassX));assertTrue(Double.isFinite(p.centerOfMassY));assertTrue(Double.isFinite(p.groundClearanceM));
  assertTrue(Double.isFinite(p.groundReaction));assertTrue(Double.isFinite(p.traction));assertTrue(Double.isFinite(p.balance));
  assertTrue(Double.isFinite(p.supportLeft));assertTrue(Double.isFinite(p.supportRight));assertTrue(Double.isFinite(p.landingInstability));
  assertTrue(Double.isFinite(p.slipVelocity));assertTrue(Double.isFinite(p.slipSeverity));assertTrue(Double.isFinite(p.fallBodyHeightM));
 }
}
