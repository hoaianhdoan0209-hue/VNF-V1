package com.aicharacter.v3;
import org.json.JSONObject;
public final class EnvironmentState {
 public String weather="CLEAR";public double weatherIntensity=.15,cloudCover=.15,wind=.12,ambientBrightness=.9;public long weatherSince,lastNaturalWeatherBucket=-1;
 public void setWeather(String w,double intensity,long now){weather=w;weatherIntensity=intensity;cloudCover="RAIN".equals(w)?.84:"CLOUDY".equals(w)?.58:.15;weatherSince=now;}
 public void updateForTime(double minutes){double h=minutes/60.0;double daylight=Math.max(.16,Math.sin((h-5.5)/13.0*Math.PI));ambientBrightness=Math.max(.12,Math.min(1,daylight*(1-cloudCover*.38)));}
 public String dayPhase(double minutes){double h=minutes/60.0;return h<6||h>=20?"NIGHT":h<10?"MORNING":h<17?"DAY":"EVENING";}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("weather",weather);j.put("weatherIntensity",weatherIntensity);j.put("cloudCover",cloudCover);j.put("wind",wind);j.put("ambientBrightness",ambientBrightness);j.put("weatherSince",weatherSince);j.put("lastNaturalWeatherBucket",lastNaturalWeatherBucket);}catch(Exception ignored){}return j;}
 public static EnvironmentState fromJson(JSONObject j){EnvironmentState e=new EnvironmentState();if(j==null)return e;e.weather=j.optString("weather","CLEAR").toUpperCase();e.weatherIntensity=j.optDouble("weatherIntensity",.15);e.cloudCover=j.optDouble("cloudCover",.15);e.wind=j.optDouble("wind",.12);e.ambientBrightness=j.optDouble("ambientBrightness",.9);e.weatherSince=j.optLong("weatherSince",0);e.lastNaturalWeatherBucket=j.optLong("lastNaturalWeatherBucket",-1);return e;}
}
