package com.aicharacter.v3;
import java.util.*;
/** Complete offline cognition-to-expression bridge. It observes existing state; it never chooses for the girl or calls network. */
public final class BehaviorExpressionEngine {
 private BehaviorExpressionEngine(){}
 public static final class Snapshot{
  public final String posture,attention,hesitation,memoryEcho;
  Snapshot(String p,String a,String h,String m){posture=p;attention=a;hesitation=h;memoryEcho=m;}
  public String stageDirection(){StringBuilder b=new StringBuilder();append(b,posture);append(b,attention);append(b,hesitation);return b.toString();}
  private static void append(StringBuilder b,String x){if(x==null||x.isEmpty())return;if(b.length()>0)b.append(" ");b.append(x);}
 }
 public static Snapshot observe(WorldState s,long now){
  if(s==null)return new Snapshot("","","","");
  String posture="",attention="",hesitation="",memory="";
  if(s.body!=null){
   if(s.body.pain>20)posture="Cô ấy khẽ giữ chỗ đang đau, cử động chậm hơn.";
   else if(s.body.energy<25)posture="Vai cô ấy hơi trùng xuống vì mệt.";
  }
  if(posture.isEmpty()){
   if("seek_shelter".equals(s.currentIntention))posture="Cô ấy liên tục để ý những chỗ có thể trú.";
   else if("reflect".equals(s.currentIntention))posture="Cô ấy đứng lặng lâu hơn bình thường, mắt vẫn để ở cảnh vật trước mặt.";
   else if("find_cat".equals(s.currentIntention))posture="Ánh mắt cô ấy đảo qua những chỗ con mèo thường xuất hiện.";
   else if("seek_solitude".equals(s.currentIntention))posture="Cô ấy giữ một khoảng riêng, như chưa muốn bị kéo khỏi dòng suy nghĩ.";
  }
  if(s.relationship!=null){
   if(s.relationship.hurt+s.relationship.irritation>32)attention="Khi nghe mèo, cô ấy không quay lại ngay.";
   else if(s.relationship.comfort+s.relationship.trust>85)attention="Nghe tiếng mèo, ánh mắt cô ấy dịu đi một chút.";
  }
  ThoughtState t=lastThought(s);boolean currentThought=t!=null&&(t.createdAt<=0||now-t.createdAt<=180000L);if(currentThought&&t.uncertainty>.58){if(t.emotionalWeight>.62)hesitation="Cô ấy khựng lại một chút, như đang cân nhắc điều có ý nghĩa với mình.";else hesitation="Cô ấy ngập ngừng một nhịp như vẫn đang cân nhắc.";}else if(currentThought&&t.uncertainty>.38&&s.planState!=null&&s.planState.active())hesitation="Ánh mắt cô ấy chậm lại một nhịp trước khi tiếp tục việc đang làm.";
  MemoryEntry m=currentThought?relatedMemory(s,t):null;
  if(m!=null&&m.importance>.48)memory=m.summary==null?"":m.summary;
  return new Snapshot(posture,attention,hesitation,memory);
 }
 public static String rememberedContext(WorldState s,long now){
  String x=observe(s,now).memoryEcho;if(x.isEmpty())return "";return x.length()>110?x.substring(0,110)+"…":x;
 }
 private static ThoughtState lastThought(WorldState s){return s.thoughts==null||s.thoughts.isEmpty()?null:s.thoughts.get(s.thoughts.size()-1);}
 private static MemoryEntry relatedMemory(WorldState s,ThoughtState t){
  if(t==null||t.relatedMemories==null||t.relatedMemories.isEmpty()||s.memories==null)return null;
  for(int i=s.memories.size()-1;i>=0;i--){MemoryEntry m=s.memories.get(i);if(m!=null&&t.relatedMemories.contains(m.memoryId))return m;}return null;
 }
}