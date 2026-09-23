package com.aicharacter.v3;

/**
 * Presentation-only emotional camera director.
 *
 * Reads Haru's lived emotional/behavioral state and chooses framing only.
 * It never changes simulation, cognition, emotion, movement or World Truth.
 */
public final class EmotionCameraDirector {
 public enum Shot{WIDE,MEDIUM,CLOSE,INTIMATE_CLOSE}
 public static final class Frame{
  public final Shot shot;public final float zoom,subjectScreenY,focusX,focusBlend,ease;public final long minHoldMs;public final String reason;
  Frame(Shot s,float z,float sy,float fx,float fb,float e,long hold,String r){shot=s;zoom=z;subjectScreenY=sy;focusX=fx;focusBlend=fb;ease=e;minHoldMs=hold;reason=r;}
 }
 private Shot current=Shot.WIDE,candidate=Shot.WIDE;
 private long currentSince,candidateSince;

 public void reset(){current=Shot.WIDE;candidate=Shot.WIDE;currentSince=0;candidateSince=0;}

 public Frame direct(WorldState s,long now){
  Desired d=desired(s,now);
  if(currentSince<=0){current=d.shot;candidate=d.shot;currentSince=now;candidateSince=now;}
  if(d.shot!=current){
   if(d.shot!=candidate){candidate=d.shot;candidateSince=now;}
   long age=Math.max(0,now-currentSince),candidateAge=Math.max(0,now-candidateSince);
   boolean upgrade=rank(d.shot)>rank(current);
   long currentHold=holdFor(current),settle=upgrade?180L:680L;
   if((upgrade&&(d.urgency>=.78||age>=Math.min(650,currentHold/3)))||(!upgrade&&age>=currentHold&&candidateAge>=settle)){
    current=d.shot;currentSince=now;candidate=current;candidateSince=now;
   }
  }else{candidate=current;candidateSince=now;}

  float zoom=zoomFor(current),sy=screenYFor(current),focusX=focusX(s,d.social),blend=focusBlendFor(current,d.social),ease=easeFor(current);
  String reason=d.reason;
  if(current!=d.shot)reason="holding "+current+" while candidate "+d.shot+" settles; "+d.reason;
  return new Frame(current,zoom,sy,focusX,blend,ease,holdFor(current),reason);
 }

 private Desired desired(WorldState s,long now){
  if(s==null)return new Desired(Shot.WIDE,0,false,"no character state");
  boolean travel=s.girlTravel!=null&&s.girlTravel.active;
  EmotionEpisodeState episode=latestActiveEpisode(s,now);
  double intensity=episode==null?aggregateEmotion(s):episode.intensity;
  double fear=s.emotion==null?0:s.emotion.fear,anger=s.emotion==null?0:s.emotion.anger,sad=s.emotion==null?0:s.emotion.sadness,lonely=s.emotion==null?0:s.emotion.loneliness,joy=s.emotion==null?0:s.emotion.joy,curiosity=s.emotion==null?0:s.emotion.curiosity;
  double peak=Math.max(intensity,Math.max(Math.max(fear,anger),Math.max(Math.max(sad,lonely),Math.max(joy,curiosity))));
  boolean social=isSocialMoment(s,episode,now),reflective=isReflective(s,now),bodyAlarm=episode!=null&&"interoception".equals(episode.sourceKind)&&episode.intensity>.34;
  if(travel){
   if((fear>.68||bodyAlarm)&&peak>.62)return new Desired(Shot.MEDIUM,peak,social,"strong affect during travel; stay readable without losing route context");
   return new Desired(Shot.WIDE,peak,social,"travel needs environmental context");
  }
  if(social&&peak>=.67)return new Desired(Shot.INTIMATE_CLOSE,peak,social,"strong social emotion is the current visible moment");
  if((fear>=.70||anger>=.72||sad>=.72||lonely>=.74||joy>=.78)&&peak>=.66)return new Desired(Shot.INTIMATE_CLOSE,peak,social,"strong emotion needs facial and upper-body readability");
  if(social&&peak>=.38)return new Desired(Shot.CLOSE,peak,social,"social interaction should keep Haru and the nearby cat readable");
  if(bodyAlarm||reflective&&peak>=.42||peak>=.58)return new Desired(Shot.CLOSE,peak,social,bodyAlarm?"body alarm is emotionally salient":"current emotion/thought deserves a close character frame");
  if(reflective||peak>=.30)return new Desired(Shot.MEDIUM,peak,social,reflective?"current reflection benefits from a nearer quiet frame":"moderate emotion benefits from a nearer frame");
  return new Desired(Shot.WIDE,peak,social,"no strong emotional presentation need");
 }

