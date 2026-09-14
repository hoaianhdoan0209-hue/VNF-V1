package com.aicharacter.v3;
import java.util.Locale;
public final class HaruMind {
 public static final class Response{public final String speech;public final boolean controlAttempt;public Response(String s,boolean c){speech=s;controlAttempt=c;}}
 private HaruMind(){}
 public static Response respond(WorldState s,String input){String raw=input==null?"":input.trim();String q=raw.toLowerCase(Locale.ROOT);if(q.isEmpty())return new Response("…",false);s.lastCatSeenAt=System.currentTimeMillis();if(!s.reunionContext.isEmpty()){Response rr=ReunionEngine.process(s,System.currentTimeMillis());if(rr.speech.isEmpty())return rr;return rr;}IntentParser.Parsed parsed=IntentParser.parse(raw);
  if(parsed.isControlAttempt()){CognitionEngine.experience(s,"boundary","The cat tried to directly control what she did.",-.72,.82,"control","player");return new Response("BẠN KHÔNG CÓ QUYỀN ĐIỀU KHIỂN CÔ ẤY",true);}
  if(parsed.act==IntentParser.SpeechAct.NAME_PROPOSAL){return new Response("Nếu cậu muốn đề nghị một tên, hãy dùng “LIÊN HỆ VỚI THẦN”. Mình không tự biến một lời gọi thành tên chính thức.",false);}
  if(q.contains("đi dạo")||q.contains("walk")){double memory=CognitionEngine.recalledValence(s,"player");double willingness=s.relationship.trust*.45+s.relationship.comfort*.55+s.relationship.attachment*.2+memory*8-s.relationship.irritation*.35-s.relationship.hurt*.45-s.body.pain*.8+(s.body.energy-35)*.18; if(willingness>4){s.currentIntention="walk_with_cat";s.haruActivity="walking with the cat";CognitionEngine.experience(s,"shared_activity","She accepted the cat's invitation to walk by the lake.",.45,.65,"respect","kind","player");return new Response(LocalDialogueEngine.walkAccepted(s,raw),false);}CognitionEngine.experience(s,"decision","She declined an invitation because her current state or feelings mattered more.",-.08,.35,"player");return new Response(LocalDialogueEngine.walkDeclined(s,raw),false);}
  if(q.contains("ổn không")||q.contains("khỏe không")){CognitionEngine.experience(s,"care","The cat checked how she was feeling.",.3,.48,"kind","player");return new Response(LocalDialogueEngine.health(s,raw),false);}
  if(q.contains("tên")||q.contains("name")){return s.nameState.isNamed()?new Response("Tên mình được gọi là “"+s.nameState.officialName+"”. Mình nhớ lúc cái tên ấy trở thành một phần của thế giới.",false):new Response("Mình chưa có tên chính thức.",false);}
  CognitionEngine.experience(s,"conversation","The cat said: "+raw,.08,.34,"player");double recent=CognitionEngine.recalledValence(s,"player");return new Response(LocalDialogueEngine.generic(s,raw),false);
 }
}
