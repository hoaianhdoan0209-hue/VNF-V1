package com.aicharacter.v3;

import android.graphics.*;

/** Small pixel contact FX that make actors feel grounded in each biome. Presentation only. */
public final class PixelContactFxRenderer {
 private PixelContactFxRenderer(){}

 public static int variantCount(){return 4;}

 public static void draw(Canvas c,Paint p,WorldState s,String area,float x,float ground,float motion,float anim){
  if(c==null||p==null||s==null||area==null||motion<=.08f)return;
  x=sn(x);ground=sn(ground);motion=Math.max(0,Math.min(1,motion));
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);

  if("lakeside".equals(area))water(c,p,x,ground,motion,anim);
  else if("garden".equals(area))petals(c,p,x,ground,motion,anim);
  else if("grove".equals(area))moss(c,p,x,ground,motion,anim);
  else dust(c,p,x,ground,motion,anim);
 }

 private static void water(Canvas c,Paint p,float x,float g,float m,float a){
  int n=2+(int)(m*4);
  for(int i=0;i<n;i++){
   float phase=(float)((a*2.2+i*.31)%1.0),side=(i%2==0?-1:1);
   float xx=sn(x+side*(9+i*4)+side*phase*10),yy=sn(g-4-phase*(8+i*2));
   int alpha=(int)((1-phase)*(55+80*m));
   rect(c,p,Color.argb(Math.max(6,alpha),173,211,210),xx-2,yy-2,xx+2,yy+2);
   if(i<2)rect(c,p,Color.argb(Math.max(4,alpha/2),220,234,229),xx,yy-2,xx+2,yy);
  }
  rect(c,p,Color.argb((int)(20+28*m),133,181,184),x-16,g-2,x+16,g);
 }

 private static void petals(Canvas c,Paint p,float x,float g,float m,float a){
  int[] col={Color.rgb(235,177,190),Color.rgb(226,205,137),Color.rgb(180,171,218)};
  int n=2+(int)(m*3);
  for(int i=0;i<n;i++){
   float phase=(float)((a*1.55+i*.23)%1.0),side=(i%2==0?-1:1);
   float xx=sn(x+side*(8+i*5)+side*phase*8),yy=sn(g-3-phase*(6+i));
   int alpha=(int)((1-phase)*(65+85*m));
   int base=col[i%col.length];
   rect(c,p,Color.argb(Math.max(8,alpha),Color.red(base),Color.green(base),Color.blue(base)),xx-3,yy-1,xx+3,yy+1);
  }
  rect(c,p,Color.argb((int)(18+24*m),72,112,63),x-12,g-1,x+12,g+1);
 }

 private static void moss(Canvas c,Paint p,float x,float g,float m,float a){
  int n=2+(int)(m*4);
  for(int i=0;i<n;i++){
   float phase=(float)((a*1.35+i*.19)%1.0),side=(i%2==0?-1:1);
   float xx=sn(x+side*(7+i*4)+side*phase*6),yy=sn(g-2-phase*(5+i*.8f));
   int alpha=(int)((1-phase)*(50+70*m));
   int rr=i%2==0?111:145,gg=i%2==0?137:151,bb=i%2==0?78:95;
   rect(c,p,Color.argb(Math.max(7,alpha),rr,gg,bb),xx-2,yy-2,xx+3,yy+2);
  }
  rect(c,p,Color.argb((int)(16+22*m),48,74,52),x-13,g-1,x+13,g+1);
 }

 private static void dust(Canvas c,Paint p,float x,float g,float m,float a){
  int n=1+(int)(m*4);
  for(int i=0;i<n;i++){
   float phase=(float)((a*1.7+i*.27)%1.0),side=(i%2==0?-1:1);
   float xx=sn(x+side*(8+i*4)+side*phase*7),yy=sn(g-2-phase*(4+i));
   int alpha=(int)((1-phase)*(35+65*m));
   rect(c,p,Color.argb(Math.max(5,alpha),169,148,106),xx-2,yy-1,xx+3,yy+2);
  }
 }

 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);
 }
}
