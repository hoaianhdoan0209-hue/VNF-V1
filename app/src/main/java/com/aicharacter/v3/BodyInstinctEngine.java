package com.aicharacter.v3;
/** Homeostatic automatic responses. It may alter posture/movement capacity, never choose destinations or life plans. */
public final class BodyInstinctEngine{private BodyInstinctEngine(){}
 public static void advance(WorldState s,long now){if(s==null||s.body==null)return;if(s.bodyInstinct==null)s.bodyInstinct=new BodyInstinctState();BodyInstinctState x=s.bodyInstinct;long prior=x.lastUpdatedAt<=0?now:x.lastUpdatedAt;if(now<=prior){x.lastUpdatedAt=Math.max(x.lastUpdatedAt,now);return;}double min=(now-prior)/60000.0;x.lastUpdatedAt=now;double cold=s.thermal==null?0:s.thermal.coldLoad,heat=s.thermal==null?0:s.thermal.heatLoad,breath=s.respiration==null?0:s.respiration.breathingLoad;
  x.shiver=follow(x.shiver,Math.max(0,(cold-.30)/.70),min,3);x.sweat=follow(x.sweat,Math.max(0,(heat-.28)/.72),min,6);x.breathDrive=follow(x.breathDrive,breath,min,2);x.guarding=follow(x.guarding,Math.max(0,(s.body.pain-8)/55.0),min,2);x.fatigueDroop=follow(x.fatigueDroop,Math.max(s.body.sleepiness/100.0,Math.max(0,(35-s.body.energy)/35.0)),min,8);
  double max=Math.max(Math.max(x.shiver,x.sweat),Math.max(x.breathDrive,Math.max(x.guarding,x.fatigueDroop)));if(max<.22)x.posture="NEUTRAL";else if(max==x.guarding)x.posture="GUARDING";else if(max==x.breathDrive)x.posture="CATCHING_BREATH";else if(max==x.shiver)x.posture="COLD";else if(max==x.sweat)x.posture="HEAT";else x.posture="FATIGUED";
  // Homeostasis has a real energetic cost, but it does not fabricate an intention.
  s.body.energy=Math.max(0,s.body.energy-min*(x.shiver*.010+x.breathDrive*.006+x.sweat*.003));s.body.clamp();
 }
 public static double movementCapacity(WorldState s){if(s==null||s.bodyInstinct==null)return 1;BodyInstinctState x=s.bodyInstinct;double penalty=x.shiver*.10+x.breathDrive*.24+x.guarding*.28+x.fatigueDroop*.18+Math.max(0,x.sweat-.6)*.08;return Math.max(.42,Math.min(1,1-penalty));}
 private static double follow(double v,double target,double min,double tau){target=Math.max(0,Math.min(1,target));double k=1-Math.exp(-min/tau);return Math.max(0,Math.min(1,v+(target-v)*k));}
}