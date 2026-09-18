package com.aicharacter.v3;import java.util.*;
/** V0.97 measurable psychological contributions to candidate utility. */
public final class HistoryDecisionEngine {private HistoryDecisionEngine(){}
 public static double place(WorldState s,String place,long now,Map<String,Double> out){
  double memory=0,w=0;for(MemoryEntry m:MemoryRetrievalEngine.retrieve(s,place,null,null,s.currentIntention,10,now))if(place.equals(m.location)){double rw=MemoryRetrievalEngine.score(m,now,place,null,null,s.currentIntention,s);memory+=m.valence*rw;w+=rw;}
  memory=w==0?0:memory/w*18;double assoc=0;PlaceAssociation a=s.placeAssociations.get(place);if(a!=null)assoc=a.attraction()*14;double pref=0;PreferenceState p=s.preferences.get("place:"+place);if(p!=null)pref=p.value*16;
  out.put("history_memory",memory);out.put("place_association",assoc);out.put("place_preference",pref);return memory+assoc+pref;
 }
 public static double personality(WorldState s,String intention,boolean weatherRisk,Map<String,Double>out){
  double v=0;if("reflect".equals(intention)||"observe".equals(intention)||"observe_creature".equals(intention)||"explore".equals(intention)){double x=(s.personality.curiosity-.5)*16;out.put("personality_curiosity",x);v+=x;}
  if("seek_solitude".equals(intention)){double x=(s.personality.independence-.5)*14;out.put("personality_independence",x);v+=x;}
  if("find_cat".equals(intention)){double x=(s.personality.sociability-.5)*7+(s.personality.patience-.5)*8;out.put("personality_social_persistence",x);v+=x;}
  if(weatherRisk){double x=-(s.personality.caution-.5)*22;out.put("personality_caution",x);v+=x;}return v;
 }
 public static double relationship(WorldState s,String intention,Map<String,Double>out){if(!"find_cat".equals(intention))return 0;double v=0;
  double[] vals={s.relationship.affection*.025,s.relationship.trust*.035,s.relationship.attachment*.20,s.relationship.comfort*.035,s.relationship.gratitude*.025,-s.relationship.irritation*.075,-s.relationship.hurt*.06};
  String[] k={"rel_affection","rel_trust","rel_attachment","rel_comfort","rel_gratitude","rel_irritation","rel_hurt"};for(int i=0;i<k.length;i++){out.put(k[i],vals[i]);v+=vals[i];}return v;}
 public static double habit(WorldState s,String action,String place,long now,Map<String,Double>out){HabitState h=s.habits.get(action+"@"+place);double v=h==null?0:h.effectiveStrength(now,CognitionEngine.timeContext(s.worldMinutes))*11;out.put("habit",v);return v;}
}