package com.aicharacter.v3;

/** Knowledge/belief revision requires a causal Haru memory as evidence. External reference alone cannot write knowledge. */
public final class HaruKnowledgeRevisionEngine {
 private HaruKnowledgeRevisionEngine(){}

 public static boolean applyEvidence(WorldState s,String concept,String claim,double polarity,double weight,String sourceLayer,String sourceRef,MemoryEntry source,long now){
  if(s==null||source==null||concept==null||concept.trim().isEmpty()||!hasMemory(s,source.memoryId))return false;
  if(s.characterGod==null)s.characterGod=new CharacterGodState();
  String key=concept.trim();
  ConceptKnowledgeState k=s.characterGod.conceptKnowledge.computeIfAbsent(key,x->{ConceptKnowledgeState z=new ConceptKnowledgeState();z.concept=x;return z;});
  k.applyEvidence(claim,polarity,weight,sourceLayer,sourceRef,"memory:"+source.memoryId+":"+key+":"+sourceLayer,now);
  return true;
 }

 private static boolean hasMemory(WorldState s,String id){for(MemoryEntry m:s.memories)if(m!=null&&m.memoryId.equals(id))return true;return false;}
}
