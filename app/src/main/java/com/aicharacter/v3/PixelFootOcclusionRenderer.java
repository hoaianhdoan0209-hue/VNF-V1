package com.aicharacter.v3;

import android.graphics.*;

/** Local foreground occlusion around actor feet so sprites sit inside the biome. */
public final class PixelFootOcclusionRenderer {
 private PixelFootOcclusionRenderer(){}

 public static int biomeModeCount(){return 4;}

 public static void draw(Canvas c,Paint p,String area,float x,float ground,float anim,float strength){
  if(c==null||p==null||area==null||strength<=.05f)return;
  strength=Math.max(0,Math.min(1,strength));x=sn(x);ground=sn(ground);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);
  if("lakeside".equals(area))reeds(c,p,x,ground,anim,strength);
  else if("garden".equals(area))grass(c,p,x,ground,anim,strength);
  else if("grove".equals(area))moss(c,p,x,ground,anim,strength);
  else home(c,p,x,ground,strength);
 }

 public static String visualArea(String areaId){
  if(areaId==null)return "home";
  String a=areaId.toLowerCase(java.util.Locale.ROOT);
  if(a.contains("lake"))return "lakeside";
  if(a.contains("garden"))return "garden";
  if(a.contains("grove")||a.contains("quiet"))return "grove";
  return "home";
 }

 private static void grass(Canvas c,Paint p,float x,float g,float a,float s){
  int dark=Color.argb((int)(130*s),50,92,55),light=Color.argb((int)(145*s),79,126,67);
  for(int i=-3;i<=3;i++){
   float xx=x+i*5f,h=6+(Math.abs(i)%3)*3f,sw=sn((float)Math.sin(a*.9+i)*2f*s);
   rect(c,p,dark,xx-1,g-h*.55f,xx+2,g+1);
   rect(c,p,light,xx+sw,g-h,xx+sw+2,g-h*.42f);
  }
 }

 private static void reeds(Canvas c,Paint p,float x,float g,float a,float s){
  int stem=Color.argb((int)(145*s),54,91,64),tip=Color.argb((int)(150*s),116,91,58);
  for(int i=-3;i<=3;i++){
   float xx=x+i*6f,h=8+(Math.abs(i)%3)*5f,sw=sn((float)Math.sin(a*.75+i*.7)*2.5f*s);
   rect(c,p,stem,xx-1,g-h*.48f,xx+2,g+1);
   rect(c,p,stem,xx+sw,g-h,xx+sw+2,g-h*.42f);
   if((i&1)==0)rect(c,p,tip,xx+sw-1,g-h-4,xx+sw+3,g-h);
  }
 }

 private static void moss(Canvas c,Paint p,float x,float g,float a,float s){
  int dark=Color.argb((int)(125*s),45,71,51),light=Color.argb((int)(145*s),80,111,65);
  rect(c,p,dark,x-20,g-3,x+20,g+2);
  for(int i=-4;i<=4;i++){
   float xx=x+i*4.5f,h=3+(i*i%3)*2f;
   rect(c,p,light,xx-2,g-h,xx+3,g);
  }
  if(s>.6f)rect(c,p,Color.argb((int)(80*s),128,137,79),x+7,g-6,x+10,g-3);
 }

 private static void home(Canvas c,Paint p,float x,float g,float s){
  int col=Color.argb((int)(90*s),112,100,72);
  for(int i=-2;i<=2;i++){float xx=x+i*7;rect(c,p,col,xx-2,g-3-(i&1)*2,xx+3,g);}
 }

 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);
 }
}
