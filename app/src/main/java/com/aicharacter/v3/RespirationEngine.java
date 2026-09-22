package com.aicharacter.v3;
/** Advances respiratory state from physical air plus metabolic demand. Air limitation and breathing intensity remain separate causes. */
public final class RespirationEngine{private RespirationEngine(){}
 public static void advance(WorldState s,long now){
  if(s==null)return;if(s.atmosphere==null)s.atmosphere=new AtmosphereState();if(s.respiration==null)s.respiration=new RespirationState();RespirationState r=s.respiration;r.normalize();long prior=r.lastUpdatedAt<=0?now:r.lastUpdatedAt;if(now<=prior){r.lastUpdatedAt=Math.max(r.lastUpdatedAt,now);return;}double minutes=(now-prior)/60000.0;if(!Double.isFinite(minutes)||minutes<=0){r.lastUpdatedAt=now;return;}r.lastUpdatedAt=now;
  double oxygen=finite(s.atmosphere.oxygenFraction,.2095),pressure=finite(s.atmosphere.pressureKPa,101.325),quality=unit(s.atmosphere.airQuality,1);double available=finite(oxygen*(pressure/101.325),.2095);double targetSat=available>=.195?.98:available>=.18?.95:available>=.16?.90:.82;double airLoad=unit((.205-available)*8+(1-quality)*.45,0);
  double demand=s.metabolism==null?.18:unit(s.metabolism.demand,.18),debt=s.metabolism==null?0:unit(s.metabolism.oxygenDebt,0);double targetVent=unit(.12+demand*.58+debt*.42+airLoad*.28,.18);
  double kSat=finite(1-Math.exp(-minutes/8.0),0),kLoad=finite(1-Math.exp(-minutes/5.0),0),kVent=finite(1-Math.exp(-minutes/1.8),0);r.oxygenSaturation+= (targetSat-r.oxygenSaturation)*kSat;r.breathingLoad+=(airLoad-r.breathingLoad)*kLoad;r.ventilationDrive+=(targetVent-r.ventilationDrive)*kVent;r.normalize();
 }
 private static double finite(double v,double fallback){return Double.isFinite(v)?v:fallback;}
 private static double unit(double v,double fallback){v=finite(v,fallback);return Math.max(0,Math.min(1,v));}
}
