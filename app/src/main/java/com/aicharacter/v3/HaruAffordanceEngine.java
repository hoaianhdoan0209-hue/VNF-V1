package com.aicharacter.v3;

import java.util.*;

/** Builds goals from currently perceived world affordances instead of a closed intention menu. */
public final class HaruAffordanceEngine {
 public static final class Candidate{
  public final String goalId,objectId,goal,evidence;public final double score;
  Candidate(String g,String o,String goal,String evidence,double score){goalId=g;objectId=o;this.goal=goal;this.evidence=evidence;this.score=score;}
 }
 private HaruAffordanceEngine(){}

 public static void observeQuestions(WorldState s,long now){
  if(s==null||s.world==null)return;ensure(s);HaruVisionEngine.Snapshot view=HaruVisionEngine.observe(s);
  for(HaruVisionEngine.Seen seen:view.seen){
   if(seen.familiar||seen.clarity<.38)continue;
   String qid="q_world_"+clean(seen.id);OpenQuestionState q=s.characterGod.openQuestions.get(qid);
   if(q==null&&openCount(s)>=8)break;
   if(q==null){q=new OpenQuestionState();q.questionId=qid;q.topic="world:"+seen.id;q.aboutObjectId=seen.id;q.question="What is this, and what does it do here?";q.reason="noticed an unfamiliar visible thing";q.createdAt=now;q.lastRevisitedAt=now;s.characterGod.openQuestions.put(qid,q);}
   else if(!"RESOLVED".equals(q.status)&&now-q.lastRevisitedAt>=10L*60L*1000L){q.lastRevisitedAt=now;q.revisits++;}
   break;
  }
 }

 public static Candidate bestCandidate(WorldState s,long now){
  if(s==null||s.world==null||unsafeForOptionalInquiry(s))return null;ensure(s);
  HaruVisionEngine.Snapshot view=HaruVisionEngine.observe(s);Candidate best=null;
  for(HaruVisionEngine.Seen seen:view.seen){
   WorldObject o=s.world.object(seen.id);if(o==null||!o.enabled||seen.clarity<.30)continue;
   ConceptKnowledgeState k=s.characterGod.conceptKnowledge.get("world:"+o.id);double known=k==null?0:k.confidence;
   OpenQuestionState q=findQuestion(s,o.id);double question=q!=null&&!"RESOLVED".equals(q.status)?.24:0;
   double novelty=(1-known)*.38+(seen.familiar?.02:.18),living=("creature".equals(o.type)||o.tags.contains("flora")||o.tags.contains("living"))?.12:0,resource=(o.tags.contains("water")||o.tags.contains("food")||o.tags.contains("herb")||o.tags.contains("resource"))?.09:0;
   double curiosity=s.personality==null?.5:s.personality.curiosity,energy=s.body==null?.5:Math.max(0,Math.min(1,s.body.energy/100.0));PreferenceState pref=s.preferences.get("activity:observe");double learned=pref==null?0:Math.max(-.12,Math.min(.12,pref.value*.12));
   double reasoning=HaruReasoningEngine.inquiryStrategyBias(s),informationGain=HaruReasoningEngine.informationGainBias(s,o.id);double score=novelty+question+living+resource+curiosity*.16+energy*.08+learned+reasoning+informationGain;
   Candidate c=new Candidate("affordance:"+o.id,o.id,"understand "+label(o),"visible_object:"+o.id,score);if(best==null||c.score>best.score)best=c;
  }
  return best!=null&&best.score>=.56?best:null;
 }

