package com.aicharacter.v3;import java.util.*;
/** Executable V0.97 paired-state assertions. Throws on failure; does not print hardcoded PASS. */
public final class V097DevTest{private V097DevTest(){}
 public static void assertHistoryChangesScore(WorldState a,WorldState b,long now){double x=reflectScore(a,now),y=reflectScore(b,now);if(Math.abs(x-y)<1)throw new IllegalStateException("history did not materially change reflect score: "+x+" vs "+y);}
 public static void assertPersonalityMatters(WorldState a,WorldState b,long now){double x=reflectScore(a,now),y=reflectScore(b,now);if(Math.abs(x-y)<1)throw new IllegalStateException("personality contribution dead");}
 public static void assertBeliefRevision(BeliefState b,double before){if(!(b.confidence<before))throw new IllegalStateException("contradictory evidence failed to lower confidence");}
 public static void assertProtectedMemory(WorldState s,String id,long now){MemoryConsolidationEngine.consolidate(s,now);for(MemoryEntry m:s.memories)if(m.memoryId.equals(id))return;throw new IllegalStateException("protected memory lost");}
 public static void assertCarriedNoSearch(WorldState s){if(GirlCatSearchEngine.shouldSearch(s,System.currentTimeMillis()))throw new IllegalStateException("knowingly carried cat treated as missing");}
 private static double reflectScore(WorldState s,long now){LifeDecisionEngine.choose(s,now);String t=s.lastDecisionTrace;int i=t.indexOf("- reflect");if(i<0)return-999;int p=t.indexOf(" total=",i),e=t.indexOf('\n',p);return Double.parseDouble(t.substring(p+7,e));}
}