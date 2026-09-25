package com.aicharacter.v3;

import android.graphics.*;

/**
 * Authored block-pixel cat presentation.
 * Reads persisted cat/social/physics state only; never mutates simulation.
 */
public final class CatPixelSpriteRenderer {
 private static final float PX=3f;
 private static final int OUT=Color.rgb(43,35,35);
 private static final int CREAM=Color.rgb(238,221,190),CREAM_SHADE=Color.rgb(205,181,151);
 private static final int ORANGE=Color.rgb(196,116,72),ORANGE_HI=Color.rgb(229,155,96);
 private static final int DARK=Color.rgb(83,65,63),DARK_HI=Color.rgb(122,92,85);
 private static final int PINK=Color.rgb(213,137,137),EYE=Color.rgb(215,202,104),PUPIL=Color.rgb(45,39,38);
 private CatPixelSpriteRenderer(){}

 public static boolean draw(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float x,float ground,float anim,float cameraZoom,float divinePresence){
  if(c==null||p==null||s==null||v==null||v.state==CatAnimationController.State.ATTACHED)return false;
  x=PixelArtRenderPolicy.snapLogical(x);ground=PixelArtRenderPolicy.snapLogical(ground);
  int frame=frameIndex(s,v,anim);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);p.setColorFilter(null);p.setAlpha(255);
  drawShadow(c,p,x,ground,v);
  c.save();c.scale(v.facingRight?1f:-1f,1f,x,ground);
  switch(v.state){
   case SLEEP:drawSleep(c,p,x,ground,frame,divinePresence);break;
   case SETTLE:drawSettle(c,p,x,ground,frame,divinePresence);break;
   case BRACE:drawBrace(c,p,x,ground,frame,divinePresence);break;
   default:drawStanding(c,p,s,v,x,ground,frame,divinePresence);break;
  }
  c.restore();return true;
 }

 public static boolean drawAttached(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float haruX,float haruGround,float anim,float cameraZoom,float divinePresence){
  if(c==null||p==null||v==null||v.state!=CatAnimationController.State.ATTACHED)return false;
  float x=PixelArtRenderPolicy.snapLogical(haruX+44f),g=PixelArtRenderPolicy.snapLogical(haruGround-118f);
  int frame=((int)Math.floor(anim*2.0f))&7;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);p.setAlpha(255);
  c.save();c.scale(v.facingRight?1f:-1f,1f,x,g);
  // compact carried pose
  body(c,p,x,g-7*PX,10*PX,6*PX);
  head(c,p,x+6*PX,g-12*PX,frame,v.attention,v.guardedness,divinePresence);
  rect(c,p,OUT,x-5*PX,g-4*PX,x+6*PX,g-PX);
  rect(c,p,CREAM,x-4*PX,g-4*PX,x+5*PX,g-2*PX);
  tail(c,p,x-5*PX,g-7*PX,frame,CatAnimationController.State.ATTACHED,(float)v.attention);
  c.restore();return true;
 }

 static int frameIndex(WorldState s,CatAnimationController.Visual v,float anim){
  if(v==null)return 0;
  if(v.moving()&&s!=null&&s.catRig!=null){
   double phase=s.catRig.gaitPhase;if(Double.isFinite(phase)){int f=(int)Math.floor((phase-Math.floor(phase))*8.0);return Math.max(0,Math.min(7,f));}
  }
  float fps=v.moving()?8.0f:v.state==CatAnimationController.State.RUB?6.0f:2.5f;
  int f=(int)Math.floor(anim*fps)%8;return f<0?f+8:f;
 }

 private static void drawStanding(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float x,float g,int frame,float divine){
  boolean moving=v.moving();
  float step=moving?PixelMotionCadence.stride(frame)*PX:0,bob=moving?PixelMotionCadence.lift(frame)*PX:0;
  if(v.state==CatAnimationController.State.RUB)bob=((frame==2||frame==3)?-PX:0);
  float bodyY=g-9*PX+bob;
  float crouch=v.state==CatAnimationController.State.RETREAT?2*PX:v.state==CatAnimationController.State.APPROACH?PX:0;
  bodyY+=crouch;
  float secondary=moving?PixelMotionCadence.secondary(frame)*PX:0;
  float headX=x+8*PX+secondary*.22f+(v.state==CatAnimationController.State.RUB?(frame<4?2*PX:PX):0);
  float headY=bodyY-4*PX+(v.state==CatAnimationController.State.RETREAT?PX:0);

  tail(c,p,x-8*PX-secondary*.12f,bodyY,frame,v.state,(float)v.attention);
  float compression=moving?PixelMotionCadence.compression(frame):0;
  body(c,p,x,bodyY,(17f+compression*.75f)*PX,(8f-compression*.45f)*PX);
  // coat patches make the sprite readable even at small size
  rect(c,p,ORANGE,x-6*PX,bodyY-3*PX,x-1*PX,bodyY+PX);
  rect(c,p,DARK,x+2*PX,bodyY-3*PX,x+6*PX,bodyY+2*PX);
  rect(c,p,ORANGE_HI,x-5*PX,bodyY-3*PX,x-3*PX,bodyY-2*PX);

  legs(c,p,x,bodyY,g,step,moving,v.state);
  head(c,p,headX,headY,frame,v.attention,v.guardedness,divine);

  if(v.state==CatAnimationController.State.RUB){
   rect(c,p,CREAM,x+10*PX,headY+3*PX,x+13*PX,headY+5*PX);
   rect(c,p,PINK,x+12*PX,headY+3*PX,x+13*PX,headY+4*PX);
  }
  if(v.state==CatAnimationController.State.WATCH){
   // one raised forepaw on attentive frames
   if(frame==2||frame==3){rect(c,p,OUT,x+6*PX,g-7*PX,x+9*PX,g-2*PX);rect(c,p,CREAM,x+7*PX,g-6*PX,x+8*PX,g-3*PX);}
  }
 }

 private static void body(Canvas c,Paint p,float x,float y,float w,float h){
  // dark outline and stepped back/belly
  rect(c,p,OUT,x-w*.50f,y-h*.55f,x+w*.48f,y+h*.50f);
  rect(c,p,OUT,x-w*.42f,y-h*.75f,x+w*.30f,y+h*.62f);
  rect(c,p,CREAM,x-w*.43f,y-h*.45f,x+w*.42f,y+h*.38f);
  rect(c,p,CREAM,x-w*.34f,y-h*.62f,x+w*.26f,y+h*.46f);
  rect(c,p,CREAM_SHADE,x-w*.38f,y+h*.20f,x+w*.38f,y+h*.38f);
 }

 private static void head(Canvas c,Paint p,float x,float y,int frame,double attention,double guarded,double divine){
  // ears as stair-step pixel triangles
  rect(c,p,OUT,x-5*PX,y-7*PX,x-2*PX,y-2*PX);rect(c,p,OUT,x+2*PX,y-7*PX,x+5*PX,y-2*PX);
  rect(c,p,OUT,x-4*PX,y-9*PX,x-2*PX,y-6*PX);rect(c,p,OUT,x+2*PX,y-9*PX,x+4*PX,y-6*PX);
  rect(c,p,PINK,x-3*PX,y-7*PX,x-2*PX,y-5*PX);rect(c,p,PINK,x+2*PX,y-7*PX,x+3*PX,y-5*PX);
  // face outline
  rect(c,p,OUT,x-6*PX,y-5*PX,x+6*PX,y+5*PX);
  rect(c,p,OUT,x-5*PX,y-6*PX,x+5*PX,y+6*PX);
  rect(c,p,CREAM,x-5*PX,y-4*PX,x+5*PX,y+4*PX);
  rect(c,p,CREAM,x-4*PX,y-5*PX,x+4*PX,y+5*PX);
  // calico patches
  rect(c,p,ORANGE,x-5*PX,y-5*PX,x-PX,y-1*PX);
  rect(c,p,DARK,x+2*PX,y-5*PX,x+5*PX,y-PX);
  rect(c,p,ORANGE_HI,x-4*PX,y-4*PX,x-2*PX,y-3*PX);
  // blink / guarded squint
  boolean blink=frame==6||guarded>.82;
  if(blink){
   rect(c,p,PUPIL,x-3*PX,y,x-PX,y+PX);rect(c,p,PUPIL,x+2*PX,y,x+4*PX,y+PX);
  }else{
   rect(c,p,EYE,x-3*PX,y-PX,x-PX,y+PX);rect(c,p,EYE,x+2*PX,y-PX,x+4*PX,y+PX);
   float pupil=(float)Math.max(1,attention>.55?2:1)*PX;
   rect(c,p,PUPIL,x-2.5f*PX,y-PX*.5f,x-2.5f*PX+PX,y-PX*.5f+pupil);
   rect(c,p,PUPIL,x+2.5f*PX,y-PX*.5f,x+2.5f*PX+PX,y-PX*.5f+pupil);
  }
  rect(c,p,PINK,x-.7f*PX,y+2*PX,x+.7f*PX,y+3*PX);
  rect(c,p,PUPIL,x,y+3*PX,x+PX,y+4*PX);
  if(divine>.05){int a=(int)(35+90*Math.min(1,divine));p.setColor(Color.argb(a,246,226,174));c.drawRect(x-6*PX,y-7*PX,x-5*PX,y-6*PX,p);c.drawRect(x+6*PX,y-4*PX,x+7*PX,y-3*PX,p);}
 }

 private static void legs(Canvas c,Paint p,float x,float bodyY,float g,float step,boolean moving,CatAnimationController.State state){
  float front=x+6*PX,hind=x-5*PX;
  if(state==CatAnimationController.State.RETREAT){front-=PX;hind-=PX;}
  float a=moving?step:0,b=moving?-step:0;
  leg(c,p,front,bodyY+2*PX,g,a,CREAM);
  leg(c,p,front-2*PX,bodyY+2*PX,g,-a*.55f,CREAM_SHADE);
  leg(c,p,hind,bodyY+2*PX,g,b,CREAM);
  leg(c,p,hind-2*PX,bodyY+2*PX,g,-b*.55f,CREAM_SHADE);
 }
 private static void leg(Canvas c,Paint p,float x,float y,float g,float shift,int fill){
  float foot=x+shift;
  rect(c,p,OUT,x-PX,y,x+PX,g-2*PX);
  rect(c,p,fill,x-.5f*PX,y+PX,x+.5f*PX,g-2*PX);
  rect(c,p,OUT,foot-PX,g-2*PX,foot+2*PX,g);
  rect(c,p,CREAM,foot-.5f*PX,g-1.6f*PX,foot+1.5f*PX,g-.4f*PX);
 }

 private static void tail(Canvas c,Paint p,float x,float y,int frame,CatAnimationController.State state,float attention){
  int sway[]={0,1,2,2,1,0,-1,-1};float s=sway[frame]*PX;
  boolean up=state==CatAnimationController.State.APPROACH||state==CatAnimationController.State.RUB||state==CatAnimationController.State.ATTACHED||attention>.62f;
  if(state==CatAnimationController.State.RETREAT){
   seg(c,p,x,y,x-4*PX,y+PX);seg(c,p,x-4*PX,y+PX,x-8*PX+s,y+2*PX);seg(c,p,x-8*PX+s,y+2*PX,x-11*PX+s,y+PX);return;
  }
  if(up){
   seg(c,p,x,y,x-4*PX,y-2*PX);seg(c,p,x-4*PX,y-2*PX,x-5*PX+s,y-7*PX);seg(c,p,x-5*PX+s,y-7*PX,x-3*PX+s,y-10*PX);return;
  }
  seg(c,p,x,y,x-4*PX,y-PX);seg(c,p,x-4*PX,y-PX,x-8*PX+s,y-3*PX);seg(c,p,x-8*PX+s,y-3*PX,x-11*PX+s,y-PX);
 }
 private static void seg(Canvas c,Paint p,float x1,float y1,float x2,float y2){
  int steps=Math.max(1,(int)(Math.max(Math.abs(x2-x1),Math.abs(y2-y1))/PX));
  for(int i=0;i<=steps;i++){float t=i/(float)steps,x=x1+(x2-x1)*t,y=y1+(y2-y1)*t;rect(c,p,OUT,x-PX,y-PX,x+PX,y+PX);rect(c,p,DARK,x-.5f*PX,y-.5f*PX,x+.5f*PX,y+.5f*PX);}
 }

 private static void drawSettle(Canvas c,Paint p,float x,float g,int frame,double divine){
  float breath=(frame==2||frame==3)?PX:0,y=g-5*PX-breath;
  tail(c,p,x-7*PX,y,frame,CatAnimationController.State.SETTLE,.3f);
  rect(c,p,OUT,x-9*PX,y-5*PX,x+8*PX,y+2*PX);rect(c,p,CREAM,x-8*PX,y-4*PX,x+7*PX,y+PX);
  rect(c,p,ORANGE,x-6*PX,y-4*PX,x-2*PX,y);rect(c,p,DARK,x+2*PX,y-4*PX,x+6*PX,y);
  head(c,p,x+6*PX,y-6*PX,frame,.35,.15,divine);
  rect(c,p,OUT,x-5*PX,g-2*PX,x+7*PX,g);rect(c,p,CREAM,x-4*PX,g-1.7f*PX,x+6*PX,g-.4f*PX);
 }

 private static void drawSleep(Canvas c,Paint p,float x,float g,int frame,double divine){
  float breath=(frame==2||frame==3||frame==4)?PX:0,y=g-4*PX-breath;
  // curled 16-bit style sleeping silhouette
  rect(c,p,OUT,x-10*PX,y-6*PX,x+9*PX,y+2*PX);rect(c,p,OUT,x-8*PX,y-8*PX,x+7*PX,y+3*PX);
  rect(c,p,CREAM,x-8*PX,y-5*PX,x+7*PX,y+PX);rect(c,p,CREAM,x-6*PX,y-7*PX,x+5*PX,y+2*PX);
  rect(c,p,ORANGE,x-5*PX,y-7*PX,x,y-3*PX);rect(c,p,DARK,x+2*PX,y-5*PX,x+6*PX,y);
  head(c,p,x+6*PX,y-6*PX,6,.05,.1,divine);
  rect(c,p,PUPIL,x+3*PX,y-6*PX,x+5*PX,y-5*PX);rect(c,p,PUPIL,x+7*PX,y-6*PX,x+9*PX,y-5*PX);
  tail(c,p,x-6*PX,y,frame,CatAnimationController.State.SLEEP,.05f);
 }

 private static void drawBrace(Canvas c,Paint p,float x,float g,int frame,double divine){
  float y=g-6*PX;
  tail(c,p,x-8*PX,y,frame,CatAnimationController.State.RETREAT,.15f);
  rect(c,p,OUT,x-9*PX,y-5*PX,x+8*PX,y+2*PX);rect(c,p,CREAM,x-8*PX,y-4*PX,x+7*PX,y+PX);
  rect(c,p,DARK,x-5*PX,y-4*PX,x,y);rect(c,p,ORANGE,x+2*PX,y-4*PX,x+6*PX,y);
  head(c,p,x+7*PX,y-4*PX,frame,.82,.78,divine);
  // planted wide paws
  rect(c,p,OUT,x-8*PX,g-2*PX,x-3*PX,g);rect(c,p,OUT,x+4*PX,g-2*PX,x+9*PX,g);
  rect(c,p,CREAM,x-7*PX,g-1.5f*PX,x-4*PX,g-.3f*PX);rect(c,p,CREAM,x+5*PX,g-1.5f*PX,x+8*PX,g-.3f*PX);
 }

 private static void drawShadow(Canvas c,Paint p,float x,float g,CatAnimationController.Visual v){
  float w=v.state==CatAnimationController.State.SLEEP?12*PX:v.state==CatAnimationController.State.SETTLE?10*PX:11*PX;
  rect(c,p,Color.argb(v.moving()?34:44,0,0,0),x-w,g-PX,x+w,g+PX);
  rect(c,p,Color.argb(18,20,27,25),x-w*.65f,g-PX*.5f,x+w*.65f,g+PX*.5f);
 }
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
}
