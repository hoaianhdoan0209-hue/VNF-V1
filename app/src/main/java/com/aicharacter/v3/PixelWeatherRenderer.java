package com.aicharacter.v3;

import android.graphics.*;

/** Hard-edge weather particles aligned to the VNF pixel grid. */
public final class PixelWeatherRenderer {
 private PixelWeatherRenderer(){}

 public static void drawRain(Canvas c,Paint p,WorldState s,float anim){
  if(c==null||p==null||s==null||s.environment==null||!"RAIN".equals(s.environment.weather))return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);
  float intensity=(float)Math.max(.12,Math.min(1,s.environment.weatherIntensity));
  float wind=(float)Math.max(0,Math.min(1,s.environment.wind));

  drawLayer(c,p,anim,intensity,wind,34,96f,1f,10f,Color.argb(58,192,213,221),0);
  drawLayer(c,p,anim,intensity,wind,26,154f,2f,18f,Color.argb(92,204,225,231),1);
  drawLayer(c,p,anim,intensity,wind,16,245f,3f,31f,Color.argb(128,220,236,239),2);

  int splashes=8+(int)(intensity*17);
  for(int i=0;i<splashes;i++){
   float x=mod(i*211f+anim*(72f+wind*84f),2440f)-20f;
   float y=824+(i%5)*38f;
   float phase=(float)((anim*(.7f+intensity*.4f)+i*.173f)%1.0);
   float ww=4+phase*(10+intensity*18);
   int a=(int)((1-phase)*(42+intensity*62));
   int col=Color.argb(Math.max(4,a),197,221,225);
   rect(c,p,col,x-ww,y,x+ww,y+2);
   if(phase<.22f){
    rect(c,p,Color.argb(Math.max(5,a),214,232,234),x-1,y-6-intensity*8,x+1,y);
   }
  }
 }

 private static void drawLayer(Canvas c,Paint p,float anim,float intensity,float wind,int count,float speed,float thickness,float len,int color,int layer){
  for(int i=0;i<count;i++){
   float localSpeed=speed+intensity*speed*.72f+(i%5)*9f;
   float x=mod(i*(137f+layer*31f)+anim*localSpeed,2520f)-60f;
   float y=mod(i*(83f+layer*17f)+anim*(localSpeed*.73f),1180f)-80f;
   float slant=wind*(5+layer*8+intensity*10);
   float dy=len+intensity*(8+layer*6)+(i%3)*4;
   float w=Math.max(1,thickness);
   // Raster stair-step instead of a smooth diagonal line.
   int steps=Math.max(2,(int)(dy/6f));
   for(int s=0;s<steps;s++){
    float t=s/(float)steps;
    float px=x-slant*(1-t),py=y-dy*(1-t);
    rect(c,p,color,px,py,px+w,py+5+layer);
   }
  }
 }

 private static float mod(float v,float m){float r=v%m;return r<0?r+m:r;}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);
  c.drawRect(PixelArtRenderPolicy.snapLogical(l),PixelArtRenderPolicy.snapLogical(t),PixelArtRenderPolicy.snapLogical(r),PixelArtRenderPolicy.snapLogical(b),p);
 }
}
