package com.aicharacter.v3;
import android.graphics.*;

/**
 * Presentation-only Haru renderer.
 * Uses authored pixel-art frames at one uniform scale. Biology may translate/rotate posture subtly,
 * but never changes body width/height or world truth.
 */
public final class HaruVisualRenderer{
 static final float BODY_SCALE=1.42f,ILLUSTRATED_SCALE=1.17f;
 static final int AUTHORED_FRAME_W=144,AUTHORED_FRAME_H=216;
 private HaruVisualRenderer(){}

 public static void drawReflection(Canvas c,Paint p,AssetManifest assets,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float contactGround,float anim,float strength,boolean water){
  if(strength<=.01f)return;
  if(useIllustratedRenderer()){
   float squash=water?.66f:.54f,wobble=water?(float)Math.sin(anim*.82f)*3.2f:(float)Math.sin(anim*.37f)*1.1f;
   String phase=s.environment==null?"DAY":s.environment.dayPhase(s.worldMinutes),area="";
   if(s.world!=null){WorldArea a=s.world.areaAt(s.haruX);if(a!=null)area=a.id==null?"":a.id;}
   c.save();c.clipRect(x-138,contactGround-1,x+138,1080);c.translate(wobble,0);c.scale(ILLUSTRATED_SCALE,-squash*ILLUSTRATED_SCALE,x,contactGround);
   try{HaruIllustratedRenderer.draw(c,p,s,v,x,bodyGround,anim,(int)Math.max(10,Math.min(62,62*strength)),0f,0f,phase,area);}catch(Throwable ignored){}
   c.restore();
   p.setStyle(Paint.Style.FILL);int bands=water?7:4;
   for(int i=0;i<bands;i++){float y=contactGround+10+i*(water?17f:13f),phaseWave=(float)Math.sin(anim*(.65f+i*.03f)+i*.9f),ww=(water?52:38)+(i%3)*18f;p.setColor(Color.argb((int)Math.max(3,(water?15:8)*strength),water?184:145,water?211:162,water?211:153));c.drawRoundRect(x-ww+phaseWave*9,y,x+ww+phaseWave*9,y+(water?2.2f:1.5f),1,1,p);}
   return;
  }
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
 public static void draw(Canvas c,Paint p,AssetManifest assets,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float contactGround,float anim){draw(c,p,assets,s,v,x,bodyGround,contactGround,anim,1f,0f);}
 public static void draw(Canvas c,Paint p,AssetManifest assets,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float contactGround,float anim,float cameraZoom){draw(c,p,assets,s,v,x,bodyGround,contactGround,anim,cameraZoom,0f);}
 public static void draw(Canvas c,Paint p,AssetManifest assets,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float contactGround,float anim,float cameraZoom,float divinePresence){
  float lift=Math.max(0,contactGround-bodyGround),shadowScale=shadowScale(lift),closeT=Math.max(0f,Math.min(1f,(cameraZoom-1f)/.56f));
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
  drawLayeredContactShadow(c,p,x,contactGround,shadowW,shadowDx,shadowAlpha,closeT);
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

  float postureX=0,postureY=0,rotation=0;HaruMotionStyleEngine.Style motion=HaruMotionStyleEngine.derive(s,v,anim);postureX+=motion.translateX;postureY+=motion.translateY;rotation+=motion.rotationDeg;
  BodyRigState rig=s.bodyRig;
  if(rig!=null){postureX+=(float)Math.max(-5,Math.min(5,rig.spineLean*18.0+rig.pelvisTilt*7.0));rotation+=(float)Math.max(-2.6,Math.min(2.6,rig.spineLean*11.0));}
  BiologyVisualOutput bio=BiologyVisualOutput.from(s);
  float breath=(float)Math.sin(anim*(1.35+bio.breathingIntensity*2.2))*(.35f+(float)bio.breathingIntensity*1.55f)*motion.breathScale;
  float thermalTremor=(float)Math.abs(bio.thermalDiscomfort)*.32f;
  postureY+=(float)Math.min(8.0,bio.postureLoad*4.5+bio.fatigue*2.0+bio.dominantPain*2.6+bio.recoveryLoad*.8)+breath+motion.settle*1.4f;
  postureX+=(float)Math.sin(anim*(17.0+bio.tremor*7.0))*Math.min(1.5f,(float)(bio.tremor*1.3+thermalTremor));
  rotation+=(float)Math.max(-1.3,Math.min(1.3,bio.gaitAsymmetry*1.05+Math.sin(anim*.55)*bio.dominantPain*.55));
  if(HaruExpressionEngine.supports(v.state)){HaruExpressionEngine.Visual expression=HaruExpressionEngine.derive(s,Math.max(s.lastSimulatedAt,s.lastOpenedAt));postureY+=(float)(expression.headDropPx*.42);postureX+=(float)(expression.gazeX*expression.intensity*.35);rotation+=(float)(expression.headTiltDeg*.18);}

  Rect src=new Rect(frame*fw,0,Math.min(sheet.getWidth(),(frame+1)*fw),fh);
  RectF dst=new RectF(left,top,left+fw*sc,top+fh*sc);
  if(closeT>.02f)drawCloseSubjectLight(c,p,s,dst,closeT,phase);
  if(divinePresence>.01f)drawDivineSubjectLight(c,p,s,dst,divinePresence,closeT,phase);
  int bodyAlpha=(int)Math.max(190,Math.min(255,190+65*s.environment.ambientBrightness));
  String area="";if(s.world!=null){WorldArea wa=s.world.areaAt(s.haruX);if(wa!=null)area=wa.id==null?"":wa.id;}
  if(useIllustratedRenderer()){
   c.save();
   c.translate(postureX,postureY);
   c.rotate(rotation,x,bodyGround);
   c.scale(ILLUSTRATED_SCALE,ILLUSTRATED_SCALE,x,bodyGround);
   if(v.flipX)c.scale(-1f,1f,x,bodyGround);
   try{HaruIllustratedRenderer.draw(c,p,s,v,x,bodyGround,anim,bodyAlpha,closeT,divinePresence,phase,area);c.restore();return;}
   catch(Throwable ignored){c.restore();p.setShader(null);p.setColorFilter(null);p.setAlpha(255);}
  }
  c.save();
  c.translate(postureX,postureY);
  c.rotate(rotation,x,bodyGround);
  if(v.flipX)c.scale(-1f,1f,x,bodyGround);
  if(closeT>.02f){int separator="NIGHT".equals(phase)?Color.rgb(21,29,42):Color.rgb(38,42,38);float oo=.75f+1.25f*closeT;p.setColorFilter(new PorterDuffColorFilter(separator,PorterDuff.Mode.SRC_IN));p.setAlpha((int)(18+34*closeT));c.drawBitmap(sheet,src,new RectF(dst.left-oo,dst.top,dst.right-oo,dst.bottom),p);c.drawBitmap(sheet,src,new RectF(dst.left+oo,dst.top,dst.right+oo,dst.bottom),p);c.drawBitmap(sheet,src,new RectF(dst.left,dst.top-oo,dst.right,dst.bottom-oo),p);c.drawBitmap(sheet,src,new RectF(dst.left,dst.top+oo,dst.right,dst.bottom+oo),p);p.setColorFilter(null);p.setAlpha(255);}

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
  if(closeT>.04f)drawClosePortraitGrade(c,p,s,sheet,src,dst,closeT,phase,area);
  if(divinePresence>.01f){p.setColorFilter(new PorterDuffColorFilter(Color.rgb(239,225,189),PorterDuff.Mode.SRC_ATOP));p.setAlpha((int)Math.min(42,8+30*Math.max(0,Math.min(1,divinePresence))));c.drawBitmap(sheet,src,dst,p);p.setColorFilter(null);p.setAlpha(255);}
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

 private static void drawDivineSubjectLight(Canvas c,Paint p,WorldState s,RectF dst,float divinePresence,float closeT,String phase){
  float d=Math.max(0f,Math.min(1f,divinePresence)),cx=dst.centerX(),faceY=dst.top+dst.height()*.235f,torsoY=dst.top+dst.height()*.48f;
  int warm="NIGHT".equals(phase)?Color.rgb(199,215,236):Color.rgb(246,226,184),cool="NIGHT".equals(phase)?Color.rgb(132,181,214):Color.rgb(179,214,207);
  p.setStyle(Paint.Style.FILL);p.setAntiAlias(true);
  float fr=dst.width()*(.52f+.08f*closeT);p.setShader(new RadialGradient(cx,faceY,fr,Color.argb((int)(18*d),Color.red(warm),Color.green(warm),Color.blue(warm)),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawCircle(cx,faceY,fr,p);p.setShader(null);
  float tr=dst.width()*(.78f+.10f*closeT);p.setShader(new RadialGradient(cx,torsoY,tr,Color.argb((int)(10*d),Color.red(cool),Color.green(cool),Color.blue(cool)),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawCircle(cx,torsoY,tr,p);p.setShader(null);p.setAntiAlias(false);
 }

 private static void drawLayeredContactShadow(Canvas c,Paint p,float x,float ground,float shadowW,float shadowDx,int alpha,float closeT){
  p.setStyle(Paint.Style.FILL);
  p.setColor(Color.argb(Math.max(7,alpha/4),0,0,0));c.drawOval(x-shadowW*1.14f+shadowDx,ground-10,x+shadowW*1.14f+shadowDx,ground+13,p);
  p.setColor(Color.argb(Math.max(10,(int)(alpha*(.62f+.18f*closeT))),0,0,0));c.drawOval(x-shadowW*.78f+shadowDx*.78f,ground-6,x+shadowW*.78f+shadowDx*.78f,ground+9,p);
  p.setColor(Color.argb(Math.max(12,(int)(alpha*(.82f+.12f*closeT))),4,7,7));c.drawOval(x-shadowW*.43f+shadowDx*.42f,ground-3.6f,x+shadowW*.43f+shadowDx*.42f,ground+5.5f,p);
 }

 private static void drawClosePortraitGrade(Canvas c,Paint p,WorldState s,Bitmap sheet,Rect src,RectF dst,float closeT,String phase,String area){
  int key;if("NIGHT".equals(phase))key=Color.rgb(156,188,228);else if("EVENING".equals(phase))key=Color.rgb(255,183,126);else if("MORNING".equals(phase))key=Color.rgb(255,220,171);else if("quiet_grove".equals(area))key=Color.rgb(188,217,176);else key=Color.rgb(222,226,201);
  double humidity=s.atmosphere==null?.55:Math.max(0,Math.min(1,s.atmosphere.relativeHumidity));boolean rain=s.environment!=null&&"RAIN".equals(s.environment.weather);if(rain){key=Color.rgb(184,209,218);}else if(humidity>.76&&!"NIGHT".equals(phase)){key=Color.rgb(207,222,205);}int lower="NIGHT".equals(phase)?Color.rgb(36,53,78):rain?Color.rgb(63,77,84):Color.rgb(84,78,66);
  c.save();c.clipRect(dst.left,dst.top,dst.right,dst.top+dst.height()*.58f);p.setColorFilter(new PorterDuffColorFilter(key,PorterDuff.Mode.SRC_ATOP));p.setAlpha((int)(8+18*closeT));c.drawBitmap(sheet,src,dst,p);c.restore();
  c.save();c.clipRect(dst.left,dst.top+dst.height()*.54f,dst.right,dst.bottom);p.setColorFilter(new PorterDuffColorFilter(lower,PorterDuff.Mode.SRC_ATOP));p.setAlpha((int)(5+10*closeT));c.drawBitmap(sheet,src,dst,p);c.restore();
  p.setColorFilter(null);p.setAlpha(255);
 }

 private static void drawCloseSubjectLight(Canvas c,Paint p,WorldState s,RectF dst,float closeT,String phase){
  String area="";if(s.world!=null){WorldArea a=s.world.areaAt(s.haruX);if(a!=null)area=a.id==null?"":a.id;}
  int color;if("NIGHT".equals(phase))color=Color.rgb(128,166,213);else if("EVENING".equals(phase))color=Color.rgb(246,164,108);else if("MORNING".equals(phase))color=Color.rgb(247,211,158);else if("quiet_grove".equals(area))color=Color.rgb(171,205,168);else if("garden_path".equals(area))color=Color.rgb(220,226,178);else color=Color.rgb(169,208,218);
  float cx=dst.centerX(),cy=dst.top+dst.height()*.40f,r=dst.height()*(.43f+.05f*closeT);int alpha=(int)(7+15*closeT);
  p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(cx,cy,r,Color.argb(alpha,Color.red(color),Color.green(color),Color.blue(color)),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.save();c.scale(.72f,1f,cx,cy);c.drawCircle(cx,cy,r,p);c.restore();p.setShader(null);
 }

 private static boolean useIllustratedRenderer(){return true;}
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
