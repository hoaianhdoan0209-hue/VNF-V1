package com.aicharacter.v3;

/**
 * Presentation-only moment detector. It listens to durable causal events after they happened
 * and temporarily emphasizes camera/light/sound semantics without changing simulation.
 */
public final class MomentDirector {
 public enum Kind{NONE,REUNION,SOCIAL_WARMTH,SOCIAL_RETREAT,INSIGHT,DIVINE_BEAT}
 public static final class Cue{
  public final Kind kind;public final boolean active;public final float minZoom,focusX,focusBlend,ease,visualIntensity;public final long holdMs,startedAt,endsAt;public final String sourceEventId,reason;public final SoundEvent sound;
  Cue(Kind kind,boolean active,float minZoom,float focusX,float focusBlend,float ease,float visualIntensity,long holdMs,long startedAt,long endsAt,String sourceEventId,String reason,SoundEvent sound){
   this.kind=kind==null?Kind.NONE:kind;this.active=active;this.minZoom=safe(minZoom,1f,1.62f);this.focusX=Float.isFinite(focusX)?focusX:0;this.focusBlend=safe(focusBlend,0,1);this.ease=safe(ease,.04f,.18f);this.visualIntensity=safe(visualIntensity,0,1);this.holdMs=Math.max(0,holdMs);this.startedAt=Math.max(0,startedAt);this.endsAt=Math.max(this.startedAt,endsAt);this.sourceEventId=sourceEventId==null?"":sourceEventId;this.reason=reason==null?"":reason;this.sound=sound;
  }
  static Cue none(){return new Cue(Kind.NONE,false,1f,0,0,.08f,0,0,0,0,"","",null);}
 }
 private static final long RECENT_EVENT_MS=9000L;
 private Cue active=Cue.none();
 private String lastAcceptedEventId="";

 public Cue direct(WorldState s,long now){
  if(active.active&&now<active.endsAt)return active;
  active=Cue.none();
  if(s==null||s.worldHistory==null||s.worldHistory.isEmpty())return active;
  WorldHistoryEntry best=null;int bestPriority=-1;
  for(int i=s.worldHistory.size()-1;i>=0;i--){
   WorldHistoryEntry e=s.worldHistory.get(i);if(e==null||e.eventId==null||e.eventId.equals(lastAcceptedEventId))continue;
   long age=now-e.time;if(age<0||age>RECENT_EVENT_MS)continue;
   int p=priority(e.type);if(p>bestPriority){best=e;bestPriority=p;if(p>=5)break;}
  }
  if(best==null||bestPriority<1)return active;
  active=fromEvent(s,best,now);if(active.active)lastAcceptedEventId=best.eventId;return active;
 }

 public void reset(){active=Cue.none();lastAcceptedEventId="";}

