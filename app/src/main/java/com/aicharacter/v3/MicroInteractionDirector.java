package com.aicharacter.v3;

/**
 * Presentation-only micro interaction detector.
 * It never starts travel, changes relationship, or writes memories.
 */
public final class MicroInteractionDirector {
 public enum Kind{NONE,CAT_RUB,HARU_LOOK_DOWN}
 public static final class Cue{
  public final Kind kind;public final boolean active;public final double intensity;public final long sourceTime;public final String reason;
  Cue(Kind kind,boolean active,double intensity,long sourceTime,String reason){this.kind=kind==null?Kind.NONE:kind;this.active=active;this.intensity=cl(intensity);this.sourceTime=Math.max(0,sourceTime);this.reason=reason==null?"":reason;}
  static Cue none(){return new Cue(Kind.NONE,false,0,0,"");}
 }
 private static final long WINDOW_MS=4200L;
 private MicroInteractionDirector(){}

 public static Cue derive(WorldState s,long now){
  if(s==null||s.catState==null||s.catSocial==null)return Cue.none();
  if("girl".equals(s.catState.attachedToEntity))return Cue.none();
  if(s.girlTravel!=null&&s.girlTravel.active)return Cue.none();
  if(s.catTravel!=null&&s.catTravel.active)return Cue.none();
  if(s.girlPhysics!=null&&(!s.girlPhysics.grounded||s.girlPhysics.falling))return Cue.none();
  if(s.catPhysics!=null&&(!s.catPhysics.grounded||s.catPhysics.falling))return Cue.none();
  if(s.body!=null&&(s.body.pain>34||s.body.energy<24||s.body.sleepiness>86))return Cue.none();
  if(s.catState.energy<18||s.catState.sleepiness>96)return Cue.none();

  double distance=Math.abs(s.haruX-s.catState.x);if(distance>92)return Cue.none();
  double familiarity=cl(s.catSocial.familiarity),comfort=cl(s.catSocial.comfort),wariness=cl(s.catSocial.wariness);
  if(familiarity<.40||comfort<.42||wariness>.45)return Cue.none();

  WorldHistoryEntry event=recentWarmEvent(s,now);if(event==null)return Cue.none();
  double closeness=cl((92-distance)/52.0),trustLike=cl(familiarity*.46+comfort*.54-wariness*.38),intensity=cl(.40+closeness*.30+trustLike*.30);
  long age=Math.max(0,now-event.time);
  if(age<2550L)return new Cue(Kind.CAT_RUB,true,intensity,event.time,"recent voluntary cat closeness + real near distance");
  return new Cue(Kind.HARU_LOOK_DOWN,true,intensity*.82,event.time,"Haru visually acknowledges a completed warm cat interaction");
 }

 private static WorldHistoryEntry recentWarmEvent(WorldState s,long now){
  if(s.worldHistory==null)return null;
  for(int i=s.worldHistory.size()-1;i>=0;i--){
   WorldHistoryEntry e=s.worldHistory.get(i);if(e==null)continue;long age=now-e.time;if(age<0)continue;if(age>WINDOW_MS)break;
   String t=e.type==null?"":e.type.toUpperCase(java.util.Locale.ROOT),summary=e.summary==null?"":e.summary.toUpperCase(java.util.Locale.ROOT);
   if("CAT_SOCIAL_SETTLE_NEAR".equals(t))return e;
   if("CAT_SOCIAL_RESPONSE_COMPLETED".equals(t)&&summary.contains("APPROACH"))return e;
  }
  return null;
 }
 private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
