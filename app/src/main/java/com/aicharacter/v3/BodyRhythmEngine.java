package com.aicharacter.v3;
/** Shared real-time body rhythm. Both active and reconstructed life advance the same 1:1 rates. */
public final class BodyRhythmEngine {
 private BodyRhythmEngine(){}
 public static void advanceMinutes(WorldState s,double minutes){
  if(s==null||s.body==null||minutes<=0)return;
  if(isSleeping(s)){
   // Sustained sleep restores the body over real hours, not as a one-frame reward.
   s.body.energy+=minutes*.12;       // ~7.2 points/hour
   s.body.sleepiness-=minutes*.15;  // ~9 points/hour
  }else{
   // Awake baseline: roughly 3 energy points and 4 sleepiness points per real hour.
   s.body.energy-=minutes*.050;
   s.body.sleepiness+=minutes*.067;
  }
  s.body.clamp();
 }
 public static boolean isSleeping(WorldState s){return s!=null&&"sleeping".equals(s.haruActivity);}
 /** A sleeping girl remains asleep until recovery is meaningful; this is pressure-based, not a fixed schedule. */
 public static boolean shouldRemainAsleep(WorldState s){return isSleeping(s)&&s.body!=null&&(s.body.energy<88||s.body.sleepiness>18);}
 public static void advanceSeconds(WorldState s,double seconds){advanceMinutes(s,seconds/60.0);}
}
