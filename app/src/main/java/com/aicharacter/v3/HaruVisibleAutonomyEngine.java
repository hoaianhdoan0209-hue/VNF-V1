package com.aicharacter.v3;

/**
 * Runtime guarantee that healthy autonomy becomes physically visible.
 * It only intervenes after a long period with no real Haru displacement and
 * chooses from the currently authored/perceived world. It never overrides
 * sleep, urgent body needs, active plans, search, social spacing or unsafe states.
 */
public final class HaruVisibleAutonomyEngine {
 public static final long MAX_HEALTHY_STILL_MS=6000L,STARTUP_VISIBLE_RESERVE_MS=6000L;
 private static final float MIN_LOCAL_MOVE_PX=82f,MAX_LOCAL_TARGET_PX=620f;
 private HaruVisibleAutonomyEngine(){}

 public static boolean beginIfNeeded(WorldState s,long now){
  if(!eligible(s,now))return false;
  WorldArea here=s.world.areaAt(s.haruX);if(here==null)return false;

  WorldObject local=bestLocalTarget(s,here);
  if(local!=null&&beginLocalInspection(s,local,now))return true;

  WorldArea next=bestAdjacentArea(s,here,now);
  return next!=null&&beginAreaExploration(s,next,now);
 }

 public static boolean eligible(WorldState s,long now){
  if(!safeForVisibleChoice(s,now))return false;
  long anchor=s.lastHaruPhysicalMoveAt>0?s.lastHaruPhysicalMoveAt:Math.max(s.lastOpenedAt,s.createdAt);
  return anchor>0&&now>=anchor&&now-anchor>=MAX_HEALTHY_STILL_MS;
 }

 public static boolean reserveStartupVisibleChoice(WorldState s,long now){
  if(!safeForVisibleChoice(s,now))return false;
  long opened=s.lastOpenedAt>0?s.lastOpenedAt:s.createdAt;
  if(opened<=0||now<opened||now-opened>=STARTUP_VISIBLE_RESERVE_MS)return false;
  return s.lastHaruPhysicalMoveAt<=0||s.lastHaruPhysicalMoveAt<opened;
 }

 private static boolean safeForVisibleChoice(WorldState s,long now){
  if(s==null||s.world==null||s.body==null||s.girlTravel==null)return false;
  if(BodyRhythmEngine.isSleeping(s)||s.girlTravel.active)return false;
  if(s.planState!=null&&s.planState.active())return false;
  if(s.catSearch!=null&&s.catSearch.active)return false;
  if(s.body.energy<54||s.body.sleepiness>62||s.body.pain>20||s.body.health<78)return false;
  if(DigestionHydrationEngine.hunger(s)>.62||DigestionHydrationEngine.thirst(s)>.56||DigestionHydrationEngine.bladderUrgency(s)>.62)return false;
  WorldArea here=s.world.areaAt(s.haruX);
  return here==null||s.environment==null||!"RAIN".equals(s.environment.weather)||s.environment.weatherIntensity<=.72||WorldSemantics.exposure(here)<=.55;
 }

 private static WorldObject bestLocalTarget(WorldState s,WorldArea here){
  WorldObject best=null;double bestScore=-999;
  for(WorldObject o:s.world.objects){
   if(o==null||!o.enabled||o.id==null||o.id.isEmpty()||!here.id.equals(o.areaId))continue;
   float x=HaruVisionEngine.actualX(s,o),distance=Math.abs(x-s.haruX);
   if(distance<MIN_LOCAL_MOVE_PX||distance>MAX_LOCAL_TARGET_PX)continue;
   if(!HaruVisionEngine.canSee(s,o))continue;
   double score=.18;
   if("creature".equals(o.type))score+=.34;
   if(o.tags!=null&&(o.tags.contains("living")||o.tags.contains("vegetation")||o.tags.contains("flora")))score+=.18;
   if(o.interactable)score+=.12;
   if(o.tags!=null&&(o.tags.contains("observe")||o.tags.contains("landmark")||o.tags.contains("water")||o.tags.contains("herb")))score+=.12;
   score+=Math.max(0,.16-distance/MAX_LOCAL_TARGET_PX*.10);
   score+=(s.personality==null?.5:s.personality.curiosity)*.18;
   ConceptKnowledgeState k=s.characterGod==null?null:s.characterGod.conceptKnowledge.get("world:"+o.id);
   if(k==null)score+=.16;else score+=(1-Math.max(0,Math.min(1,k.confidence)))*.10;
   if(best==null||score>bestScore){best=o;bestScore=score;}
  }
  return best;
 }

