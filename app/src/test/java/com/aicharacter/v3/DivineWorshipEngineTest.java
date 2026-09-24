package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public class DivineWorshipEngineTest {
 @Test public void offeringEligibilityIsTraitDrivenAndOnlySomeSpeciesQualify(){
  int eligible=0;for(SpeciesEvolutionCatalog.Species s:SpeciesEvolutionCatalog.all())if(DivineWorshipEngine.eligibleForOffering(s.key))eligible++;
  assertTrue("expected some offering-capable species",eligible>0);
  assertTrue("offering must not be universal",eligible<SpeciesEvolutionCatalog.BASE_SPECIES_COUNT);
 }

 @Test public void childGodChoosesFollowerFromLivePopulationWithoutPreselectedSpecies(){
  WorldState s=WorldState.fresh();s.livingWorld=new LivingWorldState();
  SpeciesPopulationState a=s.livingWorld.population("reedling","lake");a.relativeAbundance=.32;a.carryingCapacity=.55;a.recoveryPressure=.60;a.resourcePressure=.12;a.weatherPressure=.08;
  SpeciesPopulationState b=s.livingWorld.population("driftwing","verge");b.relativeAbundance=.48;b.carryingCapacity=.62;b.recoveryPressure=.66;b.resourcePressure=.10;b.weatherPressure=.05;
  long now=System.currentTimeMillis();DivineWorshipEngine.advance(s,60,now);
  assertNotNull(s.divineEcology);assertEquals(1,s.divineEcology.minorGods.size());assertEquals(1,s.divineEcology.bonds.size());
  WorshipBondState chosen=s.divineEcology.bonds.values().iterator().next();
  assertTrue(SpeciesEvolutionCatalog.isBaseSpecies(chosen.speciesKey));
  assertTrue("follower must come from a population that actually exists",chosen.speciesKey.equals("reedling")||chosen.speciesKey.equals("driftwing"));
 }

 @Test public void offeringConsumesPopulationAndBuildsGrace(){
  String key=null;for(SpeciesEvolutionCatalog.Species sp:SpeciesEvolutionCatalog.all())if(DivineWorshipEngine.eligibleForOffering(sp.key)){key=sp.key;break;}assertNotNull(key);
  WorldState s=WorldState.fresh();s.livingWorld=new LivingWorldState();s.divineEcology=new DivineEcologyState();
  SpeciesPopulationState p=s.livingWorld.population(key,"lake");p.relativeAbundance=.60;p.carryingCapacity=.72;p.recoveryPressure=.70;p.resourcePressure=.10;p.mortalityPressure=.10;
  MinorGodState g=new MinorGodState();g.id="minor_god_01";g.graceReserve=.10;g.followersChosen=4;g.lastSelectionAt=System.currentTimeMillis();s.divineEcology.minorGods.put(g.id,g);
  WorshipBondState bond=s.divineEcology.bond(g.id,key,"lake");bond.selectedAt=1;bond.devotion=.92;bond.offeringAffinity=DivineWorshipEngine.offeringPotential(key);bond.lastOfferingAt=0;
  double beforePopulation=p.relativeAbundance,beforeGrace=g.graceReserve;long now=System.currentTimeMillis();DivineWorshipEngine.advance(s,30,now);
  assertTrue(p.relativeAbundance<beforePopulation);assertTrue(g.graceReserve>beforeGrace);assertEquals(1,bond.totalOfferings);assertTrue(bond.cumulativePopulationCost>0);
 }

 @Test public void divineStatePersistsAcrossSaveJson() throws Exception {
  WorldState s=WorldState.fresh();s.divineEcology=new DivineEcologyState();MinorGodState g=new MinorGodState();g.id="minor_god_01";g.followersChosen=2;g.totalOfferings=1;s.divineEcology.minorGods.put(g.id,g);
  WorshipBondState b=s.divineEcology.bond(g.id,"reedling","lake");b.devotion=.71;b.blessingPower=.33;b.totalOfferings=1;
  WorldState restored=WorldState.fromJson(s.toJson());
  assertEquals(1,restored.divineEcology.minorGods.size());assertEquals(1,restored.divineEcology.bonds.size());WorshipBondState rb=restored.divineEcology.bonds.values().iterator().next();assertEquals("reedling",rb.speciesKey);assertEquals(.71,rb.devotion,.0001);
 }
}
