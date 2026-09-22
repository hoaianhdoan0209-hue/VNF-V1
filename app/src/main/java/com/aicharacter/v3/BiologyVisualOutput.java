package com.aicharacter.v3;

/**
 * Read-only biology -> presentation contract.
 * Renderer/visual code may consume this snapshot, but must not mutate physiology.
 * All normalized channels are derived from WorldState truth.
 */
public final class BiologyVisualOutput {
 public final double breathingIntensity;
 public final double postureLoad;
 public final double tremor;
 public final double fatigue;
 public final double gaitChange;
 public final double gaitAsymmetry;
 public final double thermalDiscomfort;
 public final double recoveryLoad;
 public final String recoveryState;
 public final HumanAnatomyModel.Region dominantPainRegion;
 public final double dominantPain;
 public final double headPain,neckPain,chestPain,abdomenPain,leftArmPain,rightArmPain,leftLegPain,rightLegPain;

 private BiologyVisualOutput(double breathing,double posture,double tremor,double fatigue,double gait,double asym,double thermal,double recovery,String recoveryState,HumanAnatomyModel.Region region,double dominant,LocalizedPainState p){
  this.breathingIntensity=cl(breathing);this.postureLoad=cl(posture);this.tremor=cl(tremor);this.fatigue=cl(fatigue);this.gaitChange=cl(gait);this.gaitAsymmetry=Math.max(-1,Math.min(1,asym));this.thermalDiscomfort=Math.max(-1,Math.min(1,thermal));this.recoveryLoad=cl(recovery);this.recoveryState=recoveryState==null?"BASELINE":recoveryState;this.dominantPainRegion=region;this.dominantPain=cl(dominant);
  LocalizedPainState x=p==null?new LocalizedPainState():p;headPain=cl(x.head);neckPain=cl(x.neck);chestPain=cl(x.chest);abdomenPain=cl(x.abdomen);leftArmPain=cl(x.leftArm);rightArmPain=cl(x.rightArm);leftLegPain=cl(x.leftLeg);rightLegPain=cl(x.rightLeg);
 }

 public static BiologyVisualOutput from(WorldState s){
  if(s==null)return new BiologyVisualOutput(0,0,0,0,0,0,0,0,"BASELINE",null,0,null);
  RespirationState r=s.respiration;BodyInstinctState instinct=s.bodyInstinct;MusculoskeletalState mus=s.musculoskeletal;NervousState nervous=s.nervous;LocalizedPainState pain=s.localizedPain;ThermalState thermal=s.thermal;MetabolismState metabolism=s.metabolism;
  double ventilation=r==null?.16:r.ventilationDrive,airEffort=r==null?0:r.breathingLoad,oxygenDebt=metabolism==null?0:metabolism.oxygenDebt;
  double breathing=cl(ventilation*.78+airEffort*.28+oxygenDebt*.24);
  double legFatigue=mus==null?0:mus.legFatigue,coreFatigue=mus==null?0:mus.coreFatigue;
  double lowEnergy=s.body==null?0:cl((42-s.body.energy)/42.0),sleepiness=s.body==null?0:cl(s.body.sleepiness/100.0);
  double fatigue=cl(legFatigue*.34+coreFatigue*.20+lowEnergy*.22+sleepiness*.22+oxygenDebt*.18);
  double guard=instinct==null?0:instinct.guarding,droop=instinct==null?0:instinct.fatigueDroop,balance=nervous==null?0:nervous.balanceAlarm;
  double posture=cl(guard*.38+droop*.34+fatigue*.24+balance*.20);
  double shiver=instinct==null?0:instinct.shiver,correction=nervous==null?0:nervous.motorCorrection;
  double tremor=cl(shiver*.72+correction*.16+fatigue*.18);
  double lp=pain==null?0:pain.leftLeg,rp=pain==null?0:pain.rightLeg,legPain=Math.max(lp,rp),asym=rp-lp;
  double gait=cl(Math.abs(asym)*.62+legPain*.30+legFatigue*.34+balance*.26+(1-MusculoskeletalEngine.locomotorCapacity(s))*.38);
  double thermalSigned=thermal==null?0:cl(thermal.heatLoad)-cl(thermal.coldLoad);
  double recovery=cl((BodyRhythmEngine.isSleeping(s)?1:BodyRhythmEngine.isResting(s)?.62:0)+Math.max(0,legPain-.18)*.30+fatigue*.16);
  String state=BodyRhythmEngine.isSleeping(s)?"SLEEP_RECOVERY":BodyRhythmEngine.isResting(s)?"REST_RECOVERY":recovery>.45?"ACTIVE_RECOVERY":fatigue>.66?"STRAINED":"BASELINE";
  HumanAnatomyModel.Region region=HumanAnatomyModel.dominantPainRegion(s);double dominant=region==null?0:HumanAnatomyModel.painLoad(s,region);
  return new BiologyVisualOutput(breathing,posture,tremor,fatigue,gait,asym,thermalSigned,recovery,state,region,dominant,pain);
 }
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
