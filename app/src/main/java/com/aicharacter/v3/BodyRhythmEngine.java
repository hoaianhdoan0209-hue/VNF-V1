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
   if(s.body.pain>0)s.body.pain-=minutes*.006;
   if(s.body.health<100)s.body.health+=minutes*.004;
  }else if(isResting(s)){
   // Quiet protected rest helps gradually while awake, but is weaker than sleep.
   s.body.energy+=minutes*.035;
   s.body.sleepiness-=minutes*.025;
   if(s.body.pain>0)s.body.pain-=minutes*.002;
  }else{
   // Awake baseline: roughly 3 energy points and 4 sleepiness points per real hour.
   s.body.energy-=minutes*.050;
   s.body.sleepiness+=minutes*.067;
  }
  s.body.clamp();
 }
 public static boolean isSleeping(WorldState s){return s!=null&&"sleeping".equals(s.haruActivity);}
 public static boolean isResting(WorldState s){if(s==null||s.haruActivity==null)return false;String a=s.haruActivity;return"resting".equals(a)||"taking a quiet rest".equals(a)||"settling after rest".equals(a);}
 /** A sleeping girl remains asleep until recovery is meaningful; this is pressure-based, not a fixed schedule. */
 public static boolean shouldRemainAsleep(WorldState s){return isSleeping(s)&&s.body!=null&&(s.body.energy<88||s.body.sleepiness>18||s.body.pain>38||s.body.health<62);}
 public static boolean needsRecoveryTransition(WorldState s){return s!=null&&s.body!=null&&(s.body.energy<28||s.body.sleepiness>72||s.body.pain>28||s.body.health<68);}
 /** Collapse is deterministic body failure, never random drama. Pain above 20 only creates risk; severe combined strain is required. */
 public static boolean shouldCollapse(WorldState s){if(s==null||s.body==null||s.body.pain<=20)return false;double strain=(s.body.pain-20)*.72+Math.max(0,24-s.body.energy)*1.15+Math.max(0,55-s.body.health)*.9+Math.max(0,s.body.sleepiness-82)*.45;return strain>=38;}
 public static void collapse(WorldState s,long now){if(s==null||s.body==null)return;s.girlTravel.active=false;s.currentIntention="";s.persistentIntentionTarget="";s.intentionStartedAt=now;s.haruActivity="sleeping";if(s.planState==null)s.planState=new PlanState();s.planState.status="PAUSED";s.planState.pausedAt=now;s.planState.pauseReason="body collapse interrupted the current plan";s.planState.lastProgressAt=now;s.emotion.fear=Math.min(1,s.emotion.fear+.12);CognitionEngine.experience(s,"body_collapse","Her body gave out under severe combined pain and exhaustion.",-.42,.82,"pain","recovery");}
 public static void advanceSeconds(WorldState s,double seconds){advanceMinutes(s,seconds/60.0);}
}
