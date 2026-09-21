package com.aicharacter.v3;
import java.util.Arrays;
import org.json.JSONObject;

/** Non-destructive checks for the V3 personality/expression/causal foundation. */
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
    .90,.65,.95,"__dev_v3_probe__",null,
    Arrays.asList("explore","observe","novel"));
   PersonalDevelopmentEngine.learn(learned,lived);
   double delta=learned.personality.curiosity-before;
   boolean slowLearning=delta>0&&delta<.01&&bounded(learned.personality);

   PersonalityState mature=new PersonalityState();
   mature.curiosity=.72;mature.curiosityEvidence=120;mature.normalize();
   double matureBefore=mature.curiosity;
   mature.slowlyLearn("explore",-1,1.5);
   double oneShockDrop=matureBefore-mature.curiosity;
   double afterOne=mature.curiosity;
   for(int i=0;i<12;i++)mature.slowlyLearn("explore",-1,1.5);
   double repeatedDrop=afterOne-mature.curiosity;
   boolean contradictionResistance=oneShockDrop>0&&oneShockDrop<.0025&&mature.curiosityOpposition>=0&&repeatedDrop>oneShockDrop;

   WorldState repetition=copy(source,now);
   MemoryEntry freshEvidence=new MemoryEntry("dev_repeat_fresh",now,"v3_repeat_probe","fresh observation",.55,.35,.95,"__dev_repeat__",null,Arrays.asList("explore","observe"));
   double freshWeight=PersonalDevelopmentEngine.evidenceWeight(repetition,freshEvidence);
   for(int i=0;i<6;i++)repetition.memories.add(new MemoryEntry("dev_repeat_old_"+i,now-(i+1)*1000L,"v3_repeat_probe","repeated observation "+i,.55,.35,.95,"__dev_repeat__",null,Arrays.asList("explore","observe")));
   MemoryEntry repeatedEvidence=new MemoryEntry("dev_repeat_next",now,"v3_repeat_probe","another repeated observation",.55,.35,.95,"__dev_repeat__",null,Arrays.asList("explore","observe"));
   double repeatedWeight=PersonalDevelopmentEngine.evidenceWeight(repetition,repeatedEvidence);
   boolean repetitionDamping=repeatedWeight<freshWeight*.60;

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

   WorldState causal=copy(source,now);
   PlanState p=new PlanState();p.planId="dev_v3_causal";p.intentionId="observe_lake";p.status="COMPLETED";
   p.arrivedAt=now-3000;p.actionResolvedAt=now-2000;p.lastProgressAt=now-15000;
   MemoryEntry outcome=new MemoryEntry("dev_v3_outcome_memory",now-1000,"planned_action_outcome","synthetic causal audit outcome",.42,.12,.95,"lakeside",null,Arrays.asList("observe_lake","success"));
   causal.memories.add(outcome);p.outcomeMemoryId=outcome.memoryId;p.outcomeLearnedAt=outcome.time;causal.planState=p;
   String beforeReviewIntention=causal.currentIntention;boolean beforeReviewTravel=causal.girlTravel.active;
   boolean reviewed=PlanOutcomeReviewEngine.reviewIfReady(causal,now);
   boolean causalOrdering=reviewed&&PlanCausalAudit.valid(causal,p)&&p.postOutcomeReviewedAt>=p.outcomeLearnedAt&&
    same(beforeReviewIntention,causal.currentIntention)&&beforeReviewTravel==causal.girlTravel.active;
   PlanState broken=new PlanState();broken.planId="dev_v3_broken";broken.arrivedAt=now-1000;broken.actionResolvedAt=now;broken.outcomeLearnedAt=now-5000;
   boolean catchesBrokenOrder=!PlanCausalAudit.valid(causal,broken);

   boolean pass=slowLearning&&contradictionResistance&&repetitionDamping&&expressionReadOnly&&clockParity&&causalOrdering&&catchesBrokenOrder;
   return "DEV V3 FOUNDATION: "+(pass?"PASS":"FAIL")+"\n"+
    "slowLearning="+slowLearning+" deltaCuriosity="+fmt(delta)+"\n"+
    "contradictionResistance="+contradictionResistance+" oneShockDrop="+fmt(oneShockDrop)+" repeatedDrop="+fmt(repeatedDrop)+"\n"+
    "repetitionDamping="+repetitionDamping+" freshWeight="+fmt(freshWeight)+" repeatedWeight="+fmt(repeatedWeight)+"\n"+
    "expressionReadOnly="+expressionReadOnly+" visible="+(expression.stageDirection().isEmpty()?"<none>":expression.stageDirection())+"\n"+
    "activeOfflineClockParity="+clockParity+"\n"+
    "causalOrdering="+causalOrdering+" catchesBrokenOrder="+catchesBrokenOrder+"\n"+
    "contract=arrival -> action -> learned outcome -> review; repeated evidence is damped; personality changes slowly under lived evidence";
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
