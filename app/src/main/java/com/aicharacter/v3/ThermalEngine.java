package com.aicharacter.v3;
/** Couples air, rain exposure and shelter to body thermal load on causal time. */
public final class ThermalEngine{private ThermalEngine(){}
 public static void advance(WorldState s,long now){if(s==null)return;if(s.thermal==null)s.thermal=new ThermalState();ThermalState t=s.thermal;long prior=t.lastUpdatedAt<=0?now:t.lastUpdatedAt;if(now<=prior){t.lastUpdatedAt=Math.max(t.lastUpdatedAt,now);return;}double minutes=(now-prior)/60000.0;t.lastUpdatedAt=now;WorldArea area=s.world==null?null:s.world.areaAt(s.haruX);double exposure=WorldSemantics.exposure(area),rain="RAIN".equals(s.environment.weather)?s.environment.weatherIntensity:0,humidity=s.atmosphere==null?.62:s.atmosphere.relativeHumidity,temp=EcologyEngine.localTemperatureC(s,area),wind=s.environment.wind;
 double wetTarget=Math.max(s.worldWetness,Math.min(1,rain*exposure));double wetK=1-Math.exp(-minutes/(rain>0?18.0:75.0));t.skinWetness+= (wetTarget-t.skinWetness)*wetK;
 double chill=Math.max(0,(18-temp)/18.0)+t.skinWetness*.42+wind*exposure*.22;double heat=Math.max(0,(temp-30)/15.0)+humidity*Math.max(0,(temp-27)/25.0);double k=1-Math.exp(-minutes/25.0);t.coldLoad+=(Math.min(1,chill)-t.coldLoad)*k;t.heatLoad+=(Math.min(1,heat)-t.heatLoad)*k;
 double targetCore=37.0-t.coldLoad*.55+t.heatLoad*.45;double coreK=1-Math.exp(-minutes/90.0);t.coreTemperatureC+=(targetCore-t.coreTemperatureC)*coreK;t.coreTemperatureC=Math.max(34,Math.min(40,t.coreTemperatureC));
 if(s.body!=null){double burden=Math.max(t.coldLoad,t.heatLoad);if(burden>.55)s.body.energy=Math.max(0,s.body.energy-minutes*.018*(burden-.45));if(burden>.82)s.body.health=Math.max(0,s.body.health-minutes*.002*(burden-.75));s.body.clamp();}
 }
}