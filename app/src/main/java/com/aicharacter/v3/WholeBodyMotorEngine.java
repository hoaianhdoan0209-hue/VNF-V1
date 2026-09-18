package com.aicharacter.v3;
/** Couples ground contact, pelvis, spine, shoulders and head into one locomotor pose. */
public final class WholeBodyMotorEngine{private WholeBodyMotorEngine(){}
 public static void advance(WorldState s,double dt){if(s==null||dt<=0)return;if(s.bodyRig==null)s.bodyRig=new BodyRigState();BodyRigState r=s.bodyRig;PhysicsBodyState p=s.girlPhysics;BodyInstinctState b=s.bodyInstinct;double speed=s.girlTravel!=null&&s.girlTravel.active?s.girlTravel.lastSpeed:0;double gait=Math.max(0,Math.min(1,speed/92.0));r.stridePhase=(r.stridePhase+dt*(1.1+gait*1.7))%1.0;double wave=Math.sin(r.stridePhase*Math.PI*2);double imbalance=p==null?0:(1-p.balance);double guard=b==null?0:b.guarding,breath=b==null?0:b.breathDrive,fatigue=b==null?0:b.fatigueDroop;
  double load=.5+wave*.32*gait;r.leftLegLoad=clamp(load,.08,.92);r.rightLegLoad=1-r.leftLegLoad;
  r.pelvisTilt=wave*.045*gait+imbalance*.06;r.spineLean=clamp(gait*.035+fatigue*.09+guard*.07,-.12,.18);r.shoulderCounter=-wave*.10*gait-guard*.08;r.headStabilization=clamp(-r.spineLean*.55-breath*.015,-.10,.10);r.stanceWidth=clamp(.48+imbalance*.22+guard*.10,.35,.82);
 }
 private static double clamp(double v,double a,double b){return Math.max(a,Math.min(b,v));}
}