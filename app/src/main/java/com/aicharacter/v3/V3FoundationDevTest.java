package com.aicharacter.v3;
import java.util.Arrays;
import org.json.JSONObject;

/** Non-destructive checks for the V3 personality/expression foundation. */
public final class V3FoundationDevTest {
 private V3FoundationDevTest(){}

 public static String run(WorldState source){
  if(source==null)return "DEV V3 FOUNDATION: FAIL <no state>";
  long now=System.currentTimeMillis();
  try{
   WorldState learned=copy(source,now);
   double before=learned.personality.curiosity;
   MemoryEntry lived=new MemoryEntry(
    "v3_foundation_probe",now,"v3_probe","A novel detail rewarded careful observation.",
    .90,.65,.95,HaruPerception.currentPlaceId(learned),null,
    Arrays.asList("explore","observe","novel"));
   PersonalDevelopmentEngine.learn(learned,lived);
   double delta=learned.personality.curiosity-before;
   boolean slowLearning=delta>0&&delta<.01&&bounded(learned.personality);

   String intention=learned.currentIntention;
   String planId=learned.planState==null?"":learned.planState.planId;
   boolean travelling=learned.girlTravel!=null&&learned.girlTravel.active;
   BehaviorExpressionEngine.Snapshot expression=BehaviorExpressionEngine.observe(learned,now);
   boolean expressionReadOnly=same(intention,learned.currentIntention)&&
    same(planId,learned.planState==null?"":learned.planState.planId)&&
    travelling==(learned.girlTravel!=null&&learned.girlTravel.active);

   WorldState active=copy(source,now),offline=copy(source,now);
   LifeSimulationKernel.beginSlice(active,60.0,now,LifeSimulationKernel.Mode.ACTIVE);
   LifeSimulationKernel.beginSlice(offline,60.0,now,LifeSimulationKernel.Mode.OFFLINE);
   boolean clockParity=near(active.body.energy,offline.body.energy)&&
    near(active.body.sleepiness,offline.body.sleepiness)&&
    near(active.emotion.curiosity,offline.emotion.curiosity);

   boolean pass=slowLearning&&expressionReadOnly&&clockParity;
   return "DEV V3 FOUNDATION: "+(pass?"PASS":"FAIL")+"\n"+
    "slowLearning="+slowLearning+" deltaCuriosity="+fmt(delta)+"\n"+
    "expressionReadOnly="+expressionReadOnly+" visible="+
      (expression.stageDirection().isEmpty()?"<none>":expression.stageDirection())+"\n"+
    "activeOfflineClockParity="+clockParity+"\n"+
    "contract=memory/outcome -> slow personality -> contextual expression; plans remain autonomous";
  }catch(Exception e){
   return "DEV V3 FOUNDATION: FAIL "+e.getClass().getSimpleName()+": "+e.getMessage();
  }
 }

 private static WorldState copy(WorldState s,long now)throws Exception{
  JSONObject raw=s.toJson();
  WorldState c=WorldState.fromJson(raw);
  c.world=s.world;
  if(c.world!=null&&c.runtime!=null)c.runtime.mergeDefinition(c.world);
  StateInvariantChecker.normalize(c,now);
  return c;
 }
 private static boolean bounded(PersonalityState p){
  return p!=null&&in(p.curiosity)&&in(p.caution)&&in(p.sociability)&&in(p.independence)&&in(p.patience);
 }
 private static boolean in(double v){return Double.isFinite(v)&&v>=0&&v<=1;}
 private static boolean near(double a,double b){return Math.abs(a-b)<1e-9;}
 private static boolean same(String a,String b){return a==null?b==null:a.equals(b);}
 private static String fmt(double v){return String.format(java.util.Locale.US,"%.6f",v);}
}