 private static WorldArea bestAdjacentArea(WorldState s,WorldArea here,long now){
  if(here.connections==null||here.connections.isEmpty())return null;
  int start=Math.floorMod((int)((now/60000L)+(s.createdAt&0x7fffffffL)),here.connections.size());
  for(int i=0;i<here.connections.size();i++){
   String id=here.connections.get((start+i)%here.connections.size());WorldArea a=s.world.area(id);
   if(a==null||a.id.equals(here.id))continue;
   if(s.environment!=null&&"RAIN".equals(s.environment.weather)&&s.environment.weatherIntensity>.65&&WorldSemantics.exposure(a)>.72)continue;
   return a;
  }
  return null;
 }

 private static boolean beginLocalInspection(WorldState s,WorldObject o,long now){
  PlanState p=basePlan(s,now,"inspect_nearby","understand something nearby","VISIBLE_AUTONOMY",o.id,"OBSERVE");
  p.steps.add("TRAVEL_LOCAL:"+o.id);p.steps.add("OBSERVE:"+o.id);p.steps.add("REVIEW:"+o.id);
  HaruReasoningEngine.attachReasoningToPlan(s,p,now);
  s.planState=p;s.currentIntention=p.intentionId;s.persistentIntentionTarget=o.id;s.intentionStartedAt=now;
  s.haruActivity="looking more closely at something nearby";
  float target=HaruVisionEngine.actualX(s,o),range=Math.max(34f,Math.min(76f,o.interactionRadius>0?o.interactionRadius*.62f:52f));
  boolean started=TravelEngine.startLocal(s,s.girlTravel,"girl",o.id,target,range,p.planId,now);
  if(!started){fail(s,p,now,"nearby target was not physically reachable");return false;}
  WorldEventBus.publishId(s,now,"visible_autonomy_"+p.planId,"HARU_VISIBLE_AUTONOMY_STARTED",o.id,"Haru chose a nearby world target after prolonged physical stillness.");
  return true;
 }

 private static boolean beginAreaExploration(WorldState s,WorldArea area,long now){
  PlanState p=basePlan(s,now,"ambient_explore","see what has changed nearby","VISIBLE_AUTONOMY",area.id,"OBSERVE");
  p.steps.add("TRAVEL:"+area.id);p.steps.add("OBSERVE_AREA:"+area.id);p.steps.add("REVIEW:"+area.id);
  HaruReasoningEngine.attachReasoningToPlan(s,p,now);
  s.planState=p;s.currentIntention=p.intentionId;s.persistentIntentionTarget=area.id;s.intentionStartedAt=now;
  s.haruActivity="choosing to look around somewhere else";
  boolean started=TravelEngine.start(s,s.girlTravel,"girl",area.id,p.planId,now);
  if(!started){fail(s,p,now,"adjacent area was not physically reachable");return false;}
  WorldEventBus.publishId(s,now,"visible_autonomy_"+p.planId,"HARU_VISIBLE_AUTONOMY_STARTED",area.id,"Haru chose to explore a nearby area after prolonged physical stillness.");
  return true;
 }

 private static PlanState basePlan(WorldState s,long now,String intention,String goal,String origin,String destination,String action){
  PlanState p=new PlanState();p.planId="plan_visible_"+Long.toHexString(now);p.intentionId=intention;p.goal=goal;p.origin=origin;
  p.candidateGoalId="visible:"+destination;p.triggerEvidenceId="physical_stillness:"+s.lastHaruPhysicalMoveAt;p.destination=destination;p.plannedAction=action;
  p.status="ACTIVE";p.commitment=.46;p.createdAt=now;p.lastProgressAt=now;return p;
 }
 private static void fail(WorldState s,PlanState p,long now,String why){
  p.status="FAILED";p.lastOutcome=why;p.lastProgressAt=now;s.currentIntention="";s.persistentIntentionTarget="";s.haruActivity="reconsidering where to go";
  PlanExecutor.learnTerminalOutcome(s,p,now,"visible_autonomy_failed",why,true);
 }
}
