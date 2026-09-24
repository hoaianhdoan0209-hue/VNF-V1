package com.aicharacter.v3;

/** Read-only world -> audio presentation mapping. Never mutates simulation state. */
public final class AudioSceneEmitter {
 private AudioSceneEmitter(){}

 public static AudioSceneFrame derive(WorldState s,double divinePresence){
  if(s==null)return AudioSceneFrame.quiet();
  WorldArea haruArea=s.world==null?null:s.world.areaAt(s.haruX),catArea=s.world==null||s.catState==null?null:s.world.areaAt(s.catState.x);
  String area=haruArea==null?"unknown":haruArea.id;
  EnvironmentState e=s.environment==null?new EnvironmentState():s.environment;
  String phase=e.dayPhase(s.worldMinutes),weather=e.weather==null?"CLEAR":e.weather;
  double rain="RAIN".equals(weather)?cl(e.weatherIntensity):0,wind=cl(e.wind),wet=cl(s.worldWetness),water=haruArea!=null&&has(haruArea,"water")?1:0,exposure=cl(WorldSemantics.exposure(haruArea));
  double haruSpeed=s.girlTravel==null?0:cl(Math.abs(s.girlTravel.lastSpeed)/120.0),catSpeed=s.catTravel==null?0:cl(Math.abs(s.catTravel.lastSpeed)/95.0);
  BiologyVisualOutput bio=BiologyVisualOutput.from(s);double breathing=cl(bio.breathingIntensity),fear=s.emotion==null?0:cl(s.emotion.fear),anger=s.emotion==null?0:cl(s.emotion.anger),stress=cl(Math.max(Math.max(fear,anger*.72),breathing*.78));
  double catAttention=s.catSocial==null?0:cl(s.catSocial.attention);String catMode=s.catSocial==null?"IDLE":safe(s.catSocial.mode,"IDLE");
  double purr=catPurr(s);
  boolean haruWalking=s.girlTravel!=null&&s.girlTravel.active&&haruSpeed>.02,catMoving=s.catTravel!=null&&s.catTravel.active&&catSpeed>.02,catSleeping=s.catState!=null&&!s.catState.awake;
  String haruSurface=SurfaceAcoustics.surfaceAt(s,s.haruX).name(),catSurface=SurfaceAcoustics.surfaceAt(s,s.catState==null?s.haruX:s.catState.x).name();
  return new AudioSceneFrame(area,weather,phase,catMode,haruSurface,catSurface,rain,wind,wet,water,exposure,haruSpeed,breathing,stress,catSpeed,catAttention,purr,cl(divinePresence),haruWalking,catMoving,catSleeping);
 }
 private static double catPurr(WorldState s){
  if(s==null||s.catState==null||s.catSocial==null||!s.catState.awake)return 0;
  double d=Math.abs(s.haruX-s.catState.x);if(d>155)return 0;
  double comfort=cl(s.catSocial.comfort),familiarity=cl(s.catSocial.familiarity),wariness=cl(s.catSocial.wariness);
  boolean settled="SETTLE_NEAR".equals(s.catSocial.mode),warmEvent=MicroInteractionDirector.derive(s,Math.max(s.lastSimulatedAt,s.lastOpenedAt)).active;
  double near=cl((155-d)/105.0),base=comfort*.42+familiarity*.34+near*.24-wariness*.48;
  if(settled)base+=.18;if(warmEvent)base+=.12;return cl(base);
 }
 private static boolean has(WorldArea a,String q){if(a==null||a.tags==null)return false;for(String x:a.tags.split(","))if(q.equalsIgnoreCase(x.trim()))return true;return false;}
 private static String safe(String s,String d){return s==null||s.trim().isEmpty()?d:s;}
 private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
