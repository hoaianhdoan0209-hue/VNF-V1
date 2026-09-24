package com.aicharacter.v3;

/**
 * Haru-owned autonomous speech selection.
 * Sources must already exist in simulation state/history; this engine never invents world facts.
 */
public final class HaruProactiveSpeechEngine {
 private static final long MIN_GAP_MS=45000L,EVENT_WINDOW_MS=14000L,INTENTION_WINDOW_MS=9000L,THOUGHT_WINDOW_MS=180000L;
 public static final class Cue{
  public final String text,expression,sourceId;public final long createdAt;
  Cue(String t,String e,String s,long at){text=t==null?"":t;expression=e==null?"":e;sourceId=s==null?"":s;createdAt=Math.max(0,at);}
 }
 private HaruProactiveSpeechEngine(){}

 public static boolean advance(WorldState s,long now,LifeSimulationKernel.Mode mode){
  if(s==null||mode!=LifeSimulationKernel.Mode.ACTIVE)return false;
  if(s.haruSpeech==null)s.haruSpeech=new HaruProactiveSpeechState();
  HaruProactiveSpeechState ps=s.haruSpeech;ps.normalize();
  if(ps.pending())return false;
  if(ps.lastSpokenAt>0&&now>=ps.lastSpokenAt&&now-ps.lastSpokenAt<MIN_GAP_MS)return false;
  if(BodyRhythmEngine.isSleeping(s))return false;
  Candidate c=eventCandidate(s,now);
  if(c==null)c=intentionCandidate(s,now);
  if(c==null)c=bodyWeatherCandidate(s,now);
  if(c==null)c=thoughtCandidate(s,now);
  if(c==null||c.sourceId.equals(ps.lastSourceEventId))return false;
  ps.pendingText=c.text;ps.pendingExpression=c.expression;ps.pendingSourceEventId=c.sourceId;ps.pendingCreatedAt=now;ps.deliveredAt=0;
  return true;
 }

 public static Cue consume(WorldState s,long now){
  if(s==null||s.haruSpeech==null)return null;HaruProactiveSpeechState ps=s.haruSpeech;ps.normalize();if(!ps.pending())return null;
  Cue c=new Cue(ps.pendingText,ps.pendingExpression,ps.pendingSourceEventId,ps.pendingCreatedAt);
  ps.deliveredAt=Math.max(now,ps.pendingCreatedAt);ps.lastSpokenAt=ps.deliveredAt;ps.lastSourceEventId=ps.pendingSourceEventId;ps.spokenCount++;
  ps.pendingText="";ps.pendingExpression="";ps.pendingSourceEventId="";ps.pendingCreatedAt=0;
  return c;
 }

 private static Candidate eventCandidate(WorldState s,long now){
  if(s.worldHistory==null)return null;
  for(int i=s.worldHistory.size()-1;i>=0;i--){
   WorldHistoryEntry e=s.worldHistory.get(i);if(e==null||e.eventId==null)continue;long age=now-e.time;if(age<0)continue;if(age>EVENT_WINDOW_MS)break;
   String t=e.type==null?"":e.type.toUpperCase(java.util.Locale.ROOT),summary=e.summary==null?"":e.summary.toUpperCase(java.util.Locale.ROOT);
   if("CAT_SOCIAL_SETTLE_NEAR".equals(t))return new Candidate("Nó tự ở lại gần mình rồi…","khẽ nhìn xuống phía con mèo",e.eventId);
   if("CAT_SOCIAL_RESPONSE_COMPLETED".equals(t)&&summary.contains("APPROACH"))return new Candidate("Nó vừa tự lại gần mình.","ánh mắt dịu xuống một chút",e.eventId);
   if("CAT_SOCIAL_RESPONSE_COMPLETED".equals(t)&&summary.contains("RETREAT"))return new Candidate("Nó muốn có thêm khoảng cách. Mình sẽ để nó yên một chút.","lùi sự chú ý lại, không ép gần hơn",e.eventId);
   if("PLAN_POST_OUTCOME_REVIEWED".equals(t)&&summary.contains("STATUS=FAILED"))return new Candidate("Cách vừa rồi không ổn. Mình phải nghĩ lại.","im một nhịp rồi suy nghĩ",e.eventId);
   if(t.contains("CAUSAL_EXPERIMENT_RESOLVED")||t.contains("PREDICTION_RESOLVED"))return new Candidate("Mình vừa hiểu thêm được một chút về chuyện đó.","ánh mắt tập trung như vừa nối được một ý",e.eventId);
  }
  return null;
 }

