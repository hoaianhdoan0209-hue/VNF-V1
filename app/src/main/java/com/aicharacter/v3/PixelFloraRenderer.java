package com.aicharacter.v3;

import android.graphics.*;

/** Pixel-native renderer for persistent living flora. */
public final class PixelFloraRenderer {
 private static final float G=2f;
 private PixelFloraRenderer(){}

 public static boolean supports(WorldObject o){
  if(o==null||o.id==null)return false;
  String id=o.id;
  return id.startsWith("hearth_bloom")||id.startsWith("silverfold")||id.startsWith("pulse_grass")
      ||id.startsWith("lam_thread")||id.startsWith("shimmer_mat")||id.startsWith("echo_frond")
      ||id.startsWith("threadbloom");
 }
 public static int familyCount(){return 7;}

 public static boolean draw(Canvas c,Paint p,WorldState s,WorldObject o,FloraLifeState f,float screenX,float anim){
  if(!supports(o)||c==null||p==null||f==null)return false;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);
  float x=sn(screenX),y=sn(o.y),open=(float)(.45+.55*f.opening);
  float sway=sn((float)Math.sin(anim*(.45+f.body.rhythm*1.5)+(o.id.hashCode()&31))*(float)(2+11*f.swayDrive));
  float pulse=(float)(.5+.5*Math.sin(anim*(.55+f.growthPulse*1.4)+(o.id.hashCode()&15)));
  if(o.id.startsWith("hearth_bloom"))drawHearthBloom(c,p,x,y,sway,pulse);
  else if(o.id.startsWith("silverfold"))drawSilverfold(c,p,x,y,sway,open);
  else if(o.id.startsWith("pulse_grass"))drawPulseGrass(c,p,x,y,sway,f);
  else if(o.id.startsWith("lam_thread"))drawLamThread(c,p,x,y,sway,pulse,f);
  else if(o.id.startsWith("shimmer_mat"))drawShimmerMat(c,p,x,y,sway,pulse,f,o.width);
  else if(o.id.startsWith("echo_frond"))drawEchoFrond(c,p,x,y,sway,open);
  else drawThreadbloom(c,p,x,y,sway,pulse,open);
  return true;
 }

 private static void drawHearthBloom(Canvas c,Paint p,float x,float y,float sway,float pulse){
  for(int i=-3;i<=3;i++){
   float dx=i*6*G,hh=(9+Math.abs(i%3)*3)*G;
   rect(c,p,Color.rgb(55,88,61),x+dx-G,y-hh,x+dx+G,y);
   int leaf=i%2==0?Color.rgb(88,124,82):Color.rgb(102,136,91);
   rect(c,p,leaf,x+dx-3*G+sway*.20f,y-hh+2*G,x+dx+3*G+sway*.20f,y-hh+5*G);
  }
  int glow=(int)(110+100*pulse);
  rect(c,p,Color.argb(glow,236,194,119),x-4*G+sway*.18f,y-15*G,x+4*G+sway*.18f,y-8*G);
  rect(c,p,Color.argb(Math.min(255,glow+25),251,224,155),x-2*G+sway*.18f,y-13*G,x+2*G+sway*.18f,y-10*G);
 }

 private static void drawSilverfold(Canvas c,Paint p,float x,float y,float sway,float open){
  for(int i=-3;i<=3;i++){
   float dx=i*7*G,h=(15+Math.abs(i%2)*5)*G,tip=sway*(1-Math.abs(i)*.10f);
   int dark=Color.rgb(96,119,104),light=Color.rgb(150,173,154),shine=Color.rgb(187,199,181);
   rect(c,p,dark,x+dx-G,y-h*.54f,x+dx+G,y);
   float half=(3+2*open)*G;
   rect(c,p,light,x+dx+tip-half,y-h,x+dx+tip+half,y-h+4*G);
   rect(c,p,shine,x+dx+tip-G,y-h+G,x+dx+tip+2*G,y-h+2*G);
   rect(c,p,dark,x+dx+tip-half,y-h+4*G,x+dx+tip+half*.70f,y-h+6*G);
  }
 }

 private static void drawPulseGrass(Canvas c,Paint p,float x,float y,float sway,FloraLifeState f){
  for(int i=-5;i<=5;i++){
   float dx=i*4*G,h=(9+(i*i%4)*3)*G,tip=sway*(.25f+Math.abs(i)*.05f);
   rect(c,p,Color.rgb(63,105,61),x+dx-G,y-h*.50f,x+dx+G,y);
   rect(c,p,Color.rgb(94,142,78),x+dx+tip-G,y-h,x+dx+tip+G,y-h*.43f);
   if((i&2)==0&&f.sense!=null&&f.sense.lumenSense>.45){
    int a=(int)(35+90*f.sense.lumenSense);rect(c,p,Color.argb(a,204,223,144),x+dx+tip-G,y-h-2*G,x+dx+tip+G,y-h);
   }
  }
 }

 private static void drawLamThread(Canvas c,Paint p,float x,float y,float sway,float pulse,FloraLifeState f){
  double moist=f.sense==null?0:f.sense.moistureSense;
  for(int i=-3;i<=3;i++){
   float dx=i*6*G,h=(25+Math.abs(i%3)*6)*G,tip=sway*(.50f+Math.abs(i)*.07f);
   rect(c,p,Color.rgb(56,101,94),x+dx-G,y-h*.48f,x+dx+G,y);
   rect(c,p,Color.rgb(76,132,122),x+dx+tip-G,y-h,x+dx+tip+G,y-h*.42f);
   int a=(int)(70+120*moist);
   rect(c,p,Color.argb(a,132,198,199),x+dx+tip-2*G,y-h-3*G,x+dx+tip+2*G,y-h+G);
   if(pulse>.65f)rect(c,p,Color.argb(a,202,230,218),x+dx+tip-G,y-h-2*G,x+dx+tip+G,y-h);
  }
 }

 private static void drawShimmerMat(Canvas c,Paint p,float x,float y,float sway,float pulse,FloraLifeState f,float width){
  float half=Math.max(16*G,width*.45f);double moist=f.sense==null?0:f.sense.moistureSense;
  rect(c,p,Color.rgb(52,88,77),x-half,y-5*G,x+half,y);
  for(int i=-5;i<=5;i++){
   float xx=x+i*(half/5f)+sway*.10f,hh=(2+(i&1))*G;
   rect(c,p,Color.rgb(78,123,106),xx-2*G,y-hh-3*G,xx+2*G,y-hh);
   int a=(int)(35+120*moist);if(pulse>.48f)rect(c,p,Color.argb(a,168,215,207),xx-G,y-hh-5*G,xx+G,y-hh-3*G);
  }
 }

 private static void drawEchoFrond(Canvas c,Paint p,float x,float y,float sway,float open){
  rect(c,p,Color.rgb(50,86,62),x-2*G,y-29*G,x+2*G,y);
  float half=(9+5*open)*G,top=y-46*G;
  rect(c,p,Color.rgb(73,119,83),x-half+sway,y-34*G,x+half+sway,y-29*G);
  rect(c,p,Color.rgb(102,145,103),x-half*.72f+sway,top,x+half*.72f+sway,y-34*G);
  rect(c,p,Color.rgb(128,161,119),x-2*G+sway,top-2*G,x+3*G+sway,top+2*G);
 }

 private static void drawThreadbloom(Canvas c,Paint p,float x,float y,float sway,float pulse,float open){
  for(int i=-2;i<=2;i++){
   float dx=i*6*G,top=y-(32+Math.abs(i)*4)*G,tip=sway*.55f;
   rect(c,p,Color.rgb(60,94,72),x+dx-G,y-18*G,x+dx+G,y);
   rect(c,p,Color.rgb(75,112,88),x+dx+tip-G,top,x+dx+tip+G,y-17*G);
   float r=(2+open)*G;int flower=i%2==0?Color.rgb(188,132,160):Color.rgb(207,154,174);
   rect(c,p,flower,x+dx+tip-r,top-2*G,x+dx+tip+r,top+2*G);
   if(pulse>.55f)rect(c,p,Color.argb(105,235,197,202),x+dx+tip-G,top-G,x+dx+tip+G,top+G);
  }
 }

 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);
 }
}
