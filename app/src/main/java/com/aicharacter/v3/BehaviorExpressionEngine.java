package com.aicharacter.v3;
import java.util.*;
/** Offline bridge from cognition to observable behavior. Never chooses for the girl and never calls network. */
public final class BehaviorExpressionEngine {
 private BehaviorExpressionEngine(){}
 public static final class Snapshot{
  public final String posture,attention,hesitation,memoryEcho;
  Snapshot(String p,String a,String h,String m){posture=p;attention=a;hesitation=h;memoryEcho=m;}
  public String stageDirection(){StringBuilder b=new StringBuilder();if(!posture.isEmpty())b.append(posture);if(!attention.isEmpty()){if(b.length()>0)b.append(" ");b.append(attention);}if(!hesitation.isEmpty()){if(b.length()>0)b.append(" ");b.append(hesitation);}return b.toString();}
 }
 public static Snapshot observe(WorldState s,long now){
  String posture="",attention="",hesitation="",memory="";
  if(s.body.pain>20)posture="Cô ấy khẽ giữ chỗ đang đau, cử động chậm hơn.";
  else if(s.body.energy<25)posture="Vai cô ấy hơi trùng xuống vì mệt.";
  else if("seek_shelter".equals(s.currentIntention))posture="Cô ấy liên tục để ý những chỗ có thể trú.";
  else if("reflect".equals(s.currentIntention))posture="Cô ấy đứng lặng lâu hơn bình thường, mắt vẫn để ở cảnh vật trước mặt.";
  else if("find_cat".equals(s.currentIntention))posture="Ánh mắt cô ấy đảo qua những chỗ con mèo thường xuất hiện.";
  if(s.relationship.hurt+s.relationship.irritation>32)attention="Khi nghe mèo, cô ấy không quay lại ngay.";
  else if(s.relationship.comfort+s.relationship.trust>85)attention="Nghe tiếng mèo, ánh mắt cô ấy dịu đi một chút.";
  ThoughtState t=lastThought(s);
  if(t!=null&&t.uncertainty>.58)hesitation="Cô ấy ngập ngừng một nhịp như vẫn đang cân nhắc.";
  MemoryEntry m=relatedMemory(s,t,now);
  if(m!=null&&m.importance>.48)memory=m.summary;
  return new Snapshot(posture,attention,hesitation,memory);
 }
 public static String rememberedContext(WorldState s,long now){
  Snapshot x=observe(s,now);if(x.memoryEcho.isEmpty())return "";
  if(x.memoryEcho.length()>110)return x.memoryEcho.substring(0,110)+"…";return x.memoryEcho;
 }
 private static ThoughtState lastThought(WorldState s){return s.thoughts==null||s.thoughts.isEmpty()?null:s.thoughts.get(s.thoughts.size()-1);}
 private static MemoryEntry relatedMemory(WorldState s,ThoughtState t,long now){
  if(t==null||t.relatedMemories==null||t.relatedMemories.isEmpty())return null;
  for(int i=s.memories.size()-1;i>=0;i--){MemoryEntry m=s.memories.get(i);if(t.relatedMemories.contains(m.memoryId))return m;}return null;
 }
}