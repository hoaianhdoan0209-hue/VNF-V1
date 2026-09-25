package com.aicharacter.v3;

import android.graphics.*;

/** Pixel-native landmark and night FX. Presentation only. */
public final class PixelLandmarkFxRenderer {
 private PixelLandmarkFxRenderer(){}

 public static void drawNight(Canvas c,Paint p,WorldState s,String area,float cam,float anim){
  if(c==null||p==null||s==null||s.environment==null||!"NIGHT".equals(s.environment.dayPhase(s.worldMinutes)))return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  if("home".equals(area))nightHome(c,p,cam,anim);
  else if("garden".equals(area))nightGarden(c,p,anim);
  else if("lakeside".equals(area))nightLakeside(c,p,anim);
  else nightGrove(c,p,anim);
 }

 public static void drawAnimation(Canvas c,Paint p,WorldState s,String area,float cam,float anim){
  if(c==null||p==null||s==null||s.environment==null)return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  if("home".equals(area))animateHome(c,p,cam,anim);
  else if("garden".equals(area))animateGarden(c,p,cam,anim);
  else if("lakeside".equals(area))animateLakeside(c,p,s,cam,anim);
  else animateGrove(c,p,cam,anim);
 }

 public static void drawInteraction(Canvas c,Paint p,WorldState s,String area,float cam,float anim){
  if(c==null||p==null||s==null||s.environment==null)return;
  String phase=s.environment.dayPhase(s.worldMinutes);
  double bright=Math.max(.12,Math.min(1,s.environment.ambientBrightness));
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  if("home".equals(area)){
   float x=sn(545-cam*.28f),y=600;
   if("EVENING".equals(phase)||"NIGHT".equals(phase)){
    glow(c,p,x,y,110,Color.rgb(255,174,91),"NIGHT".equals(phase)?28:20);
   }else{
    band(c,p,Color.argb((int)(8+12*bright),239,220,172),x-90,y+64,x+70,y+70);
   }
  }else if("garden".equals(area)){
   float x=sn(1200-cam*.22f),y=735;
   glow(c,p,x,y,92,Color.rgb(224,235,199),(int)(9+16*bright));
   for(int i=0;i<7;i++){
    float sx=sn(x-75+i*25f+(float)Math.sin(anim*.9+i)*5),sy=sn(y-42+(i%3)*13f);
    band(c,p,Color.argb(14+(i%3)*7,230,244,227),sx,sy,sx+3,sy+3);
   }
  }else if("lakeside".equals(area)){
   float x=sn(1210-cam*.15f),y=690;
   for(int i=0;i<8;i++){
    float yy=sn(y+i*18f),xx=sn(x-145+(i%4)*65f+(float)Math.sin(anim*.45+i)*15f);
    band(c,p,Color.argb((int)(8+12*bright)+(i%3)*3,223,232,205),xx,yy,xx+56+(i%3)*18,yy+3);
   }
   if(!"NIGHT".equals(phase))glow(c,p,x+180,y+40,120,Color.rgb(236,210,145),(int)(7+9*bright));
  }else{
   float x=sn(430-cam*.16f),y=790;
   int a="NIGHT".equals(phase)?17:(int)(7+8*bright);
   glow(c,p,x,y,126,Color.rgb(184,211,135),a);
   for(int i=0;i<9;i++){
    float sx=sn(x-160+i*38f),sy=sn(y-20+(i%3)*21f);
    band(c,p,Color.argb(10+(i%4)*5,198,215,142),sx,sy,sx+3+(i%2)*2,sy+3);
   }
  }
 }

 public static int motifFamilies(){return 4;}

 private static void nightHome(Canvas c,Paint p,float cam,float anim){
  float hx=sn(540-cam*.28f);
  for(int i=0;i<3;i++){
   float x=sn(hx+i*54f),y=sn(520+(i%2)*24f);
   glow(c,p,x,y,56+i*12,Color.rgb(255,176,86),24-i*4);
  }
  for(int i=0;i<8;i++){
   float x=sn(hx-55+(i*37)%180),y=sn(540+(i%3)*34+(float)Math.sin(anim*.55+i)*7);
   band(c,p,Color.argb(18+(i%4)*6,255,207,118),x,y,x+3+(i%2)*2,y+3);
  }
 }

 private static void nightGarden(Canvas c,Paint p,float anim){
  for(int i=0;i<4;i++){
   float x=470+i*480f,y=650+(i%2)*70f;
   glow(c,p,x,y,78+i*9,Color.rgb(239,198,144),9+(i%2)*4);
  }
 }

