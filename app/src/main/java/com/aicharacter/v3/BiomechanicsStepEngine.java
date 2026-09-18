package com.aicharacter.v3;
/** Shared active/offline ordering for mechanics. Keeps biology -> muscle -> rig -> joints -> physics causal order identical. */
public final class BiomechanicsStepEngine{private BiomechanicsStepEngine(){}
 public static void advance(WorldState s,double seconds,long now){if(s==null||seconds<=0)return;double remain=Math.min(seconds,3600);while(remain>0){double dt=Math.min(remain,.25);CardioMetabolicEngine.advance(s,dt);MusculoskeletalEngine.advance(s,dt);WholeBodyMotorEngine.advance(s,dt);JointConstraintEngine.solve(s);CatWholeBodyMotorEngine.advance(s,dt);long subNow=Math.max(0L,now-(long)(remain*1000.0)+(long)(dt*1000.0));WholeBodyPhysicsEngine.prepare(s,"girl",dt,subNow);WholeBodyPhysicsEngine.prepare(s,"cat",dt,subNow);remain-=dt;}}
}