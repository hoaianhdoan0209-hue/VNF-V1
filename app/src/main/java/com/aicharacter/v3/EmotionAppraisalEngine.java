package com.aicharacter.v3;

import java.util.*;

/**
 * Appraises lived experience into emotion. The same event may produce different
 * emotions because appraisal depends on body, goals, beliefs, relationship and memory.
 */
public final class EmotionAppraisalEngine {
 private EmotionAppraisalEngine(){}

 public static EmotionEpisodeState onExperience(WorldState s,MemoryEntry m,Experience e){
  if(s==null||m==null)return null;ensure(s);EmotionEpisodeState x=new EmotionEpisodeState();x.id="emotion_"+clean(m.memoryId);x.sourceMemoryId=m.memoryId;x.sourceKind=m.kind==null?"":m.kind;x.targetId=targetOf(m,e);x.cause=m.summary==null?"":m.summary;x.createdAt=m.time;x.updatedAt=m.time;
  NeedState n=NeedState.evaluate(s);double sig=cl01(Math.max(.08,m.importance)),val=clSigned(m.valence),body=bodyLoad(s),rel=relationshipRelevance(s,m),unc=uncertainty(s,m),goal=goalCongruence(m,e,val),control=perceivedControl(s,m,e);
  double threat=cl01(body*.38+Math.max(0,n.safety/100.0)*.34+tag(m,"danger")*.48+tag(m,"control")*.28+Math.max(0,-val)*unc*.26);
  double loss=cl01(tag(m,"absence")*.62+tag(m,"loss")*.72+rel*Math.max(0,-val)*.38+(m.kind!=null&&m.kind.contains("failure")?.14:0));
  double frustration=cl01(tag(m,"control")*.70+tag(m,"blocked")*.56+tag(m,"failure")*.30+Math.max(0,-goal)*control*.32);
  x.goalCongruence=goal;x.threat=threat;x.loss=loss;x.frustration=frustration;x.uncertainty=unc;x.socialRelevance=rel;x.bodilyLoad=body;x.perceivedControl=control;x.valence=val;
  double joy=cl01(Math.max(0,goal)*(.52+.28*rel)+tag(m,"kind")*.26+tag(m,"respect")*.22);
  double fear=cl01(threat*(.58+.42*unc));
  double sadness=cl01(loss*(.62+.38*(1-control)));
  double anger=cl01(frustration*(.52+.38*control)+tag(m,"control")*.26);
  double curiosity=cl01(unc*(.46+.42*Math.max(0,s.personality.curiosity))*(1-threat*.55)+tag(m,"curiosity")*.34+tag(m,"novelty")*.32);
  double loneliness=cl01(tag(m,"absence")*rel*.72+Math.max(0,s.mood==null?0:s.mood.loneliness)*.24);
  double relief=cl01(tag(m,"relief")*.7+(Math.max(0,goal)*tag(m,"safety"))*.45);
  x.arousal=cl01(Math.max(Math.max(fear,anger),Math.max(curiosity,body))*.74+Math.abs(val)*.18);
  x.intensity=cl01(sig*(.45+.55*Math.max(Math.max(joy,fear),Math.max(Math.max(sadness,anger),Math.max(curiosity,loneliness)))));
  x.primaryEmotion=primary(joy,fear,sadness,anger,curiosity,loneliness,relief);
  apply(s,x,joy,fear,sadness,anger,curiosity,loneliness,relief);
  s.emotionEpisodes.add(x);while(s.emotionEpisodes.size()>36)s.emotionEpisodes.remove(0);
  return x;
 }

 public static String currentCause(WorldState s,String emotion){
  if(s==null||emotion==null)return"";for(int i=s.emotionEpisodes.size()-1;i>=0;i--){EmotionEpisodeState x=s.emotionEpisodes.get(i);if(x!=null&&"ACTIVE".equals(x.status)&&emotion.equals(x.primaryEmotion))return x.cause;}return"";
 }

