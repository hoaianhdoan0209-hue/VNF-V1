package com.aicharacter.v3;
/** Adds bounded lived context to offline speech without exposing raw engine values. */
public final class OfflineSpeechContext {
 private OfflineSpeechContext(){}
 public static String enrich(WorldState s,String speech,long now){
  if(speech==null||speech.isEmpty())return speech;
  String mem=BehaviorExpressionEngine.rememberedContext(s,now);
  if(mem.isEmpty()||!shouldSurface(s,speech))return speech;
  String hint=humanMemory(mem);
  if(hint.isEmpty())return speech;
  return speech+" "+hint;
 }
 private static boolean shouldSurface(WorldState s,String speech){
  if(s==null||s.thoughts==null||s.thoughts.isEmpty())return false;
  ThoughtState t=s.thoughts.get(s.thoughts.size()-1);
  return t!=null&&t.emotionalWeight>=.42&&(t.uncertainty>=.35||speech.contains("nhớ")||speech.contains("lý do"));
 }
 private static String humanMemory(String raw){
  String x=raw.trim();if(x.isEmpty())return "";
  // Internal memories are often authored in English; never leak raw developer prose to player dialogue.
  String l=x.toLowerCase(java.util.Locale.ROOT);
  if(l.contains("cat")&&l.contains("search"))return "Chuyện mình từng phải đi tìm cậu vẫn làm mình để ý hơn.";
  if(l.contains("walk"))return "Mình vẫn nhớ cảm giác của lần mình đi cùng cậu trước đó.";
  if(l.contains("hurt")||l.contains("boundary")||l.contains("control"))return "Chuyện trước đó vẫn khiến mình dè chừng một chút.";
  if(l.contains("reunion")||l.contains("found"))return "Mình vẫn nhớ cảm giác lúc tìm thấy cậu.";
  return "";
 }
}