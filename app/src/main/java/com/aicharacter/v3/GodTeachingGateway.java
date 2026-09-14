package com.aicharacter.v3;
import java.text.Normalizer;import java.util.*;
/** Safe divine teaching: creates evidence/learning opportunities, never direct bodily control. */
public final class GodTeachingGateway{
 private GodTeachingGateway(){}
 private static final Set<String> SAFE_ACTIONS=new LinkedHashSet<>(Arrays.asList("observe_carefully","read","sketch","tend_garden","prepare_simple_food","clean_space","care_for_creature"));
 public static boolean looksLikeTeaching(String raw){String q=norm(raw);return q.startsWith("day ")||q.startsWith("day:")||q.contains("day co ay")||q.startsWith("teach ")||q.contains("cap nhat kien thuc")||q.contains("hoc hanh dong");}
 public static String teach(WorldState s,String raw,long now){
  String n=norm(raw);String payload=raw;int c=raw.indexOf(':');if(c>=0&&c+1<raw.length())payload=raw.substring(c+1).trim();
  if(payload.isEmpty())return "Hãy nói rõ điều muốn Thần dạy. Ví dụ: DẠY CÔ ẤY: kiến thức về mưa.";
  boolean action=n.contains("hanh dong")||n.contains("ky nang")||n.contains("skill");
  if(action)return teachAction(s,payload,now);
  return teachKnowledge(s,payload,now);
 }
 private static String teachKnowledge(WorldState s,String topic,long now){
  String key=key(topic);if(key.isEmpty())return "Nội dung dạy chưa đủ rõ.";
  int old=s.knowledge.containsKey(key)?s.knowledge.get(key):0;
  int gain=(int)Math.max(1,Math.floor(AgeDevelopmentEngine.learningFactor(s)));
  int next=Math.min(AgeDevelopmentEngine.knowledgeCeiling(s),old+gain);
  s.knowledge.put(key,next);
  s.memories.add(new MemoryEntry(now,"divine_teaching","Thần offered a lesson about "+topic+". She understood it at her present capacity, not as absolute truth.",.72));
  CognitionEngine.experience(s,"learning","She received a lesson about "+topic+".",.12,.46,"learning","god_teaching");
  return "Đã tạo một trải nghiệm học về “"+topic+"”. Mức hiểu: "+old+" → "+next+"/4. Đây là điều cô ấy đã tiếp nhận; Thần không ép cô ấy tin tuyệt đối hay hành động theo nó.";
 }
 private static String teachAction(WorldState s,String text,long now){
  String k=key(text);String chosen="";for(String a:SAFE_ACTIONS)if(k.contains(a)||k.contains(a.replace('_',' '))){chosen=a;break;}
  if(chosen.isEmpty())return "Thần chưa thể cài hành động tùy ý vào cô ấy. Hành động phải thuộc repertoire an toàn và được học qua trải nghiệm.";
  double old=s.skills.containsKey(chosen)?s.skills.get(chosen):0;
  double step=.12*AgeDevelopmentEngine.learningFactor(s);double next=Math.min(1,old+step);s.skills.put(chosen,next);
  if(next>=.30)s.learnedActions.add(chosen);
  s.memories.add(new MemoryEntry(now,"skill_learning","She practiced the idea of "+chosen+" after divine teaching.",.68));
  return "Đã mở cơ hội học “"+chosen+"”. Kỹ năng "+String.format(Locale.US,"%.0f%% → %.0f%%",old*100,next*100)+(next>=.30?"; hành động đã vào repertoire lựa chọn.":"; chưa đủ quen để trở thành lựa chọn ổn định.");
 }
 private static String key(String x){String n=norm(x).replaceAll("[^a-z0-9]+","_").replaceAll("^_+|_+$","");return n.length()>48?n.substring(0,48):n;}
 private static String norm(String x){String n=Normalizer.normalize(x==null?"":x,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);return n.replace('đ','d').trim();}
}