 private static void apply(WorldState s,EmotionEpisodeState x,double joy,double fear,double sadness,double anger,double curiosity,double loneliness,double relief){
  double k=.10+.34*x.intensity;s.emotion.joy=blend(s.emotion.joy,joy,k);s.emotion.fear=blend(s.emotion.fear,fear,k);s.emotion.sadness=blend(s.emotion.sadness,sadness,k);s.emotion.anger=blend(s.emotion.anger,anger,k);s.emotion.curiosity=blend(s.emotion.curiosity,curiosity,k);s.emotion.loneliness=blend(s.emotion.loneliness,loneliness,k);
  double calmTarget=cl01(1-Math.max(Math.max(fear,anger),x.bodilyLoad*.75)+relief*.35);s.emotion.calm=blend(s.emotion.calm,calmTarget,.08+.25*x.intensity);
  if(s.mood!=null){s.mood.pleasantness=clSigned(s.mood.pleasantness+x.valence*x.intensity*.10+relief*.05);s.mood.arousal=clSigned(s.mood.arousal+x.arousal*x.intensity*.09);s.mood.loneliness=clSigned(s.mood.loneliness+loneliness*x.intensity*.06);}
  s.haruMood=s.emotion.dominant();
 }

 private static double relationshipRelevance(WorldState s,MemoryEntry m){if(s.relationship==null)return 0;boolean social=m.participants.contains("cat")||tag(m,"social")>0||tag(m,"kind")>0||tag(m,"respect")>0||tag(m,"control")>0||tag(m,"absence")>0;if(!social)return 0;double attachment=cl01(s.relationship.attachment/100.0),affection=cl01(s.relationship.affection/100.0),trust=cl01(s.relationship.trust/100.0);return cl01(.30+.32*attachment+.20*affection+.18*trust);}
 private static double bodyLoad(WorldState s){double pain=s.body==null?0:cl01(s.body.pain/100.0),stress=s.endocrine==null?0:cl01(s.endocrine.stressResponse),breath=s.respiration==null?0:cl01(s.respiration.breathingLoad),alarm=s.nervous==null?0:cl01(Math.max(s.nervous.balanceAlarm,s.nervous.protectiveReflex)),thermal=s.thermal==null?0:cl01(Math.max(s.thermal.coldLoad,s.thermal.heatLoad));return cl01(pain*.30+stress*.24+breath*.18+alarm*.18+thermal*.10);}
 private static double uncertainty(WorldState s,MemoryEntry m){double u=.28;if(tag(m,"novelty")>0||tag(m,"curiosity")>0)u+=.24;if(tag(m,"failure")>0||tag(m,"absence")>0)u+=.18;if(m.confidence<.8)u+=( .8-m.confidence)*.45;if(s.characterGod!=null&&s.characterGod.reasoning!=null){for(OpenQuestionState q:s.characterGod.openQuestions.values())if(q!=null&&!"RESOLVED".equals(q.status)){u+=.04;break;}}return cl01(u);}
 private static double goalCongruence(MemoryEntry m,Experience e,double val){double g=val;if(tag(m,"success")>0)g+=.34;if(tag(m,"failure")>0)g-=.38;if(tag(m,"respect")>0||tag(m,"kind")>0)g+=.14;if(tag(m,"control")>0||tag(m,"danger")>0)g-=.18;return clSigned(g);}
 private static double perceivedControl(WorldState s,MemoryEntry m,Experience e){double c=.5;if(tag(m,"blocked")>0)c-=.24;if(tag(m,"failure")>0)c-=.12;if(tag(m,"control")>0)c+=.18;if(tag(m,"success")>0)c+=.10;if(s.personality!=null)c+=(s.personality.independence-.5)*.12;return cl01(c);}
 private static String targetOf(MemoryEntry m,Experience e){if(e!=null&&e.objectId!=null&&!e.objectId.isEmpty())return e.objectId;if(!m.participants.isEmpty())return m.participants.get(0);return m.location==null?"":m.location;}
 private static double tag(MemoryEntry m,String t){return m!=null&&m.hasTag(t)?1:0;}
 private static String primary(double joy,double fear,double sadness,double anger,double curiosity,double loneliness,double relief){double best=.18;String name="calm";double[]v={joy,fear,sadness,anger,curiosity,loneliness,relief};String[]n={"joyful","afraid","sad","angry","curious","lonely","relieved"};for(int i=0;i<v.length;i++)if(v[i]>best){best=v[i];name=n[i];}return name;}
 private static double blend(double a,double b,double k){a=cl01(a);b=cl01(b);return cl01(a+(b-a)*cl01(k));}
 private static void ensure(WorldState s){if(s.emotion==null)s.emotion=new EmotionState();if(s.mood==null)s.mood=new MoodState();}
 private static String clean(String s){return(s==null?"unknown":s).replaceAll("[^A-Za-z0-9._-]","_");}
 private static double cl01(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}private static double clSigned(double v){return Double.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
}
