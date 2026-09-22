package com.aicharacter.v3;
/** Advances respiratory state from physical air plus metabolic demand. Air limitation and breathing intensity remain separate causes. */
public final class RespirationEngine{private RespirationEngine(){}
 public static void advance(WorldState s,long now){
  if(s==null)return;if(s.atmosphere==null)s.atmosphere=new AtmosphereState();if(s.respiration==null)s.respiration=new RespirationState();RespirationState r=s.respiration;long prior=r.lastUpdatedAt<=0?now:r.lastUpdatedAt;if(now<=prior){r.lastUpdatedAt=Math.max(r.lastUpdatedAt,now);return;}double minutes=(now-prior)/60000.0;r.lastUpdatedAt=now;
  double available=s.atmosphere.oxygenFraction*(s.atmosphere.pressureKPa/101.325);double targetSat=available>=.195?.98:available>=.18?.95:available>=.16?.90:.82;double airLoad=cl((.205-available)*8+(1-s.atmosphere.airQuality)*.45);
  double demand=s.metabolism==null?.18:s.metabolism.demand,debt=s.metabolism==null?0:s.metabolism.oxygenDebt;double targetVent=cl(.12+demand*.58+debt*.42+airLoad*.28);
  double kSat=1-Math.exp(-minutes/8.0),kLoad=1-Math.exp(-minutes/5.0),kVent=1-Math.exp(-minutes/1.8);r.oxygenSaturation+=(targetSat-r.oxygenSaturation)*kSat;r.breathingLoad+=(airLoad-r.breathingLoad)*kLoad;r.ventilationDrive+=(targetVent-r.ventilationDrive)*kVent;
  r.oxygenSaturation=Math.max(.5,Math.min(1,r.oxygenSaturation));r.breathingLoad=cl(r.breathingLoad);r.ventilationDrive=cl(r.ventilationDrive);
 }
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
