package com.aicharacter.v3;
import java.util.Locale;
public final class IntentParser {
 public enum SpeechAct{COMMAND,INVITATION,QUESTION,NAME_PROPOSAL,STATEMENT}
 public static final class Parsed{public final SpeechAct act;public final boolean targetsHaru,directBodyAction;public Parsed(SpeechAct a,boolean t,boolean d){act=a;targetsHaru=t;directBodyAction=d;}public boolean isControlAttempt(){return act==SpeechAct.COMMAND&&targetsHaru&&directBodyAction;}}
 private IntentParser(){}
 public static Parsed parse(String input){String q=(input==null?"":input).trim().toLowerCase(Locale.ROOT);boolean invitation=q.contains("không?")||q.contains("nhé")||q.contains("cùng")||q.contains("với mình")||q.contains("would you")||q.contains("want to");boolean name=q.startsWith("gọi bạn là ")||q.startsWith("tên bạn là ");boolean question=q.contains("?")||q.startsWith("bạn có")||q.startsWith("cậu có")||q.startsWith("tại sao")||q.startsWith("sao ");boolean action=containsAny(q,"đi ","đứng","ngồi","nằm","chạy","theo tôi","move","walk","sit","stand");boolean imperative=containsAny(q,"ngay","phải ","hãy ","đi ra","đứng dậy","ngồi xuống","you must","do it now")||startsWithAction(q);SpeechAct a=name?SpeechAct.NAME_PROPOSAL:invitation?SpeechAct.INVITATION:(imperative&&action)?SpeechAct.COMMAND:question?SpeechAct.QUESTION:SpeechAct.STATEMENT;return new Parsed(a,true,action);}
 private static boolean startsWithAction(String q){return q.startsWith("đi ")||q.startsWith("chạy ")||q.startsWith("đứng ")||q.startsWith("ngồi ")||q.startsWith("nằm ")||q.startsWith("move ")||q.startsWith("walk ")||q.startsWith("run ")||q.startsWith("sit ")||q.startsWith("stand ");}
 private static boolean containsAny(String q,String...xs){for(String x:xs)if(q.contains(x))return true;return false;}
}
