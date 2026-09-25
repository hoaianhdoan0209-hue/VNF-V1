package com.aicharacter.v3;

import android.graphics.*;

/**
 * Pixel-native biome detail pass.
 * Adds area-specific blocky scenery and motion without changing world simulation state.
 */
public final class PixelBiomeDetailRenderer {
 private static final float G=2f;
 private PixelBiomeDetailRenderer(){}

 public static boolean supports(String area){
  return "home".equals(area)||"garden".equals(area)||"lakeside".equals(area)||"grove".equals(area);
 }
 public static int motifCount(String area){
  if("home".equals(area))return 4;
  if("garden".equals(area))return 5;
  if("lakeside".equals(area))return 6;
  if("grove".equals(area))return 5;
  return 0;
 }

 public static void drawMid(Canvas c,Paint p,WorldState s,String area,float cam,float anim){
  if(c==null||p==null||s==null||!supports(area))return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  if("home".equals(area))drawHomeMid(c,p,s,cam,anim);
  else if("garden".equals(area))drawGardenMid(c,p,s,cam,anim);
  else if("lakeside".equals(area))drawLakesideMid(c,p,s,cam,anim);
  else drawGroveMid(c,p,s,cam,anim);
 }

 public static void drawForeground(Canvas c,Paint p,WorldState s,String area,float cam,float anim){
  if(c==null||p==null||s==null||!supports(area))return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  if("home".equals(area))drawHomeFront(c,p,s,cam,anim);
  else if("garden".equals(area))drawGardenFront(c,p,s,cam,anim);
  else if("lakeside".equals(area))drawLakesideFront(c,p,s,cam,anim);
  else drawGroveFront(c,p,s,cam,anim);
 }

 private static void drawHomeMid(Canvas c,Paint p,WorldState s,float cam,float anim){
  float x=sn(535-cam*.28f),y=sn(565);
  int phase=s.environment==null?0:phase(s.environment.dayPhase(s.worldMinutes));
  int warm=phase==3?Color.rgb(255,183,91):Color.rgb(232,190,112);
  block(p,c,warm,x-18,y-42,36,42);
  block(p,c,Color.rgb(117,78,53),x-22,y-46,44,5);
  block(p,c,Color.rgb(82,54,42),x-23,y-2,46,5);
  int glow=phase==3?210:115;p.setColor(Color.argb(glow,255,198,103));
  for(int i=0;i<3;i++){float xx=sn(x-12+i*12);c.drawRect(xx,y-35,xx+6,y-18,p);}
  for(int i=0;i<5;i++){float xx=sn(430-cam*.20f+i*72),yy=sn(678+(i%2)*10);p.setColor(Color.rgb(88,104,69));c.drawRect(xx,yy-16,xx+4,yy,p);p.setColor(Color.rgb(125,143,82));c.drawRect(xx-4,yy-14,xx+8,yy-10,p);}
  float smoke=(anim*13f)%120f;for(int i=0;i<4;i++){float yy=sn(y-62-smoke-i*21),xx=sn(x+9+(float)Math.sin(anim*.45+i)*6);p.setColor(Color.argb(24-i*4,205,207,191));c.drawRect(xx-5,yy-3,xx+7,yy+3,p);}
 }

 private static void drawHomeFront(Canvas c,Paint p,WorldState s,float cam,float anim){
  for(int i=0;i<8;i++){float x=sn(80+i*310-cam),y=sn(912+(i%3)*15);p.setColor(Color.rgb(68,84,58));c.drawRect(x,y-10,x+4,y,p);p.setColor(i%2==0?Color.rgb(180,157,98):Color.rgb(142,123,82));c.drawRect(x-4,y-13,x+9,y-9,p);}
  if(s.worldWetness>.18){p.setColor(Color.argb(40,219,190,132));for(int i=0;i<5;i++){float x=sn(250+i*430-cam*.08f),y=sn(870+(i%2)*22);c.drawRect(x,y,x+48+i*8,y+2,p);}}
 }

 private static void drawGardenMid(Canvas c,Paint p,WorldState s,float cam,float anim){
  double wind=s.environment==null?0:Math.max(0,Math.min(1,s.environment.wind));
  for(int i=0;i<26;i++){
   float base=sn(80+i*92-cam*.55f),y=sn(818+(i%4)*13),sway=sn((float)Math.sin(anim*(.8+wind*1.4)+i*.63)*(2+wind*5));
   p.setColor(Color.rgb(62+(i%3)*8,112+(i%4)*7,66));c.drawRect(base,y-22,base+3,y,p);
   p.setColor(petal(i));c.drawRect(base-4+sway,y-28,base+8+sway,y-22,p);c.drawRect(base+sway,y-32,base+4+sway,y-18,p);
  }
  for(int i=0;i<7;i++){float x=sn(180+i*310-cam*.25f),y=sn(700+(i%3)*31);p.setColor(Color.rgb(102,119,76));c.drawRect(x-10,y-5,x+11,y+5,p);p.setColor(Color.rgb(129,146,91));c.drawRect(x-5,y-8,x+6,y+2,p);}
 }

