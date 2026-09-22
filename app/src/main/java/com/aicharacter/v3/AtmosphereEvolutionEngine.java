package com.aicharacter.v3;
/**
 * Evolves the persisted GLOBAL/background atmosphere.
 * Contract: target values are world-scale and MUST NOT depend on Haru position.
 * Biome/water/shelter microclimate is derived separately by EcologyEngine.
 */
public final class AtmosphereEvolutionEngine{
 private AtmosphereEvolutionEngine(){}

 public static void advance(WorldState s,long now){
  if(s==null||s.environment==null)return;
  if(s.atmosphere==null)s.atmosphere=new AtmosphereState();
  s.atmosphere.syncDerived();
  EnvironmentState e=s.environment;
  long prior=e.lastAtmosphereUpdateAt>0?e.lastAtmosphereUpdateAt:(s.lastSimulatedAt>0?s.lastSimulatedAt:now);
  if(now<=prior){
   if(e.lastAtmosphereUpdateAt<=0)e.lastAtmosphereUpdateAt=prior;
   return;
  }

  double minutes=(now-prior)/60000.0;
  double hour=((s.worldMinutes/60.0)%24.0+24.0)%24.0;
  double diurnal=Math.sin((hour-9.0)/24.0*Math.PI*2.0)*2.4;
  double priorRain="RAIN".equals(e.weather)?finite01(e.weatherIntensity):0;

  WorldClimate climate=worldClimate(s);
  double tempTarget=climate.baseTemperatureC+diurnal-priorRain*1.4-finite01(e.wind)*.35;
  double humidityTarget=cl(.22+climate.baseMoisture*.62+priorRain*.16-finite01(e.wind)*.04);
  double pressureTarget=101.325*Math.exp(-climate.meanElevationM/8434.5);

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
    force=finite01(a.influenceAt(now));
    evolution=Double.isFinite(a.evolution)?a.evolution:0;
   }
  }

  double naturalCloud=cl((s.atmosphere.relativeHumidity-.42)*1.08+climate.baseMoisture*.18);
  double cloudTarget=cl(naturalCloud+force*.34+evolution*.035);
  double windTarget=cl(.08+Math.abs(tempTarget-s.atmosphere.temperatureC)*.020+cloudTarget*.08+force*.42+evolution*.02);
  e.cloudCover=follow(finite01(e.cloudCover),cloudTarget,minutes,20);
  e.wind=follow(finite01(e.wind),windTarget,minutes,28);

  double rainPotential=e.cloudCover*.70+s.atmosphere.relativeHumidity*.25+force*.18;
  String previous=e.weather==null?"CLEAR":e.weather;
  String next=rainPotential>=.76?"RAIN":e.cloudCover>=.48?"CLOUDY":"CLEAR";
  e.weatherIntensity="RAIN".equals(next)?cl(.30+(rainPotential-.76)*2.4):
          "CLOUDY".equals(next)?cl(.16+e.cloudCover*.52):Math.max(.05,e.cloudCover*.20);
  if(!next.equals(previous)){
   e.weather=next;
   e.weatherSince=now;
   WorldEventBus.publishId(s,now,"atmo_weather_"+Long.toHexString(now)+"_"+next,"WEATHER",next,"Global atmospheric conditions crossed into "+next+".");
  }else e.weather=next;

  s.atmosphere.syncDerived();
  e.lastAtmosphereUpdateAt=now;
 }

 private static WorldClimate worldClimate(WorldState s){
  if(s==null||s.world==null||s.world.areas.isEmpty())return new WorldClimate(24,.50,0);
  double temp=0,moisture=0,elevation=0,weight=0;
  for(WorldArea area:s.world.areas){
   if(area==null)continue;
   BiomeProfile b=s.world.biome(area.biomeId);
   double span=Math.max(1,area.right-area.left);
   double t=b==null?24:b.baseTemperatureC;
   double m=b==null?.50:b.baseMoisture;
   temp+=t*span;
   moisture+=cl(m)*span;
   elevation+=Math.max(-400,area.elevationM)*span;
   weight+=span;
  }
  if(weight<=0)return new WorldClimate(24,.50,0);
  return new WorldClimate(temp/weight,moisture/weight,elevation/weight);
 }

 private static final class WorldClimate{
  final double baseTemperatureC,baseMoisture,meanElevationM;
  WorldClimate(double t,double m,double e){baseTemperatureC=t;baseMoisture=m;meanElevationM=e;}
 }

 private static double follow(double v,double target,double minutes,double tauMin){
  v=Double.isFinite(v)?v:target;
  target=Double.isFinite(target)?target:v;
  if(minutes<=0||!Double.isFinite(minutes))return v;
  double k=1-Math.exp(-minutes/Math.max(.05,tauMin));
  return v+(target-v)*k;
 }
 private static double finite01(double v){return Double.isFinite(v)?cl(v):0;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
