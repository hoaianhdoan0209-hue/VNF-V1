package com.aicharacter.v3;
/**
 * Evolves physical atmosphere/environment from persisted world state.
 * Uses EnvironmentState.lastAtmosphereUpdateAt as the causal cursor so active/offline share one timeline.
 * No Haru cognition, memory, personality or intention is written here.
 */
public final class AtmosphereEvolutionEngine{
 private AtmosphereEvolutionEngine(){}

 public static void advance(WorldState s,long now){
  if(s==null||s.environment==null)return;
  if(s.atmosphere==null)s.atmosphere=new AtmosphereState();
  EnvironmentState e=s.environment;
  long prior=e.lastAtmosphereUpdateAt>0?e.lastAtmosphereUpdateAt:(s.lastSimulatedAt>0?s.lastSimulatedAt:now);
  if(now<=prior){
   s.atmosphere.syncDerived();
   if(e.lastAtmosphereUpdateAt<=0)e.lastAtmosphereUpdateAt=prior;
   return;
  }

  double minutes=(now-prior)/60000.0;
  WorldArea area=s.world==null?null:s.world.areaAt(s.haruX);
  BiomeProfile biome=area==null||s.world==null?null:s.world.biome(area.biomeId);
  double elevationM=area==null?0:Math.max(-400,area.elevationM);
  double baseTemp=biome==null?24:biome.baseTemperatureC;
  double hour=((s.worldMinutes/60.0)%24.0+24.0)%24.0;
  double diurnal=Math.sin((hour-9.0)/24.0*Math.PI*2.0)*2.8;
  double exposure=WorldSemantics.exposure(area);
  double priorRain="RAIN".equals(e.weather)?e.weatherIntensity:0;
  double tempTarget=baseTemp+diurnal*exposure-priorRain*1.8-e.wind*.45-Math.max(0,elevationM)*.0035;
  if(area!=null&&!area.weatherExposed)tempTarget=tempTarget*.35+22*.65;

  double biomeMoisture=biome==null?.48:biome.baseMoisture;
  double localMoisture=biomeMoisture+(area==null?0:area.localMoistureOffset);
  if(hasTag(area,"water")||hasTag(area,"wet_margin"))localMoisture+=.08;
  if(hasTag(area,"mist"))localMoisture+=.04;
  localMoisture=cl(localMoisture);
  double humidityTarget=cl(.20+localMoisture*.66+priorRain*.16-e.wind*.04);
  double pressureTarget=101.325*Math.exp(-elevationM/8434.5);

  s.atmosphere.temperatureC=follow(s.atmosphere.temperatureC,tempTarget,minutes,18);
  s.atmosphere.relativeHumidity=follow(s.atmosphere.relativeHumidity,humidityTarget,minutes,12);
  s.atmosphere.pressureKPa=follow(s.atmosphere.pressureKPa,pressureTarget,minutes,35);
  s.atmosphere.oxygenFraction=follow(s.atmosphere.oxygenFraction,.2095,minutes,120);
  s.atmosphere.carbonDioxideFraction=follow(s.atmosphere.carbonDioxideFraction,.00042,minutes,180);
  s.atmosphere.airQuality=follow(s.atmosphere.airQuality,1.0,minutes,90);

  double force=0,evolution=0;
  AtmospherePerturbation a=s.atmospherePerturbation;
  if(a!=null&&a.active){
   double age=(now-a.startedAt)/60000.0;
   if(age>=a.durationMinutes){
    a.active=false;
    WorldEventBus.publishId(s,now,"atmo_end_"+a.id,"ATMOSPHERIC_PERTURBATION_ENDED","environment","The atmospheric disturbance dissipated.");
   }else{
    force=a.influenceAt(now);
    evolution=a.evolution;
   }
  }

  double naturalCloud=cl((s.atmosphere.relativeHumidity-.42)*1.12+localMoisture*.20);
  double cloudTarget=cl(naturalCloud+force*.34+evolution*.035);
  double windTarget=cl(.08+Math.abs(tempTarget-s.atmosphere.temperatureC)*.022+cloudTarget*.08+force*.42+evolution*.02);
  e.cloudCover=follow(cl(e.cloudCover),cloudTarget,minutes,20);
  e.wind=follow(cl(e.wind),windTarget,minutes,28);

  double rainPotential=e.cloudCover*.70+s.atmosphere.relativeHumidity*.25+force*.18;
  String previous=e.weather==null?"CLEAR":e.weather;
  String next=rainPotential>=.76?"RAIN":e.cloudCover>=.48?"CLOUDY":"CLEAR";
  e.weatherIntensity="RAIN".equals(next)?cl(.30+(rainPotential-.76)*2.4):
          "CLOUDY".equals(next)?cl(.16+e.cloudCover*.52):Math.max(.05,e.cloudCover*.20);
  if(!next.equals(previous)){
   e.weather=next;
   e.weatherSince=now;
   WorldEventBus.publishId(s,now,"atmo_weather_"+Long.toHexString(now)+"_"+next,"WEATHER",next,"Physical atmospheric conditions crossed into "+next+".");
  }else e.weather=next;

  s.atmosphere.syncDerived();
  e.lastAtmosphereUpdateAt=now;
 }

 private static boolean hasTag(WorldArea a,String q){
  if(a==null||a.tags==null||q==null)return false;
  for(String x:a.tags.split(","))if(q.equalsIgnoreCase(x.trim()))return true;
  return false;
 }
 private static double follow(double v,double target,double minutes,double tauMin){
  if(minutes<=0)return v;
  double k=1-Math.exp(-minutes/Math.max(.05,tauMin));
  return v+(target-v)*k;
 }
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
