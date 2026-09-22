package com.aicharacter.v3;

import java.util.*;

/** Haru owns the response to every durable lesson. God never writes Haru cognition directly. */
public final class HaruTeachingOpportunityEngine {
 static final String OFFER_TYPE="DIVINE_TEACHING_OFFERED";
 static final String RESPONSE_TYPE="HARU_TEACHING_RESPONSE";
 public enum Response{LISTEN,DEFER,QUESTION,REJECT,PARTIALLY_UNDERSTAND}
 private static final long MAX_AGE=48L*60L*60L*1000L;
 private HaruTeachingOpportunityEngine(){}

 public static boolean observe(WorldState s,long now){
  if(s==null)return false;if(s.characterGod==null)s.characterGod=new CharacterGodState();
  LessonState lesson=nextLesson(s,now);if(lesson==null)return false;
  Response response=decide(s,lesson,now);applyResponse(s,lesson,response,now);return true;
 }

 static Response decide(WorldState s,LessonState l,long now){
  if(BodyRhythmEngine.isSleeping(s)||bodyOverloaded(s))return Response.DEFER;
  int missing=missingPrerequisites(s,l);if(missing>0)return Response.QUESTION;
  double sourceTrust=l.sourceConfidence>=0?l.sourceConfidence:.35;
  double prior=priorExperience(s,l),relevance=relevance(s,l),known=knownConfidence(s,l);
  boolean conflict=conflicts(s,l);
  if(sourceTrust<.20&&conflict)return Response.REJECT;
  double curiosity=s.personality==null?.5:s.personality.curiosity,patience=s.personality==null?.5:s.personality.patience,calm=s.emotion==null?.5:s.emotion.calm;
  double energy=s.body==null?.5:unit(s.body.energy/100.0),pain=s.body==null?0:unit(s.body.pain/100.0);
  double comprehension=unit(.24+.26*curiosity+.20*patience+.15*calm+.15*energy+.15*known-.16*pain);
  double readiness=unit(.20*curiosity+.14*patience+.12*calm+.18*energy+.18*relevance+.10*sourceTrust+.08*prior-.14*pain-.08*l.attempts);
  if(sourceTrust<.20&&relevance<.45)return Response.QUESTION;
  if(readiness<.26&&relevance<.35)return Response.REJECT;
  if(readiness<.36)return Response.DEFER;
  if(l.difficulty>comprehension+.10)return Response.PARTIALLY_UNDERSTAND;
  if(readiness>=.58&&sourceTrust>=.28)return Response.LISTEN;
  if(readiness>=.42)return Response.PARTIALLY_UNDERSTAND;
  return Response.QUESTION;
 }

 private static void applyResponse(WorldState s,LessonState l,Response r,long now){
  l.attempts++;l.lastResponse=r.name();l.updatedAt=now;
  if(r==Response.DEFER){l.status="DEFERRED";l.nextReviewAt=now+15L*60L*1000L;respond(s,l,r,now,null,"body/context took priority");return;}
  if(r==Response.QUESTION){l.status="QUESTIONED";l.nextReviewAt=now+20L*60L*1000L;OpenQuestionState q=new OpenQuestionState();q.questionId="q_lesson_"+l.lessonId;q.topic=firstConcept(l);q.question="What evidence or prerequisite would make this clearer?";q.reason="Haru found the lesson incomplete or difficult";q.createdAt=now;q.lastRevisitedAt=now;s.characterGod.openQuestions.putIfAbsent(q.questionId,q);respond(s,l,r,now,null,"Haru formed a question instead of accepting the claim");return;}
  if(r==Response.REJECT){l.status="REJECTED";l.nextReviewAt=0;respond(s,l,r,now,null,"current evidence/context did not justify accepting the lesson");return;}

  boolean partial=r==Response.PARTIALLY_UNDERSTAND;
  Experience e=new Experience("divine_lesson_experience",
          partial?"She listened but only formed a partial understanding of "+l.topic+".":"She chose to listen carefully to "+l.topic+".",
          .03,partial?.34:.46).atTime(now).id("lesson_experience_"+l.lessonId+"_"+l.attempts).at(HaruPerception.currentPlaceId(s),"").with("god")
          .tag("learning").tag("god_teaching").tag("lesson:"+l.lessonId).tag("lesson_response:"+r.name()).tag("causal_event:god_teaching_offer_"+l.lessonId);
  for(String c:l.concepts)e.tag("lesson_topic:"+c);
  MemoryEntry memory=CognitionEngine.process(s,e);
  double source=l.sourceConfidence>=0?l.sourceConfidence:.35,weight=(partial?.30:.58)*(.65+.35*source)*(1-.35*l.difficulty);
  for(String concept:l.concepts)HaruKnowledgeRevisionEngine.applyEvidence(s,concept,l.claim,1,weight,l.sourceLayer,l.sourceRef,memory,now);
  if(partial){l.status="PARTIAL";l.nextReviewAt=now+30L*60L*1000L;}else{l.status="LEARNED";l.nextReviewAt=0;}
  // SKILL_THEORY deliberately never changes skills or learnedActions. Practice/outcome remains the only skill path.
  respond(s,l,r,now,memory,partial?"partial causal learning":"causal learning");
 }

