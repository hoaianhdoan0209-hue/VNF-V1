package com.aicharacter.v3;

import android.graphics.*;

/** Presentation-only pixel landmarks for the core authored world props. */
public final class PixelWorldPropRenderer {
 private static final int INK=Color.rgb(39,34,31);
 private static final int WOOD_DARK=Color.rgb(91,61,45);
 private static final int WOOD=Color.rgb(139,92,59);
 private static final int WOOD_LIGHT=Color.rgb(181,126,76);
 private static final int ROOF=Color.rgb(77,60,55);
 private static final int ROOF_LIGHT=Color.rgb(116,84,69);
 private static final int STONE=Color.rgb(92,100,92);
 private static final int STONE_LIGHT=Color.rgb(126,133,118);
 private static final int WINDOW=Color.rgb(255,197,104);
 private static final int WINDOW_HOT=Color.rgb(255,229,151);

 private PixelWorldPropRenderer(){}

 public static boolean supports(WorldObject o){
  if(o==null)return false;
  return "shelter_wood".equals(o.assetRef)||"bench_wood_01".equals(o.assetRef);
 }

 public static boolean draw(Canvas c,Paint p,WorldState s,WorldObject o,float screenX){
  if(!supports(o)||c==null||p==null)return false;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);
  float x=PixelArtRenderPolicy.snapLogical(screenX),g=PixelArtRenderPolicy.snapLogical(o.y);
  if("shelter_wood".equals(o.assetRef))drawShelter(c,p,s,x,g,o);
  else drawBench(c,p,s,x,g,o);
  return true;
 }

 private static void drawShelter(Canvas c,Paint p,WorldState s,float x,float g,WorldObject o){
  float w=Math.max(360,o.width),h=Math.max(300,o.height);
  float bodyL=x-w*.40f,bodyR=x+w*.40f,bodyT=g-h*.64f,bodyB=g-18;
  float block=12f;

  // Block shadow / foundation.
  rect(c,p,Color.argb(52,0,0,0),x-w*.46f,g-18,x+w*.46f,g+4);
  for(int i=0;i<8;i++){
   float sx=bodyL+i*(bodyR-bodyL)/8f;
   rect(c,p,i%2==0?STONE:STONE_LIGHT,sx,g-38,sx+(bodyR-bodyL)/8f+2,g-18);
  }
  rect(c,p,INK,bodyL-6,g-42,bodyR+6,g-36);

  // Cabin body outline and warm timber fill.
  rect(c,p,INK,bodyL-8,bodyT-6,bodyR+8,bodyB+2);
  rect(c,p,WOOD_DARK,bodyL,bodyT,bodyR,bodyB);
  for(float y=bodyT+16;y<bodyB-8;y+=24){
   rect(c,p,WOOD,y==bodyT+16?bodyL+2:bodyL,y,bodyR,y+15);
   rect(c,p,WOOD_LIGHT,bodyL+6,y+3,bodyR-6,y+6);
   rect(c,p,Color.rgb(105,67,48),bodyL,y+15,bodyR,y+20);
  }

  // Vertical beams keep the silhouette readable from a distance.
  for(float bx: new float[]{bodyL+18,x-82,x+84,bodyR-24}){
   rect(c,p,INK,bx-7,bodyT-2,bx+9,bodyB);
   rect(c,p,WOOD_LIGHT,bx-2,bodyT+4,bx+4,bodyB-8);
  }

  // Stepped roof; no smooth diagonals.
  float roofTop=g-h*.94f;
  for(int row=0;row<9;row++){
   float yy=roofTop+row*block;
   float half=72+row*24;
   rect(c,p,INK,x-half-10,yy-4,x+half+10,yy+block+4);
   rect(c,p,row%2==0?ROOF:ROOF_LIGHT,x-half,yy,x+half,yy+block);
   if(row%2==1)for(float xx=x-half+10;xx<x+half-8;xx+=36)rect(c,p,WOOD_DARK,xx,yy+7,xx+18,yy+11);
  }
  rect(c,p,INK,x-w*.46f,g-h*.66f,x+w*.46f,g-h*.61f);

  // Chimney + square smoke pixels.
  float chx=x-w*.24f;
  rect(c,p,INK,chx-19,roofTop-43,chx+21,roofTop+8);
  rect(c,p,WOOD_DARK,chx-13,roofTop-38,chx+15,roofTop+6);
  rect(c,p,STONE_LIGHT,chx-10,roofTop-34,chx+12,roofTop-28);
  float drift=(float)Math.sin((s==null?0:s.worldMinutes)*.015f)*6f;
  rect(c,p,Color.argb(54,191,198,185),chx-10+drift,roofTop-65,chx+8+drift,roofTop-51);
  rect(c,p,Color.argb(36,191,198,185),chx+6+drift,roofTop-84,chx+24+drift,roofTop-69);

  boolean dark=s!=null&&s.environment!=null&&("NIGHT".equals(s.environment.dayPhase(s.worldMinutes))||"EVENING".equals(s.environment.dayPhase(s.worldMinutes)));
  // Left window.
  drawWindow(c,p,x-w*.22f,g-h*.48f,70,78,dark);
  // Smaller right window.
  drawWindow(c,p,x+w*.23f,g-h*.48f,62,72,dark);

  // Door centered near the authored interaction point.
  float doorX=x+42,doorT=g-158;
  rect(c,p,INK,doorX-48,doorT-8,doorX+48,g-34);
  rect(c,p,Color.rgb(88,57,43),doorX-40,doorT,doorX+40,g-36);
  for(float yy=doorT+10;yy<g-44;yy+=22)rect(c,p,WOOD,doorX-34,yy,doorX+34,yy+6);
  rect(c,p,WOOD_LIGHT,doorX-34,doorT+5,doorX-28,g-43);
  rect(c,p,WINDOW_HOT,doorX+23,g-93,doorX+31,g-85);

  // Roof moss/grass in block clusters.
  int moss=Color.rgb(83,117,67),mossLight=Color.rgb(118,145,78);
  for(int i=0;i<12;i++){
   float mx=x-w*.39f+i*(w*.78f/11f),my=g-h*.65f-(i%3)*4;
   rect(c,p,i%2==0?moss:mossLight,mx-10,my-8,mx+14,my+5);
   if(i%3==0)rect(c,p,mossLight,mx,my-17,mx+5,my-5);
  }

  // Pixel porch, useful as grounding cue for Haru and cat.
  rect(c,p,INK,x-18,g-34,x+138,g-24);
  rect(c,p,WOOD_LIGHT,x-12,g-31,x+132,g-27);
  rect(c,p,INK,x+112,g-26,x+126,g+1);
  rect(c,p,WOOD_DARK,x+115,g-24,x+123,g-1);
 }

 private static void drawWindow(Canvas c,Paint p,float cx,float cy,float ww,float hh,boolean glow){
  rect(c,p,INK,cx-ww*.5f-7,cy-hh*.5f-7,cx+ww*.5f+7,cy+hh*.5f+7);
  if(glow){
   // Square stepped glow keeps the light pixel-authored.
   rect(c,p,Color.argb(22,255,180,84),cx-ww*.78f,cy-hh*.78f,cx+ww*.78f,cy+hh*.78f);
   rect(c,p,Color.argb(32,255,187,88),cx-ww*.64f,cy-hh*.64f,cx+ww*.64f,cy+hh*.64f);
  }
  rect(c,p,WINDOW,cx-ww*.5f,cy-hh*.5f,cx+ww*.5f,cy+hh*.5f);
  rect(c,p,WINDOW_HOT,cx-ww*.40f,cy-hh*.40f,cx+ww*.40f,cy+hh*.40f);
  rect(c,p,INK,cx-3,cy-hh*.5f,cx+3,cy+hh*.5f);
  rect(c,p,INK,cx-ww*.5f,cy-3,cx+ww*.5f,cy+3);
 }

 private static void drawBench(Canvas c,Paint p,WorldState s,float x,float g,WorldObject o){
  float w=Math.max(170,o.width),h=Math.max(105,o.height);
  rect(c,p,Color.argb(45,0,0,0),x-w*.52f,g-9,x+w*.52f,g+4);

  // Feet/legs.
  for(float lx:new float[]{x-w*.34f,x+w*.26f}){
   rect(c,p,INK,lx-10,g-h*.37f,lx+13,g);
   rect(c,p,WOOD_DARK,lx-5,g-h*.35f,lx+8,g-3);
   rect(c,p,INK,lx-18,g-6,lx+20,g+2);
  }

  // Seat.
  rect(c,p,INK,x-w*.49f,g-h*.46f-7,x+w*.49f,g-h*.30f+7);
  rect(c,p,WOOD,x-w*.46f,g-h*.44f,x+w*.46f,g-h*.33f);
  rect(c,p,WOOD_LIGHT,x-w*.43f,g-h*.42f,x+w*.43f,g-h*.38f);
  for(float sx=x-w*.38f;sx<x+w*.39f;sx+=46)rect(c,p,WOOD_DARK,sx,g-h*.44f,sx+5,g-h*.33f);

  // Back supports.
  rect(c,p,INK,x-w*.37f,g-h*.94f,x-w*.28f,g-h*.39f);
  rect(c,p,INK,x+w*.28f,g-h*.94f,x+w*.37f,g-h*.39f);
  rect(c,p,WOOD_DARK,x-w*.345f,g-h*.91f,x-w*.305f,g-h*.42f);
  rect(c,p,WOOD_DARK,x+w*.305f,g-h*.91f,x+w*.345f,g-h*.42f);

  // Three chunky back planks.
  for(int i=0;i<3;i++){
   float yy=g-h*.88f+i*h*.16f;
   rect(c,p,INK,x-w*.48f,yy-5,x+w*.48f,yy+h*.12f+5);
   rect(c,p,i==1?WOOD_LIGHT:WOOD,x-w*.45f,yy,x+w*.45f,yy+h*.10f);
   rect(c,p,WOOD_DARK,x-w*.42f,yy+h*.075f,x+w*.42f,yy+h*.10f);
  }

  // A few moss pixels make it belong to Lakeside.
  int moss=Color.rgb(93,126,76),mossLight=Color.rgb(126,151,88);
  rect(c,p,moss,x-w*.40f,g-h*.93f,x-w*.28f,g-h*.88f);
  rect(c,p,mossLight,x-w*.36f,g-h*.98f,x-w*.32f,g-h*.91f);
  rect(c,p,moss,x+w*.18f,g-h*.57f,x+w*.34f,g-h*.52f);
 }

 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);
  c.drawRect(PixelArtRenderPolicy.snapLogical(l),PixelArtRenderPolicy.snapLogical(t),PixelArtRenderPolicy.snapLogical(r),PixelArtRenderPolicy.snapLogical(b),p);
 }
}