 private static EmotionEpisodeState latestActiveEpisode(WorldState s,long now){
  if(s.emotionEpisodes==null)return null;
  for(int i=s.emotionEpisodes.size()-1;i>=0;i--){EmotionEpisodeState x=s.emotionEpisodes.get(i);if(x==null||!"ACTIVE".equals(x.status)||x.intensity<.08)continue;if(x.updatedAt>0&&now>=x.updatedAt&&now-x.updatedAt>12L*60L*1000L)continue;return x;}
  return null;
 }
 private static boolean isReflective(WorldState s,long now){
  String id=s.currentIntention==null?"":s.currentIntention,a=s.haruActivity==null?"":s.haruActivity.toLowerCase();
  if("reflect".equals(id)||"quiet_pause".equals(id)||a.contains("think")||a.contains("quiet thought"))return true;
  if(s.thoughts!=null&&!s.thoughts.isEmpty()){ThoughtState t=s.thoughts.get(s.thoughts.size()-1);return t!=null&&t.isCurrent(now,s.currentIntention)&&t.emotionalWeight>=.40;}
  return false;
 }
 private static boolean isSocialMoment(WorldState s,EmotionEpisodeState e,long now){
  if(s==null)return false;boolean near=Math.abs(s.haruX-s.catX)<=360f||s.catState!=null&&"girl".equals(s.catState.attachedToEntity);
  boolean related=e!=null&&("cat".equals(e.targetId)||e.socialRelevance>=.42||(e.cause!=null&&e.cause.toLowerCase().contains("cat")));
  boolean intention="find_cat".equals(s.currentIntention)||s.reunionContext!=null&&!s.reunionContext.isEmpty();
  return near&&(related||intention);
 }
 private static float focusX(WorldState s,boolean social){
  if(s==null)return 0;if(social&&Math.abs(s.haruX-s.catX)<=430f)return s.haruX*.68f+s.catX*.32f;return s.haruX;
 }
 private static float focusBlendFor(Shot s,boolean social){if(s==Shot.WIDE)return social?.22f:.10f;if(s==Shot.MEDIUM)return social?.48f:.58f;if(s==Shot.CLOSE)return social?.72f:.84f;return social?.78f:.94f;}
 private static float zoomFor(Shot s){if(s==Shot.MEDIUM)return 1.16f;if(s==Shot.CLOSE)return 1.36f;if(s==Shot.INTIMATE_CLOSE)return 1.56f;return 1f;}
 private static float screenYFor(Shot s){if(s==Shot.MEDIUM)return .61f;if(s==Shot.CLOSE)return .58f;if(s==Shot.INTIMATE_CLOSE)return .555f;return .62f;}
 private static float easeFor(Shot s){if(s==Shot.INTIMATE_CLOSE)return .105f;if(s==Shot.CLOSE)return .115f;if(s==Shot.MEDIUM)return .10f;return .085f;}
 private static long holdFor(Shot s){if(s==Shot.INTIMATE_CLOSE)return 3400L;if(s==Shot.CLOSE)return 2700L;if(s==Shot.MEDIUM)return 1900L;return 1200L;}
 private static int rank(Shot s){return s==Shot.INTIMATE_CLOSE?3:s==Shot.CLOSE?2:s==Shot.MEDIUM?1:0;}
 private static double aggregateEmotion(WorldState s){if(s==null||s.emotion==null)return 0;return Math.max(Math.max(s.emotion.fear,s.emotion.anger),Math.max(Math.max(s.emotion.sadness,s.emotion.loneliness),Math.max(s.emotion.joy,s.emotion.curiosity)));}
 private static final class Desired{final Shot shot;final double urgency;final boolean social;final String reason;Desired(Shot s,double u,boolean social,String r){shot=s;urgency=Double.isFinite(u)?Math.max(0,Math.min(1,u)):0;this.social=social;reason=r;}}
}