 public static boolean beginPlanIfCompelling(WorldState s,long now){
  if(s==null||s.world==null||s.planState!=null&&s.planState.active()||s.girlTravel!=null&&s.girlTravel.active)return false;
  Candidate c=bestCandidate(s,now);if(c==null)return false;WorldObject o=s.world.object(c.objectId);if(o==null)return false;
  PlanState p=new PlanState();p.planId="plan_aff_"+Long.toHexString(now)+"_"+Integer.toHexString(c.objectId.hashCode());p.intentionId="affordance_inquiry";p.goal=c.goal;p.origin="WORLD_AFFORDANCE";p.candidateGoalId=c.goalId;p.triggerEvidenceId=c.evidence;p.destination=o.id;p.plannedAction="OBSERVE";p.status="ACTIVE";p.commitment=Math.max(.30,Math.min(.72,.28+c.score*.38));p.createdAt=now;p.lastProgressAt=now;
  p.steps.add("TRAVEL:"+o.areaId);p.steps.add("OBSERVE:"+o.id);p.steps.add("COMPARE_EVIDENCE:"+o.id);p.steps.add("REVIEW:"+c.goalId);p.stepIndex=0;
  OpenQuestionState q=findQuestion(s,o.id);if(q!=null)p.questionId=q.questionId;
  HaruReasoningEngine.attachReasoningToPlan(s,p,now);
  s.planState=p;s.currentIntention="affordance_inquiry";s.persistentIntentionTarget=o.id;s.intentionStartedAt=now;s.haruActivity="following up on something she noticed";
  boolean started=TravelEngine.start(s,s.girlTravel,"girl",o.areaId,p.planId,now);
  if(!started){p.status="FAILED";p.lastOutcome="affordance route unavailable";p.lastProgressAt=now;s.currentIntention="";s.persistentIntentionTarget="";PlanExecutor.learnTerminalOutcome(s,p,now,"affordance_route_failed",p.lastOutcome,true);return false;}
  WorldEventBus.publishId(s,now,"aff_goal_"+p.planId,"HARU_GOAL_FORMED",o.id,"Haru formed a goal from a visible world affordance.");
  return true;
 }

 public static void reviewPlan(WorldState s,PlanState p,MemoryEntry outcome,long now){
  if(s==null||p==null||outcome==null||!"WORLD_AFFORDANCE".equals(p.origin))return;ensure(s);
  if(!"COMPLETED".equals(p.status))return;
  String concept="world:"+p.destination;ConceptKnowledgeState k=s.characterGod.conceptKnowledge.computeIfAbsent(concept,x->{ConceptKnowledgeState z=new ConceptKnowledgeState();z.concept=x;return z;});
  WorldObject o=s.world==null?null:s.world.object(p.destination);String claim=o==null?p.destination:label(o);
  k.applyEvidence(claim,1,.55,"VNF_WORLD_TRUTH","memory:"+outcome.memoryId,outcome.memoryId,now);
  OpenQuestionState q=p.questionId==null||p.questionId.isEmpty()?findQuestion(s,p.destination):s.characterGod.openQuestions.get(p.questionId);
  if(q!=null){q.lastRevisitedAt=now;q.revisits++;if(k.confidence>=.75){q.status="RESOLVED";q.resolvedAt=now;}else q.status="PARTIAL";}
  p.stepIndex=p.steps.size();
 }

 public static boolean goalStillUseful(WorldState s,PlanState p){
  if(s==null||p==null||!"WORLD_AFFORDANCE".equals(p.origin))return true;ensure(s);WorldObject o=s.world==null?null:s.world.object(p.destination);if(o==null||!o.enabled)return false;
  ConceptKnowledgeState k=s.characterGod.conceptKnowledge.get("world:"+p.destination);OpenQuestionState q=p.questionId==null||p.questionId.isEmpty()?findQuestion(s,p.destination):s.characterGod.openQuestions.get(p.questionId);
  return !(k!=null&&k.confidence>=.82&&(q==null||"RESOLVED".equals(q.status)));
 }

 private static boolean unsafeForOptionalInquiry(WorldState s){return s.body!=null&&(s.body.energy<42||s.body.sleepiness>68||s.body.pain>28||s.body.health<70)||DigestionHydrationEngine.thirst(s)>.58||DigestionHydrationEngine.hunger(s)>.68;}
 private static OpenQuestionState findQuestion(WorldState s,String objectId){if(s==null||s.characterGod==null)return null;for(OpenQuestionState q:s.characterGod.openQuestions.values())if(objectId!=null&&objectId.equals(q.aboutObjectId)&&!"REJECTED".equals(q.status))return q;return null;}
 private static int openCount(WorldState s){int n=0;for(OpenQuestionState q:s.characterGod.openQuestions.values())if(!"RESOLVED".equals(q.status))n++;return n;}
 private static String label(WorldObject o){String x=o.haruDescription==null||o.haruDescription.trim().isEmpty()?o.type:o.haruDescription;return x==null||x.isEmpty()?o.id:x;}
 private static String clean(String s){return(s==null?"unknown":s).replaceAll("[^A-Za-z0-9._-]","_");}
 private static void ensure(WorldState s){if(s.characterGod==null)s.characterGod=new CharacterGodState();}
}
