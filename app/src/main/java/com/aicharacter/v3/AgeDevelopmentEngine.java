package com.aicharacter.v3;
/** Real-time maturation. Age expands learning capacity; it never injects a fixed life script. */
public final class AgeDevelopmentEngine {
 private AgeDevelopmentEngine(){}
 public static void apply(WorldState s,long now){
  if(s.lastDevelopmentAt<=0)s.lastDevelopmentAt=s.createdAt;
  // Birthday hook is deliberately one-shot; ordinary learning remains experience driven.
  if(s.pendingBirthdayLearning){
   double curiosityBoost=Math.min(.035,.008+(s.brainGrowth-5.0)*.004);
   s.emotion.curiosity=Math.min(1,s.emotion.curiosity+curiosityBoost);
   s.pendingBirthdayLearning=false;
  }
  s.lastDevelopmentAt=now;
 }
 public static void onBirthday(WorldState s,int age,long now){
  s.memories.add(new MemoryEntry(now,"development","Growing older changed what she may be ready to understand, not who controls her choices.",.88));
  while(s.memories.size()>240)s.memories.remove(0);
 }
 public static int knowledgeCeiling(WorldState s){return 4;}
 public static double learningFactor(WorldState s){return Math.max(.55,Math.min(1.35,.75+s.brainGrowth/20.0));}
}
