package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelBiomeDetailRendererTest {
 @Test public void allPlayableBiomesHavePixelNativeMotifs(){
  String[] areas={"home","garden","lakeside","grove"};
  for(String area:areas){
   assertTrue(area,PixelBiomeDetailRenderer.supports(area));
   assertTrue(area+" should have several distinct motifs",PixelBiomeDetailRenderer.motifCount(area)>=4);
  }
 }
 @Test public void eachBiomeKeepsDistinctDetailDensity(){
  assertNotEquals(PixelBiomeDetailRenderer.motifCount("home"),PixelBiomeDetailRenderer.motifCount("lakeside"));
  assertNotEquals(PixelBiomeDetailRenderer.motifCount("garden"),PixelBiomeDetailRenderer.motifCount("home"));
  assertEquals(0,PixelBiomeDetailRenderer.motifCount("unknown"));
  assertFalse(PixelBiomeDetailRenderer.supports("unknown"));
 }
}
