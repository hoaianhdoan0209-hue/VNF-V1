package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelReactionFxRendererTest {
 @Test public void reactionFxCoversThreeActorFamilies(){
  assertEquals(3,PixelReactionFxRenderer.cueFamilyCount());
 }
 @Test public void reactionFxStaysSeparateFromContactAndShadowLayers(){
  assertEquals(4,PixelContactFxRenderer.variantCount());
  assertEquals(3,PixelLightingRenderer.actorContactBandCount());
  assertNotEquals(PixelContactFxRenderer.variantCount(),PixelReactionFxRenderer.cueFamilyCount());
 }
}
