package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class WildlifeManifestationEngineTest {
 private static final long T0=1_900_000_000_000L;

 @Test public void populationFieldsMaterializeOnlyABoundedVisibleSample(){
  WorldState s=state();
  String a=SpeciesEvolutionCatalog.all().get(5).key;
  String b=SpeciesEvolutionCatalog.all().get(6).key;
  String c=SpeciesEvolutionCatalog.all().get(7).key;
  seed(s,a,.72,.82);seed(s,b,.61,.79);seed(s,c,.54,.74);

  assertEquals(2,WildlifeManifestationEngine.sync(s,T0));
  long visible=s.world.objects.stream().filter(WildlifeManifestationEngine::isDynamic).count();
  assertEquals(WildlifeManifestationEngine.MAX_DYNAMIC_PER_AREA,visible);

  assertEquals(0,WildlifeManifestationEngine.sync(s,T0+1000));
  assertEquals(visible,s.world.objects.stream().filter(WildlifeManifestationEngine::isDynamic).count());
 }

 @Test public void lowAbundanceSpeciesStayPopulationOnly(){
  WorldState s=state();
  String species=SpeciesEvolutionCatalog.all().get(8).key;
  seed(s,species,.01,.80);
  assertEquals(0,WildlifeManifestationEngine.sync(s,T0));
  assertFalse(s.world.objects.stream().anyMatch(WildlifeManifestationEngine::isDynamic));
 }

 @Test public void visibleWildlifeCanGroundHaruSpeakingFirst(){
  WorldState s=state();
  String species=SpeciesEvolutionCatalog.all().get(9).key;
  seed(s,species,.68,.76);
  assertEquals(1,WildlifeManifestationEngine.sync(s,T0));
  assertTrue(s.worldHistory.stream().anyMatch(e->"ECOLOGY_VISIBLE_MANIFESTATION".equals(e.type)&&species.equals(e.entity)));
  assertTrue(HaruProactiveSpeechEngine.advance(s,T0+10,LifeSimulationKernel.Mode.ACTIVE));
  assertTrue(s.haruSpeech.pending());
  assertTrue(s.haruSpeech.pendingText.contains("sinh vật"));
 }

 @Test public void unfamiliarVisibleWildlifeCanBecomeAHaruOwnedGoal(){
  WorldState s=state();
  String species=SpeciesEvolutionCatalog.all().get(11).key;
  seed(s,species,.70,.82);
  WildlifeManifestationEngine.sync(s,T0);
  String id=WildlifeManifestationEngine.objectId("lakeside",species);
  WorldObject o=s.world.object(id);assertNotNull(o);
  // Keep it visible but outside passive observation range so Haru must physically approach by her own plan.
  o.x=700f;o.interactionX=700f;CreatureLifeState life=s.livingWorld.creature(id);life.x=700f;life.areaId="lakeside";life.lastUpdatedAt=T0;

  HaruAffordanceEngine.observeQuestions(s,T0+5);
  assertTrue(s.characterGod.openQuestions.values().stream().anyMatch(q->id.equals(q.aboutObjectId)));
  assertTrue(HaruAffordanceEngine.beginPlanIfCompelling(s,T0+10));
  assertEquals("WORLD_AFFORDANCE",s.planState.origin);
  assertEquals(id,s.planState.destination);
  assertEquals("affordance_inquiry",s.currentIntention);
 }

 @Test public void representativeLifeStateSurvivesDefinitionRehydration()throws Exception{
  WorldState s=state();
  String species=SpeciesEvolutionCatalog.all().get(10).key;
  seed(s,species,.66,.80);
  WildlifeManifestationEngine.sync(s,T0);
  String id=WildlifeManifestationEngine.objectId("lakeside",species);
  CreatureLifeState life=s.livingWorld.creature(id);life.hunger=.73;life.activity="forage";

  WorldState restored=WorldState.fromJson(s.toJson());
  restored.world=world();
  assertEquals(1,WildlifeManifestationEngine.sync(restored,T0+5000));
  CreatureLifeState loaded=restored.livingWorld.creatures.get(id);
  assertNotNull(loaded);assertEquals(.73,loaded.hunger,1e-12);assertEquals("forage",loaded.activity);
  assertNotNull(restored.world.object(id));
 }

 private static void seed(WorldState s,String species,double abundance,double capacity){
  SpeciesPopulationState p=s.livingWorld.population(species,"lakeside");
  p.relativeAbundance=abundance;p.carryingCapacity=capacity;p.resourcePressure=.12;p.weatherPressure=.08;p.lastUpdatedAt=T0;p.clamp();
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();s.createdAt=T0-100000;s.lastOpenedAt=T0;s.lastSimulatedAt=T0;s.world=world();
  s.haruX=400;s.catX=460;s.catState.x=460;s.catState.areaId="lakeside";s.body.energy=94;s.body.sleepiness=6;s.body.pain=0;s.body.health=100;
  s.currentIntention="";s.intentionStartedAt=0;s.haruActivity="standing quietly";s.worldHistory.clear();s.haruSpeech=new HaruProactiveSpeechState();
  return s;
 }

 private static WorldModel world(){
  WorldModel w=new WorldModel();
  WorldArea a=new WorldArea("lakeside","Lake","lake",0,900,846,true,"lumenmere,water,wet_margin,vegetation");
  a.biomeId="lumenmere_margin";w.areas.add(a);
  BiomeProfile b=new BiomeProfile();b.id="lumenmere_margin";b.label="lake";b.tags="lumenmere,water,wet_margin,vegetation";b.vegetation=b.tags;b.fauna=b.tags;b.baseMoisture=.75;b.baseTemperatureC=24;b.waterRegime="water,wet_margin";w.biomes.put(b.id,b);
  return w;
 }
}
