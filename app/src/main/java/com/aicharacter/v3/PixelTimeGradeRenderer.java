package com.aicharacter.v3;

import android.graphics.*;

/** Pixel-native full-screen time/weather grading. */
public final class PixelTimeGradeRenderer {
 private PixelTimeGradeRenderer(){}

 public static void draw(Canvas c,Paint p,WorldState s,String area,int accent,float width,float height,float anim){
  if(c==null||p==null||s==null||s.environment==null)return;
  String phase=s.environment.dayPhase(s.worldMinutes);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);

  int[] bands=palette(phase);
  float bh=height/bands.length;
  for(int i=0;i<bands.length;i++){
   p.setColor(bands[i]);
   c.drawRect(0,PixelArtRenderPolicy.snapLogical(i*bh),width,PixelArtRenderPolicy.snapLogical((i+1)*bh+2),p);
  }

  if(!"NIGHT".equals(phase)){
   float t=(float)Math.max(0,Math.min(1,(s.worldMinutes-360.0)/(14.0*60.0)));
   float sx=PixelArtRenderPolicy.snapLogical(120f+2160f*t);
   float alt=(float)Math.max(0,Math.sin(t*Math.PI));
   float sy=PixelArtRenderPolicy.snapLogical(330f-alt*185f);
   boolean eve=sx>1200f;
   int warm=eve?Color.rgb(255,162,95):Color.rgb(255,225,166);
   steppedGlow(c,p,sx,sy,150+alt*44,warm,(int)(10+(1-alt)*20));
  }else{
   steppedGlow(c,p,Math.min(width-120,1880),210,132,Color.rgb(158,186,230),24);
   for(int i=0;i<24;i++){
    float x=PixelArtRenderPolicy.snapLogical(80+(i*337)%Math.max(120,(int)width-120));
    float y=PixelArtRenderPolicy.snapLogical(70+(i*149)%360);
    int a=20+(i%5)*7;p.setColor(Color.argb(a,216,229,240));
    float sz=(i%4==0)?4:2;c.drawRect(x,y,x+sz,y+sz,p);
   }
  }

  if("RAIN".equals(s.environment.weather)){
   p.setColor(Color.argb(30,25,45,61));c.drawRect(0,0,width,height,p);
  }else if("CLOUDY".equals(s.environment.weather)){
   p.setColor(Color.argb(14,72,88,91));c.drawRect(0,0,width,height,p);
  }

  int ar=Color.red(accent),ag=Color.green(accent),ab=Color.blue(accent);
  p.setColor(Color.argb("grove".equals(area)?12:8,Math.max(0,ar-40),Math.max(0,ag-35),Math.max(0,ab-30)));
  for(int i=0;i<5;i++){
   float inset=i*width*.055f;
   c.drawRect(inset,i*5,width-inset,i*5+4,p);
   c.drawRect(inset,height-4-i*5,width-inset,height-i*5,p);
  }
 }

 public static int bandCount(){return 8;}

 private static int[] palette(String phase){
  if("NIGHT".equals(phase))return new int[]{
   Color.argb(26,7,14,33),Color.argb(22,9,19,40),Color.argb(18,12,24,43),Color.argb(15,16,31,45),
   Color.argb(13,20,37,44),Color.argb(11,24,41,42),Color.argb(9,27,43,39),Color.argb(8,29,44,36)};
  if("EVENING".equals(phase))return new int[]{
   Color.argb(16,89,55,91),Color.argb(15,111,65,95),Color.argb(14,139,78,94),Color.argb(13,169,93,89),
   Color.argb(12,194,109,83),Color.argb(11,216,127,80),Color.argb(10,230,145,84),Color.argb(9,234,159,93)};
  if("MORNING".equals(phase))return new int[]{
   Color.argb(11,238,199,151),Color.argb(10,244,207,158),Color.argb(10,246,215,168),Color.argb(9,242,221,179),
   Color.argb(9,231,224,184),Color.argb(8,219,225,186),Color.argb(8,207,224,187),Color.argb(7,197,220,185)};
  return new int[]{
   Color.argb(5,181,215,221),Color.argb(5,186,218,220),Color.argb(5,194,220,214),Color.argb(5,203,222,207),
   Color.argb(5,211,223,200),Color.argb(5,217,223,193),Color.argb(5,222,224,188),Color.argb(5,225,224,183)};
 }

 private static void steppedGlow(Canvas c,Paint p,float x,float y,float size,int color,int alpha){
  int r=Color.red(color),g=Color.green(color),b=Color.blue(color);
  for(int i=4;i>=0;i--){
   float s=PixelArtRenderPolicy.snapLogical(size*(.30f+i*.17f));
   int a=Math.max(2,(int)(alpha*(1f-i*.14f)));
   p.setColor(Color.argb(a,r,g,b));
   c.drawRect(x-s,y-s*.55f,x+s,y+s*.55f,p);
  }
 }
}
