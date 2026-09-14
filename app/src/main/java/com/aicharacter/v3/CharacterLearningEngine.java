package com.aicharacter.v3;import java.util.*;
/** Consequence learning. Never chooses destinations; it only changes learned state from real outcomes. */
public final class CharacterLearningEngine {private CharacterLearningEngine(){}
 public static void learnOutcome(WorldState s,MemoryEntry m){learnOutcome(s,m,m==null?"":m.memoryId);}
 public static boolean learnOutcome(WorldState s,MemoryEntry m,String experienceId){
  if(m==null)return false;if(experienceId!=null&&!experienceId.isEmpty()&&!s.processedLearningIds.add(experienceId))return false;
  long now=m.time; double sig=Math.max(.15,m.importance);
  if(!m.location.isEmpty()){PreferenceState p=s.preferences.computeIfAbsent("place:"+m.location,k->new PreferenceState(k,0));p.learn(m.valence,sig,now);
   PlaceAssociation a=s.placeAssociations.computeIfAbsent(m.location,PlaceAssociation::new);a.learn(m.valence,sig,now);}
  for(String t:m.tags)if(t.equals("rest")||t.equals("reflect")||t.equals("observe")||t.equals("search")){
   PreferenceState p=s.preferences.computeIfAbsent("activity:"+t,k->new PreferenceState(k,0));p.learn(m.valence,sig,now);
   if(m.valence>=-.15){String k=t+"@"+m.location;HabitState h=s.habits.computeIfAbsent(k,x->new HabitState(x,CognitionEngine.timeContext(s.worldMinutes),t,m.location));h.reinforce(sig,now);}}
  if(m.hasTag("danger")||m.hasTag("failed_search"))s.personality.slowlyLearn("danger",1,sig);
  if(m.hasTag("explore")||m.hasTag("novel"))s.personality.slowlyLearn("explore",1,sig);
  if(m.hasTag("social")||m.hasTag("reunion"))s.personality.slowlyLearn("social",m.valence>=0?1:-1,sig);
  return true;
 }
 public static void searchEvidence(WorldState s,String place,boolean found,MemoryEntry source){
  String subject="cat_at:"+place;CognitionEngine.reviseFromMemory(s,subject,found?"likely":"uncertain",found?1:-1,found?.9:.58,source);
 }
}