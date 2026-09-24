package com.aicharacter.v3;

/**
 * Presentation-layer semantic sound request.
 * No Android audio API is used here; an audio renderer can consume this contract later.
 */
public final class SoundEvent {
 public enum Layer{AMBIENT,FOLEY,SOCIAL,MOMENT,DIVINE}
 public final String id,semanticId;
 public final Layer layer;
 public final double intensity,spatialX,panHint,pitchHint,durationSeconds;
 public final boolean oneShot;
 public final long createdAt;

 public SoundEvent(String id,String semanticId,Layer layer,double intensity,double spatialX,double panHint,double pitchHint,double durationSeconds,boolean oneShot,long createdAt){
  this.id=safe(id,"sound_"+Long.toHexString(Math.max(0,createdAt)));
  this.semanticId=safe(semanticId,"silence");
  this.layer=layer==null?Layer.MOMENT:layer;
  this.intensity=cl01(intensity);
  this.spatialX=finite(spatialX,0);
  this.panHint=clamp(panHint,-1,1);
  this.pitchHint=clamp(pitchHint,.5,1.8);
  this.durationSeconds=clamp(durationSeconds,.02,30);
  this.oneShot=oneShot;
  this.createdAt=Math.max(0,createdAt);
 }
 public boolean valid(){return !semanticId.isEmpty()&&Double.isFinite(intensity)&&Double.isFinite(spatialX)&&Double.isFinite(panHint)&&Double.isFinite(pitchHint)&&Double.isFinite(durationSeconds);}
 private static String safe(String s,String d){return s==null||s.trim().isEmpty()?d:s.trim();}
 private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
 private static double cl01(double v){return clamp(v,0,1);}
 private static double clamp(double v,double a,double b){return Double.isFinite(v)?Math.max(a,Math.min(b,v)):a;}
}