 private static Cue fromEvent(WorldState s,WorldHistoryEntry e,long now){
  String t=e.type==null?"":e.type.toUpperCase(java.util.Locale.ROOT);
  if(t.contains("REUNION")||t.contains("CAT_FOUND")||t.contains("CAT_RETURN")){
   return cue(s,e,Kind.REUNION,1.56f,.84f,.105f,.62f,4300,now,"reunion became causally real","social_reunion",SoundEvent.Layer.MOMENT,.78,1.02);
  }
  if(t.equals("CAT_SOCIAL_SETTLE_NEAR")||t.equals("CAT_SOCIAL_RESPONSE_COMPLETED")&&contains(e.summary,"APPROACH")){
   return cue(s,e,Kind.SOCIAL_WARMTH,1.42f,.78f,.11f,.46f,3400,now,"voluntary social closeness completed","cat_settle_near",SoundEvent.Layer.SOCIAL,.58,.98);
  }
  if(t.equals("HARU_SOCIAL_SPACING_COMPLETED")||t.equals("CAT_SOCIAL_RESPONSE_STARTED")&&contains(e.summary,"APPROACH")){
   return cue(s,e,Kind.SOCIAL_WARMTH,1.34f,.72f,.11f,.34f,2800,now,"social approach is visible now","social_approach",SoundEvent.Layer.SOCIAL,.46,1.0);
  }
  if(t.equals("CAT_SOCIAL_RESPONSE_STARTED")&&contains(e.summary,"RETREAT")||t.contains("SOCIAL_RESPONSE_YIELDED")||t.contains("SOCIAL_RESPONSE_INTERRUPTED")){
   return cue(s,e,Kind.SOCIAL_RETREAT,1.20f,.62f,.10f,.28f,2500,now,"social tension needs context rather than intimacy","social_retreat",SoundEvent.Layer.SOCIAL,.42,.90);
  }
  if(t.contains("CAUSAL_EXPERIMENT_RESOLVED")||t.contains("PREDICTION")&&t.contains("RESOLVED")||t.contains("BELIEF")&&t.contains("UPDATED")){
   return cue(s,e,Kind.INSIGHT,1.20f,.68f,.095f,.25f,2600,now,"a reasoning update deserves a quiet beat","insight_soft",SoundEvent.Layer.MOMENT,.36,1.08);
  }
  if(t.startsWith("GOD_")||t.startsWith("DIVINE_")){
   return cue(s,e,Kind.DIVINE_BEAT,1.16f,.55f,.085f,.42f,3000,now,"divine event became visible","divine_presence_pulse",SoundEvent.Layer.DIVINE,.52,.82);
  }
  return Cue.none();
 }

 private static Cue cue(WorldState s,WorldHistoryEntry e,Kind kind,float zoom,float focusBlend,float ease,float intensity,long hold,long now,String why,String semantic,SoundEvent.Layer layer,double soundIntensity,double pitch){
  float fx=focusX(s,kind);double pan=0;if(s!=null&&s.world!=null){WorldArea a=s.world.areaAt(fx);if(a!=null){double mid=(a.left+a.right)*.5,half=Math.max(1,(a.right-a.left)*.5);pan=Math.max(-1,Math.min(1,(fx-mid)/half));}}
  SoundEvent sound=new SoundEvent("snd_"+e.eventId,semantic,layer,soundIntensity,fx,pan,pitch,kind==Kind.REUNION?2.8:1.8,true,now);
  return new Cue(kind,true,zoom,fx,focusBlend,ease,intensity,hold,now,now+hold,e.eventId,why+"; "+e.type,sound);
 }

 private static float focusX(WorldState s,Kind kind){
  if(s==null)return 0;if((kind==Kind.REUNION||kind==Kind.SOCIAL_WARMTH||kind==Kind.SOCIAL_RETREAT)&&s.catState!=null&&Math.abs(s.haruX-s.catState.x)<=520f)return s.haruX*.58f+s.catState.x*.42f;return s.haruX;
 }
 private static int priority(String type){
  String t=type==null?"":type.toUpperCase(java.util.Locale.ROOT);
  if(t.contains("REUNION")||t.contains("CAT_FOUND")||t.contains("CAT_RETURN"))return 6;
  if(t.equals("CAT_SOCIAL_SETTLE_NEAR"))return 5;
  if(t.equals("CAT_SOCIAL_RESPONSE_COMPLETED")||t.equals("HARU_SOCIAL_SPACING_COMPLETED"))return 4;
  if(t.equals("CAT_SOCIAL_RESPONSE_STARTED")||t.contains("SOCIAL_RESPONSE_YIELDED")||t.contains("SOCIAL_RESPONSE_INTERRUPTED"))return 3;
  if(t.contains("CAUSAL_EXPERIMENT_RESOLVED")||t.contains("PREDICTION")&&t.contains("RESOLVED")||t.contains("BELIEF")&&t.contains("UPDATED"))return 2;
  if(t.startsWith("GOD_")||t.startsWith("DIVINE_"))return 1;return 0;
 }
 private static boolean contains(String s,String needle){return s!=null&&s.toUpperCase(java.util.Locale.ROOT).contains(needle);}
 private static float safe(float v,float a,float b){return Float.isFinite(v)?Math.max(a,Math.min(b,v)):a;}
}
