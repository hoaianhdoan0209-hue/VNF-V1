package com.aicharacter.v3;
import java.util.*;
/** Haru learns meanings such as safe/danger/shelter/helpful from subjective consequences, not System semantic tags. */
public final class HaruAbstractConceptEngine{
 public static final String SAFE="safe",DANGER="danger",SHELTER="shelter",LIFE_SOURCE="life_source",HELPFUL="helpful",UNCOMFORTABLE="uncomfortable",INTERESTING="interesting";
 private static final long SAMPLE_MS=120000L,EVIDENCE_COOLDOWN_MS=6L*60000L;
 private HaruAbstractConceptEngine(){}
 public static void observe(WorldState s,long now){
  if(s==null||s.world==null||BodyRhythmEngine.isSleeping(s))return;ensure(s);if(s.abstractMeanings.lastObservedAt>0&&now-s.abstractMeanings.lastObservedAt<SAMPLE_MS)return;
  AbstractMeaningState.Snapshot cur=snapshot(s,now),old=s.abstractMeanings.last;
  if(old!=null&&old.at>0&&now-old.at>=45000L){if(cur.placeKey.equals(old.placeKey))learnDelta(s,cur.placeKey,old,cur,1.0,now);if(!cur.targetKey.isEmpty()&&cur.targetKey.equals(old.targetKey))learnDelta(s,cur.targetKey,old,cur,.78,now);}
  touch(s,cur.placeKey,now);if(!cur.targetKey.isEmpty())touch(s,cur.targetKey,now);s.abstractMeanings.last=cur;s.abstractMeanings.lastObservedAt=now;trim(s,now);
 }
 public static double confidence(WorldState s,String targetId,String meaning){
  if(s==null||s.abstractMeanings==null)return 0;double v=0;String key=targetKey(s,targetId);v=Math.max(v,meaningAt(s,key,meaning));WorldArea a=areaForTarget(s,targetId);if(a!=null)v=Math.max(v,meaningAt(s,"place:"+a.id,meaning)*.88);return v;
 }
 public static double decisionBias(WorldState s,String targetId,NeedState n){
  if(s==null||n==null)return 0;double safe=confidence(s,targetId,SAFE),danger=confidence(s,targetId,DANGER),shelter=confidence(s,targetId,SHELTER),life=confidence(s,targetId,LIFE_SOURCE),help=confidence(s,targetId,HELPFUL),bad=confidence(s,targetId,UNCOMFORTABLE),interest=confidence(s,targetId,INTERESTING);
  double needLife=Math.max(n.hunger,n.thirst)/100.0,needShelter=Math.max(n.safety,n.physicalComfort)/100.0,needCur=n.curiosity/100.0;
  return safe*4.5+shelter*(4+10*needShelter)+life*(2+12*needLife)+help*5+interest*(1+7*needCur)-danger*(5+12*n.safety/100.0)-bad*(2+9*n.physicalComfort/100.0);
 }
 public static String currentSummary(WorldState s){
  if(s==null||s.abstractMeanings==null)return"";String placeKey="place:"+HaruPerception.currentPlaceId(s);List<String>place=meaningLabels(s,placeKey);String objectSummary="";
  HaruVisionEngine.Snapshot v=HaruVisionEngine.observe(s);for(HaruVisionEngine.Seen seen:v.seen){String key="object:"+seen.id;List<String>x=meaningLabels(s,key);if(x.isEmpty())continue;WorldObject o=s.world==null?null:s.world.object(seen.id);String name=o==null?seen.label:HaruNamingEngine.personalOrDescription(s,o);objectSummary=" Với "+name+", mình đang nghi nó là "+join(x)+".";break;}
  if(place.isEmpty()&&objectSummary.isEmpty())return"";String base=place.isEmpty()?"":("Mình đang dần hiểu nơi này là "+join(place)+".");return base+objectSummary+" Đó đều là ý nghĩa mình rút ra từ trải nghiệm, nên mình vẫn có thể đổi ý.";
 }
 private static List<String> meaningLabels(WorldState s,String key){List<String>x=new ArrayList<>();for(String m:new String[]{SAFE,DANGER,SHELTER,LIFE_SOURCE,HELPFUL,UNCOMFORTABLE,INTERESTING}){double c=meaningAt(s,key,m);if(c>=.34)x.add(label(m)+" ("+certainty(c)+")");}return x;}

