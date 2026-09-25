package com.aicharacter.v3;

import java.util.*;

/**
 * Converts healthy, low-pressure idle choices into visibly lived autonomy.
 * This never overrides urgent body/safety needs. The chosen destination comes
 * from Haru's curiosity, memory coverage, world semantics and reachability.
 */
public final class HaruVisibleAgencyEngine {
 private static final double MIN_CURIOSITY=.46;
 private HaruVisibleAgencyEngine(){}

 public static LifeDecision adjustChoice(WorldState s,LifeDecision selected,long now){
  if(s==null||selected==null||selected.intention==null||s.world==null)return selected;
  if(!healthyForExploration(s)||s.personality==null||s.personality.curiosity<MIN_CURIOSITY)return selected;
  if(isProtectedNeed(selected.intention.id)||choiceAlreadyMovesVisibly(s,selected.intention))return selected;

  WorldArea here=s.world.areaAt(s.haruX);
  WorldArea best=null;double bestScore=Double.NEGATIVE_INFINITY;
  for(WorldArea a:s.world.areas){
   if(a==null||here!=null&&a.id.equals(here.id))continue;
   List<String> route=WorldPathPlanner.route(s,"girl",here==null?"":here.id,a.id);
   if(route==null||route.isEmpty())continue;
   double score=areaScore(s,a,now);
   if(score>bestScore){bestScore=score;best=a;}
  }
  if(best==null||bestScore<3.0)return selected;

  Intention in=new Intention("explore_world",0,"curiosity",best.id,
    "see what has changed there and gather firsthand evidence",.28,now+5400000L);
  LifeDecision d=new LifeDecision(in)
    .reason("curiosity",s.personality.curiosity*12)
    .reason("memory_novelty",memoryNovelty(s,best.id,now)*10)
    .reason("world_interest",semanticInterest(best))
    .reason("healthy_body",4)
    .reason("visible_agency_recovery",3);
  in.utility=d.reasons.values().stream().mapToDouble(Double::doubleValue).sum();
  WorldEventBus.publishId(s,now,"haru_visible_choice_"+Long.toHexString(now),
    "HARU_VISIBLE_AUTONOMY_CHOSEN",best.id,
    "Haru independently chose a reachable place to investigate after a low-pressure idle choice.");
  return d;
 }

 static boolean healthyForExploration(WorldState s){
  if(s==null||s.body==null||s.hydration==null||s.digestive==null)return false;
  return s.body.energy>=58&&s.body.sleepiness<=48&&s.body.pain<=18&&s.body.health>=82
    &&DigestionHydrationEngine.thirst(s)<.44&&DigestionHydrationEngine.hunger(s)<.52
    &&DigestionHydrationEngine.bladderUrgency(s)<.58&&!BodyRhythmEngine.isSleeping(s);
 }

 static boolean isProtectedNeed(String id){
  return "sleep".equals(id)||"seek_shelter".equals(id)||"recover".equals(id)
    ||"eat".equals(id)||"drink".equals(id)||"toilet".equals(id)
    ||"try_herb".equals(id);
 }

 static boolean choiceAlreadyMovesVisibly(WorldState s,Intention in){
  if(s==null||in==null||s.world==null)return false;
  WorldObject o=s.world.object(in.targetId);
  if(o!=null)return Math.abs(HaruVisionEngine.actualX(s,o)-s.haruX)>=80f;
  WorldArea a=s.world.area(in.targetId),here=s.world.areaAt(s.haruX);
  return a!=null&&(here==null||!a.id.equals(here.id));
 }

 private static double areaScore(WorldState s,WorldArea a,long now){
  double score=memoryNovelty(s,a.id,now)*10+semanticInterest(a);
  WorldArea here=s.world.areaAt(s.haruX);
  if(here!=null){
   double distance=Math.abs(((a.left+a.right)*.5)-s.haruX);
   score+=Math.min(5,distance/260.0);
  }
  if(s.environment!=null&&"RAIN".equals(s.environment.weather)&&s.environment.weatherIntensity>.62)
   score-=WorldSemantics.exposure(a)*9;
  return score;
 }

 private static double semanticInterest(WorldArea a){
  if(a==null)return 0;String tags=a.tags==null?"":a.tags;
  double x=0;
  if(tags.contains("vegetation"))x+=2.4;
  if(tags.contains("water")||tags.contains("lake"))x+=2.2;
  if(tags.contains("quiet")||tags.contains("reflect"))x+=1.3;
  if(tags.contains("grove"))x+=1.5;
  if(tags.contains("path"))x+=.8;
  return x;
 }

 private static double memoryNovelty(WorldState s,String areaId,long now){
  if(s==null||s.memories==null||areaId==null)return 1;
  int recent=0,total=0;
  for(int i=s.memories.size()-1;i>=0;i--){
   MemoryEntry m=s.memories.get(i);if(m==null||!areaId.equals(m.location))continue;
   total++;
   if(m.time>0&&now>=m.time&&now-m.time<=24L*3600000L)recent++;
   if(total>=12)break;
  }
  return Math.max(.08,1.0-Math.min(.82,recent*.14+Math.max(0,total-recent)*.045));
 }
}
