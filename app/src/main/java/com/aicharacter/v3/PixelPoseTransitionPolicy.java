package com.aicharacter.v3;

/** Crisp sprite-pose transition policy: never alpha-blend authored pixel frames. */
public final class PixelPoseTransitionPolicy {
 public static final long HOLD_MS=70L;
 private PixelPoseTransitionPolicy(){}

 public static boolean showPrevious(long elapsedMs){
  return elapsedMs>=0&&elapsedMs<HOLD_MS;
 }
 public static float alphaForPrevious(long elapsedMs){return showPrevious(elapsedMs)?1f:0f;}
 public static float alphaForCurrent(long elapsedMs){return showPrevious(elapsedMs)?0f:1f;}
}
