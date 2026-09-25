package com.aicharacter.v3;

import android.graphics.*;

/**
 * Chunky authored pixel Haru.
 * Presentation only: pose comes from GirlAnimationController and never changes simulation state.
 */
public final class HaruPixelRenderer {
 private static final float PX=4f;
 private static final int OUT=Color.rgb(38,31,34);
 private static final int HAIR=Color.rgb(58,42,43),HAIR_HI=Color.rgb(91,61,58);
 private static final int SKIN=Color.rgb(239,198,164),SKIN_SHADOW=Color.rgb(205,151,130),BLUSH=Color.rgb(196,111,111);
 private static final int TOP=Color.rgb(231,220,202),TOP_SHADE=Color.rgb(190,179,164),ACCENT=Color.rgb(142,82,86);
 private static final int SKIRT=Color.rgb(72,67,76),SKIRT_HI=Color.rgb(98,91,102),SHOE=Color.rgb(43,39,45);
 private static final int EYE=Color.rgb(47,39,42);
 private HaruPixelRenderer(){}

 public static void draw(Canvas c,Paint p,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float anim,float divinePresence){
  if(c==null||p==null||v==null)return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);p.setColorFilter(null);p.setAlpha(255);
  boolean left=v.flipX||v.state==GirlAnimationController.State.WALK_LEFT||v.state==GirlAnimationController.State.SEARCH_LEFT;
  int frame=((int)Math.floor(anim*Math.max(.1f,v.fps)))&7;
  float g=bodyGround+bob(v.state,frame);
  c.save();if(left)c.scale(-1f,1f,x,g);
  switch(v.state){
   case SLEEP:drawSleep(c,p,x,g,frame,divinePresence);break;
   case SIT:drawSit(c,p,x,g,frame,divinePresence);break;
   case CROUCH:drawCrouch(c,p,x,g,frame,divinePresence);break;
   default:drawStanding(c,p,s,v,x,g,frame,divinePresence);break;
  }
  c.restore();
 }

 public static float spriteHeight(){return 43*PX;}

 private static void drawStanding(Canvas c,Paint p,WorldState s,GirlAnimationController.Visual v,float x,float g,int frame,float divine){
  boolean walk=v.state==GirlAnimationController.State.WALK_LEFT||v.state==GirlAnimationController.State.WALK_RIGHT;
  boolean search=v.state==GirlAnimationController.State.SEARCH_LEFT||v.state==GirlAnimationController.State.SEARCH_RIGHT;
  float[] gait={-1.8f,-1.0f,-.35f,.55f,1.8f,1.0f,.35f,-.55f};
  float step=walk?gait[frame]*PX:0,other=-step;
  float lean=walk?1.0f*PX:search?.7f*PX:v.state==GirlAnimationController.State.REACT?-.6f*PX:0;
  float headTop=g-42*PX;

  // Hair silhouette: stepped chibi mass, with long side locks.
  rect(c,p,OUT,x-8*PX+lean,headTop+2*PX,x+8*PX+lean,headTop+14*PX);
  rect(c,p,OUT,x-7*PX+lean,headTop,x+5*PX+lean,headTop+3*PX);
  rect(c,p,OUT,x-9*PX+lean,headTop+5*PX,x-5*PX+lean,g-23*PX);
  rect(c,p,OUT,x+5*PX+lean,headTop+6*PX,x+9*PX+lean,g-24*PX);
  rect(c,p,HAIR,x-7*PX+lean,headTop+2*PX,x+7*PX+lean,headTop+13*PX);
  rect(c,p,HAIR,x-8*PX+lean,headTop+6*PX,x-5*PX+lean,g-24*PX);
  rect(c,p,HAIR,x+5*PX+lean,headTop+7*PX,x+8*PX+lean,g-25*PX);
  rect(c,p,HAIR_HI,x-5*PX+lean,headTop+2*PX,x-2*PX+lean,headTop+5*PX);

  // Face with hair fringe.
  rect(c,p,OUT,x-5*PX+lean,headTop+5*PX,x+5*PX+lean,headTop+15*PX);
  rect(c,p,SKIN,x-4*PX+lean,headTop+6*PX,x+4*PX+lean,headTop+14*PX);
  rect(c,p,HAIR,x-5*PX+lean,headTop+4*PX,x+5*PX+lean,headTop+7*PX);
  rect(c,p,HAIR,x-4*PX+lean,headTop+6*PX,x-2*PX+lean,headTop+9*PX);
  boolean blink=!walk&&(frame==6||frame==7);
  if(blink){
   rect(c,p,EYE,x-2*PX+lean,headTop+10*PX,x-1*PX+lean,headTop+11*PX);
   rect(c,p,EYE,x+2*PX+lean,headTop+10*PX,x+3*PX+lean,headTop+11*PX);
  }else{
   rect(c,p,EYE,x-2*PX+lean,headTop+9*PX,x-PX+lean,headTop+11*PX);
   rect(c,p,EYE,x+2*PX+lean,headTop+9*PX,x+3*PX+lean,headTop+11*PX);
  }
  rect(c,p,BLUSH,x+3*PX+lean,headTop+12*PX,x+4*PX+lean,headTop+13*PX);
  rect(c,p,ACCENT,x+PX+lean,headTop+13*PX,x+3*PX+lean,headTop+14*PX);

  // Neck.
  rect(c,p,OUT,x-2*PX,g-28*PX,x+2*PX,g-24*PX);
  rect(c,p,SKIN,x-PX,g-28*PX,x+PX,g-24*PX);

  // Torso outline with shoulders and fitted waist.
  rect(c,p,OUT,x-6*PX,g-25*PX,x+6*PX,g-13*PX);
  rect(c,p,OUT,x-5*PX,g-27*PX,x+5*PX,g-12*PX);
  rect(c,p,TOP,x-5*PX,g-25*PX,x+5*PX,g-14*PX);
  rect(c,p,TOP_SHADE,x-5*PX,g-18*PX,x+5*PX,g-14*PX);
  rect(c,p,TOP,x-4*PX,g-24*PX,x+4*PX,g-18*PX);
  rect(c,p,ACCENT,x-PX,g-25*PX,x+PX,g-22*PX);
  rect(c,p,ACCENT,x+PX,g-23*PX,x+4*PX,g-22*PX);

  float armFront=walk?-step*.55f:0,armBack=walk?step*.55f:0;
  if(v.state==GirlAnimationController.State.REACT){armFront=-4*PX;armBack=-3*PX;}
  if(v.state==GirlAnimationController.State.THINK){armFront=-5*PX;armBack=PX;}
  if(search){armFront=-5*PX;armBack=2*PX;}
  drawArm(c,p,x+5*PX,g-23*PX,armFront,true);
  drawArm(c,p,x-5*PX,g-23*PX,armBack,false);

  // State-specific readable silhouette.
  if(v.state==GirlAnimationController.State.THINK){
   rect(c,p,OUT,x+5*PX,g-32*PX,x+8*PX,g-27*PX);rect(c,p,SKIN,x+6*PX,g-31*PX,x+7*PX,g-28*PX);
  }else if(search){
   rect(c,p,OUT,x+4*PX,g-34*PX,x+12*PX,g-30*PX);
   rect(c,p,SKIN,x+5*PX,g-33*PX,x+11*PX,g-31*PX);
  }else if(v.state==GirlAnimationController.State.REACT){
   rect(c,p,OUT,x+6*PX,g-29*PX,x+9*PX,g-25*PX);
   rect(c,p,SKIN,x+7*PX,g-28*PX,x+8*PX,g-26*PX);
  }

  // Skirt, visibly separated from top.
  rect(c,p,OUT,x-6*PX,g-14*PX,x+6*PX,g-8*PX);
  rect(c,p,SKIRT,x-5*PX,g-13*PX,x+5*PX,g-9*PX);
  rect(c,p,SKIRT_HI,x-4*PX,g-13*PX,x+4*PX,g-12*PX);
  rect(c,p,SKIRT,x-6*PX,g-10*PX,x+6*PX,g-8*PX);

  // Legs and shoes. Walk always has a distinct stride even on neutral capture frames.
  if(walk&&Math.abs(step)<PX)step=(frame<2?-1:1)*PX;
  drawLeg(c,p,x-3*PX,g-8*PX,g,step);
  drawLeg(c,p,x+2*PX,g-8*PX,g,other);

  // A tiny hair/clothing motion cue keeps idle visibly alive.
  if(!walk&&(frame==2||frame==5)){rect(c,p,HAIR_HI,x-8*PX+lean,g-29*PX,x-7*PX+lean,g-25*PX);}
  if(walk){rect(c,p,Color.argb(70,223,213,190),x-10*PX,g-PX,x-8*PX,g);}
  drawDivinePixels(c,p,x,headTop,divine);
 }

 private static void drawArm(Canvas c,Paint p,float shoulderX,float y,float swing,boolean front){
  int sleeve=front?TOP:TOP_SHADE;
  float dir=shoulderX>=0?1:-1,sx=shoulderX+swing*.18f;
  rect(c,p,OUT,sx-2*PX,y,sx+2*PX,y+10*PX);
  rect(c,p,sleeve,sx-PX,y+PX,sx+PX,y+6*PX);
  float handX=sx+swing*.45f;
  rect(c,p,OUT,handX-1.5f*PX,y+6*PX,handX+1.5f*PX,y+10*PX);
  rect(c,p,SKIN,handX-PX,y+7*PX,handX+PX,y+9*PX);
  if(Math.abs(swing)>2*PX)rect(c,p,SKIN_SHADOW,handX-dir*PX,y+8*PX,handX,y+9*PX);
 }

 private static void drawLeg(Canvas c,Paint p,float x,float top,float ground,float step){
  float shift=step,knee=top+4*PX;
  rect(c,p,OUT,x-2*PX,top,x+2*PX,knee+PX);
  rect(c,p,SKIN,x-PX,top+PX,x+PX,knee);
  float shinX=x+shift*.30f;
  rect(c,p,OUT,shinX-2*PX,knee,shinX+2*PX,ground-2*PX);
  rect(c,p,SKIN,shinX-PX,knee,shinX+PX,ground-3*PX);
  rect(c,p,OUT,shinX-2*PX+shift,ground-3*PX,shinX+4*PX+shift,ground);
  rect(c,p,SHOE,shinX-PX+shift,ground-2*PX,shinX+3*PX+shift,ground-PX*.25f);
 }

 private static void drawSit(Canvas c,Paint p,float x,float g,int frame,float divine){
  float y=g-(frame==2?PX:0),headTop=y-36*PX;
  drawCompactHead(c,p,x,headTop);
  rect(c,p,OUT,x-6*PX,y-22*PX,x+6*PX,y-9*PX);rect(c,p,TOP,x-5*PX,y-21*PX,x+5*PX,y-11*PX);
  rect(c,p,ACCENT,x-PX,y-21*PX,x+PX,y-18*PX);
  rect(c,p,OUT,x-6*PX,y-11*PX,x+7*PX,y-5*PX);rect(c,p,SKIRT,x-5*PX,y-10*PX,x+6*PX,y-6*PX);
  rect(c,p,OUT,x-3*PX,y-7*PX,x+11*PX,y-3*PX);rect(c,p,SKIN,x-2*PX,y-6*PX,x+10*PX,y-4*PX);
  rect(c,p,OUT,x+8*PX,y-4*PX,x+14*PX,y);rect(c,p,SHOE,x+9*PX,y-3*PX,x+13*PX,y-PX*.3f);
  rect(c,p,OUT,x-4*PX,y-13*PX,x+2*PX,y-9*PX);rect(c,p,SKIN,x-3*PX,y-12*PX,x+PX,y-10*PX);
  drawDivinePixels(c,p,x,headTop,divine);
 }

 private static void drawCrouch(Canvas c,Paint p,float x,float g,int frame,float divine){
  float y=g+(frame==1?PX:0),headTop=y-33*PX;
  drawCompactHead(c,p,x,headTop);
  rect(c,p,OUT,x-7*PX,y-20*PX,x+7*PX,y-8*PX);rect(c,p,TOP,x-6*PX,y-19*PX,x+6*PX,y-10*PX);
  rect(c,p,OUT,x-6*PX,y-10*PX,x+6*PX,y-5*PX);rect(c,p,SKIRT,x-5*PX,y-9*PX,x+5*PX,y-6*PX);
  rect(c,p,OUT,x+4*PX,y-13*PX,x+12*PX,y-9*PX);rect(c,p,SKIN,x+5*PX,y-12*PX,x+11*PX,y-10*PX);
  rect(c,p,OUT,x-4*PX,y-6*PX,x+3*PX,y);rect(c,p,SKIN,x-3*PX,y-5*PX,x+2*PX,y-2*PX);
  rect(c,p,OUT,x+4*PX,y-6*PX,x+11*PX,y);rect(c,p,SHOE,x+6*PX,y-2*PX,x+11*PX,y);
  drawDivinePixels(c,p,x,headTop,divine);
 }

 private static void drawSleep(Canvas c,Paint p,float x,float g,int frame,float divine){
  float breath=(frame==1||frame==2)?PX:0,y=g-3*PX-breath;
  rect(c,p,Color.argb(45,0,0,0),x-16*PX,g-PX,x+18*PX,g+PX);
  rect(c,p,OUT,x-14*PX,y-10*PX,x-2*PX,y+PX);rect(c,p,HAIR,x-13*PX,y-9*PX,x-3*PX,y);
  rect(c,p,OUT,x-11*PX,y-7*PX,x-4*PX,y);rect(c,p,SKIN,x-10*PX,y-6*PX,x-5*PX,y-PX);
  rect(c,p,EYE,x-8*PX,y-3*PX,x-6*PX,y-2*PX);
  rect(c,p,OUT,x-3*PX,y-8*PX,x+9*PX,y+PX);rect(c,p,TOP,x-2*PX,y-7*PX,x+8*PX,y);
  rect(c,p,OUT,x+7*PX,y-7*PX,x+16*PX,y);rect(c,p,SKIRT,x+8*PX,y-6*PX,x+15*PX,y-PX);
  rect(c,p,OUT,x+14*PX,y-4*PX,x+21*PX,y);rect(c,p,SHOE,x+16*PX,y-3*PX,x+20*PX,y-PX*.3f);
  drawDivinePixels(c,p,x,y-11*PX,divine);
 }

 private static void drawCompactHead(Canvas c,Paint p,float x,float top){
  rect(c,p,OUT,x-8*PX,top+2*PX,x+8*PX,top+14*PX);rect(c,p,OUT,x-7*PX,top,x+5*PX,top+3*PX);
  rect(c,p,HAIR,x-7*PX,top+2*PX,x+7*PX,top+13*PX);rect(c,p,HAIR_HI,x-5*PX,top+2*PX,x-2*PX,top+5*PX);
  rect(c,p,OUT,x-5*PX,top+5*PX,x+5*PX,top+15*PX);rect(c,p,SKIN,x-4*PX,top+6*PX,x+4*PX,top+14*PX);
  rect(c,p,HAIR,x-5*PX,top+4*PX,x+5*PX,top+7*PX);rect(c,p,EYE,x-2*PX,top+9*PX,x-PX,top+11*PX);rect(c,p,EYE,x+2*PX,top+9*PX,x+3*PX,top+11*PX);
  rect(c,p,BLUSH,x+3*PX,top+12*PX,x+4*PX,top+13*PX);
 }

 private static void drawDivinePixels(Canvas c,Paint p,float x,float top,float divine){
  float d=Math.max(0,Math.min(1,divine));if(d<.04f)return;
  p.setColor(Color.argb((int)(45+120*d),244,222,159));
  c.drawRect(x-10*PX,top+3*PX,x-9*PX,top+4*PX,p);c.drawRect(x+9*PX,top-2*PX,x+10*PX,top-PX,p);
  if(d>.45f)c.drawRect(x+12*PX,top+9*PX,x+13*PX,top+10*PX,p);
 }

 private static float bob(GirlAnimationController.State state,int frame){
  if(state==GirlAnimationController.State.WALK_LEFT||state==GirlAnimationController.State.WALK_RIGHT)return(frame==1||frame==2||frame==5||frame==6)?-PX:0;
  if(state==GirlAnimationController.State.REACT)return(frame==1||frame==5)?-PX:0;
  if(state==GirlAnimationController.State.SEARCH_LEFT||state==GirlAnimationController.State.SEARCH_RIGHT)return(frame==2||frame==6)?-PX:0;
  return 0;
 }

 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
}
