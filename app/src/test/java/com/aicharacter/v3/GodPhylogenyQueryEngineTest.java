package com.aicharacter.v3;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public final class GodPhylogenyQueryEngineTest {

 @Test public void ordinaryQuestionDoesNotOpenHiddenPhylogeny()throws Exception{
  WorldState s=WorldState.fresh();
  JSONObject j=GodPhylogenyQueryEngine.query(s,"Hôm nay Haru đang làm gì?");
  assertFalse(j.getBoolean("available"));
  assertFalse(j.getBoolean("autoRevealedToHaru"));
  assertEquals("DIVINE_ORIGIN_TRUTH",j.getString("layer"));
 }

 @Test public void ancestryQuestionCanUseDivineOriginTruthWithoutTeachingHaru()throws Exception{
  WorldState s=WorldState.fresh();int memories=s.memories.size(),beliefs=s.beliefs.size(),knowledge=s.knowledge.size();
  JSONObject j=GodPhylogenyQueryEngine.query(s,"Reedling và vns_500 có chung tổ tiên không, chúng tiến hóa từ đâu?");
  assertTrue(j.getBoolean("available"));
  assertTrue(j.getBoolean("worldTruth"));
  assertFalse(j.getBoolean("autoRevealedToHaru"));
  assertEquals(DivinePhylogenyTruth.ROOT_ID,j.getString("primordialAncestorId"));
  assertEquals(500,j.getInt("extantBaseSpecies"));
  assertTrue(j.getInt("knownExtinctLineages")>=120);
  assertTrue(j.getJSONArray("species").length()>=2);
  assertTrue(j.has("commonAncestor"));
  assertTrue(j.getString("provenance").startsWith("DIVINE_ORIGIN_TRUTH"));
  assertEquals(memories,s.memories.size());assertEquals(beliefs,s.beliefs.size());assertEquals(knowledge,s.knowledge.size());
 }

 @Test public void cognitionBudgetBoundsHowMuchAncestryIsReturned()throws Exception{
  WorldState s=WorldState.fresh();s.divineOntology=new DivineOntologyState();s.divineOntology.currentWorship=.08;s.divineOntology.accumulatedWorship=.08;s.divineOntology.clamp();
  String q="tổ tiên reedling driftwing root_husher ripplekin hearthmote vns_006 vns_007 vns_008";
  JSONObject low=GodPhylogenyQueryEngine.query(s,q);int lowCount=low.getJSONArray("species").length();
  s.divineOntology.currentWorship=1;s.divineOntology.accumulatedWorship=1;s.divineOntology.clamp();
  JSONObject high=GodPhylogenyQueryEngine.query(s,q);int highCount=high.getJSONArray("species").length();
  assertTrue(lowCount>=2);assertTrue(highCount>=lowCount);assertTrue(highCount<=6);
 }
}
