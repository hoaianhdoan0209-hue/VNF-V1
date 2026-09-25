package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelLightingRendererTest {
 @Test public void lightingUsesMultipleDiscretePixelBands(){
  assertTrue(PixelLightingRenderer.bandCount()>=4);
  assertEquals(2,PixelArtRenderPolicy.FRAME_SCALE);
 }
 @Test public void pixelGridSnapsLightingCoordinates(){
  float step=PixelArtRenderPolicy.FRAME_SCALE;
  float snapped=PixelArtRenderPolicy.snapLogical(123.3f);
  assertEquals(0f,Math.abs(snapped)%step,1e-6);
 }
}
