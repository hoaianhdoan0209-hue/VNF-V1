package com.aicharacter.v3;

import android.graphics.*;

/**
 * Pixel-scale facial overlay for close emotional shots.
 * It only decorates stable front-facing poses and never mutates world state.
 */
public final class HaruFacialOverlayRenderer {
 private static final int SKIN=Color.rgb(244,207,166);
 private static final int EYE_WHITE=Color.rgb(252,225,188);
 private static final int INK=Color.rgb(75,46,43);
 private static final int CHEEK=Color.rgb(194,137,101);
 private HaruFacialOverlayRenderer(){}

 public static void draw(Canvas c,Paint p,WorldState s,GirlAnimationController.Visual pose,RectF dst){
  if(c==null||p==null||s==null||pose==null||dst==null||!HaruExpressionEngine.supports(pose.state))return;
  long now=Math.max(s.lastSimulatedAt,s.lastOpenedAt);HaruExpressionEngine.Visual v=HaruExpressionEngine.derive(s,now);
  if(v.intensity<.10&&!v.socialFocus&&!v.blink)return;
  float unit=dst.width()/Math.max(1f,HaruVisualRenderer.authoredFrameWidth());
  float cx=dst.left+dst.width()*.515f,cy=dst.top+dst.height()*.230f+(float)v.headDropPx*unit;
  float eyeY=cy-6.0f*unit,eyeDx=5.4f*unit,mouthY=cy+6.0f*unit;
  int oldAlpha=p.getAlpha();Paint.Style oldStyle=p.getStyle();Paint.Cap oldCap=p.getStrokeCap();float oldStroke=p.getStrokeWidth();ColorFilter oldFilter=p.getColorFilter();boolean oldAA=p.isAntiAlias();
  p.setShader(null);p.setColorFilter(null);p.setAntiAlias(false);p.setStyle(Paint.Style.FILL);p.setAlpha(255);
  double localGazeX=pose.flipX?-v.gazeX:v.gazeX,localTilt=pose.flipX?-v.headTiltDeg:v.headTiltDeg;
  c.save();c.rotate((float)localTilt,cx,cy);

  drawEye(c,p,cx-eyeDx,eyeY,unit,v,localGazeX);
  drawEye(c,p,cx+eyeDx,eyeY,unit,v,localGazeX);
  drawBrows(c,p,cx,eyeY,eyeDx,unit,v);
  drawMouth(c,p,cx,mouthY,unit,v);
  drawCheeks(c,p,cx,cy,unit,v);

  c.restore();p.setAlpha(oldAlpha);p.setStyle(oldStyle);p.setStrokeWidth(oldStroke);p.setStrokeCap(oldCap);p.setColorFilter(oldFilter);p.setAntiAlias(oldAA);
 }

 private static void drawEye(Canvas c,Paint p,float x,float y,float u,HaruExpressionEngine.Visual v,double localGazeX){
  float patchW=4.6f*u,patchH=3.8f*u;p.setColor(SKIN);c.drawRect(x-patchW*.5f,y-patchH*.5f,x+patchW*.5f,y+patchH*.5f,p);
  float open=(float)Math.max(.18,Math.min(1,v.eyeOpen)),whiteH=Math.max(.9f*u,(1.0f+open*1.45f)*u),whiteW=3.2f*u;
  p.setColor(EYE_WHITE);c.drawRect(x-whiteW*.5f,y-whiteH*.5f,x+whiteW*.5f,y+whiteH*.5f,p);
  float px=x+(float)localGazeX*1.05f*u,py=y+(float)v.gazeY*.72f*u,pupilW=1.35f*u,pupilH=Math.max(.85f*u,whiteH*.76f);
  p.setColor(INK);c.drawRect(px-pupilW*.5f,py-pupilH*.5f,px+pupilW*.5f,py+pupilH*.5f,p);
  if(v.socialFocus&&!v.blink&&open>.45){p.setColor(Color.rgb(255,244,221));float gl=.46f*u;c.drawRect(px-.40f*u,py-.48f*u,px-.40f*u+gl,py-.48f*u+gl,p);}
 }

 private static void drawBrows(Canvas c,Paint p,float cx,float eyeY,float dx,float u,HaruExpressionEngine.Visual v){
  p.setColor(INK);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,1.25f*u));p.setStrokeCap(Paint.Cap.SQUARE);
  float y=eyeY-4.0f*u,outerDelta=(float)v.browLift*.35f*u,innerDelta=(float)(-v.browLift*1.15+v.browPinch*1.35)*u;
  float lx=cx-dx,rx=cx+dx;
  c.drawLine(lx-2.1f*u,y+outerDelta,lx+2.0f*u,y+innerDelta,p);
  c.drawLine(rx+2.1f*u,y+outerDelta,rx-2.0f*u,y+innerDelta,p);
  p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
 }

 private static void drawMouth(Canvas c,Paint p,float cx,float y,float u,HaruExpressionEngine.Visual v){
  float patchW=10.5f*u,patchH=5.2f*u;p.setColor(SKIN);c.drawRect(cx-patchW*.5f,y-patchH*.5f,cx+patchW*.5f,y+patchH*.5f,p);
  float curve=(float)v.mouthCurve,open=(float)v.mouthOpen;
  p.setColor(INK);
  if(open>.24){
   float w=(3.2f+open*2.2f)*u,h=(1.0f+open*2.4f)*u;c.drawRect(cx-w*.5f,y-h*.45f,cx+w*.5f,y+h*.55f,p);
   if(curve>.25){p.setColor(Color.rgb(233,151,145));c.drawRect(cx-w*.28f,y+h*.10f,cx+w*.28f,y+h*.40f,p);}
   return;
  }
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1f,1.15f*u));p.setStrokeCap(Paint.Cap.SQUARE);
  float span=4.3f*u,corner=y-curve*1.25f*u,center=y+curve*.72f*u;
  c.drawLine(cx-span,corner,cx,center,p);c.drawLine(cx,center,cx+span,corner,p);
  p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
 }

 private static void drawCheeks(Canvas c,Paint p,float cx,float cy,float u,HaruExpressionEngine.Visual v){
  if(v.cheekWarmth<.18)return;int a=(int)Math.max(12,Math.min(78,18+v.cheekWarmth*60));p.setColor(Color.argb(a,Color.red(CHEEK),Color.green(CHEEK),Color.blue(CHEEK)));
  float y=cy+1.0f*u,dx=8.3f*u,w=2.4f*u,h=1.25f*u;c.drawRect(cx-dx-w*.5f,y-h*.5f,cx-dx+w*.5f,y+h*.5f,p);c.drawRect(cx+dx-w*.5f,y-h*.5f,cx+dx+w*.5f,y+h*.5f,p);
 }
}
