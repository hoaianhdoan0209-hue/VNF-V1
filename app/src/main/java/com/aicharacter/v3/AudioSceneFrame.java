package com.aicharacter.v3;

/** Immutable presentation snapshot consumed by the audio renderer. */
public final class AudioSceneFrame {
 public final String areaId,weather,dayPhase,catMode;
 public final double rain,wind,wetness,water,haruSpeed,haruBreathing,haruStress,catSpeed,catAttention,divinePresence;
 public final boolean haruWalking,catMoving,catSleeping;

 public AudioSceneFrame(String areaId,String weather,String dayPhase,String catMode,double rain,double wind,double wetness,double water,double haruSpeed,double haruBreathing,double haruStress,double catSpeed,double catAttention,double divinePresence,boolean haruWalking,boolean catMoving,boolean catSleeping){
  this.areaId=safe(areaId,"unknown");this.weather=safe(weather,"CLEAR");this.dayPhase=safe(dayPhase,"DAY");this.catMode=safe(catMode,"IDLE");
  this.rain=cl(rain);this.wind=cl(wind);this.wetness=cl(wetness);this.water=cl(water);this.haruSpeed=cl(haruSpeed);this.haruBreathing=cl(haruBreathing);this.haruStress=cl(haruStress);this.catSpeed=cl(catSpeed);this.catAttention=cl(catAttention);this.divinePresence=cl(divinePresence);
  this.haruWalking=haruWalking;this.catMoving=catMoving;this.catSleeping=catSleeping;
 }
 public boolean finite(){return Double.isFinite(rain)&&Double.isFinite(wind)&&Double.isFinite(wetness)&&Double.isFinite(water)&&Double.isFinite(haruSpeed)&&Double.isFinite(haruBreathing)&&Double.isFinite(haruStress)&&Double.isFinite(catSpeed)&&Double.isFinite(catAttention)&&Double.isFinite(divinePresence);}
 public static AudioSceneFrame quiet(){return new AudioSceneFrame("unknown","CLEAR","DAY","IDLE",0,0,0,0,0,.05,0,0,0,0,false,false,false);}
 private static String safe(String s,String d){return s==null||s.trim().isEmpty()?d:s.trim();}
 private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
