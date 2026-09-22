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
  p.setColor(Color.argb((int)Math.max(18,58-lift*.16f),0,0,0));
  c.drawOval(x-47*shadowScale,contactGround-8,x+47*shadowScale,contactGround+10,p);

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
  BiologyVisualOutput bio=BiologyVisualOutput.from(s);
  float breath=(float)Math.sin(anim*(1.35+bio.breathingIntensity*2.2))*(.35f+(float)bio.breathingIntensity*1.55f);
  float thermalTremor=(float)Math.abs(bio.thermalDiscomfort)*.32f;
  postureY+=(float)Math.min(8.0,bio.postureLoad*4.5+bio.fatigue*2.0+bio.dominantPain*2.6+bio.recoveryLoad*.8)+breath;
  postureX+=(float)Math.sin(anim*(17.0+bio.tremor*7.0))*Math.min(1.5f,(float)(bio.tremor*1.3+thermalTremor));
  rotation+=(float)Math.max(-1.3,Math.min(1.3,bio.gaitAsymmetry*1.05+Math.sin(anim*.55)*bio.dominantPain*.55));

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
