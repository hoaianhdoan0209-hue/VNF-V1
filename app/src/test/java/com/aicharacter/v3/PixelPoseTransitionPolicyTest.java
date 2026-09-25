package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelPoseTransitionPolicyTest {
 @Test public void poseTransitionNeverAlphaBlendsFrames(){
  assertEquals(1f,PixelPoseTransitionPolicy.alphaForPrevious(0),0f);
  assertEquals(0f,PixelPoseTransitionPolicy.alphaForCurrent(0),0f);
  assertEquals(0f,PixelPoseTransitionPolicy.alphaForPrevious(PixelPoseTransitionPolicy.HOLD_MS),0f);
  assertEquals(1f,PixelPoseTransitionPolicy.alphaForCurrent(PixelPoseTransitionPolicy.HOLD_MS),0f);
 }
 @Test public void shortHoldPreventsSingleFramePoseFlicker(){
  assertTrue(PixelPoseTransitionPolicy.HOLD_MS>=40);
  assertTrue(PixelPoseTransitionPolicy.HOLD_MS<=100);
 }
}
