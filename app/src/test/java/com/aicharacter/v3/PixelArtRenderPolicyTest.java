package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelArtRenderPolicyTest {
 @Test public void frameUsesStableIntegerPixelBlocks(){
  assertTrue(PixelArtRenderPolicy.FRAME_SCALE>=2);
  assertEquals(360,PixelArtRenderPolicy.internalWidth(1080));
  assertEquals(640,PixelArtRenderPolicy.internalHeight(1920));
  assertEquals(1f/PixelArtRenderPolicy.FRAME_SCALE,PixelArtRenderPolicy.logicalToRasterScale(),1e-9);
 }
 @Test public void oddScreenSizesRoundUpWithoutCropping(){
  int scale=PixelArtRenderPolicy.FRAME_SCALE;
  assertTrue(PixelArtRenderPolicy.internalWidth(1081)*scale>=1081);
  assertTrue(PixelArtRenderPolicy.internalHeight(1921)*scale>=1921);
 }
}
