package com.aicharacter.v3;

/** Pure presentation composition rule for emotion camera + transient semantic moments. */
public final class CameraCompositionPolicy {
 public static final float BASE_PIXEL_ZOOM=1.16f;
 private CameraCompositionPolicy(){}

 public static float targetZoom(EmotionCameraDirector.Frame emotion,MomentDirector.Cue moment){
  float emotional=presentedZoom(emotion==null?1f:emotion.zoom);
  if(moment==null||!moment.active)return emotional;
  float semantic=presentedZoom(moment.minZoom);
  // Retreat is intentionally allowed to widen below the normal pixel baseline.
  // Keep the authored semantic zoom here instead of remapping it toward BASE_PIXEL_ZOOM.
  if(moment.kind==MomentDirector.Kind.SOCIAL_RETREAT){float retreat=clamp(moment.minZoom,1.05f,1.65f);return clamp(Math.min(emotional,retreat),1.05f,1.65f);}
  return clamp(Math.max(emotional,semantic),BASE_PIXEL_ZOOM,1.65f);
 }

 private static float presentedZoom(float authored){
  float z=Float.isFinite(authored)?authored:1f;
  return clamp(BASE_PIXEL_ZOOM+(Math.max(1f,z)-1f)*.72f,BASE_PIXEL_ZOOM,1.65f);
 }
 private static float clamp(float v,float lo,float hi){return Float.isFinite(v)?Math.max(lo,Math.min(hi,v)):lo;}
}
