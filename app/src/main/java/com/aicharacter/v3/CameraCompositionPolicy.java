package com.aicharacter.v3;

/** Pure presentation composition rule for emotion camera + transient semantic moments. */
public final class CameraCompositionPolicy {
 public static final float BASE_PIXEL_ZOOM=1.16f;
 private CameraCompositionPolicy(){}

 public static float targetZoom(EmotionCameraDirector.Frame emotion,MomentDirector.Cue moment){
  float emotional=presentedZoom(emotion==null?1f:emotion.zoom);
  if(moment==null||!moment.active)return emotional;
  float semantic=presentedZoom(moment.minZoom);
  // Retreat can widen the shot, but the all-pixel presentation never drops
  // back to the distant pre-pixel framing.
  if(moment.kind==MomentDirector.Kind.SOCIAL_RETREAT)return clamp(Math.min(emotional,semantic),1.08f,1.65f);
  return clamp(Math.max(emotional,semantic),BASE_PIXEL_ZOOM,1.65f);
 }

 private static float presentedZoom(float authored){
  float z=Float.isFinite(authored)?authored:1f;
  return clamp(BASE_PIXEL_ZOOM+(Math.max(1f,z)-1f)*.72f,BASE_PIXEL_ZOOM,1.65f);
 }
 private static float clamp(float v,float lo,float hi){return Float.isFinite(v)?Math.max(lo,Math.min(hi,v)):lo;}
}
