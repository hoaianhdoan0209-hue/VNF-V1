package com.aicharacter.v3;
/** Causal world consequences of weather/air. World state is mutated here; rendering only reads the result. */
public final class EnvironmentConsequences {private EnvironmentConsequences(){}
 public static void advance(WorldState s,double minutes){
  if(s==null||s.environment==null||minutes<=0)return;
  WorldArea a=s.world==null?null:s.world.areaAt(s.haruX);
  double rain="RAIN".equals(s.environment.weather)?s.environment.weatherIntensity:0;
  double exp=WorldSemantics.exposure(a);
  double humidity=s.atmosphere==null?.55:s.atmosphere.relativeHumidity;
  double temp=EcologyEngine.localTemperatureC(s,a);
  double wetGain=rain*exp*minutes/90.0;
  double evaporative=Math.max(.08,1-humidity*.72)*(1+.55*s.environment.wind)*Math.max(.35,(temp+10)/35.0);
  double dryRate=(rain>0?(1-exp)/150.0:evaporative/180.0)*minutes;
  s.worldWetness=Math.max(0,Math.min(1,s.worldWetness+wetGain-dryRate));
  s.visibility=WorldSemantics.visibility(s,a);
  if(s.worldWetness>.65&&exp>.7){
   double vulnerability=1+s.body.injuryBurden()*.45;
   s.body.energy=Math.max(0,s.body.energy-minutes*.025*vulnerability);
   s.mood.pleasantness=Math.max(-1,s.mood.pleasantness-minutes*.0015);
  }
 }
}
