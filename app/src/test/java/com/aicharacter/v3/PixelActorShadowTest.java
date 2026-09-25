package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelActorShadowTest {
 @Test public void actorContactShadowUsesThreePixelBands(){
  assertEquals(3,PixelLightingRenderer.actorContactBandCount());
 }
 @Test public void liftedActorsCastSmallerContactShadows(){
  float grounded=PixelLightingRenderer.contactScaleForLift(0);
  float hovering=PixelLightingRenderer.contactScaleForLift(88);
  float high=PixelLightingRenderer.contactScaleForLift(400);
  assertEquals(1f,grounded,1e-6f);
  assertTrue(hovering<grounded);
  assertEquals(.34f,high,1e-6f);
 }
}
