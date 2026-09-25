package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelTimeGradeRendererTest {
 @Test public void timeGradeUsesManyDiscreteBands(){
  assertTrue(PixelTimeGradeRenderer.bandCount()>=8);
  assertEquals(2,PixelArtRenderPolicy.FRAME_SCALE);
 }
}
