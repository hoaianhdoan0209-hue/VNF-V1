package com.aicharacter.v3;

import android.graphics.*;

/** Discrete pixel reaction cues for non-combat readability. Presentation only. */
public final class PixelReactionFxRenderer {
 private PixelReactionFxRenderer(){}

 public static int cueFamilyCount(){return 3;}

 public static void drawHaru(Canvas c,Paint p,GirlAnimationController.Visual v,float x,float ground,float anim){
  if(c==null||p==null||v==null)return;
  if(v.state==GirlAnimationController.State.REACT)burst(c,p,x,ground-174f,anim,Color.rgb(245,211,142),1f);
  else if(v.state==GirlAnimationController.State.THINK)thought(c,p,x+36f,ground-166f,anim);
  else if(v.state==GirlAnimationController.State.SEARCH_LEFT||v.state==GirlAnimationController.State.SEARCH_RIGHT)
    scan(c,p,x+(v.flipX?-42f:42f),ground-158f,anim);
 }

 public static void drawCat(Canvas c,Paint p,CatAnimationController.Visual v,float x,float ground,float anim){
  if(c==null||p==null||v==null)return;
  if(v.state==CatAnimationController.State.BRACE||v.state==CatAnimationController.State.RETREAT)
    burst(c,p,x+(v.facingRight?36f:-36f),ground-58f,anim,Color.rgb(231,170,122),.75f);
  else if(v.state==CatAnimationController.State.WATCH)
    scan(c,p,x+(v.facingRight?38f:-38f),ground-54f,anim);
 }

 public static void drawWildlifeAlert(Canvas c,Paint p,float x,float y,float alert,float anim,int light){
  if(c==null||p==null||alert<.62f)return;
  int alpha=(int)(55+145*Math.min(1,alert));
  int col=Color.argb(alpha,Color.red(light),Color.green(light),Color.blue(light));
  float pulse=(float)(.5+.5*Math.sin(anim*4.2));
  for(int i=0;i<3;i++){
   float side=i==0?-1f:i==1?1f:0f;
   float xx=sn(x+side*(14+i*3)),yy=sn(y-18-i*6-pulse*3);
   rect(c,p,col,xx-2,yy-4,xx+2,yy+2);
  }
 }

 private static void burst(Canvas c,Paint p,float x,float y,float anim,int color,float scale){
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  float pulse=(float)(.5+.5*Math.sin(anim*4.8)),u=4f*scale;
  int a=(int)(130+90*pulse);
  int col=Color.argb(a,Color.red(color),Color.green(color),Color.blue(color));
  float[][] pt={{0,-16},{12,-8},{16,3},{-12,-8},{-16,3}};
  for(float[] q:pt){float xx=sn(x+q[0]*scale),yy=sn(y+q[1]*scale-pulse*2);rect(c,p,col,xx-u*.5f,yy-u,xx+u*.5f,yy+u*.25f);}
 }

 private static void thought(Canvas c,Paint p,float x,float y,float anim){
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  float bob=(float)Math.sin(anim*1.7)*2f;
  int col=Color.argb(150,216,224,207);
  rect(c,p,col,x-2,y+8+bob,x+2,y+12+bob);
  rect(c,p,col,x+6,y+bob,x+12,y+6+bob);
  rect(c,p,col,x+14,y-10+bob,x+24,y-2+bob);
 }

 private static void scan(Canvas c,Paint p,float x,float y,float anim){
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  int col=Color.argb(150,177,218,207);
  float phase=(float)(.5+.5*Math.sin(anim*2.2));
  rect(c,p,col,x,y-8,x+2,y+8);
  rect(c,p,col,x+6+phase*4,y-12,x+8+phase*4,y+12);
  rect(c,p,Color.argb(75,177,218,207),x+12+phase*7,y-6,x+14+phase*7,y+6);
 }

 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);
 }
}
