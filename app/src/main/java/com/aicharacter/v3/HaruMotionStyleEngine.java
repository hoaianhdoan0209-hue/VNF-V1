package com.aicharacter.v3;

/** Presentation-only motion styling layered on top of authoritative Haru physics. */
public final class HaruMotionStyleEngine {
 public static final class Style{
  public final float translateX,translateY,rotationDeg,breathScale,settle;public final String reason;
  Style(float x,float y,float r,float breath,float settle,String reason){translateX=x;translateY=y;rotationDeg=r;breathScale=breath;this.settle=settle;this.reason=reason==null?"":reason;}
 }
 private HaruMotionStyleEngine(){}

 public static Style derive(WorldState s,GirlAnimationController.Visual v,float anim){
  if(s==null||v==null)return new Style(0,0,0,1,0,"no visual state");
  float x=0,y=0,r=0,breath=1,settle=0;String reason="neutral";
  boolean social=s.socialProximity!=null&&"social_adjust".equals(s.currentIntention)&&s.catState!=null;
  if(v.isWalk()&&s.bodyRig!=null){
   float ph=(float)(s.bodyRig.stridePhase*Math.PI*2),speed=(float)Math.max(0,Math.min(1,Math.abs(s.girlTravel.lastSpeed)/120.0));
   y+=(float)Math.sin(ph*2)*(.55f+1.15f*speed);
   r+=(float)Math.sin(ph)*(.22f+.42f*speed);
   reason="stride-linked locomotion";
  }
  if(social){
   float dir=Math.signum(s.catState.x-s.haruX);String mode=s.socialProximity.mode==null?"":s.socialProximity.mode;
   if("APPROACH".equals(mode)){x+=dir*1.7f;r+=dir*.42f;y+=(float)Math.sin(anim*.85f)*.35f;reason="soft voluntary social approach";}
   else if("GUARDED".equals(mode)){x-=dir*1.8f;r-=dir*.55f;y+=1.1f;settle=.22f;reason="guarded social spacing";}
  }
  if(v.state==GirlAnimationController.State.THINK){y+=(float)Math.sin(anim*.52f)*.55f;r+=(float)Math.sin(anim*.31f)*.22f;settle=.18f;breath=.92f;reason="quiet reflective motion";}
  else if(v.state==GirlAnimationController.State.SIT){y+=(float)Math.sin(anim*.62f)*.45f;settle=.32f;breath=.82f;reason="settled resting motion";}
  else if(v.state==GirlAnimationController.State.REACT){breath=1.14f;}
  if(s.emotion!=null){
   double fear=cl(s.emotion.fear),sad=cl(s.emotion.sadness),joy=cl(s.emotion.joy),curious=cl(s.emotion.curiosity);
   y+=(float)(sad*.9-curious*.25-joy*.18);settle+=(float)(sad*.16+fear*.06);breath*=(float)(1+fear*.20+joy*.05);
  }
  return new Style(finite(x),finite(y),finite(r),Math.max(.65f,Math.min(1.35f,breath)),Math.max(0,Math.min(1,settle)),reason);
 }
 private static float finite(float v){return Float.isFinite(v)?v:0;}private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