 private static Candidate intentionCandidate(WorldState s,long now){
  if(s.currentIntention==null||s.currentIntention.isEmpty()||s.intentionStartedAt<=0||now<s.intentionStartedAt||now-s.intentionStartedAt>INTENTION_WINDOW_MS)return null;
  String id=s.currentIntention,src="intention:"+id+":"+s.intentionStartedAt;
  if("find_cat".equals(id))return new Candidate("Mình đi tìm nó một chút.","nhìn về phía những nơi con mèo có thể đã đi qua",src);
  if("observe_lake".equals(id))return new Candidate("Mình muốn ra nhìn mặt hồ một lúc.","ánh mắt hướng về phía hồ",src);
  if("explore_garden".equals(id))return new Candidate("Mình muốn đi dọc lối cỏ xem có gì thay đổi.","chú ý đến khu vườn",src);
  if("study_ecology".equals(id)||"watch_reedling".equals(id))return new Candidate("Mình muốn quan sát chúng thêm một lúc.","tập trung vào những chuyển động nhỏ quanh mình",src);
  if("reflect".equals(id)||"quiet_pause".equals(id)||"seek_solitude".equals(id))return new Candidate("Mình muốn yên một chút để nghĩ.","thả lỏng vai và im đi một nhịp",src);
  if("drink".equals(id))return new Candidate("Mình khát rồi. Mình về lấy nước.","liếm môi rất nhẹ rồi đổi hướng",src);
  if("eat".equals(id))return new Candidate("Mình bắt đầu đói rồi. Mình về ăn một chút.","để ý đến cảm giác trong bụng",src);
  if("sleep".equals(id)||"recover".equals(id))return new Candidate("Mình cần nghỉ một lúc.","cơ thể chậm xuống rõ rệt",src);
  if("seek_shelter".equals(id))return new Candidate("Mình muốn tìm chỗ kín hơn trước.","nhìn về phía nơi trú gần nhất",src);
  return null;
 }

 private static Candidate bodyWeatherCandidate(WorldState s,long now){
  WorldArea a=s.world==null?null:s.world.areaAt(s.haruX);double exposure=WorldSemantics.exposure(a);
  if(s.environment!=null&&"RAIN".equals(s.environment.weather)&&s.environment.weatherIntensity>.68&&exposure>.55){
   long bucket=now/(5L*60000L);return new Candidate("Mưa nặng hơn rồi…","ngẩng nhìn lớp mưa ngoài trời","weather:heavy_rain:"+bucket);
  }
  if(s.body!=null&&s.body.pain>35){long bucket=now/(10L*60000L);return new Candidate("Mình đang đau khá rõ. Mình không muốn cố quá.","khẽ giữ cơ thể ở tư thế bảo vệ","body:pain:"+bucket);}
  return null;
 }

 private static Candidate thoughtCandidate(WorldState s,long now){
  if(s.thoughts==null||s.thoughts.isEmpty())return null;ThoughtState t=s.thoughts.get(s.thoughts.size()-1);if(t==null||t.createdAt<=0||now<t.createdAt||now-t.createdAt>THOUGHT_WINDOW_MS)return null;
  if(t.emotionalWeight<.48&&t.uncertainty<.62)return null;
  String src="thought:"+t.createdAt+":"+(t.subject==null?"":Integer.toHexString(t.subject.hashCode()));
  if(t.uncertainty>=.68)return new Candidate("Có một chuyện mình vẫn chưa chắc. Mình muốn quan sát thêm trước khi kết luận.","nghĩ rất lâu rồi vẫn chưa vội kết luận",src);
  return new Candidate("Mình cứ nghĩ về chuyện vừa xảy ra.","im đi một chút, như đang nối các ký ức lại với nhau",src);
 }

 private static final class Candidate{final String text,expression,sourceId;Candidate(String t,String e,String s){text=t;expression=e;sourceId=s;}}
}
