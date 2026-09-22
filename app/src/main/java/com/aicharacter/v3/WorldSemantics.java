package com.aicharacter.v3;
/** Derived authored semantics: read-only world interpretation, never invented presentation state. */
public final class WorldSemantics {private WorldSemantics(){}
 public static double shelter(WorldArea a){if(a==null)return 0;double v=a.weatherExposed?.08:.72;if(has(a,"shelter")||has(a,"home"))v=Math.max(v,.95);return v;}
 public static double rest(WorldArea a){if(a==null)return 0;double v=.35;if(has(a,"quiet"))v+=.25;if(has(a,"home"))v+=.25;if(has(a,"water"))v+=.12;return Math.min(1,v);}
 public static double exposure(WorldArea a){if(a==null)return 1;if(a.weatherExposed)return 1;if(has(a,"interior")||has(a,"dry")||has(a,"home"))return .02;return .18;}
 public static double visibility(WorldState s,WorldArea a){
  if(s==null||s.environment==null)return 1;
  double v=s.environment.ambientBrightness;
  if("RAIN".equals(s.environment.weather))v-=.28*s.environment.weatherIntensity;
  v-=.10*s.environment.cloudCover;
  if(a!=null&&has(a,"grove"))v-=.12;
  double humidity=s.atmosphere==null?.55:s.atmosphere.relativeHumidity;
  double mist=humidity>.82?(humidity-.82)/.18:0;
  if(a!=null&&(has(a,"water")||has(a,"wet_margin")||has(a,"mist")))mist=Math.min(1,mist+.08);
  v-=.22*mist;
  if(s.atmosphere!=null)v*=.72+.28*s.atmosphere.airQuality;
  return Math.max(.08,Math.min(1,v));
 }
 public static double comfort(WorldState s,WorldArea a){
  double c=rest(a)-s.worldWetness*.25*exposure(a);
  if("RAIN".equals(s.environment.weather))c-=.35*s.environment.weatherIntensity*exposure(a);
  return c;
 }
 private static boolean has(WorldArea a,String q){
  if(a==null||a.tags==null)return false;
  for(String x:a.tags.split(","))if(q.equalsIgnoreCase(x.trim()))return true;
  return false;
 }
}
