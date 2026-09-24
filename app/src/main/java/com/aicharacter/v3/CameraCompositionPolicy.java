package com.aicharacter.v3;

/** Pure presentation composition rule for emotion camera + transient semantic moments. */
public final class CameraCompositionPolicy {
 private CameraCompositionPolicy(){}
 public static float targetZoom(EmotionCameraDirector.Frame emotion,MomentDirector.Cue moment){
  float emotional=emotion==null?1f:clamp(emotion.zoom,1f,1.65f);
  if(moment==null||!moment.active)return emotional;
  float semantic=clamp(moment.minZoom,1f,1.65f);
  if(moment.kind==MomentDirector.Kind.SOCIAL_RETREAT)return clamp(Math.min(emotional,semantic),1f,1.65f);
  return clamp(Math.max(emotional,semantic),1f,1.65f);
 }
 private static float clamp(float v,float lo,float hi){return Float.isFinite(v)?Math.max(lo,Math.min(hi,v)):lo;}
}
