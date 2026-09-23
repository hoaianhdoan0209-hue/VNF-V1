package com.aicharacter.v3;

/**
 * Lets emotional episodes persist, resolve and regulate on the same active/offline clock.
 * Regulation changes affect intensity, never memories or factual beliefs.
 */
public final class EmotionRegulationEngine {
 private EmotionRegulationEngine(){}

 public static void advance(WorldState s,double seconds,long now){
  if(s==null||seconds<=0)return;if(s.emotion==null)s.emotion=new EmotionState();if(s.mood==null)s.mood=new MoodState();
  EmotionAppraisalEngine.observeBody(s,now);
  double minutes=Math.min(seconds/60.0,360),reg=regulationStrength(s,now),joy=0,fear=0,sad=0,anger=0,curiosity=0,lonely=0,relief=0,weight=0;
  for(EmotionEpisodeState x:s.emotionEpisodes){
   if(x==null||!"ACTIVE".equals(x.status))continue;double persistence=persistence(s,x,now),tau=halfLifeMinutes(x.primaryEmotion)*(1+.85*persistence),decay=Math.exp(-minutes/Math.max(2,tau));
   double regulationFactor=Math.max(.35,1-reg*.48);x.intensity=cl01(x.intensity*Math.pow(decay,regulationFactor));x.updatedAt=Math.max(x.updatedAt,now);
   if(x.intensity<.045&&!causeStillActive(s,x,now)){x.status="RESOLVED";x.resolvedAt=now;continue;}
   double w=Math.max(.02,x.intensity);joy+=x.joy*w;fear+=x.fear*w;sad+=x.sadness*w;anger+=x.anger*w;curiosity+=x.curiosity*w;lonely+=x.loneliness*w;relief+=x.relief*w;weight+=w;
  }
  double body=bodyAlarm(s),den=Math.max(1,weight);
  double targetJoy=cl01(joy/den),targetFear=cl01(Math.max(fear/den,body*.42)),targetSad=cl01(sad/den),targetAnger=cl01(anger/den),targetCuriosity=cl01(curiosity/den),targetLonely=cl01(lonely/den);
  double targetCalm=cl01(1-Math.max(Math.max(targetFear,targetAnger),body*.65)+cl01(relief/den)*.30);
  double follow=1-Math.exp(-minutes/Math.max(.4,4.5-reg*2.2));
  s.emotion.joy=mix(s.emotion.joy,targetJoy,follow);s.emotion.fear=mix(s.emotion.fear,targetFear,follow);s.emotion.sadness=mix(s.emotion.sadness,targetSad,follow);s.emotion.anger=mix(s.emotion.anger,targetAnger,follow);s.emotion.curiosity=mix(s.emotion.curiosity,targetCuriosity,follow);s.emotion.loneliness=mix(s.emotion.loneliness,targetLonely,follow);s.emotion.calm=mix(s.emotion.calm,targetCalm,follow);
  s.emotion.normalize();s.haruMood=s.emotion.dominant();
  if(s.mood!=null){double arousalTarget=clSigned(Math.max(targetFear,targetAnger)*.75+body*.45-targetCalm*.22);s.mood.arousal=mixSigned(s.mood.arousal,arousalTarget,follow*.55);s.mood.pleasantness=mixSigned(s.mood.pleasantness,clSigned(targetJoy*.55-targetSad*.42-targetFear*.25-targetAnger*.18),follow*.28);s.mood.loneliness=mixSigned(s.mood.loneliness,targetLonely,follow*.40);s.mood.normalize();}
 }

 public static double regulationStrength(WorldState s,long now){
  if(s==null)return 0;double r=.08+(s.personality==null?0:s.personality.patience*.18)+(s.emotion==null?0:s.emotion.calm*.14);String id=s.currentIntention==null?"":s.currentIntention;
  if("quiet_pause".equals(id)||"reflect".equals(id))r+=.34;if("recover".equals(id)||"seek_shelter".equals(id))r+=.24;if("sleep".equals(id))r+=.38;
  if(s.relationship!=null&&s.lastCatSeenAt>0&&now>=s.lastCatSeenAt&&now-s.lastCatSeenAt<10L*60L*1000L)r+=cl01(s.relationship.comfort/100.0)*.08;
  return cl01(r);
 }

 private static boolean causeStillActive(WorldState s,EmotionEpisodeState x,long now){
  if("interoception".equals(x.sourceKind))return bodyAlarm(s)>.24;
  if(x.loneliness>.25||x.loss>.35){if(x.targetId.equals("cat")||x.cause.toLowerCase().contains("cat"))return s.lastCatSeenAt<=0||now-s.lastCatSeenAt>8L*60L*1000L;}
  if(x.threat>.35)return NeedState.evaluate(s).safety>32||bodyAlarm(s)>.28;
  if(x.anger>.35&&s.relationship!=null)return s.relationship.irritation>28||s.relationship.hurt>32;
  return false;
 }
 private static double persistence(WorldState s,EmotionEpisodeState x,long now){double p=causeStillActive(s,x,now)?.75:0;if(x.socialRelevance>.5&&x.loss>.3)p+=.12;if(x.bodilyLoad>.4)p+=.10;return cl01(p);}
 private static double halfLifeMinutes(String e){if("afraid".equals(e))return 18;if("angry".equals(e))return 34;if("sad".equals(e))return 105;if("lonely".equals(e))return 150;if("curious".equals(e))return 42;if("joyful".equals(e))return 70;if("relieved".equals(e))return 25;return 24;}
 private static double bodyAlarm(WorldState s){double pain=s.body==null?0:s.body.pain/100.0,stress=s.endocrine==null?0:s.endocrine.stressResponse,breath=s.respiration==null?0:s.respiration.breathingLoad,nerv=s.nervous==null?0:Math.max(s.nervous.balanceAlarm,s.nervous.protectiveReflex),thermal=s.thermal==null?0:Math.max(s.thermal.coldLoad,s.thermal.heatLoad);return cl01(pain*.28+stress*.26+breath*.20+nerv*.18+thermal*.08);}
 private static double mix(double a,double b,double k){return cl01(a+(b-a)*cl01(k));}private static double mixSigned(double a,double b,double k){return clSigned(a+(b-a)*cl01(k));}
 private static double cl01(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}private static double clSigned(double v){return Double.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
}
