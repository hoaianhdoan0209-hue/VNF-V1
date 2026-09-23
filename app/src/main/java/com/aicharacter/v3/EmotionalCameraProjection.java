package com.aicharacter.v3;

/** Pure projection math used by GameView so emotional shots produce real world zoom. */
public final class EmotionalCameraProjection {
 public static final float WORLD_W=2400f,WORLD_H=1080f;
 private EmotionalCameraProjection(){}

 public static float baseScale(int screenW,int screenH){
  if(screenW<=0||screenH<=0)return 1f;return Math.max(screenW/WORLD_W,screenH/WORLD_H);
 }
 public static float worldScale(int screenW,int screenH,float zoom){return baseScale(screenW,screenH)*safeZoom(zoom);}
 public static float viewportW(int screenW,int screenH,float zoom){float s=worldScale(screenW,screenH,zoom);return s<=0?WORLD_W:screenW/s;}
 public static float viewportH(int screenW,int screenH,float zoom){float s=worldScale(screenW,screenH,zoom);return s<=0?WORLD_H:screenH/s;}
 public static float verticalEmotionOffset(float viewportH,float subjectWorldY,float subjectScreenY,float zoom){
  float z=safeZoom(zoom);if(z<=1.01f)return 0;float desired=viewportH*clamp(subjectScreenY,.45f,.72f);float raw=desired-subjectWorldY;float blend=clamp((z-1f)/.48f,0,1);return raw*blend;
 }
 public static float projectedHeightPx(int screenW,int screenH,float logicalHeight,float zoom){return logicalHeight*worldScale(screenW,screenH,zoom);}
 private static float safeZoom(float z){return Float.isFinite(z)?clamp(z,1f,1.65f):1f;}
 private static float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
}
