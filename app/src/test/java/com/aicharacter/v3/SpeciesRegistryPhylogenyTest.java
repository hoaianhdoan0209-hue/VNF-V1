package com.aicharacter.v3;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public final class SpeciesRegistryPhylogenyTest {

 @Test public void registryContainsExactlyFiveHundredDistinctBaseSpecies(){
  assertEquals(500,SpeciesRegistryV2.size());
  assertEquals(500,FantasyEcologyDictionary.all().size());
  assertEquals(500,new LinkedHashSet<>(SpeciesRegistryV2.keys()).size());
  assertTrue(SpeciesRegistryV2.isBaseSpecies("reedling"));
  assertTrue(SpeciesRegistryV2.isBaseSpecies("vns_500"));
 }

 @Test public void everyBaseSpeciesHasAUniqueBiologicalFingerprint(){
  LinkedHashSet<String> fingerprints=new LinkedHashSet<>();
  for(SpeciesDefinition d:SpeciesRegistryV2.all()){
   assertNotNull(d);assertFalse(d.key.isEmpty());assertFalse(d.bodyPlan.isEmpty());assertFalse(d.locomotion.isEmpty());assertFalse(d.metabolism.isEmpty());assertFalse(d.sensorySignature.isEmpty());assertFalse(d.trophicNiche.isEmpty());assertFalse(d.reproduction.isEmpty());assertFalse(d.sociality.isEmpty());assertFalse(d.cognitionPotential.isEmpty());assertFalse(d.communication.isEmpty());
   assertTrue("duplicate biological fingerprint for "+d.key,fingerprints.add(d.traitFingerprint));
  }
  assertEquals(500,fingerprints.size());
 }

 @Test public void legacyVisibleSpeciesKeepDistinctRichIdentities(){
  SpeciesDefinition reed=SpeciesRegistryV2.get("reedling"),wing=SpeciesRegistryV2.get("driftwing"),root=SpeciesRegistryV2.get("root_husher"),ripple=SpeciesRegistryV2.get("ripplekin"),hearth=SpeciesRegistryV2.get("hearthmote");
  assertNotNull(reed);assertNotNull(wing);assertNotNull(root);assertNotNull(ripple);assertNotNull(hearth);
  assertTrue(reed.localPresenceEligible);assertTrue(wing.localPresenceEligible);assertTrue(root.localPresenceEligible);assertTrue(ripple.localPresenceEligible);assertTrue(hearth.localPresenceEligible);
  assertNotEquals(reed.traitFingerprint,wing.traitFingerprint);assertNotEquals(wing.traitFingerprint,root.traitFingerprint);assertNotEquals(root.traitFingerprint,ripple.traitFingerprint);assertNotEquals(ripple.traitFingerprint,hearth.traitFingerprint);
 }

 @Test public void everyLivingSpeciesDescendsFromOnePrimordialAncestor(){
  assertEquals(500,DivinePhylogenyTruth.extantSpeciesCount());
  assertTrue(DivinePhylogenyTruth.extinctLineageCount()>=120);
  assertEquals(DivinePhylogenyTruth.ROOT_ID,DivinePhylogenyTruth.root().nodeId);
  for(SpeciesDefinition d:SpeciesRegistryV2.all()){
   EvolutionaryLineageNode leaf=DivinePhylogenyTruth.lineageOf(d.key);
   assertNotNull("missing lineage for "+d.key,leaf);assertTrue(leaf.extantLeaf());assertEquals(d.key,leaf.extantSpeciesKey);assertTrue("lineage does not reach root for "+d.key,DivinePhylogenyTruth.reachesRoot(d.key));
   java.util.List<EvolutionaryLineageNode> ancestry=DivinePhylogenyTruth.ancestry(d.key,64);
   assertFalse(ancestry.isEmpty());assertEquals(DivinePhylogenyTruth.ROOT_ID,ancestry.get(ancestry.size()-1).nodeId);
  }
 }

 @Test public void distantSpeciesStillHaveARealCommonAncestor(){
  EvolutionaryLineageNode a=DivinePhylogenyTruth.commonAncestor("reedling","vns_500");
  EvolutionaryLineageNode b=DivinePhylogenyTruth.commonAncestor("vns_006","vns_499");
  assertNotNull(a);assertNotNull(b);
  assertTrue(DivinePhylogenyTruth.ROOT_ID.equals(a.nodeId)||!a.parentId.isEmpty());
  assertTrue(DivinePhylogenyTruth.ROOT_ID.equals(b.nodeId)||!b.parentId.isEmpty());
 }

 @Test public void authoredPhylogenyIsWorldTruthNotPlayerSave()throws Exception{
  WorldState s=WorldState.fresh();String json=s.toJson().toString();
  assertFalse(json.contains("primordial_life_0"));
  assertFalse(json.contains("DivinePhylogenyTruth"));
  assertFalse(json.contains("lineage_vns_500"));
  assertFalse(s.knowledge.containsKey("primordial_life_0"));
  assertFalse(s.beliefs.containsKey("all_life_common_ancestor"));
 }

 @Test public void sparsePopulationDoesNotMaterializeAllFiveHundredSpeciesPerArea(){
  WorldState s=new WorldState();s.livingWorld=new LivingWorldState();s.environment=new EnvironmentState();s.atmosphere=new AtmosphereState();s.world=new WorldModel();
  BiomeProfile b=new BiomeProfile();b.id="lumenmere_margin";b.tags="lake,lumenmere,water,wet_margin,lam_thread,vegetation,reedling,rain_tolerant";b.vegetation=b.tags;b.fauna=b.tags;b.waterRegime="lumenmere";b.baseMoisture=.8;b.baseTemperatureC=24;s.world.biomes.put(b.id,b);
  WorldArea a=new WorldArea("lake","Lake","lake",0,800,846,true,"lake,lumenmere,water,wet_margin,lam_thread,vegetation");a.biomeId=b.id;s.world.areas.add(a);
  WorldObject reed=new WorldObject("reedling_01","creature","lake","","reedling",300,846,30,20,"creature,reedling,lumenmere,wet_margin");reed.habitat="lumenmere,wet_margin,lam_thread";reed.enabled=true;s.world.objects.add(reed);
  s.haruX=100;s.catX=120;s.catState=CatState.fromJson(null,120,1000L);s.catState.areaId="lake";s.lastSimulatedAt=1000L;
  PopulationEcologyEngine.advance(s,1,61000L);
  assertNotNull(PopulationEcologyEngine.state(s,"reedling","lake"));
  assertTrue(s.livingWorld.populations.size()<100);
  assertNull(PopulationEcologyEngine.state(s,"vns_500","lake"));
 }
}
