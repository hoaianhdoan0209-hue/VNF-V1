package com.aicharacter.v3;
/** Converts hidden physiology numbers into bounded subjective experiences. Never exposes engine values to the girl. */
public final class BodyPerceptionEngine{private BodyPerceptionEngine(){}
 public static void observe(WorldState s,long now){if(s==null)return;ThermalState t=s.thermal;RespirationState r=s.respiration;if(t!=null){if(t.coldLoad>=.58)emit(s,now,"body_cold","The air and dampness left her feeling distinctly cold.",-.18,.42,"cold","body");else if(t.heatLoad>=.62)emit(s,now,"body_heat","The warmth had become tiring and uncomfortable.",-.16,.40,"heat","body");}
  if(r!=null&&r.breathingLoad>=.48)emit(s,now,"breathing_strain","Breathing felt more effortful than usual.",-.24,.52,"breathing","body");
 }
 private static void emit(WorldState s,long now,String kind,String summary,double valence,double significance,String...tags){long window=45L*60000L;for(int i=s.memories.size()-1;i>=0;i--){MemoryEntry m=s.memories.get(i);if(now-m.time>window)break;if(kind.equals(m.kind))return;}CognitionEngine.experience(s,now,kind,summary,valence,significance,tags);}
}