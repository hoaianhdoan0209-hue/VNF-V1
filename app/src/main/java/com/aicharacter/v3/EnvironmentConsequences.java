package com.aicharacter.v3;
public final class EnvironmentConsequences {private EnvironmentConsequences(){}
 public static void advance(WorldState s,double minutes){WorldArea a=s.world==null?null:s.world.areaAt(s.haruX);double rain="RAIN".equals(s.environment.weather)?s.environment.weatherIntensity:0;double exp=WorldSemantics.exposure(a);double wetGain=rain*exp*minutes/90.0;double dryRate=rain>0?(1-exp)*minutes/150.0:minutes/180.0;s.worldWetness=Math.max(0,Math.min(1,s.worldWetness+wetGain-dryRate));s.visibility=WorldSemantics.visibility(s,a);if(s.worldWetness>.65&&exp>.7){double vulnerability=1+s.body.injuryBurden()*.45;s.body.energy=Math.max(0,s.body.energy-minutes*.025*vulnerability);s.mood.pleasantness=Math.max(-1,s.mood.pleasantness-minutes*.0015);}}
}
