package com.aicharacter.v3;

/** Read-only world -> audio presentation mapping. Never mutates simulation state. */
public final class AudioSceneEmitter {
 private AudioSceneEmitter(){}

 public static AudioSceneFrame derive(WorldState s,double divinePresence){
  if(s==null)return AudioSceneFrame.quiet();
  String area="unknown";WorldArea a=s.world==null?null:s.world.areaAt(s.haruX);if(a!=null)area=a.id;
  EnvironmentState e=s.environment==null?new EnvironmentState():s.environment;
  String phase=e.dayPhase(s.worldMinutes),weather=e.weather==null?"CLEAR":e.weather;
  double rain="RAIN".equals(weather)?cl(e.weatherIntensity):0,wind=cl(e.wind),wet=cl(s.worldWetness),water="lakeside".equals(area)?1:0;
  double haruSpeed=s.girlTravel==null?0:cl(Math.abs(s.girlTravel.lastSpeed)/120.0),catSpeed=s.catTravel==null?0:cl(Math.abs(s.catTravel.lastSpeed)/95.0);
  BiologyVisualOutput bio=BiologyVisualOutput.from(s);double breathing=cl(bio.breathingIntensity),fear=s.emotion==null?0:cl(s.emotion.fear),anger=s.emotion==null?0:cl(s.emotion.anger),stress=cl(Math.max(Math.max(fear,anger*.72),breathing*.78));
  double catAttention=s.catSocial==null?0:cl(s.catSocial.attention);String catMode=s.catSocial==null?"IDLE":safe(s.catSocial.mode,"IDLE");
  boolean haruWalking=s.girlTravel!=null&&s.girlTravel.active&&haruSpeed>.02,catMoving=s.catTravel!=null&&s.catTravel.active&&catSpeed>.02,catSleeping=s.catState!=null&&!s.catState.awake;
  return new AudioSceneFrame(area,weather,phase,catMode,rain,wind,wet,water,haruSpeed,breathing,stress,catSpeed,catAttention,cl(divinePresence),haruWalking,catMoving,catSleeping);
 }
 private static String safe(String s,String d){return s==null||s.trim().isEmpty()?d:s;}
 private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
