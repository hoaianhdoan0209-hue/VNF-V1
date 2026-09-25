package com.aicharacter.v3;

import android.graphics.*;

/** Pixel-native lighting/shadow pass for the live world. Presentation only. */
public final class PixelLightingRenderer {
 private PixelLightingRenderer(){}

 public static void drawCreatureContactShadow(Canvas c,Paint p,float x,float ground,float halfWidth,float lift,float activity){
  if(c==null||p==null)return;
  x=sn(x);ground=sn(ground);halfWidth=Math.max(8f,sn(halfWidth));lift=Math.max(0,lift);activity=Math.max(0,Math.min(1,activity));
  float contact=contactScaleForLift(lift),half=sn(halfWidth*contact);
  int outer=(int)(26+18*(1-activity)),inner=(int)(14+12*(1-activity));
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  band(c,p,Color.argb(outer,5,10,10),x-half,ground-2,x+half,ground+3);
  band(c,p,Color.argb(inner,18,25,23),x-half*.66f,ground-3,x+half*.66f,ground+1);
  if(lift<8f)band(c,p,Color.argb(10,77,91,74),x-half*.42f,ground-4,x+half*.42f,ground-2);
 }

 public static void drawActorGrounding(Canvas c,Paint p,int accent,float x,float ground,float lift){
  if(c==null||p==null)return;
  x=sn(x);ground=sn(ground);lift=Math.max(0,lift);
  float contact=Math.max(.46f,1f-lift/250f),half=sn(46f*contact);
  int ar=Color.red(accent),ag=Color.green(accent),ab=Color.blue(accent);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  band(c,p,Color.argb(34,7,14,14),x-half*1.15f,ground-2,x+half*1.15f,ground+4);
  band(c,p,Color.argb(25,11,19,18),x-half*.86f,ground-3,x+half*.86f,ground+2);
  band(c,p,Color.argb(18,ar,ag,ab),x-half*.62f,ground-5,x+half*.62f,ground-3);
 }

 public static void drawObjectShadow(Canvas c,Paint p,WorldState s,WorldObject o,float x,float dir,float stretch){
  if(c==null||p==null||o==null||"creature".equals(o.type))return;
  x=sn(x);float g=sn(o.y);
  float w=Math.max(14f,Math.min(150f,o.width*.62f));
  String phase=s==null||s.environment==null?"DAY":s.environment.dayPhase(s.worldMinutes);
  float len="NIGHT".equals(phase)?.70f:Math.max(.70f,stretch),dx=("NIGHT".equals(phase)?0f:dir)*w*.34f;
  double bright=s==null||s.environment==null?.8:Math.max(.15,Math.min(1,s.environment.ambientBrightness));
  int alpha=(int)(14+26*bright);if(s!=null&&s.environment!=null&&s.environment.cloudCover>.72)alpha=(int)(alpha*.62);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  float center=sn(x+dx),half=sn(w*len);
  for(int i=0;i<4;i++){
   float inset=i*half*.13f,yy=g-3+i*2;
   int a=Math.max(3,alpha-i*5);
   band(c,p,Color.argb(a,8+i*5,15+i*6,15+i*5),center-half+inset,yy,center+half-inset,yy+2);
  }
 }

 public static void drawCloudShadows(Canvas c,Paint p,WorldState s,float anim,float cam){
  if(c==null||p==null||s==null||s.environment==null)return;
  double cover=Math.max(0,Math.min(1,s.environment.cloudCover)),wind=Math.max(0,Math.min(1,s.environment.wind));
  if(cover<.18||"NIGHT".equals(s.environment.dayPhase(s.worldMinutes)))return;
  int alpha=(int)(6+22*cover);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  for(int i=0;i<5;i++){
   float speed=10f+(float)wind*28f+i*2.5f,cx=sn((float)((i*570+anim*speed)%3100)-350-cam*.08f);
   float cy=sn(735+(i%3)*76f),rx=sn(210+(i%3)*76f);
   for(int band=0;band<4;band++){
    float inset=band*rx*.12f,yy=cy-8+band*5;
    int a=Math.max(2,alpha-i*2-band*2);
    band(c,p,Color.argb(a,18+band*7,31+band*8,32+band*7),cx-rx+inset,yy,cx+rx-inset,yy+4);
   }
  }
 }

 public static void drawDirectionalGroundLight(Canvas c,Paint p,WorldState s,int accent,float anim,float sunX,float dir,float stretch,float altitude){
  if(c==null||p==null||s==null||s.environment==null)return;
  if("NIGHT".equals(s.environment.dayPhase(s.worldMinutes)))return;
  int ar=Color.red(accent),ag=Color.green(accent),ab=Color.blue(accent);
  int alpha=(int)(8+(1-altitude)*14);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  for(int i=0;i<4;i++){
   float y=sn(715+i*62f),w=sn((270+i*105f)*stretch),drift=sn((float)Math.sin(anim*.11+i*.8)*14f);
   float x=sn(sunX+dir*(i*150f)+drift);
   int a=Math.max(4,alpha-i*3);
   int col=Color.argb(a,Math.min(255,ar+35),Math.min(255,ag+28),Math.min(255,ab+12));
   for(int band=0;band<4;band++){
    float start=x-dir*(w-band*w*.11f),end=x+dir*w*(.18f-band*.02f);
    float yy=y-18+band*8;
    band(c,p,col,Math.min(start,end),yy,Math.max(start,end),yy+5);
   }
  }
 }

 public static int bandCount(){return 4;} public static int actorContactBandCount(){return 3;} static float contactScaleForLift(float lift){return Math.max(.34f,1f-Math.max(0,lift)/220f);}
 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void band(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);
 }
}
