package com.aicharacter.v3;

import org.json.JSONObject;
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

 @Test public void unsuitableAreasDoNotAllocatePopulationState(){
  WorldState s=state();PopulationEcologyEngine.advance(s,1,T0+60000L);
  SpeciesPopulationState authored=s.livingWorld.populations.get(LivingWorldState.populationKey("hearthmote","home"));
  SpeciesPopulationState impossible=s.livingWorld.populations.get(LivingWorldState.populationKey("hearthmote","lake"));
  assertNotNull(authored);assertTrue(authored.relativeAbundance>0);assertNull(impossible);
  assertTrue("current map should allocate sparse population fields, got "+s.livingWorld.populations.size(),s.livingWorld.populations.size()<SpeciesRegistryV2.BASE_SPECIES_COUNT);
  for(SpeciesPopulationState x:s.livingWorld.populations.values())assertTrue(SpeciesRegistryV2.isBaseSpecies(x.speciesKey));
 }

 @Test public void populationAndResourceFieldsStayBounded(){
  WorldState s=state();long now=T0;
  for(int day=0;day<120;day++){now+=24L*3600000L;PopulationEcologyEngine.advance(s,1440,now);}
  assertFalse(s.livingWorld.populations.isEmpty());
  for(SpeciesPopulationState p:s.livingWorld.populations.values())assertPopulationFinite(p);
  for(BiomeLifeFieldState f:s.livingWorld.fields.values()){assertBetween(f.resourcePulse);assertBetween(f.densityVitality);}
 }

 @Test public void creatureLifeSurvivesHarshSimulationWithinBounds(){
  WorldState s=state();s.environment.weather="RAIN";s.environment.weatherIntensity=.95;s.atmosphere.temperatureC=44;long now=T0;
  for(int i=0;i<7*24;i++){now+=3600000L;LivingWorldEngine.advance(s,60,now);}
  for(CreatureLifeState c:s.livingWorld.creatures.values()){assertBetween(c.hunger);assertBetween(c.body.vitalReserve);assertBetween(c.body.fluidBalance);assertBetween(c.body.structure);assertBetween(c.body.strain);assertTrue(Double.isFinite(c.x));}
  assertBetween(s.reedling.energy);assertBetween(s.reedling.hunger);assertBetween(s.reedling.body.vitalReserve);
 }

 @Test public void finiteGuardsRejectNaNAndInfinity(){
  RespirationState r=new RespirationState();r.oxygenSaturation=Double.NaN;r.breathingLoad=Double.POSITIVE_INFINITY;r.ventilationDrive=Double.NEGATIVE_INFINITY;r.lastUpdatedAt=-4;r.normalize();
  assertFiniteRange(r.oxygenSaturation,.5,1);assertBetween(r.breathingLoad);assertBetween(r.ventilationDrive);assertEquals(0,r.lastUpdatedAt);

  SpeciesPopulationState p=new SpeciesPopulationState();p.relativeAbundance=Double.NaN;p.carryingCapacity=Double.POSITIVE_INFINITY;p.birthPressure=Double.NEGATIVE_INFINITY;p.seasonalInfluence=Double.NaN;p.lastUpdatedAt=-2;p.clamp();assertPopulationFinite(p);assertEquals(0,p.lastUpdatedAt);

  WorldState s=state();s.respiration.ventilationDrive=Double.NaN;s.respiration.breathingLoad=Double.POSITIVE_INFINITY;s.metabolism.oxygenDebt=Double.NaN;s.body.energy=Double.NaN;s.body.sleepiness=Double.POSITIVE_INFINITY;s.localizedPain.leftLeg=Double.NaN;s.localizedPain.rightLeg=Double.POSITIVE_INFINITY;s.thermal.heatLoad=Double.NaN;s.thermal.coldLoad=Double.NEGATIVE_INFINITY;
  BiologyVisualOutput out=BiologyVisualOutput.from(s);assertVisualFinite(out);

  SpeciesPopulationState poison=s.livingWorld.population("reedling","lake");poison.relativeAbundance=Double.NaN;poison.carryingCapacity=Double.POSITIVE_INFINITY;poison.resourcePressure=Double.NaN;poison.weatherPressure=Double.NEGATIVE_INFINITY;poison.lastUpdatedAt=T0;s.environment.weatherIntensity=Double.POSITIVE_INFINITY;
  PopulationEcologyEngine.advance(s,60,T0+3600000L);for(SpeciesPopulationState x:s.livingWorld.populations.values())assertPopulationFinite(x);
 }

 @Test public void activeSmallSlicesAndOfflineLargeChunksRemainCausallyClose(){
  WorldState active=state(),offline=state();long activeNow=T0,offlineNow=T0;
  for(int i=0;i<60;i++){activeNow+=60000L;kernel(active,60,activeNow,LifeSimulationKernel.Mode.ACTIVE);}
  for(int i=0;i<4;i++){offlineNow+=15L*60000L;kernel(offline,15*60,offlineNow,LifeSimulationKernel.Mode.OFFLINE);}
  assertEquals(activeNow,offlineNow);assertClose(active.body.energy,offline.body.energy,.75);assertClose(active.body.sleepiness,offline.body.sleepiness,.75);assertClose(active.hydration.hydration,offline.hydration.hydration,.015);assertClose(active.metabolism.availableEnergy,offline.metabolism.availableEnergy,.08);assertClose(active.respiration.ventilationDrive,offline.respiration.ventilationDrive,.08);
  assertEquals(active.livingWorld.populations.keySet(),offline.livingWorld.populations.keySet());
  for(String k:active.livingWorld.populations.keySet()){SpeciesPopulationState a=active.livingWorld.populations.get(k),b=offline.livingWorld.populations.get(k);assertClose(a.relativeAbundance,b.relativeAbundance,.02);assertClose(a.carryingCapacity,b.carryingCapacity,.035);}
 }

 @Test public void newBiologyPopulationFieldsSurviveSaveRoundTrip() throws Exception{
  WorldState s=state();s.respiration.ventilationDrive=.641;PopulationEcologyEngine.advance(s,1,T0+60000L);SpeciesPopulationState p=s.livingWorld.population("reedling","lake");p.relativeAbundance=.337;p.carryingCapacity=.713;p.birthPressure=.221;p.seasonalInfluence=-.31;p.lastUpdatedAt=T0+60000L;
  JSONObject encoded=s.toJson();WorldState restored=WorldState.fromJson(new JSONObject(encoded.toString()));
  assertEquals(.641,restored.respiration.ventilationDrive,1e-12);SpeciesPopulationState q=restored.livingWorld.populations.get(LivingWorldState.populationKey("reedling","lake"));assertNotNull(q);assertEquals(.337,q.relativeAbundance,1e-12);assertEquals(.713,q.carryingCapacity,1e-12);assertEquals(.221,q.birthPressure,1e-12);assertEquals(-.31,q.seasonalInfluence,1e-12);assertPopulationFinite(q);
 }

 private static void kernel(WorldState s,double seconds,long now,LifeSimulationKernel.Mode mode){LifeSimulationKernel.beginSlice(s,seconds,now,mode);LifeSimulationKernel.endSlice(s,seconds,now,false,false,mode);}
 private static void stepBiology(WorldState s,double seconds,long now){s.worldMinutes=(s.worldMinutes+seconds/60.0)%1440.0;s.environment.updateForTime(s.worldMinutes);RespirationEngine.advance(s,now);ThermalEngine.advance(s,now);BodyInstinctEngine.advance(s,now);BodyRhythmEngine.advanceSeconds(s,seconds);BiomechanicsStepEngine.advance(s,seconds,now);}

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
 private static void assertBetween(double v){assertFiniteRange(v,0,1);}
 private static void assertFiniteRange(double v,double lo,double hi){assertTrue("expected finite ["+lo+","+hi+"], got "+v,Double.isFinite(v)&&v>=lo&&v<=hi);}
 private static void assertPopulationFinite(SpeciesPopulationState p){assertNotNull(p);assertBetween(p.relativeAbundance);assertBetween(p.carryingCapacity);assertBetween(p.birthPressure);assertBetween(p.recoveryPressure);assertBetween(p.mortalityPressure);assertBetween(p.competitionPressure);assertBetween(p.migrationPressure);assertBetween(p.resourcePressure);assertBetween(p.weatherPressure);assertTrue(Double.isFinite(p.seasonalInfluence)&&p.seasonalInfluence>=-1&&p.seasonalInfluence<=1);}
 private static void assertVisualFinite(BiologyVisualOutput o){assertBetween(o.breathingIntensity);assertBetween(o.postureLoad);assertBetween(o.tremor);assertBetween(o.fatigue);assertBetween(o.gaitChange);assertTrue(Double.isFinite(o.gaitAsymmetry));assertTrue(Double.isFinite(o.thermalDiscomfort));assertBetween(o.recoveryLoad);assertBetween(o.dominantPain);assertBetween(o.headPain);assertBetween(o.neckPain);assertBetween(o.chestPain);assertBetween(o.abdomenPain);assertBetween(o.leftArmPain);assertBetween(o.rightArmPain);assertBetween(o.leftLegPain);assertBetween(o.rightLegPain);}
 private static void assertClose(double a,double b,double tolerance){assertTrue("expected |"+a+"-"+b+"| <= "+tolerance,Double.isFinite(a)&&Double.isFinite(b)&&Math.abs(a-b)<=tolerance);}
}
