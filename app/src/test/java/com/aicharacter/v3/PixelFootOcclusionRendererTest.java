package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelFootOcclusionRendererTest {
 @Test public void footOcclusionSupportsFourBiomeModes(){
  assertEquals(4,PixelFootOcclusionRenderer.biomeModeCount());
 }
 @Test public void authoredAreaIdsMapToVisualBiomes(){
  assertEquals("home",PixelFootOcclusionRenderer.visualArea("home_shelter"));
  assertEquals("garden",PixelFootOcclusionRenderer.visualArea("garden_path"));
  assertEquals("lakeside",PixelFootOcclusionRenderer.visualArea("lakeside"));
  assertEquals("grove",PixelFootOcclusionRenderer.visualArea("quiet_grove"));
 }
}
