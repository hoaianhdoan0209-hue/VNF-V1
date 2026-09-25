package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public final class PixelWildlifeMorphologyRendererTest {
 @Test public void everyCatalogBodyPlanHasPixelMorphology(){
  Set<String> bodies=new HashSet<>();
  for(SpeciesEvolutionCatalog.Species s:SpeciesEvolutionCatalog.all()){
   bodies.add(s.bodyPlan);
   assertTrue(s.bodyPlan,PixelWildlifeMorphologyRenderer.supportsBodyPlan(s.bodyPlan));
  }
  assertEquals(25,bodies.size());
  assertEquals(25,PixelWildlifeMorphologyRenderer.bodyPlanCount());
 }
 @Test public void unknownBodyPlanDoesNotMasqueradeAsAuthoredPixelSpecies(){
  assertFalse(PixelWildlifeMorphologyRenderer.supportsBodyPlan("unknown-body"));
 }
}
