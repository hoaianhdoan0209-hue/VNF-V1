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

 public static void drawReflection(Canvas c,Paint p,AssetManifest assets,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float contactGround,float anim,float strength,boolean water){
  if(strength<=.01f)return;
  Bitmap sheet=assets.get(v.asset);if(sheet==null)sheet=assets.get("girl_idle_right");if(sheet==null)return;
  int frames=Math.max(1,v.frames),fw=Math.max(1,sheet.getWidth()/frames),fh=sheet.getHeight(),frame=frameIndex(s,v,anim,frames);
  float sc=BODY_SCALE,ax=v.anchorX*fw*sc,left=x-ax,top=GirlAnimationController.renderTop(bodyGround,fh,sc,v.anchorY);
  Rect src=new Rect(frame*fw,0,Math.min(sheet.getWidth(),(frame+1)*fw),fh);
  RectF dst=new RectF(left,top,left+fw*sc,top+fh*sc);
  float squash=water?.66f:.54f,wobble=water?(float)Math.sin(anim*.82f)*3.2f:(float)Math.sin(anim*.37f)*1.1f;
  String phase=s.environment==null?"DAY":s.environment.dayPhase(s.worldMinutes);
  int tint=water?("NIGHT".equals(phase)?Color.rgb(95,132,168):Color.rgb(128,168,172)):Color.rgb(111,128,119);
  p.setFilterBitmap(false);p.setShader(null);p.setStyle(Paint.Style.FILL);
  c.save();
  c.clipRect(x-fw*sc*.72f,contactGround-1,x+fw*sc*.72f,1080);
  c.translate(wobble,0);
  c.scale(1f,-squash,x,contactGround);
  p.setColorFilter(new PorterDuffColorFilter(tint,PorterDuff.Mode.SRC_ATOP));
  p.setAlpha((int)Math.max(8,Math.min(80,(water?62:38)*strength)));
  c.drawBitmap(sheet,src,dst,p);
  p.setColorFilter(null);p.setAlpha(255);
  c.restore();
  // Horizontal breakup prevents a mirror-perfect copy and visually binds it to the surface.
  p.setStyle(Paint.Style.FILL);
  int bands=water?7:4;
  for(int i=0;i<bands;i++){float y=contactGround+10+i*(water?17f:13f),phaseWave=(float)Math.sin(anim*(.65f+i*.03f)+i*.9f),ww=(water?52:38)+(i%3)*18f;p.setColor(Color.argb((int)Math.max(3,(water?15:8)*strength),water?184:145,water?211:162,water?211:153));c.drawRoundRect(x-ww+phaseWave*9,y,x+ww+phaseWave*9,y+(water?2.2f:1.5f),1,1,p);}
 }
 public static void draw(Canvas c,Paint p,AssetManifest assets,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float contactGround,float anim){
  float lift=Math.max(0,contactGround-bodyGround),shadowScale=shadowScale(lift);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  String phase=s.environment==null?"DAY":s.environment.dayPhase(s.worldMinutes);
  float dayT=(float)Math.max(0,Math.min(1,(s.worldMinutes-360.0)/(14.0*60.0)));
  float sunX=120f+2160f*dayT,sunAlt=(float)Math.max(0,Math.sin(dayT*Math.PI));
  float shadowDir="NIGHT".equals(phase)?0f:(sunX<1200f?1f:-1f);
  float shadowLen="NIGHT".equals(phase)?.82f:.98f+(1f-sunAlt)*.74f;
  float shadowDx=shadowDir*33f*shadowScale,shadowW=47f*shadowScale*shadowLen;
  float cloud=s.environment==null?0f:(float)Math.max(0,Math.min(1,s.environment.cloudCover));
  float rainFactor=s.environment!=null&&"RAIN".equals(s.environment.weather)?.62f:1f;
  int shadowAlpha=(int)Math.max(10,(("NIGHT".equals(phase)?38:56)-lift*.16f)*(1f-cloud*.38f)*rainFactor);
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
  BiologyVisualOutput bio=BiologyVisualOutput.from(s);
  float breath=(float)Math.sin(anim*(1.35+bio.breathingIntensity*2.2))*(.35f+(float)bio.breathingIntensity*1.55f);
  float thermalTremor=(float)Math.abs(bio.thermalDiscomfort)*.32f;
  postureY+=(float)Math.min(8.0,bio.postureLoad*4.5+bio.fatigue*2.0+bio.dominantPain*2.6+bio.recoveryLoad*.8)+breath;
  postureX+=(float)Math.sin(anim*(17.0+bio.tremor*7.0))*Math.min(1.5f,(float)(bio.tremor*1.3+thermalTremor));
  rotation+=(float)Math.max(-1.3,Math.min(1.3,bio.gaitAsymmetry*1.05+Math.sin(anim*.55)*bio.dominantPain*.55));
  if(HaruExpressionEngine.supports(v.state)){HaruExpressionEngine.Visual expression=HaruExpressionEngine.derive(s,Math.max(s.lastSimulatedAt,s.lastOpenedAt));postureY+=(float)(expression.headDropPx*.42);postureX+=(float)(expression.gazeX*expression.intensity*.35);rotation+=(float)(expression.headTiltDeg*.18);}

  Rect src=new Rect(frame*fw,0,Math.min(sheet.getWidth(),(frame+1)*fw),fh);
  RectF dst=new RectF(left,top,left+fw*sc,top+fh*sc);
  c.save();
  c.translate(postureX,postureY);
  c.rotate(rotation,x,bodyGround);
  int bodyAlpha=(int)Math.max(190,Math.min(255,190+65*s.environment.ambientBrightness));
  String area="";if(s.world!=null){WorldArea wa=s.world.areaAt(s.haruX);if(wa!=null)area=wa.id==null?"":wa.id;}
  int rimColor;if("NIGHT".equals(phase))rimColor=Color.rgb(152,188,232);else if("EVENING".equals(phase))rimColor=Color.rgb(255,166,105);else if("MORNING".equals(phase))rimColor=Color.rgb(255,214,158);else if("home_shelter".equals(area))rimColor=Color.rgb(235,216,172);else if("garden_path".equals(area))rimColor=Color.rgb(226,232,184);else if("quiet_grove".equals(area))rimColor=Color.rgb(184,211,168);else rimColor=Color.rgb(188,218,225);
  float rimDx="MORNING".equals(phase)?-3.5f:"EVENING".equals(phase)?3.5f:"NIGHT".equals(phase)?1.5f:-1.5f;
  float rimDy="NIGHT".equals(phase)?-2.5f:-1f;
  RectF rimDst=new RectF(dst.left+rimDx,dst.top+rimDy,dst.right+rimDx,dst.bottom+rimDy);
  p.setColorFilter(new PorterDuffColorFilter(rimColor,PorterDuff.Mode.SRC_IN));
  p.setAlpha("NIGHT".equals(phase)?42:34);
  c.drawBitmap(sheet,src,rimDst,p);
  p.setColorFilter(null);
  p.setAlpha(bodyAlpha);
  c.drawBitmap(sheet,src,dst,p);
  int localColor=Color.TRANSPARENT,localAlpha=0;
  if("home_shelter".equals(area)){
   WorldObject shelter=s.world==null?null:s.world.object("shelter_01");
   float dist=shelter==null?260f:Math.abs(s.haruX-shelter.x),near=Math.max(0f,1f-dist/520f);
   if(near>0){localColor=Color.rgb(255,184,103);localAlpha=(int)((14+("NIGHT".equals(phase)?38:"EVENING".equals(phase)?28:14))*near);}
  }else if("garden_path".equals(area)){localColor=Color.rgb(219,232,181);localAlpha=14;}
  else if("quiet_grove".equals(area)){localColor=Color.rgb(160,204,164);localAlpha=18;}
  else{localColor=Color.rgb(151,202,216);localAlpha=20;}
  if(s.environment!=null&&"RAIN".equals(s.environment.weather)){localColor=Color.rgb(137,176,197);localAlpha=Math.max(localAlpha,20);}
  if(localAlpha>0){p.setColorFilter(new PorterDuffColorFilter(localColor,PorterDuff.Mode.SRC_ATOP));p.setAlpha(Math.min(52,localAlpha));c.drawBitmap(sheet,src,dst,p);p.setColorFilter(null);}
  p.setAlpha(255);
  HaruFacialOverlayRenderer.draw(c,p,s,v,dst);
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
