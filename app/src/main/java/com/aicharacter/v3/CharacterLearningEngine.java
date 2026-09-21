package com.aicharacter.v3;import java.util.*;
/** Consequence learning. Never chooses destinations; it only changes learned state from real outcomes. */
public final class CharacterLearningEngine {private CharacterLearningEngine(){}
 public static void learnOutcome(WorldState s,MemoryEntry m){learnOutcome(s,m,m==null?"":m.memoryId);}
 public static boolean learnOutcome(WorldState s,MemoryEntry m,String experienceId){
  if(m==null)return false;if(experienceId!=null&&!experienceId.isEmpty()){if(!s.processedLearningIds.add(experienceId))return false;trimProcessed(s);}
  long now=m.time;double sig=Math.max(.15,m.importance);boolean transientBody=isTransientBody(m);
  if(!m.location.isEmpty()){PreferenceState p=s.preferences.computeIfAbsent("place:"+m.location,k->new PreferenceState(k,0));p.learn(m.valence,sig,now);PlaceAssociation a=s.placeAssociations.computeIfAbsent(m.location,PlaceAssociation::new);a.learn(m.valence,sig,now);}
  if(transientBody){PersonalDevelopmentEngine.learn(s,m);return true;}
  for(String raw:m.tags){String t=activity(raw);if(t==null)continue;PreferenceState p=s.preferences.computeIfAbsent("activity:"+t,k->new PreferenceState(k,0));p.learn(m.valence,sig,now);if(m.valence>=-.15&&!m.location.isEmpty()){String k=t+"@"+m.location;HabitState h=s.habits.computeIfAbsent(k,x->new HabitState(x,CognitionEngine.timeContext(s.worldMinutes),t,m.location));h.reinforce(sig,now);}else if(m.valence<-.15&&!m.location.isEmpty()){HabitState h=s.habits.get(t+"@"+m.location);if(h!=null)h.weaken(Math.abs(m.valence)*sig,now);}}
  PersonalDevelopmentEngine.learn(s,m);return true;
 }
 private static boolean isTransientBody(MemoryEntry m){return m!=null&&(m.hasTag("body")||"body_cold".equals(m.kind)||"body_heat".equals(m.kind)||"breathing_strain".equals(m.kind));}
 private static void trimProcessed(WorldState s){while(s.processedLearningIds.size()>1024){Iterator<String> it=s.processedLearningIds.iterator();if(!it.hasNext())break;it.next();it.remove();}}
 private static String activity(String t){if(t==null)return null;if(t.equals("rest")||t.equals("reflect")||t.equals("observe")||t.equals("explore")||t.equals("search")||t.equals("solitude"))return t;if(t.equals("sleep")||t.equals("seek_shelter"))return"rest";if(t.equals("find_cat")||t.equals("failed_search"))return"search";if(t.equals("explore_garden"))return"explore";if(t.equals("observe_lake")||t.equals("watch_reedling")||t.equals("study_ecology")||t.equals("compare_concept"))return"observe";if(t.equals("quiet_pause"))return"solitude";if(t.equals("recover"))return"rest";if(t.equals("seek_solitude")||t.equals("alone"))return"solitude";return null;}
 public static void searchEvidence(WorldState s,String place,boolean found,MemoryEntry source){String subject="cat_at:"+place;CognitionEngine.reviseFromMemory(s,subject,found?"likely":"uncertain",found?1:-1,found?.9:.58,source);}
}