package com.aicharacter.v3;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public final class DivineOntologyWorshipTest {

 @Test public void allLivingSpeciesKnowGodAndNoSpeciesNameGetsPrivilege(){
  WorldState s=state();
  pop(s,"alpha","area_a",.60);pop(s,"beta","area_a",.60);
  DivineWorshipEngine.advance(s,60,1000L);
  SpeciesDivineState a=s.livingWorld.divine("alpha","area_a"),b=s.livingWorld.divine("beta","area_a");
  assertEquals(1.0,a.awareness,0);assertEquals(1.0,b.awareness,0);
  assertTrue(a.devotion>0);assertTrue(b.devotion>0);assertEquals(a.devotion,b.devotion,1e-9);
  assertEquals(a.reverence,b.reverence,1e-9);assertEquals(a.trust,b.trust,1e-9);
  assertTrue(s.divineOntology.godExistsAsWorldConcept);assertTrue(s.divineOntology.universalLifeAwareness);
 }

 @Test public void proposalAloneDoesNotCreateWorshipButAppliedInfluenceDoes()throws Exception{
  WorldState s=state();pop(s,"alpha","area_a",.70);pop(s,"alpha","area_b",.70);
  GodWorldEventProposal p=proposal("mist-witness","area_a",.80,1000L);
  assertTrue(GodWorldConditionContract.propose(s,p,1000L).ok);
  DivineWorshipEngine.advance(s,1,1001L);
  double beforeA=s.livingWorld.divine("alpha","area_a").devotion,beforeB=s.livingWorld.divine("alpha","area_b").devotion;
  assertEquals(beforeA,beforeB,1e-9);
  assertTrue(GodWorldConditionContract.markApplied(s,"mist-witness","WorldPhysicsTest",1100L));
  DivineWorshipEngine.advance(s,1,1101L);
  SpeciesDivineState a=s.livingWorld.divine("alpha","area_a"),b=s.livingWorld.divine("alpha","area_b");
  assertTrue(a.devotion>b.devotion);assertEquals(1,a.witnessedDivineEvents);assertEquals(0,b.witnessedDivineEvents);
  int processed=s.divineOntology.processedInfluenceEventIds.size();
  DivineWorshipEngine.advance(s,1,1102L);
  assertEquals(processed,s.divineOntology.processedInfluenceEventIds.size());assertEquals(1,a.witnessedDivineEvents);
 }

 @Test public void worshipRaisesCognitiveCapacityWithoutCreatingKnowledge(){
  WorldState s=state();SpeciesPopulationState p=pop(s,"alpha","area_a",1.0);
  DivineWorshipEngine.advance(s,1,1000L);double low=s.divineOntology.capacity.intelligenceLevel;
  SpeciesDivineState d=s.livingWorld.divine("alpha","area_a");d.devotion=.96;d.reverence=.96;d.trust=.90;d.witnessInfluence=.95;
  ConceptKnowledgeState known=new ConceptKnowledgeState();known.concept="known_fact";known.claim="evidence backed";known.confidence=.8;s.characterGod.conceptKnowledge.put("known_fact",known);
  DivineWorshipEngine.advance(s,10*1440.0,2000L);
  assertTrue(s.divineOntology.capacity.intelligenceLevel>low);
  assertTrue(s.divineOntology.capacity.reasoningDepth>=2);assertTrue(s.divineOntology.capacity.hypothesisBreadth>=2);
  assertTrue(s.divineOntology.capacity.selfCheckStrength>=.42);
  assertTrue(s.characterGod.conceptKnowledge.containsKey("known_fact"));
  assertFalse(s.characterGod.conceptKnowledge.containsKey("invented_truth"));
  double history=s.divineOntology.accumulatedWorship;
  d.devotion=.08;d.reverence=.08;d.trust=.08;d.witnessInfluence=0;p.relativeAbundance=1;
  DivineWorshipEngine.advance(s,30*1440.0,3000L);
  assertTrue(s.divineOntology.accumulatedWorship>=history);
  assertTrue(s.characterGod.conceptKnowledge.containsKey("known_fact"));
 }

 @Test public void saveRoundTripKeepsOntologySpeciesDevotionAndCapacity()throws Exception{
  WorldState s=state();pop(s,"alpha","area_a",.8);
  GodWorldEventProposal p=proposal("wind-save","area_a",.55,1000L);assertTrue(GodWorldConditionContract.propose(s,p,1000L).ok);assertTrue(GodWorldConditionContract.markApplied(s,"wind-save","WorldPhysicsTest",1010L));DivineWorshipEngine.advance(s,120,1020L);
  WorldState x=WorldState.fromJson(s.toJson());
  assertNotNull(x.divineOntology);assertTrue(x.divineOntology.godExistsAsWorldConcept);assertEquals(1,x.livingWorld.divine("alpha","area_a").awareness,0);assertEquals(s.livingWorld.divine("alpha","area_a").devotion,x.livingWorld.divine("alpha","area_a").devotion,1e-9);assertEquals(s.divineOntology.currentWorship,x.divineOntology.currentWorship,1e-9);assertEquals(s.divineOntology.capacity.reasoningDepth,x.divineOntology.capacity.reasoningDepth);assertFalse(x.divineOntology.processedInfluenceEventIds.isEmpty());
 }

 @Test public void worshipEvolutionIsStableAcrossTimePartition(){
  WorldState one=state(),many=state();pop(one,"alpha","area_a",.75);pop(many,"alpha","area_a",.75);
  SpeciesDivineState a=one.livingWorld.divine("alpha","area_a"),b=many.livingWorld.divine("alpha","area_a");a.witnessInfluence=b.witnessInfluence=.65;a.devotion=b.devotion=.62;
  DivineWorshipEngine.advance(one,120,5000L);
  for(int i=1;i<=4;i++)DivineWorshipEngine.advance(many,30,5000L+i);
  assertEquals(one.livingWorld.divine("alpha","area_a").devotion,many.livingWorld.divine("alpha","area_a").devotion,.002);
  assertEquals(one.divineOntology.currentWorship,many.divineOntology.currentWorship,.002);
  assertEquals(one.divineOntology.accumulatedWorship,many.divineOntology.accumulatedWorship,.003);
 }

 @Test public void godObservationKnowsItsLimitsQualitatively()throws Exception{
  WorldState s=state();pop(s,"alpha","area_a",.5);DivineWorshipEngine.advance(s,30,1000L);
  JSONObject root=GodObservationSnapshot.build(s),d=root.getJSONObject("divineOntology");
  assertEquals("KNOWN_TO_ALL_LIFE",d.getString("ontology"));assertTrue(d.getBoolean("evidenceBoundKnowledge"));assertTrue(d.getBoolean("learnedKnowledgePersists"));assertTrue(d.has("cognitiveCapacity"));assertFalse(d.has("omniscient"));
 }

 private static SpeciesPopulationState pop(WorldState s,String species,String area,double abundance){SpeciesPopulationState p=s.livingWorld.population(species,area);p.relativeAbundance=abundance;p.carryingCapacity=.8;p.lastUpdatedAt=1;p.clamp();return p;}
 private static GodWorldEventProposal proposal(String id,String area,double intensity,long now)throws Exception{JSONObject j=new JSONObject();j.put("kind","god-world-condition-v2");j.put("id",id);j.put("condition","WIND");j.put("desiredValue","BREEZE");j.put("scopeType","AREA");j.put("scopeTarget",area);j.put("intensity",intensity);j.put("durationMinutes",30);j.put("provenanceLayer","GOD_PROPOSAL");j.put("sourceRef","god:test");j.put("rollbackPolicy","RESTORE_PREVIOUS_BASELINE");j.put("requestedAt",now);return GodWorldEventProposal.fromJson(j,now);}
 private static WorldState state(){WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("area_a","A","a",0,100,0,true,"open"));s.world.areas.add(new WorldArea("area_b","B","b",100,200,0,true,"open"));s.haruX=20;s.catX=30;s.catState.x=30;s.catState.areaId="area_a";s.livingWorld=new LivingWorldState();s.divineOntology=new DivineOntologyState();return s;}
}
