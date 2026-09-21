package com.aicharacter.v3;
import org.json.JSONObject;
/** Persistent physical air state. Composition is Earth-informed reference physics, while authored VNF trace effects remain fictional. */
public final class AtmosphereState{
 public double oxygenFraction=.2095,inertGasFraction=.79008,carbonDioxideFraction=.00042,pressureKPa=101.325,temperatureC=24.0,relativeHumidity=.62,airQuality=1.0,airDensityKgM3=1.184;
 public void syncDerived(){
  oxygenFraction=clamp(oxygenFraction,.15,.24);carbonDioxideFraction=clamp(carbonDioxideFraction,0,.02);
  inertGasFraction=clamp(1.0-oxygenFraction-carbonDioxideFraction,.72,.84);
  pressureKPa=clamp(pressureKPa,55,115);temperatureC=clamp(temperatureC,-35,55);relativeHumidity=clamp(relativeHumidity,0,1);airQuality=clamp(airQuality,0,1);
  double kelvin=Math.max(180,temperatureC+273.15),dryDensity=(pressureKPa*1000.0)/(287.05*kelvin);
  airDensityKgM3=clamp(dryDensity*(1-.12*relativeHumidity),.55,1.55);
 }
 public JSONObject toJson(){syncDerived();JSONObject j=new JSONObject();try{j.put("oxygenFraction",oxygenFraction);j.put("inertGasFraction",inertGasFraction);j.put("carbonDioxideFraction",carbonDioxideFraction);j.put("pressureKPa",pressureKPa);j.put("temperatureC",temperatureC);j.put("relativeHumidity",relativeHumidity);j.put("airQuality",airQuality);j.put("airDensityKgM3",airDensityKgM3);}catch(Exception ignored){}return j;}
 public static AtmosphereState fromJson(JSONObject j){AtmosphereState a=new AtmosphereState();if(j!=null){a.oxygenFraction=j.optDouble("oxygenFraction",a.oxygenFraction);a.inertGasFraction=j.optDouble("inertGasFraction",a.inertGasFraction);a.carbonDioxideFraction=j.optDouble("carbonDioxideFraction",a.carbonDioxideFraction);a.pressureKPa=j.optDouble("pressureKPa",a.pressureKPa);a.temperatureC=j.optDouble("temperatureC",a.temperatureC);a.relativeHumidity=j.optDouble("relativeHumidity",a.relativeHumidity);a.airQuality=j.optDouble("airQuality",a.airQuality);a.airDensityKgM3=j.optDouble("airDensityKgM3",a.airDensityKgM3);}a.syncDerived();return a;}
 private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
}
