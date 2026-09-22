package com.aicharacter.v3;
import android.graphics.*;

/**
 * Presentation-only Haru renderer.
 * Uses authored pixel-art frames at one uniform scale. Biology may translate/rotate posture subtly,
 * but never changes body width/height or world truth.
 */
public final class HaruVisualRenderer{
 static final float BODY_SCALE=1.42f;
 static final int AUTHORED_FRAME_W=144,AUTHORED_FRAME_H=216;
 private HaruVisualRenderer(){}

 public static void draw(Canvas c,Paint p,AssetManifest assets,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float contactGround,float anim){
  float lift=Math.max(0,contactGround-bodyGround),shadowScale=shadowScale(lift);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  String phase=s.environment==null?"DAY":s.environment.dayPhase(s.worldMinutes);
  float shadowDir="MORNING".equals(phase)?1f:"EVENING".equals(phase)?-1f:0f;
  float shadowLen=("MORNING".equals(phase)||"EVENING".equals(phase))?1.62f:"NIGHT".equals(phase)?.82f:1.08f;
  float shadowDx=shadowDir*33f*shadowScale,shadowW=47f*shadowScale*shadowLen;
  int shadowAlpha=(int)Math.max(14,("NIGHT".equals(phase)?38:56)-lift*.16f);
  p.setColor(Color.argb(shadowAlpha,0,0,0));
  c.drawOval(x-shadowW+shadowDx,contactGround-7,x+shadowW+shadowDx,contactGround+10,p);
  p.setColor(Color.argb(Math.max(8,shadowAlpha/3),18,26,24));
  c.drawOval(x-shadowW*.72f+shadowDx*.75f,contactGround-4,x+shadowW*.72f+shadowDx*.75f,contactGround+7,p);

  Bitmap sheet=assets.get(v.asset);
  if(sheet==null)sheet=assets.get("girl_idle_right");
  if(sheet==null)return;
  p.setFilterBitmap(false);

  int frames=Math.max(1,v.frames),fw=Math.max(1,sheet.getWidth()/frames),fh=sheet.getHeight();
  int frame=frameIndex(s,v,anim,frames);
  float sc=BODY_SCALE,ax=v.anchorX*fw*sc;
  float left=x-ax,top=GirlAnimationController.renderTop(bodyGround,fh,sc,v.anchorY);

  float postureX=0,postureY=0,rotation=0;
  BodyRigState rig=s.bodyRig;
  if(rig!=null){postureX+=(float)Math.max(-5,Math.min(5,rig.spineLean*18.0+rig.pelvisTilt*7.0));rotation+=(float)Math.max(-2.6,Math.min(2.6,rig.spineLean*11.0));}
  BodyInstinctState bi=s.bodyInstinct;
  if(bi!=null){
   double respiratory=s.respiration==null?0:s.respiration.breathingLoad;
   double oxygen=s.respiration==null?1:s.respiration.oxygenSaturation;
   double pain=s.localizedPain==null?0:s.localizedPain.maxLoad();
   postureY+=(float)Math.min(8,bi.fatigueDroop*4.2+pain*3.4+Math.max(0,.93-oxygen)*8.0);
   postureY+=(float)Math.sin(anim*(1.55+bi.breathDrive*1.4+respiratory*.9))*(.45f+(float)Math.min(1.2,bi.breathDrive+respiratory));
   postureX+=(float)Math.sin(anim*19.0)*Math.min(1.4f,(float)bi.shiver*1.25f);
   rotation+=(float)Math.sin(anim*.55)*Math.min(1.2f,(float)pain*.7f);
  }

  Rect src=new Rect(frame*fw,0,Math.min(sheet.getWidth(),(frame+1)*fw),fh);
  RectF dst=new RectF(left,top,left+fw*sc,top+fh*sc);
  c.save();
  c.translate(postureX,postureY);
  c.rotate(rotation,x,bodyGround);
  p.setAlpha((int)Math.max(190,Math.min(255,190+65*s.environment.ambientBrightness)));
  c.drawBitmap(sheet,src,dst,p);
  p.setAlpha(255);
  c.restore();
 }

 static int frameIndex(WorldState s,GirlAnimationController.Visual v,float anim,int frames){
  if(frames<=1)return 0;
  if(v.isWalk()&&s.bodyRig!=null){
   int f=(int)Math.floor(s.bodyRig.stridePhase*frames);
   f%=frames;if(f<0)f+=frames;return f;
  }
  int f=(int)(anim*Math.max(.1f,v.fps))%frames;return f<0?f+frames:f;
 }
 static float shadowScale(float lift){return Math.max(.55f,1f-Math.max(0,lift)/260f);}
 static float fixedBodyScale(){return BODY_SCALE;}
 static int authoredFrameWidth(){return AUTHORED_FRAME_W;}
 static int authoredFrameHeight(){return AUTHORED_FRAME_H;}
}
