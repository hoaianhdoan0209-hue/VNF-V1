package com.aicharacter.v3;
/** Interleaves locomotion and body feedback on one causal timeline. */
public final class PhysicalLifeStepEngine{private PhysicalLifeStepEngine(){}
 public static void advanceOffline(WorldState s,double seconds,long now,boolean advanceGirlTravel,boolean advanceCatTravel){
  if(s==null||seconds<=0)return;
  boolean airborne=(s.girlPhysics!=null&&!s.girlPhysics.grounded)||(s.catPhysics!=null&&!s.catPhysics.grounded);
  if(airborne){advance(s,seconds,now,advanceGirlTravel,advanceCatTravel);return;}
  int bodySteps=Math.max(1,Math.min(16,(int)Math.ceil(seconds/60.0)));
  double bodyDt=seconds/bodySteps;
  long start=Math.max(0L,now-(long)(seconds*1000.0));
  for(int i=1;i<=bodySteps;i++){
   long subNow=Math.min(now,start+(long)(bodyDt*i*1000.0));
   BiomechanicsStepEngine.advance(s,bodyDt,subNow);
   if(!BodyRhythmEngine.isSleeping(s)&&BodyRhythmEngine.shouldCollapse(s)){BodyRhythmEngine.collapse(s,subNow);advanceGirlTravel=false;break;}
  }
  if(advanceGirlTravel&&s.girlTravel!=null&&s.girlTravel.active)TravelEngine.advanceOfflineSeconds(s,s.girlTravel,seconds,now);
  if(advanceCatTravel&&s.catTravel!=null&&s.catTravel.active)TravelEngine.advanceOfflineSeconds(s,s.catTravel,seconds,now);
 }
 public static void advance(WorldState s,double seconds,long now,boolean advanceGirlTravel,boolean advanceCatTravel){if(s==null||seconds<=0)return;double remain=seconds,elapsed=0;long start=Math.max(0L,now-(long)(seconds*1000.0));int guard=0;while(remain>1e-6&&guard++<200000){boolean moving=(advanceGirlTravel&&s.girlTravel!=null&&s.girlTravel.active)||(advanceCatTravel&&s.catTravel!=null&&s.catTravel.active);boolean falling=(s.girlPhysics!=null&&!s.girlPhysics.grounded)||(s.catPhysics!=null&&!s.catPhysics.grounded);double dt=Math.min(remain,(moving||falling)?.25:Math.min(60,remain));long subNow=Math.min(now,start+(long)((elapsed+dt)*1000.0));if(advanceGirlTravel&&s.girlTravel!=null&&s.girlTravel.active)TravelEngine.enterTerrainDropIfNeeded(s,s.girlTravel,subNow);if(advanceCatTravel&&s.catTravel!=null&&s.catTravel.active)TravelEngine.enterTerrainDropIfNeeded(s,s.catTravel,subNow);BiomechanicsStepEngine.advance(s,dt,subNow);if(!BodyRhythmEngine.isSleeping(s)&&BodyRhythmEngine.shouldCollapse(s)){BodyRhythmEngine.collapse(s,subNow);advanceGirlTravel=false;}if(advanceGirlTravel&&s.girlTravel!=null&&s.girlTravel.active)TravelEngine.advanceSeconds(s,s.girlTravel,dt,subNow);if(advanceCatTravel&&s.catTravel!=null&&s.catTravel.active)TravelEngine.advanceSeconds(s,s.catTravel,dt,subNow);remain-=dt;elapsed+=dt;}}
}