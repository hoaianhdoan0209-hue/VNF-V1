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
  this.breathingIntensity=unit(breathing,0);this.postureLoad=unit(posture,0);this.tremor=unit(tremor,0);this.fatigue=unit(fatigue,0);this.gaitChange=unit(gait,0);this.gaitAsymmetry=signed(asym);this.thermalDiscomfort=signed(thermal);this.recoveryLoad=unit(recovery,0);this.recoveryState=recoveryState==null?"BASELINE":recoveryState;this.dominantPainRegion=region;this.dominantPain=unit(dominant,0);
  LocalizedPainState x=p==null?new LocalizedPainState():p;headPain=unit(x.head,0);neckPain=unit(x.neck,0);chestPain=unit(x.chest,0);abdomenPain=unit(x.abdomen,0);leftArmPain=unit(x.leftArm,0);rightArmPain=unit(x.rightArm,0);leftLegPain=unit(x.leftLeg,0);rightLegPain=unit(x.rightLeg,0);
 }

 public static BiologyVisualOutput from(WorldState s){
  if(s==null)return new BiologyVisualOutput(0,0,0,0,0,0,0,0,"BASELINE",null,0,null);
  RespirationState r=s.respiration;BodyInstinctState instinct=s.bodyInstinct;MusculoskeletalState mus=s.musculoskeletal;NervousState nervous=s.nervous;LocalizedPainState pain=s.localizedPain;ThermalState thermal=s.thermal;MetabolismState metabolism=s.metabolism;
  double ventilation=r==null?.16:unit(r.ventilationDrive,.16),airEffort=r==null?0:unit(r.breathingLoad,0),oxygenDebt=metabolism==null?0:unit(metabolism.oxygenDebt,0);
  double breathing=unit(ventilation*.78+airEffort*.28+oxygenDebt*.24,0);
  double legFatigue=mus==null?0:unit(mus.legFatigue,0),coreFatigue=mus==null?0:unit(mus.coreFatigue,0);
  double bodyEnergy=s.body==null?72:finite(s.body.energy,72),bodySleepiness=s.body==null?22:finite(s.body.sleepiness,22);
  double lowEnergy=unit((42-bodyEnergy)/42.0,0),sleepiness=unit(bodySleepiness/100.0,0);
  double fatigue=unit(legFatigue*.34+coreFatigue*.20+lowEnergy*.22+sleepiness*.22+oxygenDebt*.18,0);
  double guard=instinct==null?0:unit(instinct.guarding,0),droop=instinct==null?0:unit(instinct.fatigueDroop,0),balance=nervous==null?0:unit(nervous.balanceAlarm,0);
  double posture=unit(guard*.38+droop*.34+fatigue*.24+balance*.20,0);
  double shiver=instinct==null?0:unit(instinct.shiver,0),correction=nervous==null?0:unit(nervous.motorCorrection,0);
  double tremor=unit(shiver*.72+correction*.16+fatigue*.18,0);
  double lp=pain==null?0:unit(pain.leftLeg,0),rp=pain==null?0:unit(pain.rightLeg,0),legPain=Math.max(lp,rp),asym=rp-lp;
  double locomotor=unit(MusculoskeletalEngine.locomotorCapacity(s),1);
  double gait=unit(Math.abs(asym)*.62+legPain*.30+legFatigue*.34+balance*.26+(1-locomotor)*.38,0);
  double thermalSigned=thermal==null?0:unit(thermal.heatLoad,0)-unit(thermal.coldLoad,0);
  double recovery=unit((BodyRhythmEngine.isSleeping(s)?1:BodyRhythmEngine.isResting(s)?.62:0)+Math.max(0,legPain-.18)*.30+fatigue*.16,0);
  String state=BodyRhythmEngine.isSleeping(s)?"SLEEP_RECOVERY":BodyRhythmEngine.isResting(s)?"REST_RECOVERY":recovery>.45?"ACTIVE_RECOVERY":fatigue>.66?"STRAINED":"BASELINE";
  HumanAnatomyModel.Region region=HumanAnatomyModel.dominantPainRegion(s);double dominant=region==null?0:unit(HumanAnatomyModel.painLoad(s,region),0);
  return new BiologyVisualOutput(breathing,posture,tremor,fatigue,gait,asym,thermalSigned,recovery,state,region,dominant,pain);
 }
 private static double finite(double v,double fallback){return Double.isFinite(v)?v:fallback;}
 private static double unit(double v,double fallback){v=finite(v,fallback);return Math.max(0,Math.min(1,v));}
 private static double signed(double v){if(!Double.isFinite(v))return 0;return Math.max(-1,Math.min(1,v));}
}
