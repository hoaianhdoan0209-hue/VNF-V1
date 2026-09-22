package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public class BiologyEcologyRegressionTest {
 private static final long T0=1_800_000_000_000L;

 @Test public void eightHourSleepRecoversWithoutInstantReset(){
  WorldState s=state();s.haruActivity="sleeping";s.body.energy=38;s.body.sleepiness=82;double startEnergy=s.body.energy,startSleep=s.body.sleepiness;long now=T0;
  for(int i=0;i<96;i++){now+=5*60000L;stepBiology(s,5*60,now);}
  assertTrue(s.body.energy>startEnergy+18);assertTrue(s.body.sleepiness<startSleep-28);assertTrue(s.body.energy<100);assertTrue(s.body.sleepiness>=0);
 }

 @Test public void normalDayRemainsPhysiologicallyBounded(){
  WorldState s=state();s.body.energy=72;s.body.sleepiness=18;long now=T0;
  for(int i=0;i<288;i++){
   int minute=i*5;s.haruActivity=minute>=16*60?"sleeping":"walking";
   if(i%72==0)DigestionHydrationEngine.eat(s,now);
   if(i%48==0)DigestionHydrationEngine.drink(s,now);
   now+=5*60000L;stepBiology(s,5*60,now);
  }
  assertTrue(s.body.health>85);assertTrue(s.body.energy>20);assertTrue(s.hydration.hydration>.25);assertBetween(s.metabolism.availableEnergy);assertBetween(s.circulation.perfusion);
 }

 @Test public void dehydrationReducesCirculatoryAndEnergySupport(){
  WorldState wet=state(),dry=state();wet.hydration.hydration=.92;dry.hydration.hydration=.30;wet.girlTravel.active=dry.girlTravel.active=true;wet.girlTravel.lastSpeed=dry.girlTravel.lastSpeed=82;long now=T0;
  for(int i=0;i<120;i++){now+=60000L;stepBiology(wet,60,now);stepBiology(dry,60,now);}
  assertTrue(dry.circulation.perfusion<wet.circulation.perfusion);assertTrue(dry.metabolism.availableEnergy<wet.metabolism.availableEnergy);
 }

 @Test public void heatAndColdCreateDifferentThermalLoads(){
  WorldState hot=state(),cold=state();hot.atmosphere.temperatureC=42;hot.atmosphere.relativeHumidity=.82;cold.atmosphere.temperatureC=2;cold.atmosphere.relativeHumidity=.70;hot.thermal.lastUpdatedAt=cold.thermal.lastUpdatedAt=T0;long now=T0;
  for(int i=0;i<18;i++){now+=10*60000L;ThermalEngine.advance(hot,now);ThermalEngine.advance(cold,now);}
  assertTrue(hot.thermal.heatLoad>.20);assertTrue(cold.thermal.coldLoad>.20);assertTrue(BiologyVisualOutput.from(hot).thermalDiscomfort>0);assertTrue(BiologyVisualOutput.from(cold).thermalDiscomfort<0);
 }

 @Test public void exertionRaisesDemandBreathingAndFatigue(){
  WorldState s=state();s.girlTravel.active=true;s.girlTravel.lastSpeed=92;s.respiration.lastUpdatedAt=T0;s.bodyInstinct.lastUpdatedAt=T0;long now=T0;
  for(int i=0;i<30;i++){now+=60000L;stepBiology(s,60,now);}
  BiologyVisualOutput out=BiologyVisualOutput.from(s);assertTrue(s.metabolism.demand>.40);assertTrue(s.respiration.ventilationDrive>.30);assertTrue(s.musculoskeletal.legFatigue>.10);assertTrue(out.fatigue>.08);
 }

 @Test public void localizedLegPainChangesForceAndGaitOutput(){
  WorldState s=state();s.localizedPain.leftLeg=.82;s.localizedPain.rightLeg=.06;s.body.pain=82;
  for(int i=0;i<40;i++){MusculoskeletalEngine.advance(s,.25);WholeBodyMotorEngine.advance(s,.25);}
  BiologyVisualOutput out=BiologyVisualOutput.from(s);assertTrue(s.musculoskeletal.leftLegForce<s.musculoskeletal.rightLegForce);assertEquals(HumanAnatomyModel.Region.LEFT_LEG,out.dominantPainRegion);assertTrue(out.gaitChange>.25);assertTrue(Math.abs(out.gaitAsymmetry)>.4);
 }

 @Test public void populationAndResourceFieldsStayBounded(){
  WorldState s=state();long now=T0;
  for(int day=0;day<120;day++){now+=24L*3600000L;PopulationEcologyEngine.advance(s,1440,now);}
  assertFalse(s.livingWorld.populations.isEmpty());
  for(SpeciesPopulationState p:s.livingWorld.populations.values()){assertBetween(p.relativeAbundance);assertBetween(p.carryingCapacity);assertBetween(p.birthPressure);assertBetween(p.mortalityPressure);assertBetween(p.competitionPressure);assertBetween(p.migrationPressure);}
  for(BiomeLifeFieldState f:s.livingWorld.fields.values()){assertBetween(f.resourcePulse);assertBetween(f.densityVitality);}
 }

 @Test public void creatureLifeSurvivesHarshSimulationWithinBounds(){
  WorldState s=state();s.environment.weather="RAIN";s.environment.weatherIntensity=.95;s.atmosphere.temperatureC=44;long now=T0;
  for(int i=0;i<7*24;i++){now+=3600000L;LivingWorldEngine.advance(s,60,now);}
  for(CreatureLifeState c:s.livingWorld.creatures.values()){assertBetween(c.hunger);assertBetween(c.body.vitalReserve);assertBetween(c.body.fluidBalance);assertBetween(c.body.structure);assertBetween(c.body.strain);assertTrue(Double.isFinite(c.x));}
  assertBetween(s.reedling.energy);assertBetween(s.reedling.hunger);assertBetween(s.reedling.body.vitalReserve);
 }

 @Test public void activeAndOfflineKernelShareOneCausalBiology(){
  WorldState active=state(),offline=state();active.respiration.lastUpdatedAt=offline.respiration.lastUpdatedAt=T0;active.thermal.lastUpdatedAt=offline.thermal.lastUpdatedAt=T0;active.bodyInstinct.lastUpdatedAt=offline.bodyInstinct.lastUpdatedAt=T0;long now=T0;
  for(int i=0;i<90;i++){now+=60000L;LifeSimulationKernel.beginSlice(active,60,now,LifeSimulationKernel.Mode.ACTIVE);LifeSimulationKernel.endSlice(active,60,now,false,false,LifeSimulationKernel.Mode.ACTIVE);LifeSimulationKernel.beginSlice(offline,60,now,LifeSimulationKernel.Mode.OFFLINE);LifeSimulationKernel.endSlice(offline,60,now,false,false,LifeSimulationKernel.Mode.OFFLINE);}
  assertEquals(active.body.energy,offline.body.energy,1e-8);assertEquals(active.body.sleepiness,offline.body.sleepiness,1e-8);assertEquals(active.hydration.hydration,offline.hydration.hydration,1e-8);assertEquals(active.metabolism.availableEnergy,offline.metabolism.availableEnergy,1e-8);assertEquals(active.livingWorld.populations.size(),offline.livingWorld.populations.size());
  for(String k:active.livingWorld.populations.keySet())assertEquals(active.livingWorld.populations.get(k).relativeAbundance,offline.livingWorld.populations.get(k).relativeAbundance,1e-8);
 }

 private static void stepBiology(WorldState s,double seconds,long now){
  s.worldMinutes=(s.worldMinutes+seconds/60.0)%1440.0;s.environment.updateForTime(s.worldMinutes);RespirationEngine.advance(s,now);ThermalEngine.advance(s,now);BodyInstinctEngine.advance(s,now);BodyRhythmEngine.advanceSeconds(s,seconds);BiomechanicsStepEngine.advance(s,seconds,now);
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();s.createdAt=T0;s.lastOpenedAt=T0;s.lastSimulatedAt=T0;s.world=world();s.haruX=260;s.catState.x=300;s.catState.areaId="lake";s.catX=300;s.reedling.objectId="reedling_01";s.reedling.areaId="lake";s.reedling.x=360;s.environment.weather="CLEAR";s.environment.weatherIntensity=.15;s.atmosphere.temperatureC=24;s.atmosphere.relativeHumidity=.58;s.respiration.lastUpdatedAt=T0;s.thermal.lastUpdatedAt=T0;s.bodyInstinct.lastUpdatedAt=T0;return s;
 }

 private static WorldModel world(){
  WorldModel w=new WorldModel();
  BiomeProfile lakeBiome=biome("lumenmere_margin","lake,lumenmere,water,wet_margin,lam_thread,vegetation,reedling,rain_tolerant",.78,.25,24);
  BiomeProfile vergeBiome=biome("silverfold_verge","vegetation,verge,rootmat,mist,driftwing,reedling,rain_tolerant",.56,.42,23);
  BiomeProfile groveBiome=biome("veilroot_grove","grove,veilroot,vegetation,shade,echo_frond,root_husher,rain_tolerant",.64,.78,22);
  BiomeProfile homeBiome=biome("hearth_hollow","interior,dry,quiet,warm,hearth",.34,.10,24);
  w.biomes.put(lakeBiome.id,lakeBiome);w.biomes.put(vergeBiome.id,vergeBiome);w.biomes.put(groveBiome.id,groveBiome);w.biomes.put(homeBiome.id,homeBiome);
  WorldArea lake=area("lake",0,600,"lumenmere_margin","lake,lumenmere,water,wet_margin,lam_thread,vegetation");
  WorldArea verge=area("verge",600,1200,"silverfold_verge","vegetation,verge,rootmat,mist");
  WorldArea grove=area("grove",1200,1800,"veilroot_grove","grove,veilroot,vegetation,shade,echo_frond");
  WorldArea home=area("home",1800,2400,"hearth_hollow","interior,dry,quiet,warm,hearth");
  connect(lake,verge);connect(verge,grove);connect(grove,home);w.areas.add(lake);w.areas.add(verge);w.areas.add(grove);w.areas.add(home);
  w.objects.add(creature("reedling_01","lake","creature,reedling,lumenmere,vegetation","lumenmere,wet_margin,lam_thread",360));
  w.objects.add(creature("driftwing_01","verge","creature,driftwing,verge,mist,wing","verge,mist,rootmat",760));
  w.objects.add(creature("root_husher_01","grove","creature,root_husher,veilroot,shade","veilroot,shade,echo_frond",1430));
  w.objects.add(creature("ripplekin_01","lake","creature,ripplekin,lumenmere,water","lumenmere,wet_margin,water",470));
  w.objects.add(creature("hearthmote_01","home","creature,hearthmote,interior,warm,glide","interior,dry,quiet,warm",2050));
  w.objects.add(flora("flora_lam_01","lake","vegetation,living_flora,lam_thread,wet_margin,lumenmere",430));
  w.objects.add(flora("flora_mist_01","verge","vegetation,living_flora,mistleaf,rootmat",820));
  w.objects.add(flora("flora_echo_01","grove","vegetation,living_flora,echo_frond,veilroot,shade",1510));
  w.objects.add(flora("flora_hearth_01","home","vegetation,living_flora,hearth,bloom,quiet",2110));
  return w;
 }
 private static BiomeProfile biome(String id,String tags,double moisture,double canopy,double temp){BiomeProfile b=new BiomeProfile();b.id=id;b.label=id;b.tags=tags;b.vegetation=tags;b.fauna=tags;b.baseMoisture=moisture;b.canopy=canopy;b.baseTemperatureC=temp;b.waterRegime=tags;return b;}
 private static WorldArea area(String id,float left,float right,String biome,String tags){WorldArea a=new WorldArea(id,id,id,left,right,846,true,tags);a.biomeId=biome;return a;}
 private static void connect(WorldArea a,WorldArea b){a.connections.add(b.id);b.connections.add(a.id);}
 private static WorldObject creature(String id,String area,String tags,String habitat,float x){WorldObject o=new WorldObject(id,"creature",area,"",id,x,846,34,24,tags);o.habitat=habitat;o.enabled=true;return o;}
 private static WorldObject flora(String id,String area,String tags,float x){WorldObject o=new WorldObject(id,"herb",area,"",id,x,846,40,50,tags);o.habitat=tags;o.enabled=true;return o;}
 private static void assertBetween(double v){assertTrue("expected finite [0,1], got "+v,Double.isFinite(v)&&v>=0&&v<=1);}
}