 public static String diagnostic(WorldState s){
  ensure(s);StringBuilder b=new StringBuilder("HARU ABSTRACT MEANINGS\n");for(AbstractMeaningState.Anchor a:s.abstractMeanings.anchors.values()){b.append(readableAnchor(s,a.key)).append(" | obs=").append(a.observations);for(String m:new String[]{SAFE,DANGER,SHELTER,LIFE_SOURCE,HELPFUL,UNCOMFORTABLE,INTERESTING}){AbstractMeaningState.Meaning x=a.meanings.get(m);if(x!=null&&x.confidence>.05)b.append(" | ").append(m).append("=").append(fmt(x.confidence)).append("[+").append(x.support).append("/-").append(x.contradictions).append("]");}b.append('\n');}return b.toString();
 }
 private static void learnDelta(WorldState s,String key,AbstractMeaningState.Snapshot a,AbstractMeaningState.Snapshot b,double weight,long now){
  double ds=b.safety-a.safety,dc=b.discomfort-a.discomfort,dh=b.hunger-a.hunger,dt=b.thirst-a.thirst,dp=b.pain-a.pain,dst=b.stress-a.stress,df=b.fear-a.fear,de=b.energy-a.energy,dtherm=b.thermal-a.thermal,dwet=b.wetness-a.wetness;
  if(ds<-2.5||df<-.045)reinforce(s,key,SAFE,cl((-ds)/16+(-df)*3)*weight,"being here repeatedly made her feel less threatened",now);if(ds>3||dp>1.4||df>.05)reinforce(s,key,DANGER,cl(ds/18+Math.max(0,dp)/10+Math.max(0,df)*2)*weight,"being here coincided with stronger fear, pain, or safety pressure",now);
  if(dc<-3&&(dtherm<-.025||dwet<-.04||ds<0))reinforce(s,key,SHELTER,cl((-dc)/18+Math.max(0,-dtherm)*2+Math.max(0,-dwet))*weight,"her body became more protected and comfortable here",now);
  else if(dc>3.2&&(dtherm>.025||dwet>.04||ds>2))challenge(s,key,SHELTER,cl(dc/22+Math.max(0,dtherm)*2+Math.max(0,dwet))*weight,"later experience did not protect her body the way she expected",now);
  if(dh<-4||dt<-4)reinforce(s,key,LIFE_SOURCE,cl(Math.max(-dh,-dt)/24)*weight,"hunger or thirst eased while she was here",now);
  else if((contains(a.intention,"eat","drink")||contains(b.intention,"eat","drink"))&&dh>-1.2&&dt>-1.2)challenge(s,key,LIFE_SOURCE,.28*weight,"seeking food or water here did not clearly ease the need this time",now);
  if(dp<-1.3||dst<-.04||de>2.5||dc<-3.5)reinforce(s,key,HELPFUL,cl(Math.max(Math.max(-dp/10,-dst*3),Math.max(de/18,-dc/22)))*weight,"something about this place or thing repeatedly helped her body settle",now);
  if(dc>3.5||dst>.045||dp>1.2)reinforce(s,key,UNCOMFORTABLE,cl(dc/20+Math.max(0,dst)*2+Math.max(0,dp)/12)*weight,"her body repeatedly became more uncomfortable here",now);
  boolean voluntary=contains(a.intention,"observe","explore","study","compare","reflect")||contains(b.intention,"observe","explore","study","compare","reflect");if(voluntary&&b.curiosity>38)reinforce(s,key,INTERESTING,cl(.25+b.curiosity/130.0)*weight,"she kept choosing to pay attention here while curiosity stayed active",now);else if(voluntary&&b.curiosity<24)challenge(s,key,INTERESTING,.22*weight,"repeated attention here stopped holding her curiosity as strongly",now);
 }
 private static void reinforce(WorldState s,String key,String meaning,double strength,String evidence,long now){
  if(strength<.16||key==null||key.isEmpty())return;String cd=key+":"+meaning;if(now-s.abstractMeanings.evidenceCooldown.getOrDefault(cd,0L)<EVIDENCE_COOLDOWN_MS)return;AbstractMeaningState.Anchor a=s.abstractMeanings.anchor(key);AbstractMeaningState.Meaning m=a.meaning(meaning);double before=m.confidence;m.support++;m.confidence=AbstractMeaningState.cl(m.confidence+.07+.11*strength);m.lastEvidence=evidence;m.lastUpdatedAt=now;s.abstractMeanings.evidenceCooldown.put(cd,now);String opposite=opposite(meaning);if(!opposite.isEmpty()){AbstractMeaningState.Meaning o=a.meaning(opposite);if(o.confidence>.03){o.contradictions++;o.confidence=AbstractMeaningState.cl(o.confidence-(.04+.07*strength));o.lastUpdatedAt=now;}}
  if((before<.34&&m.confidence>=.34)||m.support==4)remember(s,a,meaning,m,now);
 }
 private static void challenge(WorldState s,String key,String meaning,double strength,String evidence,long now){
  if(strength<.14||key==null||key.isEmpty())return;String cd=key+":"+meaning+":counter";if(now-s.abstractMeanings.evidenceCooldown.getOrDefault(cd,0L)<EVIDENCE_COOLDOWN_MS)return;AbstractMeaningState.Anchor a=s.abstractMeanings.anchor(key);AbstractMeaningState.Meaning m=a.meaning(meaning);double before=m.confidence;m.contradictions++;m.confidence=AbstractMeaningState.cl(m.confidence-(.05+.10*strength));m.lastEvidence=evidence;m.lastUpdatedAt=now;s.abstractMeanings.evidenceCooldown.put(cd,now);if((before>=.34&&m.confidence<.26)||m.contradictions==3)rememberRevision(s,a,meaning,m,now);
 }
 private static void rememberRevision(WorldState s,AbstractMeaningState.Anchor a,String meaning,AbstractMeaningState.Meaning m,long now){if(now-m.lastMemoryAt<30L*60000L)return;m.lastMemoryAt=now;String text="Later experience made her less sure that "+readableAnchor(s,a.key)+" really meant "+label(meaning)+" to her.";CognitionEngine.process(s,new Experience("abstract_meaning_revision",text,-.01,.42).atTime(now).at(HaruPerception.currentPlaceId(s),"").tag("abstract_concept").tag("meaning_revision:"+meaning).tag(a.key));}
 private static void remember(WorldState s,AbstractMeaningState.Anchor a,String meaning,AbstractMeaningState.Meaning m,long now){if(now-m.lastMemoryAt<30L*60000L)return;m.lastMemoryAt=now;String text="From repeated experience, she had begun to think of "+readableAnchor(s,a.key)+" as "+label(meaning)+", though she knew that impression could still change.";CognitionEngine.process(s,new Experience("abstract_meaning",text,meaning.equals(DANGER)||meaning.equals(UNCOMFORTABLE)?-.05:.05,.46).atTime(now).at(HaruPerception.currentPlaceId(s),"").tag("abstract_concept").tag("meaning:"+meaning).tag(a.key));}
 private static AbstractMeaningState.Snapshot snapshot(WorldState s,long now){NeedState n=NeedState.evaluate(s);AbstractMeaningState.Snapshot x=new AbstractMeaningState.Snapshot();x.at=now;x.placeKey="place:"+HaruPerception.currentPlaceId(s);x.targetKey=currentTargetKey(s);x.intention=s.planState==null?"":s.planState.intentionId;x.safety=n.safety;x.discomfort=n.physicalComfort;x.hunger=n.hunger;x.thirst=n.thirst;x.rest=n.rest;x.pain=s.body==null?0:s.body.pain;x.stress=s.endocrine==null?0:s.endocrine.stressResponse;x.fear=s.emotion==null?0:s.emotion.fear;x.curiosity=n.curiosity;x.energy=s.body==null?0:s.body.energy;x.thermal=s.thermal==null?0:Math.max(s.thermal.coldLoad,s.thermal.heatLoad);x.wetness=s.worldWetness;return x;}
 private static String currentTargetKey(WorldState s){if(s.planState==null||!s.planState.active()||s.planState.destination==null||s.planState.destination.isEmpty())return"";WorldObject o=s.world.object(s.planState.destination);if(o==null||!HaruVisionEngine.canSee(s,o)||Math.abs(HaruVisionEngine.actualX(s,o)-s.haruX)>220)return"";return"object:"+o.id;}
 private static void touch(WorldState s,String key,long now){if(key==null||key.isEmpty())return;AbstractMeaningState.Anchor a=s.abstractMeanings.anchor(key);a.observations++;a.lastSeenAt=now;}
 private static double meaningAt(WorldState s,String key,String meaning){if(key==null||key.isEmpty())return 0;AbstractMeaningState.Anchor a=s.abstractMeanings.anchors.get(key);if(a==null)return 0;AbstractMeaningState.Meaning m=a.meanings.get(meaning);return m==null?0:m.confidence;}
 private static String targetKey(WorldState s,String id){if(id==null||id.isEmpty())return"";if(s.world.object(id)!=null)return"object:"+id;if(s.world.area(id)!=null)return"place:"+id;return"";}
 private static WorldArea areaForTarget(WorldState s,String id){if(s==null||s.world==null||id==null)return null;WorldArea a=s.world.area(id);if(a!=null)return a;WorldObject o=s.world.object(id);return o==null?null:s.world.area(o.areaId);}
 private static String readableAnchor(WorldState s,String key){if(key.startsWith("place:")){String id=key.substring(6);WorldArea a=s.world==null?null:s.world.area(id);return a==null?id:a.name;}if(key.startsWith("object:")){String id=key.substring(7);WorldObject o=s.world==null?null:s.world.object(id);return o==null?id:HaruNamingEngine.personalOrDescription(s,o);}return key;}
 private static String label(String m){if(SAFE.equals(m))return"khá an toàn";if(DANGER.equals(m))return"có vẻ nguy hiểm";if(SHELTER.equals(m))return"một chỗ có thể che chở";if(LIFE_SOURCE.equals(m))return"một nguồn giúp duy trì cơ thể";if(HELPFUL.equals(m))return"có ích";if(UNCOMFORTABLE.equals(m))return"gây khó chịu";return"đáng tò mò";}
 private static String certainty(double c){return c>.68?"khá chắc":c>.46?"tương đối tin":"mới chỉ nghi";}
 private static String opposite(String m){if(SAFE.equals(m))return DANGER;if(DANGER.equals(m))return SAFE;if(HELPFUL.equals(m))return UNCOMFORTABLE;if(UNCOMFORTABLE.equals(m))return HELPFUL;return"";}
 private static boolean contains(String x,String...q){if(x==null)return false;String z=x.toLowerCase(Locale.ROOT);for(String s:q)if(z.contains(s))return true;return false;}
 private static String join(List<String>x){StringBuilder b=new StringBuilder();for(int i=0;i<x.size();i++){if(i>0)b.append(i==x.size()-1?" và ":", ");b.append(x.get(i));}return b.toString();}
 private static void trim(WorldState s,long now){s.abstractMeanings.evidenceCooldown.entrySet().removeIf(e->now-e.getValue()>3L*24L*3600000L);while(s.abstractMeanings.anchors.size()>96){Iterator<String>it=s.abstractMeanings.anchors.keySet().iterator();if(!it.hasNext())break;it.next();it.remove();}}
 private static void ensure(WorldState s){if(s.abstractMeanings==null)s.abstractMeanings=new AbstractMeaningState();}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
 private static String fmt(double v){return String.format(Locale.US,"%.2f",v);}
}