package com.aicharacter.v3;
/** Shared active/offline digestion and water balance. Food becomes usable body energy gradually; heat/work increase water loss. */
public final class DigestionHydrationEngine{
 private DigestionHydrationEngine(){}
 public static void advance(WorldState s,double seconds,long now){
  if(s==null||seconds<=0||s.body==null)return;if(s.digestive==null)s.digestive=new DigestiveState();if(s.hydration==null)s.hydration=new HydrationState();
  double hours=seconds/3600.0;DigestiveState d=s.digestive;HydrationState h=s.hydration;
  double work=s.girlTravel!=null&&s.girlTravel.active?Math.min(1,s.girlTravel.lastSpeed/92.0):0,heat=s.thermal==null?0:s.thermal.heatLoad,sweat=s.bodyInstinct==null?0:s.bodyInstinct.sweat;
  double digest=Math.min(d.stomachFood,hours*(.12+.08*Math.max(.25,h.hydration)));d.stomachFood=Math.max(0,d.stomachFood-digest);d.nutrientReserve=DigestiveState.cl(d.nutrientReserve+digest*.72-hours*(.012+work*.018));
  double energyGain=digest*(18.0+10.0*d.nutrientReserve);s.body.energy=Math.min(100,s.body.energy+energyGain);
  d.digestionLoad=DigestiveState.cl(d.stomachFood*.62+(1-d.nutrientReserve)*.18);
  double waterLoss=hours*(.010+work*.012+heat*.014+sweat*.020);h.hydration=Math.max(0,h.hydration-waterLoss);h.bladderFill=Math.min(1,h.bladderFill+hours*(.018+.018*h.hydration));h.renalLoad=Math.max(0,Math.min(1,(.62-h.hydration)*1.35+h.bladderFill*.18));
  if(h.hydration<.35){s.body.energy=Math.max(0,s.body.energy-hours*(.35-h.hydration)*8);if(s.circulation!=null)s.circulation.perfusion=Math.max(.2,s.circulation.perfusion-hours*(.35-h.hydration)*.025);}
  s.body.clamp();d.lastUpdatedAt=Math.max(d.lastUpdatedAt,now);h.lastUpdatedAt=Math.max(h.lastUpdatedAt,now);
 }
 public static boolean eat(WorldState s,long now){if(s==null||s.digestive==null)return false;DigestiveState d=s.digestive;if(d.stomachFood>.82)return false;d.stomachFood=Math.min(1,d.stomachFood+.58);d.nutrientReserve=Math.min(1,d.nutrientReserve+.08);d.lastMealAt=now;d.lastUpdatedAt=now;return true;}
 public static boolean drink(WorldState s,long now){if(s==null||s.hydration==null)return false;HydrationState h=s.hydration;if(h.hydration>.94&&h.bladderFill>.72)return false;h.hydration=Math.min(1,h.hydration+.34);h.bladderFill=Math.min(1,h.bladderFill+.20);h.lastDrinkAt=now;h.lastUpdatedAt=now;return true;}
 public static boolean urinate(WorldState s,long now){if(s==null||s.hydration==null||s.hydration.bladderFill<.42)return false;s.hydration.bladderFill=Math.max(.08,s.hydration.bladderFill-.72);s.hydration.renalLoad=Math.max(0,s.hydration.renalLoad-.18);s.hydration.lastUpdatedAt=now;return true;}
 public static double hunger(WorldState s){if(s==null||s.digestive==null||s.body==null)return 0;return clamp01((1-s.digestive.stomachFood)*.58+(1-s.digestive.nutrientReserve)*.24+Math.max(0,45-s.body.energy)/45.0*.28);}
 public static double thirst(WorldState s){if(s==null||s.hydration==null)return 0;return clamp01((.76-s.hydration.hydration)/.76+s.hydration.renalLoad*.16);}
 public static double bladderUrgency(WorldState s){return s==null||s.hydration==null?0:clamp01((s.hydration.bladderFill-.45)/.55);}
 private static double clamp01(double v){return Math.max(0,Math.min(1,v));}
}