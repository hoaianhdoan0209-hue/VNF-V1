package com.aicharacter.v3;
/** Evolves continuous physical air first; named weather is a downstream presentation/state threshold. No Haru cognition is written here. */
public final class AtmosphereEvolutionEngine{
 private AtmosphereEvolutionEngine(){}
 public static void advance(WorldState s,long now){
  if(s==null||s.environment==null)return;
  if(s.atmosphere==null)s.atmosphere=new AtmosphereState();
  double minutes=s.lastSimulatedAt>0?Math.max(0,(now-s.lastSimulatedAt)/60000.0):0;
  WorldArea area=s.world==null?null:s.world.areaAt(s.haruX);
  BiomeProfile biome=area==null||s.world==null?null:s.world.biome(area.biomeId);
  double elevationM=area==null?0:Math.max(-400,area.elevationM);
  double localTemp=area==null?s.atmosphere.temperatureC:EcologyEngine.localTemperatureC(s,area);
  double biomeMoisture=biome==null?.55:biome.baseMoisture;
  double rain="RAIN".equals(s.environment.weather)?s.environment.weatherIntensity:0;
  double humidityTarget=cl(.20+biomeMoisture*.62+rain*.22-s.environment.wind*.05);
  double pressureTarget=101.325*Math.exp(-elevationM/8434.5);
  double tau=Math.max(.25,minutes);
  s.atmosphere.temperatureC=follow(s.atmosphere.temperatureC,localTemp,minutes,18);
  s.atmosphere.relativeHumidity=follow(s.atmosphere.relativeHumidity,humidityTarget,minutes,12);
  s.atmosphere.pressureKPa=follow(s.atmosphere.pressureKPa,pressureTarget,minutes,35);
  s.atmosphere.oxygenFraction=follow(s.atmosphere.oxygenFraction,.2095,minutes,120);
  s.atmosphere.carbonDioxideFraction=follow(s.atmosphere.carbonDioxideFraction,.00042,minutes,180);
  s.atmosphere.airQuality=follow(s.atmosphere.airQuality,1.0,minutes,90);

  AtmospherePerturbation a=s.atmospherePerturbation;
  if(a!=null&&a.active){
   double force=a.influenceAt(now),age=(now-a.startedAt)/60000.0;
   if(age>=a.durationMinutes){a.active=false;WorldEventBus.publishId(s,now,"atmo_end_"+a.id,"ATMOSPHERIC_PERTURBATION_ENDED","environment","The atmospheric disturbance dissipated.");}
   else{
    double targetCloud=cl(s.environment.cloudCover+force*.10+a.evolution*.018);
    double targetWind=cl(s.environment.wind+force*.055+a.evolution*.012);
    s.environment.cloudCover=targetCloud;s.environment.wind=targetWind;
    String prior=s.environment.weather,next=prior;double rainPotential=targetCloud*.72+targetWind*.18+force*.24;
    if(rainPotential>=.78)next="RAIN";else if(targetCloud>=.52)next="CLOUDY";else if(targetCloud<.34&&force<.18)next="CLEAR";
    s.environment.weatherIntensity="RAIN".equals(next)?Math.max(.35,Math.min(1,rainPotential)):"CLOUDY".equals(next)?Math.max(.18,targetCloud*.55):Math.max(.08,targetCloud*.25);
    if(!next.equals(prior)){s.environment.weather=next;s.environment.weatherSince=now;WorldEventBus.publishId(s,now,"atmo_weather_"+a.id+"_"+next,"WEATHER",next,"Atmospheric conditions crossed into "+next+".");}
   }
  }
  s.atmosphere.syncDerived();
 }
 private static double follow(double v,double target,double minutes,double tauMin){if(minutes<=0)return v;double k=1-Math.exp(-minutes/Math.max(.05,tauMin));return v+(target-v)*k;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
