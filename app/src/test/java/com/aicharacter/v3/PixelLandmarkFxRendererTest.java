package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelLandmarkFxRendererTest {
 @Test public void landmarkFxCoversAllPlayableBiomes(){
  assertEquals(4,PixelLandmarkFxRenderer.motifFamilies());
 }
 @Test public void pixelFxKeepsTheGlobalTwoTimesGrid(){
  assertEquals(2,PixelArtRenderPolicy.FRAME_SCALE);
 }
}
