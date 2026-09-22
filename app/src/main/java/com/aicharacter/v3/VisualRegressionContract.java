package com.aicharacter.v3;

/** Pure-Java renderer math guard used by runtime and regression tests. */
public final class VisualRegressionContract{
 private static final float WORLD_WIDTH=2400f;
 private VisualRegressionContract(){}

 public static float layerShift(float cameraX,float parallax,float viewportWorldW){
  float cam=Float.isFinite(cameraX)?Math.max(0f,cameraX):0f;
  float factor=Float.isFinite(parallax)?Math.max(0f,parallax):0f;
  float viewport=Float.isFinite(viewportWorldW)?Math.max(1f,Math.min(WORLD_WIDTH,viewportWorldW)):WORLD_WIDTH;
  float maxShift=Math.max(0f,WORLD_WIDTH-viewport);
  float raw=cam*factor;
  if(!Float.isFinite(raw))return 0f;
  return Math.max(0f,Math.min(raw,maxShift));
 }
}
