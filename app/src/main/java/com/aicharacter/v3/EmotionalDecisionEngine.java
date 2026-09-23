package com.aicharacter.v3;

import java.util.*;

/** Mixed emotions bias deliberation without becoming commands. */
public final class EmotionalDecisionEngine {
 private EmotionalDecisionEngine(){}

 public static void apply(WorldState s,List<LifeDecision> candidates,long now){
  if(s==null||candidates==null||candidates.isEmpty()||s.emotion==null)return;EmotionState e=s.emotion;
  for(LifeDecision d:candidates){String id=d.intention.id;double v=0;
   if(isSafety(id))v+=e.fear*8.5+e.sadness*2.0;
   if(isOptionalExplore(id))v+=e.curiosity*7.2+e.joy*2.4-e.fear*7.4-e.sadness*2.8;
   if("find_cat".equals(id))v+=e.loneliness*9.0+e.joy*1.2-e.anger*2.2;
   if("seek_solitude".equals(id))v+=e.anger*5.3+e.sadness*2.0-e.loneliness*4.4;
   if("quiet_pause".equals(id)||"reflect".equals(id))v+=e.anger*4.8+e.sadness*3.6+e.fear*2.2+e.calm*1.6;
   if("sleep".equals(id))v+=e.sadness*1.5+e.fear*.8;
   if(Math.abs(v)>.001)d.reason("mixed_emotion_bias",v);
   d.intention.utility=sum(d.reasons)+(id.equals(s.currentIntention)?5:0);
  }
 }

 public static double attentionNarrowing(WorldState s){if(s==null||s.emotion==null)return 0;return cl01(s.emotion.fear*.62+s.emotion.anger*.28-s.emotion.calm*.22);}
 public static double reflectiveSupport(WorldState s){if(s==null||s.emotion==null)return 1;return Math.max(.58,Math.min(1,1-attentionNarrowing(s)*.28+s.emotion.calm*.10));}

 private static boolean isSafety(String id){return"seek_shelter".equals(id)||"recover".equals(id);}
 private static boolean isOptionalExplore(String id){return"explore_garden".equals(id)||"observe_lake".equals(id)||"watch_reedling".equals(id)||"study_herb".equals(id)||"study_ecology".equals(id)||"compare_concept".equals(id);}
 private static double sum(Map<String,Double>m){double v=0;for(double x:m.values())if(Double.isFinite(x))v+=x;return v;}private static double cl01(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
