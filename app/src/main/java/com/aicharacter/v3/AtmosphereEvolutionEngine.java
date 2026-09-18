package com.aicharacter.v3;
/** Evolves a committed atmospheric cause on the one-way causal timeline. No girl state is written here. */
public final class AtmosphereEvolutionEngine{
 private AtmosphereEvolutionEngine(){}
 public static void advance(WorldState s,long now){advanceNatural(s,now);
  if(s==null||s.environment==null)return;AtmospherePerturbation a=s.atmospherePerturbation;if(a==null||!a.active)return;
  double force=a.influenceAt(now);double age=(now-a.startedAt)/60000.0;if(age>=a.durationMinutes){a.active=false;WorldEventBus.publishId(s,now,"atmo_end_"+a.id,"ATMOSPHERIC_PERTURBATION_ENDED","environment","The atmospheric disturbance dissipated.");return;}
  // Small causes alter continuous fields first; named weather is only a thresholded downstream state.
  double targetCloud=Math.max(0,Math.min(1,s.environment.cloudCover+force*.10+a.evolution*.018));
  double targetWind=Math.max(0,Math.min(1,s.environment.wind+force*.055+a.evolution*.012));
  s.environment.cloudCover=targetCloud;s.environment.wind=targetWind;
  String prior=s.environment.weather,next=prior;double rainPotential=targetCloud*.72+targetWind*.18+force*.24;
  if(rainPotential>=.78)next="RAIN";else if(targetCloud>=.52)next="CLOUDY";else if(targetCloud<.34&&force<.18)next="CLEAR";
  double wi="RAIN".equals(next)?Math.max(.35,Math.min(1,rainPotential)):"CLOUDY".equals(next)?Math.max(.18,targetCloud*.55):Math.max(.08,targetCloud*.25);
  s.environment.weatherIntensity=wi;
  if(!next.equals(prior)){s.environment.weather=next;s.environment.weatherSince=now;WorldEventBus.publishId(s,now,"atmo_weather_"+a.id+"_"+next,"WEATHER",next,"Atmospheric conditions crossed into "+next+".");}
 }
}