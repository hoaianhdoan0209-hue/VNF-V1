package com.aicharacter.v3;
import org.json.JSONObject;
/**
 * Persistent global/background atmosphere for the VNF world.
 * Biome/local microclimate is derived separately by EcologyEngine and never changes this state because Haru moved.
 */
public final class AtmosphereState{
 public double oxygenFraction=.2095,inertGasFraction=.79008,carbonDioxideFraction=.00042,pressureKPa=101.325,temperatureC=24.0,relativeHumidity=.62,airQuality=1.0,airDensityKgM3=1.184;

 public void syncDerived(){
  oxygenFraction=finiteOr(oxygenFraction,.2095);
  carbonDioxideFraction=finiteOr(carbonDioxideFraction,.00042);
  pressureKPa=finiteOr(pressureKPa,101.325);
  temperatureC=finiteOr(temperatureC,24.0);
  relativeHumidity=finiteOr(relativeHumidity,.62);
  airQuality=finiteOr(airQuality,1.0);

  oxygenFraction=clamp(oxygenFraction,.15,.24);
  carbonDioxideFraction=clamp(carbonDioxideFraction,0,.02);
  inertGasFraction=Math.max(0,1.0-oxygenFraction-carbonDioxideFraction);
  pressureKPa=clamp(pressureKPa,55,115);
  temperatureC=clamp(temperatureC,-35,55);
  relativeHumidity=clamp(relativeHumidity,0,1);
  airQuality=clamp(airQuality,0,1);

  double kelvin=Math.max(180,temperatureC+273.15);
  double totalPa=pressureKPa*1000.0;
  double saturationPa=610.94*Math.exp(17.625*temperatureC/(temperatureC+243.04));
  if(!Double.isFinite(saturationPa)||saturationPa<0)saturationPa=0;
  double vaporPa=Math.min(totalPa*.08,Math.max(0,relativeHumidity*saturationPa));
  double dryPa=Math.max(0,totalPa-vaporPa);
  double density=dryPa/(287.05*kelvin)+vaporPa/(461.495*kelvin);
  airDensityKgM3=clamp(finiteOr(density,1.184),.55,1.60);
 }

 public JSONObject toJson(){syncDerived();JSONObject j=new JSONObject();try{j.put("oxygenFraction",oxygenFraction);j.put("inertGasFraction",inertGasFraction);j.put("carbonDioxideFraction",carbonDioxideFraction);j.put("pressureKPa",pressureKPa);j.put("temperatureC",temperatureC);j.put("relativeHumidity",relativeHumidity);j.put("airQuality",airQuality);j.put("airDensityKgM3",airDensityKgM3);}catch(Exception ignored){}return j;}
 public static AtmosphereState fromJson(JSONObject j){AtmosphereState a=new AtmosphereState();if(j!=null){a.oxygenFraction=j.optDouble("oxygenFraction",a.oxygenFraction);a.inertGasFraction=j.optDouble("inertGasFraction",a.inertGasFraction);a.carbonDioxideFraction=j.optDouble("carbonDioxideFraction",a.carbonDioxideFraction);a.pressureKPa=j.optDouble("pressureKPa",a.pressureKPa);a.temperatureC=j.optDouble("temperatureC",a.temperatureC);a.relativeHumidity=j.optDouble("relativeHumidity",a.relativeHumidity);a.airQuality=j.optDouble("airQuality",a.airQuality);a.airDensityKgM3=j.optDouble("airDensityKgM3",a.airDensityKgM3);}a.syncDerived();return a;}
 private static double finiteOr(double v,double fallback){return Double.isFinite(v)?v:fallback;}
 private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
}