 private static LessonState nextLesson(WorldState s,long now){
  List<LessonState> all=new ArrayList<>(s.characterGod.lessons.values());for(int i=all.size()-1;i>=0;i--){LessonState l=all.get(i);if(l==null)continue;if(now<l.offeredAt||now-l.offeredAt>MAX_AGE)continue;
   if("OFFERED".equals(l.status))return l;
   if(("DEFERRED".equals(l.status)||"QUESTIONED".equals(l.status)||"PARTIAL".equals(l.status))&&l.nextReviewAt>0&&now>=l.nextReviewAt)return l;
  }return null;
 }
 private static void respond(WorldState s,LessonState l,Response r,long now,MemoryEntry m,String reason){
  String id="haru_teaching_response_"+l.lessonId+"_"+l.attempts;String summary="lesson="+l.lessonId+" response="+r.name()+" reason="+reason+(m==null?"":" memory="+m.memoryId);
  WorldEventBus.publishId(s,now,id,RESPONSE_TYPE,l.lessonId,summary);
 }
 private static int missingPrerequisites(WorldState s,LessonState l){int n=0;for(String p:l.prerequisites){ConceptKnowledgeState k=s.characterGod.conceptKnowledge.get(p);if((k==null||k.confidence<.55)&&s.knowledge.getOrDefault(p,0)<2)n++;}return n;}
 private static double knownConfidence(WorldState s,LessonState l){double x=0;int n=0;for(String c:l.concepts){ConceptKnowledgeState k=s.characterGod.conceptKnowledge.get(c);x+=k==null?Math.min(1,s.knowledge.getOrDefault(c,0)/4.0):k.confidence;n++;}return n==0?0:x/n;}
 private static boolean conflicts(WorldState s,LessonState l){for(String c:l.concepts){ConceptKnowledgeState k=s.characterGod.conceptKnowledge.get(c);if(k!=null&&k.confidence>.55&&!k.claim.isEmpty()&&!k.claim.equalsIgnoreCase(l.claim))return true;}return false;}
 private static double relevance(WorldState s,LessonState l){double r=.16;String hay=((s.currentIntention==null?"":s.currentIntention)+" "+(s.planState==null?"":s.planState.goal)).toLowerCase(Locale.ROOT);for(String c:l.concepts){String n=c.toLowerCase(Locale.ROOT);if(!n.isEmpty()&&hay.contains(n))r+=.22;for(OpenQuestionState q:s.characterGod.openQuestions.values())if(!"RESOLVED".equals(q.status)&&(q.topic.equals(c)||q.question.toLowerCase(Locale.ROOT).contains(n)))r+=.24;}r+=Math.max(0,.18-knownConfidence(s,l)*.18);return unit(r);}
 private static double priorExperience(WorldState s,LessonState l){double sum=0,w=0;for(MemoryEntry m:s.memories)for(String c:l.concepts)if(m.hasTag("lesson_topic:"+c)){sum+=m.valence*Math.max(.2,m.importance);w+=Math.max(.2,m.importance);}return w==0?.5:unit(.5+sum/w*.5);}
 private static String firstConcept(LessonState l){return l.concepts.isEmpty()?l.topic:l.concepts.get(0);}
 private static boolean bodyOverloaded(WorldState s){return s.body!=null&&(s.body.energy<24||s.body.sleepiness>82||s.body.pain>62||s.body.health<52);}
 private static double unit(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):.5;}
}
