package com.aicharacter.v3;
import java.util.Locale;
public final class DivineNamingEngine {
 public static final class Result{public final boolean granted;public final String message;Result(boolean g,String m){granted=g;message=m;}}
 private DivineNamingEngine(){}
 public static Result grant(WorldState s,String proposed,long now){
  if(s.nameState.isNamed())return new Result(false,"Tên đã được ban trong lịch sử thế giới. V0.6 không hỗ trợ đổi tên tùy ý.");
  String n=proposed==null?"":proposed.trim().replaceAll("\\s+"," ");
  if(n.length()<1||n.length()>32)return new Result(false,"Tên cần từ 1 đến 32 ký tự.");
  if(!n.matches("[\\p{L}][\\p{L} .'-]{0,31}"))return new Result(false,"Tên chứa ký tự không phù hợp.");
  String id="name_"+Long.toHexString(now);s.nameState.officialName=n;s.nameState.namingEventId=id;s.nameState.grantedAt=now;
  s.worldHistory.add(new WorldHistoryEntry(now,"DIVINE_NAME_GRANTED",id,"God granted the girl's official world name."));
  MemoryEntry m=CognitionEngine.process(s,new Experience("identity_name_granted","Cô gái nhận biết rằng tên mình được gọi là "+n+".",.22,.92).at(HaruPerception.currentPlaceId(s),"").tag("identity").tag("divine_name"));
  s.knowledge.put("own_name",4);
  ThoughtState t=new ThoughtState("identity_name","divine_name","integrate_identity",.15,.82,now);t.relatedMemories.add(m.memoryId);s.thoughts.add(t);
  return new Result(true,"Tên đã được ban: "+n+". Sự kiện đã được ghi vào lịch sử thế giới.");
 }
 public static String extractProposal(String text){
  if(text==null)return"";String q=text.trim();java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?iu)(?:gọi cô ấy là|đặt tên(?: cho cô ấy)? là|tên(?: là)?)[\\s:]+([\\p{L}][\\p{L} .'-]{0,31})").matcher(q);
  return m.find()?m.group(1).trim():"";
 }
}