 private static void drawGardenFront(Canvas c,Paint p,WorldState s,float cam,float anim){
  for(int i=0;i<12;i++){float x=sn(-40+i*225-cam),y=sn(930+(i%3)*17),h=16+(i%4)*4;p.setColor(Color.rgb(48,91,59));c.drawRect(x,y-h,x+4,y,p);c.drawRect(x+8,y-h*.72f,x+12,y,p);p.setColor(Color.rgb(80,129,70));c.drawRect(x-4,y-h+4,x+9,y-h+8,p);}
  if(s.environment!=null&&s.environment.ambientBrightness>.45){for(int i=0;i<9;i++){float x=sn(120+i*260-cam*.12f),y=sn(520+(i%4)*55+(float)Math.sin(anim*.9+i)*6);p.setColor(Color.argb(44,242,223,145));c.drawRect(x,y,x+2,y+2,p);}}
 }

 private static void drawLakesideMid(Canvas c,Paint p,WorldState s,float cam,float anim){
  double wind=s.environment==null?0:Math.max(0,Math.min(1,s.environment.wind));
  for(int i=0;i<22;i++){
   float x=sn(110+i*108-cam*.68f),base=sn(842),h=24+(i%5)*7,sway=sn((float)Math.sin(anim*(.65+wind*1.4)+i*.41)*(2+wind*5));
   p.setColor(Color.rgb(56,91,65));c.drawRect(x,base-h,x+3,base,p);c.drawRect(x+sway+5,base-h*.78f,x+sway+8,base,p);
   p.setColor(Color.rgb(118,94,60));c.drawRect(x+sway-1,base-h-8,x+sway+4,base-h,p);
  }
  for(int i=0;i<8;i++){float x=sn(320+i*255-cam*.10f),y=sn(690+(i%3)*29);p.setColor(Color.argb(24,198,225,219));c.drawRect(x,y,x+34+(i%3)*14,y+2,p);}
  for(int i=0;i<5;i++){float x=sn(510+i*380-cam*.04f),y=sn(530+(i%2)*47+(float)Math.sin(anim*.7+i)*4);p.setColor(Color.argb(46,187,221,209));c.drawRect(x,y,x+2,y+2,p);}
 }

 private static void drawLakesideFront(Canvas c,Paint p,WorldState s,float cam,float anim){
  for(int i=0;i<11;i++){float x=sn(-30+i*250-cam),y=sn(948+(i%3)*15);p.setColor(Color.rgb(44,76,57));c.drawRect(x,y-28,x+5,y,p);c.drawRect(x+11,y-20,x+15,y,p);p.setColor(Color.rgb(74,111,71));c.drawRect(x-5,y-24,x+8,y-20,p);}
  if(s.environment!=null&&"NIGHT".equals(s.environment.dayPhase(s.worldMinutes))){for(int i=0;i<10;i++){float x=sn(80+i*240-cam*.16f),y=sn(520+(i%5)*48+(float)Math.sin(anim*.8+i)*8);p.setColor(Color.argb(68,187,218,174));c.drawRect(x,y,x+2,y+2,p);}}
 }

 private static void drawGroveMid(Canvas c,Paint p,WorldState s,float cam,float anim){
  for(int i=0;i<14;i++){float x=sn(70+i*180-cam*.42f),y=sn(844+(i%3)*12),w=18+(i%4)*5;p.setColor(Color.rgb(64,70,52));c.drawRect(x,y-8,x+w,y,p);p.setColor(Color.rgb(103,121,69));c.drawRect(x+3,y-11,x+w-4,y-7,p);}
  for(int i=0;i<10;i++){float x=sn(140+i*245-cam*.18f),y=sn(730+(i%4)*31);p.setColor(i%3==0?Color.rgb(123,88,104):Color.rgb(86,103,71));c.drawRect(x,y-5,x+9,y+2,p);c.drawRect(x+3,y-10,x+6,y-4,p);}
  for(int i=0;i<7;i++){float x=sn(250+i*330-cam*.08f),y=sn(460+(i%3)*73+(float)Math.sin(anim*.35+i)*5);p.setColor(Color.argb(30,198,211,135));c.drawRect(x,y,x+3,y+3,p);}
 }

 private static void drawGroveFront(Canvas c,Paint p,WorldState s,float cam,float anim){
  for(int i=0;i<9;i++){float x=sn(-70+i*315-cam),y=sn(952+(i%2)*18),h=32+(i%4)*8;p.setColor(Color.rgb(43,70,51));c.drawRect(x,y-h,x+6,y,p);c.drawRect(x+14,y-h*.68f,x+19,y,p);p.setColor(Color.rgb(65,96,60));c.drawRect(x-5,y-h+7,x+14,y-h+12,p);}
  if(s.environment!=null){double hum=s.atmosphere==null?.55:Math.max(0,Math.min(1,s.atmosphere.relativeHumidity));if(hum>.65){for(int i=0;i<5;i++){float x=sn(200+i*470-cam*.06f),y=sn(875+(i%2)*24);p.setColor(Color.argb((int)(12+28*hum),179,197,170));c.drawRect(x,y,x+72,y+3,p);}}}
 }

 private static int petal(int i){
  switch(i%5){case 0:return Color.rgb(235,178,188);case 1:return Color.rgb(229,207,142);case 2:return Color.rgb(180,171,217);case 3:return Color.rgb(212,145,168);default:return Color.rgb(221,221,199);}
 }
 private static int phase(String s){if("NIGHT".equals(s))return 3;if("EVENING".equals(s))return 2;if("MORNING".equals(s))return 1;return 0;}
 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void block(Paint p,Canvas c,int color,float x,float y,float w,float h){p.setColor(color);c.drawRect(sn(x),sn(y),sn(x+w),sn(y+h),p);}
}
