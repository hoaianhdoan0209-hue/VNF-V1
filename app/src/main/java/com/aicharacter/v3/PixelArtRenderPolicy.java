package com.aicharacter.v3;

/** Central pixel-art raster policy. Gameplay remains in full logical coordinates. */
public final class PixelArtRenderPolicy {
 public static final int FRAME_SCALE=3;
 private PixelArtRenderPolicy(){}

 public static int internalWidth(int screenWidth){return Math.max(1,(Math.max(1,screenWidth)+FRAME_SCALE-1)/FRAME_SCALE);}
 public static int internalHeight(int screenHeight){return Math.max(1,(Math.max(1,screenHeight)+FRAME_SCALE-1)/FRAME_SCALE);}
 public static float logicalToRasterScale(){return 1f/FRAME_SCALE;}
 public static int screenPixelBlock(){return FRAME_SCALE;}
}
