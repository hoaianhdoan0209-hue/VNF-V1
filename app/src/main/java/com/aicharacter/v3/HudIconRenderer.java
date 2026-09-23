package com.aicharacter.v3;
import android.graphics.*;

/** One quiet line-icon family for the world HUD. No raster buttons and no long labels. */
public final class HudIconRenderer{
 public static final int CHAT=1,MIC=2,GOD=3,SUN=4,MOON=5,CLOUD=6,RAIN=7;
 private HudIconRenderer(){}

 public static void drawButton(Canvas c,Paint p,MinimalHudLayout.Box b,int icon,boolean pressed,float density){
  float d=Math.max(.75f,density),cx=(b.l+b.r)*.5f,cy=(b.t+b.b)*.5f,rad=Math.min(b.width(),b.height())*.5f;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(pressed?176:104,8,18,24));c.drawCircle(cx,cy,rad,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((pressed?2.2f:1.35f)*d);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setColor(Color.argb(pressed?245:190,226,232,214));c.drawCircle(cx,cy,rad-1.5f*d,p);
  c.save();if(pressed)c.translate(0,1.2f*d);draw(c,p,icon,cx,cy,Math.min(23f*d,rad*.58f),d);c.restore();
  p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
 }
 public static void drawGodButton(Canvas c,Paint p,MinimalHudLayout.Box b,boolean pressed,float density,GodSessionManager.State state,long now){
  float d=Math.max(.75f,density),cx=(b.l+b.r)*.5f,cy=(b.t+b.b)*.5f,rad=Math.min(b.width(),b.height())*.5f,pulse=(float)(.5+.5*Math.sin(now/520.0));
  if(state==GodSessionManager.State.ONLINE){
   p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(cx,cy,rad*1.42f,Color.argb((int)(18+18*pulse),235,219,177),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,rad*1.42f,p);p.setShader(null);
  }
  drawButton(c,p,b,GOD,pressed,d);
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
  if(state==GodSessionManager.State.CONNECTING){
   p.setStrokeWidth(1.35f*d);p.setColor(Color.argb(180,193,220,213));RectF rr=new RectF(cx-rad*.84f,cy-rad*.84f,cx+rad*.84f,cy+rad*.84f);c.drawArc(rr,(now%1800L)/1800f*360f,112,false,p);
  }else if(state==GodSessionManager.State.ONLINE){
   p.setStrokeWidth((1.05f+.35f*pulse)*d);p.setColor(Color.argb((int)(125+70*pulse),241,225,184));c.drawCircle(cx,cy,rad*.82f,p);
   for(int i=0;i<4;i++){double a=i*Math.PI/2.0+now/2100.0;float x=cx+(float)Math.cos(a)*rad*.98f,y=cy+(float)Math.sin(a)*rad*.98f;p.setStyle(Paint.Style.FILL);p.setColor(Color.argb((int)(90+70*pulse),197,226,216));c.drawCircle(x,y,1.1f*d,p);p.setStyle(Paint.Style.STROKE);}
  }else if(state==GodSessionManager.State.DEGRADED){
   p.setStrokeWidth(1.05f*d);p.setColor(Color.argb(115,171,191,194));RectF rr=new RectF(cx-rad*.80f,cy-rad*.80f,cx+rad*.80f,cy+rad*.80f);c.drawArc(rr,18,74,false,p);c.drawArc(rr,142,52,false,p);c.drawArc(rr,246,68,false,p);
  }
  p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
 }

