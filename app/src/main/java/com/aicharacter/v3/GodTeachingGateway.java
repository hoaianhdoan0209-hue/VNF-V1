package com.aicharacter.v3;

import java.text.Normalizer;
import java.util.*;

/** God may offer durable lessons, never install Haru knowledge, memory, skill, emotion, personality or intent. */
public final class GodTeachingGateway {
 private static final Set<String> SAFE_ACTIONS=new LinkedHashSet<>(Arrays.asList(
         "observe_carefully","read","sketch","tend_garden","prepare_simple_food","clean_space","care_for_creature"));
 private GodTeachingGateway(){}

 public static boolean looksLikeTeaching(String raw){
  String q=norm(raw);return q.startsWith("day ")||q.startsWith("day:")||q.contains("day co ay")||q.startsWith("teach ")||q.contains("cap nhat kien thuc")||q.contains("hoc hanh dong");
 }

 public static String teach(WorldState s,String raw,long now){
  if(s==null)return "Không có world state để tạo lời dạy.";
  String normalized=norm(raw),payload=raw==null?"":raw.trim();int colon=payload.indexOf(':');if(colon>=0&&colon+1<payload.length())payload=payload.substring(colon+1).trim();
  if(payload.isEmpty())return "Hãy nói rõ điều muốn Thần giải thích.";
  boolean action=normalized.contains("hanh dong")||normalized.contains("ky nang")||normalized.contains("skill");
  if(action){
   String nk=key(payload),chosen="";for(String a:SAFE_ACTIONS)if(nk.contains(a)||nk.contains(a.replace('_',' '))){chosen=a;break;}
   if(chosen.isEmpty())return "Thần chỉ có thể giải thích một kỹ năng an toàn; skill chỉ tăng sau thực hành và outcome thật.";
   String id=offerLesson(s,"Theory of "+chosen,Collections.singletonList("skill_theory:"+chosen),"A safe explanation of "+chosen,
           Collections.emptyList(),Collections.singletonList("divine explanation only; practice is still required"),.48,
           "GOD_EXPLANATION","Thần","god:direct","",.45,"Not world truth; theory requires Haru's own practice.", "SKILL_THEORY",now);
   return "Thần đã đưa ra bài học "+id+". Haru có thể nghe, hoãn, hỏi lại, hiểu một phần hoặc từ chối; skill chưa thay đổi.";
  }
  String topic=payload.trim(),concept=key(topic);
  String id=offerLesson(s,topic,Collections.singletonList(concept),topic,Collections.emptyList(),
          Collections.singletonList("unverified direct explanation"),.42,"GOD_EXPLANATION","Thần","god:direct","",.40,
          "Direct explanation is not VNF World Truth and should be checked against evidence.","KNOWLEDGE",now);
  return "Thần đã đưa ra bài học "+id+". Haru chưa bị thay đổi và sẽ tự quyết định cách phản ứng.";
 }

 public static String offerReferenceLesson(WorldState s,WorldKnowledgeAnchor anchor,String topic,List<String>concepts,List<String>prerequisites,List<String>evidence,double difficulty,long now){
  if(anchor==null)return "";
  return offerLesson(s,topic,concepts,anchor.realAnchor,prerequisites,evidence,difficulty,"REAL_REFERENCE",anchor.sourceFamily,anchor.sourceRef,anchor.retrievedAt,anchor.confidence,anchor.limits,"KNOWLEDGE",now);
 }

 static String offerLesson(WorldState s,String topic,List<String>concepts,String claim,List<String>prerequisites,List<String>evidence,double difficulty,
                           String sourceLayer,String sourceFamily,String sourceRef,String retrievedAt,double sourceConfidence,String limits,String mode,long now){
  if(s==null)return "";if(s.characterGod==null)s.characterGod=new CharacterGodState();long at=Math.max(now,Math.max(s.lastOpenedAt,s.lastSimulatedAt));
  String seed=(topic==null?"":topic)+":"+at+":"+s.characterGod.lessons.size();String id="lesson_"+Long.toHexString(at)+"_"+Integer.toHexString(seed.hashCode());
  LessonState l=new LessonState();l.lessonId=id;l.topic=topic==null?"":topic.trim();l.claim=claim==null?l.topic:claim.trim();l.mode=mode==null?"KNOWLEDGE":mode;l.status="OFFERED";l.offeredAt=at;l.updatedAt=at;l.difficulty=LessonState.unit(difficulty);
  l.sourceLayer=sourceLayer==null?"GOD_EXPLANATION":sourceLayer;l.sourceFamily=sourceFamily==null?"":sourceFamily;l.sourceRef=sourceRef==null?"":sourceRef;l.retrievedAt=retrievedAt==null?"":retrievedAt;l.sourceConfidence=Double.isFinite(sourceConfidence)?Math.max(-1,Math.min(1,sourceConfidence)):-1;l.limits=limits==null?"":limits;l.provenance=l.sourceLayer+"|"+l.sourceFamily+"|"+l.sourceRef;
  add(l.concepts,concepts);add(l.prerequisites,prerequisites);add(l.evidence,evidence);if(l.concepts.isEmpty())l.concepts.add(key(l.topic));
  s.characterGod.lessons.put(id,l);
  WorldEventBus.publishId(s,at,"god_teaching_offer_"+id,HaruTeachingOpportunityEngine.OFFER_TYPE,id,"lesson="+id+" status=OFFERED sourceLayer="+l.sourceLayer);
  return id;
 }

 private static void add(List<String>out,List<String>xs){if(xs!=null)for(String x:xs)if(x!=null&&!x.trim().isEmpty())out.add(x.trim());}
 static String key(String x){String n=norm(x).replaceAll("[^a-z0-9]+","_").replaceAll("^_+|_+$","");return n.length()>64?n.substring(0,64):n;}
 private static String norm(String x){String n=Normalizer.normalize(x==null?"":x,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);return n.replace('đ','d').trim();}
}
