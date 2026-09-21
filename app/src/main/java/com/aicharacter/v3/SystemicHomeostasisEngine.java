package com.aicharacter.v3;
/** Shared active/offline endocrine + skin + immune coupling. It regulates physiology only and never selects intentions. */
public final class SystemicHomeostasisEngine{
 private SystemicHomeostasisEngine(){}
 public static void advance(WorldState s,double seconds,long now){
  if(s==null||seconds<=0||s.body==null)return;ensure(s);double hours=seconds/3600.0;EndocrineState e=s.endocrine;SkinState k=s.skin;ImmuneState im=s.immune;
  double pain=cl(s.body.pain/100.0),fear=s.emotion==null?0:cl(s.emotion.fear),thermal=s.thermal==null?0:Math.max(s.thermal.coldLoad,s.thermal.heatLoad),sleep=cl(s.body.sleepiness/100.0);
  double hydration=s.hydration==null?1:s.hydration.hydration,nutrient=s.digestive==null?1:s.digestive.nutrientReserve,hunger=DigestionHydrationEngine.hunger(s),injury=s.localizedPain==null?0:s.localizedPain.maxLoad();
  boolean sleeping=BodyRhythmEngine.isSleeping(s);double stressTarget=cl(pain*.30+fear*.20+thermal*.20+Math.max(0,.58-hydration)*.55+hunger*.13+injury*.22+(sleeping?0:sleep*.10));
  e.stressResponse=follow(e.stressResponse,stressTarget,hours,sleeping?.65:.28);
  double hour=s.worldMinutes/60.0,night=(hour>=21||hour<6)?.88:(hour>=19||hour<8)?.55:.16;
  e.circadianSleepSignal=follow(e.circadianSleepSignal,cl(night*.72+sleep*.28),hours,.45);
  e.metabolicSupport=follow(e.metabolicSupport,cl(.50+nutrient*.25+hydration*.22-e.stressResponse*.14),hours,.8);
  e.recoverySignal=follow(e.recoverySignal,cl((sleeping?.92:.58)*(1-e.stressResponse*.38)*(s.body.energy/100.0*.25+.75)),hours,.7);
  double wet=s.thermal==null?s.worldWetness:Math.max(s.worldWetness,s.thermal.skinWetness),sweat=s.bodyInstinct==null?0:s.bodyInstinct.sweat;
  k.moistureExposure=follow(k.moistureExposure,cl(wet*.82+sweat*.28),hours,.22);k.thermoregulationStrain=follow(k.thermoregulationStrain,thermal,hours,.28);
  double irritationTarget=cl(k.moistureExposure*.32+k.thermoregulationStrain*.28+k.surfaceDamage*.42);k.irritation=follow(k.irritation,irritationTarget,hours,.65);
  double barrierTarget=cl(1-k.surfaceDamage*.62-k.irritation*.24-Math.max(0,k.moistureExposure-.62)*.22);k.barrierIntegrity=follow(k.barrierIntegrity,barrierTarget,hours,1.4);
  double repair=(.18+.82*e.recoverySignal)*(.42+.58*im.readiness);k.surfaceDamage=Math.max(0,k.surfaceDamage-hours*.045*repair);
  WorldArea area=s.world==null?null:s.world.areaAt(s.haruX);double exposure=WorldSemantics.exposure(area),moist=EcologyEngine.localMoisture(s,area);
  double envTarget=cl(exposure*moist*(1-k.barrierIntegrity)*.85+k.surfaceDamage*.30);im.environmentalPressure=follow(im.environmentalPressure,envTarget,hours,1.1);
  double inflammationTarget=cl(injury*.38+k.surfaceDamage*.34+k.irritation*.18+im.environmentalPressure*.22);im.inflammation=follow(im.inflammation,inflammationTarget,hours,.85);
  double readinessTarget=cl(.48+nutrient*.18+hydration*.16+(sleeping?.14:0)-e.stressResponse*.24-im.inflammation*.16);im.readiness=follow(im.readiness,readinessTarget,hours,2.2);
  im.recoverySupport=follow(im.recoverySupport,cl(.45+im.readiness*.42-im.inflammation*.24),hours,1.0);
  if(im.inflammation>.72){s.body.energy=Math.max(0,s.body.energy-hours*(im.inflammation-.65)*2.4);s.body.clamp();}
  e.lastUpdatedAt=Math.max(e.lastUpdatedAt,now);k.lastUpdatedAt=Math.max(k.lastUpdatedAt,now);im.lastUpdatedAt=Math.max(im.lastUpdatedAt,now);
 }
 public static void registerImpact(WorldState s,double excess,long now){if(s==null||excess<=0)return;ensure(s);double q=cl(excess/950.0);s.skin.surfaceDamage=cl(s.skin.surfaceDamage+q*.22);s.skin.irritation=cl(s.skin.irritation+q*.12);s.skin.lastUpdatedAt=Math.max(s.skin.lastUpdatedAt,now);}
 public static double recoveryMultiplier(WorldState s){if(s==null)return 1;ensure(s);return Math.max(.45,Math.min(1.15,(.62+.38*s.immune.recoverySupport)*(.78+.22*s.endocrine.recoverySignal)));}
 private static void ensure(WorldState s){if(s.endocrine==null)s.endocrine=new EndocrineState();if(s.skin==null)s.skin=new SkinState();if(s.immune==null)s.immune=new ImmuneState();}
 private static double follow(double v,double target,double hours,double tauHours){double k=1-Math.exp(-hours/Math.max(.02,tauHours));return cl(v+(target-v)*k);}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}