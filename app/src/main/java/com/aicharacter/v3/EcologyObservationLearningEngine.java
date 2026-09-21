package com.aicharacter.v3;
import java.util.*;
/** Haru infers ecology from repeated visible co-occurrence. Hypotheses may be wrong and are revised only by later observations. */
public final class EcologyObservationLearningEngine{
 private static final long SAMPLE_MS=60000L,EVIDENCE_COOLDOWN_MS=8L*60000L,RECENT_CREATURE_MS=3L*60000L;
 private EcologyObservationLearningEngine(){}
 public static void observe(WorldState s,long now){
  if(s==null||s.world==null||BodyRhythmEngine.isSleeping(s))return;
  if(s.ecologyObservation==null)s.ecologyObservation=new EcologyObservationState();
  EcologyObservationState st=s.ecologyObservation;
  if(st.lastObservedAt>0&&now-st.lastObservedAt<SAMPLE_MS)return;
  HaruVisionEngine.Snapshot v=HaruVisionEngine.observe(s);if(v.attention*v.visibility<.24)return;
  List<WorldObject> creatures=new ArrayList<>(),flora=new ArrayList<>();
  for(HaruVisionEngine.Seen x:v.seen){WorldObject o=s.world.object(x.id);if(o==null)continue;if("creature".equals(o.type))creatures.add(o);else if(LivingWorldEngine.isLivingFlora(o))flora.add(o);}
  learnCreaturePairs(s,st,creatures,now);
  for(WorldObject plant:flora)learnCreatureFlora(s,st,creatures,plant,now);
  for(WorldObject c:creatures){st.creatureSeen.put(c.id,now);st.creatureX.put(c.id,(double)HaruVisionEngine.actualX(s,c));st.creatureArea.put(c.id,HaruVisionEngine.actualAreaId(s,c));}
  st.lastObservedAt=now;trim(st,now);
 }
 private static void learnCreaturePairs(WorldState s,EcologyObservationState st,List<WorldObject> creatures,long now){
  for(int i=0;i<creatures.size();i++)for(int j=i+1;j<creatures.size();j++){
   WorldObject a=creatures.get(i),b=creatures.get(j);String areaA=HaruVisionEngine.actualAreaId(s,a),areaB=HaruVisionEngine.actualAreaId(s,b);if(!areaA.equals(areaB))continue;
   Long ta=st.creatureSeen.get(a.id),tb=st.creatureSeen.get(b.id);Double xa=st.creatureX.get(a.id),xb=st.creatureX.get(b.id);String pa=st.creatureArea.get(a.id),pb=st.creatureArea.get(b.id);
   if(ta==null||tb==null||xa==null||xb==null||pa==null||pb==null||!pa.equals(areaA)||!pb.equals(areaB)||now-ta>RECENT_CREATURE_MS||now-tb>RECENT_CREATURE_MS)continue;
   double prior=Math.abs(xa-xb),current=Math.abs(HaruVisionEngine.actualX(s,a)-HaruVisionEngine.actualX(s,b)),change=current-prior;String key=pairSubject(a.id,b.id),sig=Math.abs(change)>=24?(change>0?"apart":"closer"):"stable",cool=key+":"+sig;
   if(now-st.evidenceCooldown.getOrDefault(cool,0L)<EVIDENCE_COOLDOWN_MS)continue;
   if(Math.abs(change)>=24){String value=change>0?"may_avoid_each_other":"may_seek_each_other",text="She noticed the distance between "+label(a)+" and "+label(b)+" became "+(change>0?"larger":"smaller")+" while both remained in view.";MemoryEntry m=pairMemory(s,now,a,b,text,Math.min(.66,.30+Math.abs(change)/260.0));CognitionEngine.reviseFromMemory(s,key,value,1,Math.min(.70,.28+Math.abs(change)/220.0),m,now);st.evidenceCooldown.put(cool,now);}
   else{BeliefState belief=s.beliefStates.get(key);if(belief!=null&&belief.confidence>.22){MemoryEntry m=pairMemory(s,now,a,b,"She watched "+label(a)+" and "+label(b)+" again, but their spacing did not repeat the pattern clearly.",.28);CognitionEngine.reviseFromMemory(s,key,belief.value,-1,.22,m,now);st.evidenceCooldown.put(cool,now);}}
  }
 }
 private static void learnCreatureFlora(WorldState s,EcologyObservationState st,List<WorldObject> creatures,WorldObject plant,long now){
  FloraLifeState f=LivingWorldEngine.flora(s,plant.id);if(f==null)return;EcologyObservationState.FloraSeen prior=st.flora.get(plant.id);
  if(prior!=null&&prior.seenAt>0){double delta=f.opening-prior.opening,growth=f.growthPulse-prior.growthPulse;long elapsed=Math.max(1,now-prior.seenAt);
   for(WorldObject creature:creatures){String key=floraSubject(creature.id,plant.id),cool=key+":"+signal(delta,growth);if(now-st.evidenceCooldown.getOrDefault(cool,0L)<EVIDENCE_COOLDOWN_MS)continue;long previouslySeen=st.creatureSeen.getOrDefault(creature.id,0L);boolean consecutive=previouslySeen>0&&previouslySeen>=prior.seenAt-RECENT_CREATURE_MS;double magnitude=Math.max(Math.abs(delta),Math.abs(growth)*.75);
    if(magnitude>=.07){String change=delta>.055?"opened more than before":delta<-.055?"closed more than before":"changed its living rhythm";MemoryEntry m=floraMemory(s,now,creature,plant,"She noticed that "+label(creature)+" was nearby while "+label(plant)+" "+change+".",Math.min(.68,.28+magnitude*.75));CognitionEngine.reviseFromMemory(s,key,"may_affect_response",1,Math.min(.72,.26+magnitude*.80),m,now);st.evidenceCooldown.put(cool,now);}
    else if(consecutive&&elapsed>=SAMPLE_MS){BeliefState b=s.beliefStates.get(key);if(b!=null&&b.confidence>.20){MemoryEntry m=floraMemory(s,now,creature,plant,"She watched "+label(creature)+" near "+label(plant)+", but this time the living flora did not visibly change in the same way.",.30);CognitionEngine.reviseFromMemory(s,key,b.value,-1,.26,m,now);st.evidenceCooldown.put(cool,now);}}
   }
  }
  EcologyObservationState.FloraSeen cur=new EcologyObservationState.FloraSeen();cur.opening=f.opening;cur.growthPulse=f.growthPulse;cur.seenAt=now;st.flora.put(plant.id,cur);
 }
 public static String bestQuestionTarget(WorldState s){
  if(s==null||s.world==null)return"";String best="";double score=0;
  for(Map.Entry<String,BeliefState>e:s.beliefStates.entrySet()){if(!e.getKey().startsWith("eco_hyp:"))continue;BeliefState b=e.getValue();if(b==null||b.confidence<.18||b.confidence>.72)continue;String plant=parseFlora(e.getKey());WorldObject o=s.world.object(plant);if(o==null||!o.enabled)continue;double uncertainty=1-Math.abs(b.confidence-.45)/.45,recency=recentEvidence(b)>0?.18:0,value=uncertainty+recency;if(value>score){score=value;best=plant;}}
  return best;
 }
 public static double questionDrive(WorldState s){String target=bestQuestionTarget(s);if(target.isEmpty()||s==null)return 0;double curiosity=s.personality==null?.3:s.personality.curiosity,energy=s.body==null?.5:s.body.energy/100.0;return Math.max(0,Math.min(1,curiosity*.62+energy*.22+.12));}
 public static String diagnostic(WorldState s){
  StringBuilder x=new StringBuilder("HARU ECOLOGY HYPOTHESES\n");if(s==null)return x.append("<no state>").toString();
  for(Map.Entry<String,BeliefState>e:s.beliefStates.entrySet()){String k=e.getKey();if(!k.startsWith("eco_hyp:")&&!k.startsWith("eco_pair:"))continue;BeliefState b=e.getValue();x.append(readableSubject(s,k)).append(" | belief=").append(b.value).append(" | confidence=").append(String.format(Locale.US,"%.2f",b.confidence)).append(" | evidence=").append(b.evidence.size()).append('\n');}
  return x.toString();
 }
 private static MemoryEntry floraMemory(WorldState s,long now,WorldObject creature,WorldObject flora,String text,double significance){return CognitionEngine.process(s,new Experience("ecology_observation",text,.03,significance).atTime(now).at(HaruPerception.currentPlaceId(s),flora.id).with(creature.id).tag("observe").tag("ecology").tag("creature:"+creature.id).tag("flora:"+flora.id));}
 private static MemoryEntry pairMemory(WorldState s,long now,WorldObject a,WorldObject b,String text,double significance){return CognitionEngine.process(s,new Experience("ecology_observation",text,.02,significance).atTime(now).at(HaruPerception.currentPlaceId(s),a.id).with(a.id).with(b.id).tag("observe").tag("ecology").tag("creature:"+a.id).tag("creature:"+b.id));}
 private static String floraSubject(String creature,String flora){return"eco_hyp:"+creature+"->"+flora+":response";}
 private static String pairSubject(String a,String b){if(a.compareTo(b)>0){String t=a;a=b;b=t;}return"eco_pair:"+a+"<->"+b+":spacing";}
 private static String parseFlora(String subject){int a=subject.indexOf("->"),b=subject.lastIndexOf(":");return a>=0&&b>a?subject.substring(a+2,b):"";}
 private static String parseCreature(String subject){int a=subject.indexOf("eco_hyp:"),b=subject.indexOf("->");return a>=0&&b>a?subject.substring(a+8,b):"";}
 private static String readableSubject(WorldState s,String subject){
  if(subject.startsWith("eco_pair:")){int a=subject.indexOf(":"),b=subject.indexOf("<->"),c=subject.lastIndexOf(":");String x=b>a?subject.substring(a+1,b):"",y=c>b?subject.substring(b+3,c):"";WorldObject xo=s.world==null?null:s.world.object(x),yo=s.world==null?null:s.world.object(y);return(xo==null?x:label(xo))+" <-> "+(yo==null?y:label(yo));}
  WorldObject c=s.world==null?null:s.world.object(parseCreature(subject)),f=s.world==null?null:s.world.object(parseFlora(subject));return(c==null?parseCreature(subject):label(c))+" -> "+(f==null?parseFlora(subject):label(f));
 }
 private static String label(WorldObject o){return o==null||o.haruDescription==null||o.haruDescription.isEmpty()?o==null?"something":o.id:o.haruDescription;}
 private static String signal(double delta,double growth){return delta>.055?"open":delta<-.055?"close":Math.abs(growth)>.07?"rhythm":"stable";}
 private static long recentEvidence(BeliefState b){if(b==null||b.evidence.isEmpty())return 0;return b.evidence.get(b.evidence.size()-1).timestamp;}
 private static void trim(EcologyObservationState s,long now){
  List<String> stale=new ArrayList<>();for(Map.Entry<String,Long>e:s.creatureSeen.entrySet())if(now-e.getValue()>12L*3600000L)stale.add(e.getKey());for(String id:stale){s.creatureSeen.remove(id);s.creatureX.remove(id);s.creatureArea.remove(id);}
  s.evidenceCooldown.entrySet().removeIf(e->now-e.getValue()>2L*24L*3600000L);while(s.flora.size()>80){Iterator<String>it=s.flora.keySet().iterator();if(!it.hasNext())break;it.next();it.remove();}
 }
}