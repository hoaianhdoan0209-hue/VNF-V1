package com.aicharacter.v3;

import android.graphics.*;

/** Pixel-native Veilroot landmark renderer for Grove. */
public final class PixelVeilrootRenderer {
 private static final float G=2f;
 private PixelVeilrootRenderer(){}

 public static boolean supports(WorldObject o){return o!=null&&"lake_tree_03".equals(o.id);}

 public static boolean draw(Canvas c,Paint p,WorldState s,WorldObject o,FloraLifeState f,float screenX,float anim){
  if(!supports(o)||c==null||p==null)return false;
  float x=sn(screenX),y=sn(o.y);
  String phase=f==null||f.cycle==null?"QUIET":f.cycle.phase;
  float open="WEAK".equals(phase)?.48f:"RECOVER".equals(phase)?.70f:"GROW".equals(phase)?1.08f:1f;
  float motion="WEAK".equals(phase)?.30f:"RECOVER".equals(phase)?.52f:1f;
  float sway=f==null?0:sn((float)Math.sin(anim*(.38+f.body.rhythm*1.3)+(o.id.hashCode()&31))*(float)(4+14*f.swayDrive)*motion);
  float pulse=f==null?0:(float)(.5+.5*Math.sin(anim*(.7+f.growthPulse*1.8)));

  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);

  // Root base.
  int barkDark=Color.rgb(43,54,50),bark=Color.rgb(64,78,70),barkHi=Color.rgb(87,103,88);
  rect(c,p,barkDark,x-13*G,y-7*G,x+14*G,y+G);
  rect(c,p,bark,x-9*G,y-11*G,x+10*G,y-3*G);
  rect(c,p,barkHi,x-2*G,y-13*G,x+4*G,y-5*G);

  // Stepped trunk instead of a bezier.
  float[] cx={0,-1,-2,-1,1,2,3,3};
  for(int i=0;i<8;i++){
   float yy=y-(12+i*12)*G,off=cx[i]*G+sway*(i/8f)*.55f,w=(5.5f-i*.38f)*G;
   rect(c,p,barkDark,x+off-w-2,yy-13*G,x+off+w+2,yy+3);
   rect(c,p,bark,x+off-w,yy-12*G,x+off+w,yy+2);
   rect(c,p,barkHi,x+off-w*.45f,yy-11*G,x+off-w*.15f,yy+G);
  }

  // Chunky branch arms and leaf pads.
  for(int i=-2;i<=2;i++){
   float by=y-(59-i*13)*G,dir=(i&1)==0?-1f:1f,len=(18+Math.abs(i)*4)*G*open;
   float trunkX=x+sway*.28f;
   float bx=sn(trunkX+dir*len),mid=sn(trunkX+dir*len*.52f);
   rect(c,p,barkDark,Math.min(trunkX,mid)-2*G,by-2*G,Math.max(trunkX,mid)+2*G,by+2*G);
   rect(c,p,bark,Math.min(mid,bx)-2*G,by-8*G,Math.max(mid,bx)+2*G,by-4*G);

   int leafDark=Color.rgb(62,91,72),leaf=Color.rgb(93,125,91),leafHi=Color.rgb(123,149,103);
   float padW=(7+Math.abs(i))*G,padH=(8+Math.abs(i))*G;
   rect(c,p,leafDark,bx-padW,by-padH-7*G,bx+padW,by+2*G);
   rect(c,p,leaf,bx-padW+2*G,by-padH-5*G,bx+padW-2*G,by);
   rect(c,p,leafHi,bx-padW*.35f,by-padH-3*G,bx+padW*.30f,by-padH+G);
  }

  // Crown tiers.
  float top=y-111*G+sway;
  int crownDark=Color.rgb(54,82,66),crown=Color.rgb(82,116,84),crownHi=Color.rgb(112,142,96);
  for(int tier=0;tier<4;tier++){
   float ww=(15-tier*2)*G*open,yy=top+tier*8*G;
   rect(c,p,crownDark,x-ww+sway*.20f,yy-5*G,x+ww+sway*.20f,yy+6*G);
   rect(c,p,crown,x-ww+2*G+sway*.20f,yy-3*G,x+ww-2*G+sway*.20f,yy+4*G);
   if(tier<3)rect(c,p,crownHi,x-ww*.35f+sway*.20f,yy-2*G,x+ww*.18f+sway*.20f,yy+G);
  }

  // Moisture/lumen pulses as square motes.
  if(f!=null){
   int a=(int)(60+120*pulse);
   for(int i=0;i<6;i++){
    float yy=y-(38+i*14)*G,xx=x+sn((float)Math.sin(i*1.55)*4*G)+sway*(i/6f);
    rect(c,p,Color.argb(a,184,119,148),xx-G,yy-G,xx+G,yy+G);
    if(f.sense!=null&&f.sense.moistureSense>.58&&i%2==0)
      rect(c,p,Color.argb((int)(55+95*f.sense.moistureSense),183,206,174),xx+2*G,yy-2*G,xx+3*G,yy-G);
   }
  }
  return true;
 }

 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){
  p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);
 }
}
