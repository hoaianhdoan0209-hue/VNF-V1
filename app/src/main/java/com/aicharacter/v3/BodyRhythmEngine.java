package com.aicharacter.v3;
/** Shared real-time body rhythm. Both active and reconstructed life advance the same 1:1 rates. */
public final class BodyRhythmEngine {
 private BodyRhythmEngine(){}
 public static void advanceMinutes(WorldState s,double minutes){
  if(s==null||s.body==null||minutes<=0)return;
  // Awake baseline: roughly 3 energy points and 4 sleepiness points per real hour.
  // Body pressure therefore develops across a human-scale day instead of collapsing in minutes.
  s.body.energy-=minutes*.050;
  s.body.sleepiness+=minutes*.067;
  s.body.clamp();
 }
 public static void advanceSeconds(WorldState s,double seconds){advanceMinutes(s,seconds/60.0);}
}