 public static void drawStatus(Canvas c,Paint p,Paint text,MinimalHudLayout.Box b,String phase,String weather,double tempC,float density){
  float d=Math.max(.75f,density);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(88,7,17,22));c.drawRoundRect(b.l,b.t,b.r,b.b,12*d,12*d,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d);p.setColor(Color.argb(84,211,225,207));c.drawRoundRect(b.l+.5f*d,b.t+.5f*d,b.r-.5f*d,b.b-.5f*d,12*d,12*d,p);
  float cy=(b.t+b.b)*.5f,x=b.l+18*d;draw(c,p,"NIGHT".equals(phase)?MOON:SUN,x,cy,8.5f*d,d);
  int wx="RAIN".equals(weather)?RAIN:"CLOUDY".equals(weather)?CLOUD:0;
  if(wx!=0){x+=27*d;draw(c,p,wx,x,cy,8.5f*d,d);}
  text.setTypeface(Typeface.create(Typeface.SANS_SERIF,Typeface.NORMAL));text.setTextSize(11.5f*d);text.setColor(Color.argb(220,231,232,215));text.setTextAlign(Paint.Align.RIGHT);
  c.drawText(Math.round(tempC)+"°",b.r-11*d,cy+4*d,text);text.setTextAlign(Paint.Align.LEFT);
  p.setStyle(Paint.Style.FILL);
 }
 private static void draw(Canvas c,Paint p,int icon,float cx,float cy,float r,float d){
  p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f*d);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);p.setColor(Color.argb(232,235,231,207));
  if(icon==CHAT){
   RectF q=new RectF(cx-r*.92f,cy-r*.72f,cx+r*.92f,cy+r*.52f);c.drawRoundRect(q,r*.30f,r*.30f,p);Path tail=new Path();tail.moveTo(cx-r*.30f,cy+r*.52f);tail.lineTo(cx-r*.53f,cy+r*.88f);tail.lineTo(cx+r*.02f,cy+r*.52f);c.drawPath(tail,p);
  }else if(icon==MIC){
   RectF q=new RectF(cx-r*.38f,cy-r*.86f,cx+r*.38f,cy+r*.22f);c.drawRoundRect(q,r*.38f,r*.38f,p);c.drawArc(new RectF(cx-r*.70f,cy-r*.08f,cx+r*.70f,cy+r*.78f),0,180,false,p);c.drawLine(cx,cy+r*.78f,cx,cy+r*1.02f,p);c.drawLine(cx-r*.34f,cy+r*1.02f,cx+r*.34f,cy+r*1.02f,p);
  }else if(icon==GOD){
   c.drawCircle(cx,cy,r*.78f,p);Path q=new Path();q.moveTo(cx,cy-r);q.lineTo(cx+r*.42f,cy);q.lineTo(cx,cy+r);q.lineTo(cx-r*.42f,cy);q.close();c.drawPath(q,p);c.drawCircle(cx,cy,r*.15f,p);for(int i=0;i<4;i++){double a=i*Math.PI/2;c.drawLine(cx+(float)Math.cos(a)*r*.48f,cy+(float)Math.sin(a)*r*.48f,cx+(float)Math.cos(a)*r*.92f,cy+(float)Math.sin(a)*r*.92f,p);}
  }else if(icon==SUN){
   c.drawCircle(cx,cy,r*.42f,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine(cx+(float)Math.cos(a)*r*.66f,cy+(float)Math.sin(a)*r*.66f,cx+(float)Math.cos(a)*r,cy+(float)Math.sin(a)*r,p);}
  }else if(icon==MOON){
   c.drawArc(new RectF(cx-r*.62f,cy-r*.86f,cx+r*.62f,cy+r*.86f),70,220,false,p);c.drawArc(new RectF(cx-r*.15f,cy-r*.72f,cx+r*.68f,cy+r*.72f),105,150,false,p);
  }else if(icon==CLOUD||icon==RAIN){
   Path q=new Path();q.moveTo(cx-r*.82f,cy+r*.15f);q.cubicTo(cx-r*.88f,cy-r*.18f,cx-r*.58f,cy-r*.34f,cx-r*.32f,cy-r*.27f);q.cubicTo(cx-r*.18f,cy-r*.72f,cx+r*.48f,cy-r*.70f,cx+r*.57f,cy-r*.22f);q.cubicTo(cx+r*.96f,cy-r*.20f,cx+r*.98f,cy+r*.28f,cx+r*.62f,cy+r*.34f);q.lineTo(cx-r*.50f,cy+r*.34f);c.drawPath(q,p);if(icon==RAIN){for(int i=-1;i<=1;i++)c.drawLine(cx+i*r*.42f,cy+r*.55f,cx+i*r*.42f-r*.10f,cy+r*.95f,p);}
  }
 }
}
