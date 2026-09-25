package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelMotionCadenceTest {
 @Test public void cadenceHasEightAuthoredFrames(){
  assertEquals(8,PixelMotionCadence.frameCount());
  boolean[] seen=new boolean[8];
  for(int i=-8;i<16;i++)seen[PixelMotionCadence.frame(i)]=true;
  for(int i=0;i<8;i++)assertTrue("missing frame "+i,seen[i]);
 }
 @Test public void gaitHasTwoContactsAndTwoPassingFrames(){
  int contacts=0,passing=0;
  for(int i=0;i<8;i++){if(PixelMotionCadence.contact(i))contacts++;if(PixelMotionCadence.passing(i))passing++;}
  assertEquals(2,contacts);assertEquals(2,passing);
  assertTrue(PixelMotionCadence.compression(0)>PixelMotionCadence.compression(2));
  assertTrue(PixelMotionCadence.compression(4)>PixelMotionCadence.compression(6));
 }
 @Test public void strideAndSecondaryMotionMirrorAcrossCycle(){
  assertEquals(-PixelMotionCadence.stride(0),PixelMotionCadence.stride(4),1e-6);
  assertEquals(-PixelMotionCadence.secondary(0),PixelMotionCadence.secondary(4),1e-6);
  assertEquals(PixelMotionCadence.lift(1),PixelMotionCadence.lift(5),1e-6);
 }
}
