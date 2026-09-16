package com.aicharacter.v3;
/** Shared real-time body rhythm. Both active and reconstructed life advance the same rates. */
public final class BodyRhythmEngine {
 private BodyRhythmEngine(){}
 public static void advanceMinutes(WorldState s,double minutes){
  if(s==null||s.body==null||minutes<=0)return;
  double seconds=minutes*60.0;
  s.body.energy-=seconds*.016;
  s.body.sleepiness+=seconds*.014;
  s.body.clamp();
 }
 public static void advanceSeconds(WorldState s,double seconds){advanceMinutes(s,seconds/60.0);}
}
