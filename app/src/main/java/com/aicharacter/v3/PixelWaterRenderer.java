package com.aicharacter.v3;

import android.graphics.*;

/** Hard-edge animated pixel water for Lakeside. Presentation only. */
public final class PixelWaterRenderer {
 private PixelWaterRenderer(){}

 public static void draw(Canvas c,Paint p,WorldState s,float cam,float anim){
  if(c==null||p==null||s==null||s.environment==null)return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);
  String phase=s.environment.dayPhase(s.worldMinutes);
  boolean night="NIGHT".equals(phase),evening="EVENING".equals(phase),morning="MORNING".equals(phase);
  boolean rain="RAIN".equals(s.environment.weather);
  float wind=(float)Math.max(0,Math.min(1,s.environment.wind));
  float wet=(float)Math.max(0,Math.min(1,s.worldWetness));
  int deep=night?Color.rgb(34,57,80):evening?Color.rgb(70,105,124):Color.rgb(61,111,138);
  int mid=night?Color.rgb(52,81,105):evening?Color.rgb(103,135,143):Color.rgb(78,139,158);
  int high=night?Color.rgb(105,137,164):evening?Color.rgb(202,168,131):morning?Color.rgb(181,203,190):Color.rgb(150,192,194);
  if(rain){deep=Color.rgb(45,70,86);mid=Color.rgb(65,97,109);high=Color.rgb(133,159,161);}

  final float top=604f,bottom=1080f;
  rect(c,p,deep,0,top,2400,bottom);

  // Large depth bands: fewer colors, no gradients.
  for(int band=0;band<10;band++){
   float y=top+band*48f;
   int col=(band&1)==0?mid:deep;
   rect(c,p,col,0,y,2400,y+22);
  }

  // Moving surface streaks. Their spacing/length grows toward camera.
  for(int i=0;i<72;i++){
   float depth=(i%18)/17f;
   float y=top+18+(i%18)*25f+(i/18)*5f;
   float speed=9f+wind*26f+(i%7)*2.7f;
   float x=mod(i*197f+anim*speed*8f-cam*.06f,2550f)-80f;
   float ww=14f+depth*70f+(i%5)*9f;
   int alpha=38+(int)(depth*58)+(i%4)*8;
   int col=Color.argb(Math.min(150,alpha),Color.red(high),Color.green(high),Color.blue(high));
   rect(c,p,col,x,y,x+ww,y+2+(i%3==0?2:0));
   if((i&3)==0){
    int dark=Color.argb(34+(int)(depth*36),18,48,66);
    rect(c,p,dark,x+ww*.35f,y+6,x+ww*.76f,y+8);
   }
  }

  drawReflection(c,p,s,anim,phase,wind,high);
  drawNearRipples(c,p,anim,wind,wet,high,deep);
  if(rain)drawRainRipples(c,p,s,anim,wind);
 }

 private static void drawReflection(Canvas c,Paint p,WorldState s,float anim,String phase,float wind,int high){
  float t=(float)Math.max(0,Math.min(1,(s.worldMinutes-360.0)/(14.0*60.0)));
  boolean night="NIGHT".equals(phase);
  float cx=night?1780f:120f+2160f*t;
  int warm=night?Color.rgb(167,194,229):"EVENING".equals(phase)?Color.rgb(255,183,112):Color.rgb(244,218,155);
  for(int row=0;row<15;row++){
   float y=635+row*25f;
   float depth=row/14f;
   float spread=18+depth*150;
   float jitter=(float)Math.sin(anim*(.35f+wind*.22f)+row*.83f)*(6+depth*26);
   float ww=16+depth*78+(row%4)*10;
   int a=20+(int)((1-depth)*26)+(row%3)*5;
   int col=Color.argb(a,Color.red(warm),Color.green(warm),Color.blue(warm));
   rect(c,p,col,cx-spread*.20f+jitter,y,cx-spread*.20f+jitter+ww,y+3+(row%4==0?2:0));
   if(row>4){
    float mirrorX=cx+spread*.35f-jitter*.6f;
    rect(c,p,Color.argb(Math.max(10,a-7),Color.red(high),Color.green(high),Color.blue(high)),mirrorX,y+8,mirrorX+ww*.55f,y+10);
   }
  }
 }

 private static void drawNearRipples(Canvas c,Paint p,float anim,float wind,float wet,int high,int deep){
  for(int i=0;i<18;i++){
   float x=90+(i*337)%2240+(float)Math.sin(anim*.42f+i)*22f;
   float y=805+(i%7)*38f;
   float phase=(float)((anim*(.18f+wind*.20f)+i*.137f)%1.0);
   float ww=18+phase*(55+(i%4)*18);
   int a=(int)((1-phase)*(18+wet*24));
   if(a<4)a=4;
   rect(c,p,Color.argb(a,Color.red(high),Color.green(high),Color.blue(high)),x-ww,y,x+ww,y+2);
   if(i%3==0)rect(c,p,Color.argb(Math.max(3,a/2),Color.red(deep),Color.green(deep),Color.blue(deep)),x-ww*.55f,y+5,x+ww*.35f,y+7);
  }
 }

 private static void drawRainRipples(Canvas c,Paint p,WorldState s,float anim,float wind){
  float intensity=(float)Math.max(.15,Math.min(1,s.environment.weatherIntensity));
  int count=14+(int)(intensity*18);
  for(int i=0;i<count;i++){
   float x=mod(i*181f+anim*(34+wind*31)*(1+i%3*.14f),2390f)+5;
   float y=630+(i*79)%420;
   float phase=(float)((anim*(.55+intensity*.45)+i*.19)%1.0);
   float ww=6+phase*(18+intensity*34);
   int a=(int)((1-phase)*(34+intensity*54));
   int col=Color.argb(Math.max(4,a),187,215,220);
   rect(c,p,col,x-ww,y,x+ww,y+2);
   if(phase<.22f){
    rect(c,p,Color.argb(Math.max(6,a),206,228,231),x-1,y-8-intensity*8,x+1,y-1);
   }
  }
 }

 private static float mod(float v,float m){float r=v%m;return r<0?r+m:r;}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);
  c.drawRect(PixelArtRenderPolicy.snapLogical(l),PixelArtRenderPolicy.snapLogical(t),PixelArtRenderPolicy.snapLogical(r),PixelArtRenderPolicy.snapLogical(b),p);
 }
}