 private static void nightLakeside(Canvas c,Paint p,float anim){
  // stepped moon path
  int moon=Color.argb(18,169,194,231);
  for(int i=0;i<8;i++){
   float y=560+i*52f,half=sn(150-i*11f);
   band(c,p,moon,1690-half,y,1690+half,y+18);
  }
  for(int i=0;i<12;i++){
   float y=sn(645+i*24f),x=sn(1510+(i%4)*54f+(float)Math.sin(anim*.4+i)*32f),w=70+(i%5)*24f;
   band(c,p,Color.argb(10+(i%4)*5,188,211,235),x,y,x+w,y+3);
  }
 }

 private static void nightGrove(Canvas c,Paint p,float anim){
  for(int i=0;i<4;i++){
   float cx=sn(520+i*360f+(float)Math.sin(anim*.18+i)*20f),ww=95+i*14f;
   for(int b=0;b<6;b++){
    float y=90+b*136f,half=ww*(1f-b*.10f);
    band(c,p,Color.argb(11+(i%2)*4,158,190,205),cx-half,y,cx+half,y+12);
   }
  }
 }

 private static void animateHome(Canvas c,Paint p,float cam,float anim){
  float x=sn(545-cam*.28f),y=590,pulse=(float)(.5+.5*Math.sin(anim*2.1));
  glow(c,p,x,y,58,Color.rgb(255,177,88),(int)(8+13*pulse));
  for(int i=0;i<3;i++){
   float yy=sn(y-28-i*17+(float)Math.sin(anim*.8+i)*5),xx=sn(x+18+(float)Math.sin(anim*.6+i)*7);
   band(c,p,Color.argb(10+i*4,255,210,126),xx,yy,xx+3+i,yy+3+i);
  }
 }

 private static void animateGarden(Canvas c,Paint p,float cam,float anim){
  float x=sn(1200-cam*.22f),y=770;
  // pixel fountain arcs as stepping droplets
  for(int stream=0;stream<3;stream++){
   for(int j=0;j<8;j++){
    float t=j/7f,dir=(stream==1?0:(stream==0?-1:1));
    float dx=dir*(18+stream*12)*t,dy=-sn((34+stream*9)*(1f-(2*t-1)*(2*t-1)));
    float px=sn(x+dx),py=sn(y-28+dy);
    band(c,p,Color.argb(24-stream*4,220,239,232),px,py,px+3,py+3);
   }
  }
  for(int i=0;i<8;i++){
   float t=(float)((anim*.55+i*.137)%1.0),dx=(i%2==0?-1:1)*(26+(i%4)*11)*t,dy=-62*t+72*t*t;
   float px=sn(x+dx),py=sn(y-28+dy);
   band(c,p,Color.argb(24+(i%3)*6,228,244,238),px,py,px+3+(i%2)*2,py+3);
  }
 }

 private static void animateLakeside(Canvas c,Paint p,WorldState s,float cam,float anim){
  float intensity=(float)Math.max(.15,Math.min(1,s.environment.wind+.18));
  for(int i=0;i<6;i++){
   float cx=sn(1120-cam*.15f+(i%3)*115f),cy=sn(708+(i/3)*74f);
   float phase=(float)((anim*(.35+intensity*.3)+i*.21)%1.0),rx=26+phase*52;
   int a=(int)(18*(1-phase));
   for(int b=0;b<3;b++){
    float inset=b*rx*.18f;
    band(c,p,Color.argb(Math.max(2,a-b*4),204,230,226),cx-rx+inset,cy+b*3,cx+rx-inset,cy+b*3+2);
   }
  }
 }

 private static void animateGrove(Canvas c,Paint p,float cam,float anim){
  for(int i=0;i<7;i++){
   float x=sn(180+(i*191)%1280-cam*.12f),y=sn(825+(i%3)*31f);
   float pulse=(float)(.5+.5*Math.sin(anim*1.1+i*.8));
   glow(c,p,x,y,20+(i%3)*4,Color.rgb(205,222,142),(int)(5+12*pulse));
  }
 }

 private static void glow(Canvas c,Paint p,float x,float y,float radius,int color,int alpha){
  int r=Color.red(color),g=Color.green(color),b=Color.blue(color);
  for(int i=4;i>=0;i--){
   float w=sn(radius*(.34f+i*.17f)),h=sn(radius*(.20f+i*.09f));
   int a=Math.max(2,(int)(alpha*(1f-i*.14f)));
   band(c,p,Color.argb(a,r,g,b),x-w,y-h,x+w,y+h);
  }
 }

 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void band(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);
 }
}
