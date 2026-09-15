package com.aicharacter.v3;import java.util.*;
/** Shared causal choice. History/personality/belief/relationship alter scores; clock never maps to a destination. */
public final class LifeDecisionEngine {private LifeDecisionEngine(){}
 public static LifeDecision choose(WorldState s,long now){NeedState n=NeedState.evaluate(s);double hour=s.worldMinutes/60.0;List<LifeDecision> c=new ArrayList<>();
  add(c,new Intention("sleep",0,"rest","shelter_01","sleep",.9,now+10800000),"sleepiness",n.rest,"night_context",(hour>=22||hour<6)?18:0,"pain",s.body.pain*.6);
  add(c,new Intention("seek_shelter",0,"safety","shelter_01","stay dry",.9,now+7200000),"rain","RAIN".equals(s.environment.weather)?58+s.environment.weatherIntensity*25:0,"pain",s.body.pain*.5);
  LifeDecision reflect=add(c,new Intention("reflect",0,"curiosity","bench_lake_01","reflect",.3,now+10800000),"curiosity",n.curiosity*.2,"late_light_context",hour>=16&&hour<19?7:0,"rain","RAIN".equals(s.environment.weather)?-25:0,"energy",(s.body.energy-45)*.12);
  psychological(s,reflect,"lakeside","reflect","RAIN".equals(s.environment.weather),now);world(s,reflect,"lakeside",false);
  LifeDecision solitude=add(c,new Intention("seek_solitude",0,"solitude","lake_tree_03","quiet",.4,now+7200000),"solitude",n.solitude,"hurt",s.relationship.hurt*.22,"rain","RAIN".equals(s.environment.weather)?-18:0);
  psychological(s,solitude,"quiet_grove","seek_solitude","RAIN".equals(s.environment.weather),now);world(s,solitude,"quiet_grove",false);
  double away=Math.max(0,(now-s.lastCatSeenAt)/3600000.0),belief=s.beliefStates.containsKey("cat_returns")?s.beliefStates.get("cat_returns").confidence:.5;
  LifeDecision find=add(c,new Intention("find_cat",0,"connection",rememberedCatPlace(s),"find cat",.65,now+14400000),"absence",Math.min(25,away*2.2),"returns_belief",-belief*7,"loneliness",s.mood.loneliness*18,"known_carried",(s.catState.carryKnownByGirl&&"girl".equals(s.catState.attachedToEntity))?-1000:0);
  psychological(s,find,find.intention.targetId,"find_cat","RAIN".equals(s.environment.weather),now);world(s,find,find.intention.targetId,true);
  for(LifeDecision d:c)d.intention.utility=sum(d.reasons)+(d.intention.id.equals(s.currentIntention)?5:0);
  DeliberationEngine.apply(s,c,now);c.sort((a,b)->Double.compare(b.intention.utility,a.intention.utility));LifeDecision selected=c.get(0);
  if(s.planState!=null&&s.planState.active()){LifeDecision current=null;for(LifeDecision d:c)if(d.intention.id.equals(s.planState.intentionId)){current=d;break;}if(current!=null&&selected.intention.utility-current.intention.utility<5+12*s.planState.commitment)selected=current;}
  // Thought must describe the FINAL intention after commitment/inertia, not the provisional challenger.
  DeliberationEngine.recordThought(s,c,selected,now);
  s.lastDecisionTrace=trace(c,selected,s);return selected;}
 private static void world(WorldState s,LifeDecision d,String areaId,boolean urgent){WorldArea a=s.world==null?null:s.world.area(areaId);if(a==null)return;d.reason("world_comfort",WorldSemantics.comfort(s,a)*10);d.reason("visibility",urgent?(s.visibility-.5)*4:(s.visibility-.5)*2);}
 private static void psychological(WorldState s,LifeDecision d,String place,String action,boolean risk,long now){HistoryDecisionEngine.place(s,place,now,d.reasons);HistoryDecisionEngine.personality(s,action,risk,d.reasons);HistoryDecisionEngine.habit(s,action,place,d.reasons);HistoryDecisionEngine.relationship(s,action,d.reasons);}
 private static double sum(Map<String,Double>m){double v=0;for(double x:m.values())v+=x;return v;}
 private static String trace(List<LifeDecision>c,LifeDecision selected,WorldState s){StringBuilder b=new StringBuilder("WHY HARU V2\nperception=").append(HaruPerception.currentPlaceId(s)).append(" mood=").append(s.mood.label()).append(" bodyEnergy=").append(s.body.energy).append(" pain=").append(s.body.pain).append('\n');for(LifeDecision d:c)b.append("- ").append(d.explain()).append(" total=").append(String.format(Locale.US,"%.2f",d.intention.utility)).append('\n');return b.append("selected=").append(selected.intention.id).toString();}
 private static LifeDecision add(List<LifeDecision>out,Intention i,Object...pairs){LifeDecision d=new LifeDecision(i);for(int x=0;x<pairs.length;x+=2)d.reason((String)pairs[x],((Number)pairs[x+1]).doubleValue());out.add(d);return d;}
 private static String rememberedCatPlace(WorldState s){List<MemoryEntry>ms=MemoryRetrievalEngine.retrieve(s,null,"cat","player",8);for(MemoryEntry m:ms)if(m.location!=null&&!m.location.isEmpty())return m.location;for(BeliefState b:s.beliefStates.values())if(b.subject.startsWith("cat_at:")&&b.confidence>.55)return b.subject.substring(7);return"home_shelter";}
}