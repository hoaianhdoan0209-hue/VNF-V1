package com.aicharacter.v3;

/** Pure presentation cadence for authored 8-frame pixel motion. */
public final class PixelMotionCadence {
 private static final float[] STRIDE={-1.85f,-1.30f,-.48f,.62f,1.85f,1.30f,.48f,-.62f};
 private static final float[] LIFT={0f,-1f,-1f,0f,0f,-1f,-1f,0f};
 private static final float[] COMPRESSION={1f,.55f,0f,.25f,1f,.55f,0f,.25f};
 private static final float[] SECONDARY={.8f,.45f,0f,-.45f,-.8f,-.45f,0f,.45f};
 private PixelMotionCadence(){}

 public static int frame(int frame){int f=frame%8;return f<0?f+8:f;}
 public static float stride(int frame){return STRIDE[frame(frame)];}
 public static float lift(int frame){return LIFT[frame(frame)];}
 public static float compression(int frame){return COMPRESSION[frame(frame)];}
 public static float secondary(int frame){return SECONDARY[frame(frame)];}
 public static boolean contact(int frame){int f=frame(frame);return f==0||f==4;}
 public static boolean passing(int frame){int f=frame(frame);return f==2||f==6;}
 public static int frameCount(){return 8;}
}
