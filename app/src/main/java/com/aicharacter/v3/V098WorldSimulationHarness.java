package com.aicharacter.v3;
/** UI-independent bounded multi-day harness using the production offline engine. */
public final class V098WorldSimulationHarness {private V098WorldSimulationHarness(){}
 public static Result run(WorldState s,long start,long hours){int m0=s.memories.size(),e0=s.worldHistory.size(),g0=s.godInbox.size();s.lastSimulatedAt=start;long t=System.nanoTime();String trace=OfflineLifeEngine.reconstruct(s,start+hours*3600000L);long ns=System.nanoTime()-t;return new Result(hours,ns/1_000_000.0,s.memories.size()-m0,s.worldHistory.size()-e0,s.godInbox.size()-g0,trace);}
 public static final class Result{public final long hours;public final double millis;public final int memories,events,god;public final String trace;Result(long h,double ms,int m,int e,int g,String t){hours=h;millis=ms;memories=m;events=e;god=g;trace=t;}public String toString(){return hours+"h ms="+millis+" memories+="+memories+" events+="+events+" god+="+god;}}
}
