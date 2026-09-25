package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelContactFxRendererTest {
 @Test public void contactFxSupportsFourBiomeVariants(){
  assertEquals(4,PixelContactFxRenderer.variantCount());
 }
 @Test public void actorShadowAndContactFxStayIndependentPresentationLayers(){
  assertEquals(3,PixelLightingRenderer.actorContactBandCount());
  assertTrue(PixelContactFxRenderer.variantCount()>PixelLightingRenderer.actorContactBandCount());
 }
}
