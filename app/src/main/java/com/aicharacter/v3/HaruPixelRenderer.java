package com.aicharacter.v3;

import android.graphics.*;

/** Chunky block-pixel Haru sprite driven only by existing presentation state. */
public final class HaruPixelRenderer {
 private static final float PX=6f;
 private static final int HAIR=Color.rgb(53,42,43),HAIR_HI=Color.rgb(83,61,58);
 private static final int SKIN=Color.rgb(238,199,168),SKIN_SHADOW=Color.rgb(206,157,135);
 private static final int TOP=Color.rgb(228,218,201),TOP_SHADOW=Color.rgb(184,176,164);
 private static final int BOTTOM=Color.rgb(69,67,72),SHOE=Color.rgb(45,43,48);
 private static final int EYE=Color.rgb(52,43,44),ACCENT=Color.rgb(150,91,92);
 private HaruPixelRenderer(){}

 public static void draw(Canvas c,Paint p,WorldState s,GirlAnimationController.Visual v,float x,float bodyGround,float anim,float divinePresence){
  if(c==null||p==null||v==null)return;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);p.setColorFilter(null);p.setAlpha(255);
  boolean left=v.flipX||v.state==GirlAnimationController.State.WALK_LEFT||v.state==GirlAnimationController.State.SEARCH_LEFT;
  int frame=((int)Math.floor(anim*Math.max(.1f,v.fps)))&3;
  float bob=bob(v.state,frame),ground=bodyGround+bob;
  c.save();if(left)c.scale(-1f,1f,x,ground);
  if(v.state==GirlAnimationController.State.SLEEP)drawSleep(c,p,x,ground,frame,divinePresence);
  else drawStanding(c,p,s,v,x,ground,frame,divinePresence);
  c.restore();
 }

 public static float spriteHeight(){return 47*PX;}

 private static void drawStanding(Canvas c,Paint p,WorldState s,GirlAnimationController.Visual v,float x,float g,int frame,float divine){
  float stepA=0,stepB=0,armA=0,armB=0,lean=0;
  boolean moving=v.state==GirlAnimationController.State.WALK_LEFT||v.state==GirlAnimationController.State.WALK_RIGHT;
  if(moving){
   float[] phase={-1f,0f,1f,0f};stepA=phase[frame]*2*PX;stepB=-stepA;armA=-stepA*.72f;armB=-armA;
  }else if(v.state==GirlAnimationController.State.SEARCH_LEFT||v.state==GirlAnimationController.State.SEARCH_RIGHT){
   armA=-3*PX;armB=2*PX;lean=PX;
  }else if(v.state==GirlAnimationController.State.REACT){
   armA=-3*PX;armB=-2*PX;lean=-PX;
  }else if(v.state==GirlAnimationController.State.THINK){
   armA=-5*PX;armB=PX;lean=PX*.5f;
  }

  if(v.state==GirlAnimationController.State.SIT){drawSit(c,p,x,g,frame,divine);return;}
  if(v.state==GirlAnimationController.State.CROUCH){drawCrouch(c,p,x,g,frame,divine);return;}

  float hipY=g-13*PX,torsoTop=g-27*PX,headTop=g-43*PX;
  // Back hair mass.
  rect(c,p,HAIR,x-6*PX+lean,headTop,x+6*PX+lean,g-24*PX);
  rect(c,p,HAIR_HI,x-6*PX+lean,headTop+3*PX,x-4*PX+lean,g-30*PX);
  rect(c,p,HAIR,x+5*PX+lean,headTop+5*PX,x+8*PX+lean,g-27*PX);

  // Face.
  rect(c,p,SKIN,x-4*PX+lean,headTop+5*PX,x+5*PX+lean,headTop+14*PX);
  rect(c,p,SKIN_SHADOW,x+4*PX+lean,headTop+7*PX,x+5*PX+lean,headTop+13*PX);
  rect(c,p,EYE,x+1*PX+lean,headTop+8*PX,x+2*PX+lean,headTop+9*PX);
  rect(c,p,ACCENT,x+3*PX+lean,headTop+12*PX,x+4*PX+lean,headTop+13*PX);
  // Fringe.
  rect(c,p,HAIR,x-5*PX+lean,headTop+2*PX,x+5*PX+lean,headTop+6*PX);
  rect(c,p,HAIR,x-4*PX+lean,headTop+5*PX,x-2*PX+lean,headTop+8*PX);
  rect(c,p,HAIR_HI,x-2*PX+lean,headTop+3*PX,x+lean,headTop+5*PX);

  // Neck and top.
  rect(c,p,SKIN,x-2*PX,headTop+14*PX,x+2*PX,torsoTop+2*PX);
  rect(c,p,TOP,x-5*PX,torsoTop,x+5*PX,hipY);
  rect(c,p,TOP_SHADOW,x-5*PX,torsoTop+8*PX,x+5*PX,hipY);
  rect(c,p,TOP,x-4*PX,torsoTop+1*PX,x+4*PX,torsoTop+8*PX);
  rect(c,p,ACCENT,x+4*PX,torsoTop+2*PX,x+5*PX,torsoTop+10*PX);

  // Arms with state-driven swing/pose.
  drawArm(c,p,x-5*PX,torsoTop+3*PX,armA,false);
  drawArm(c,p,x+5*PX,torsoTop+3*PX,armB,true);
  if(v.state==GirlAnimationController.State.THINK){
   rect(c,p,SKIN,x+5*PX,g-32*PX,x+7*PX,g-27*PX);
   rect(c,p,SKIN,x+4*PX,g-34*PX,x+6*PX,g-32*PX);
  }
  if(v.state==GirlAnimationController.State.SEARCH_LEFT||v.state==GirlAnimationController.State.SEARCH_RIGHT){
   rect(c,p,SKIN,x+5*PX,g-31*PX,x+10*PX,g-29*PX);
   rect(c,p,SKIN,x+9*PX,g-32*PX,x+11*PX,g-28*PX);
  }

  // Skirt/shorts block.
  rect(c,p,BOTTOM,x-5*PX,hipY,x+5*PX,g-10*PX);
  rect(c,p,Color.rgb(88,84,91),x-4*PX,hipY,x+4*PX,hipY+2*PX);

  // Legs.
  drawLeg(c,p,x-3*PX,g-10*PX,g,stepA);
  drawLeg(c,p,x+2*PX,g-10*PX,g,stepB);

  // Pixel movement accents.
  if(moving&&frame!=1){p.setColor(Color.argb(74,218,209,190));c.drawRect(x-9*PX,g-PX,x-7*PX,g,p);}
  drawDivinePixels(c,p,x,headTop,divine);
 }

 private static void drawArm(Canvas c,Paint p,float x,float y,float swing,boolean front){
  int sleeve=front?TOP:TOP_SHADOW;
  float sx=x+swing*.18f,handY=y+9*PX+swing*.45f;
  rect(c,p,sleeve,sx-PX,y,sx+2*PX,y+7*PX);
  rect(c,p,SKIN,sx-PX, y+6*PX, sx+2*PX,handY+2*PX);
 }

 private static void drawLeg(Canvas c,Paint p,float x,float top,float ground,float step){
  float knee=top+5*PX,footShift=step;
  rect(c,p,SKIN_SHADOW,x-PX,top,x+2*PX,knee);
  rect(c,p,SKIN,x-PX+footShift*.25f,knee,x+2*PX+footShift*.25f,ground-2*PX);
  rect(c,p,SHOE,x-2*PX+footShift,ground-3*PX,x+3*PX+footShift,ground);
 }

 private static void drawSit(Canvas c,Paint p,float x,float g,int frame,float divine){
  float y=g-5*PX+(frame==2?PX:0),headTop=y-38*PX;
  rect(c,p,HAIR,x-7*PX,headTop,x+7*PX,y-20*PX);
  rect(c,p,SKIN,x-4*PX,headTop+5*PX,x+5*PX,headTop+14*PX);
  rect(c,p,EYE,x+1*PX,headTop+8*PX,x+2*PX,headTop+9*PX);
  rect(c,p,HAIR,x-5*PX,headTop+2*PX,x+5*PX,headTop+6*PX);
  rect(c,p,TOP,x-5*PX,y-23*PX,x+5*PX,y-11*PX);
  rect(c,p,BOTTOM,x-5*PX,y-11*PX,x+5*PX,y-5*PX);
  rect(c,p,SKIN,x+2*PX,y-7*PX,x+9*PX,y-4*PX);
  rect(c,p,SKIN,x-2*PX,y-7*PX,x+5*PX,y-4*PX);
  rect(c,p,SHOE,x+7*PX,y-5*PX,x+12*PX,y-2*PX);
  rect(c,p,SHOE,x+2*PX,y-5*PX,x+7*PX,y-2*PX);
  drawDivinePixels(c,p,x,headTop,divine);
 }

 private static void drawCrouch(Canvas c,Paint p,float x,float g,int frame,float divine){
  float y=g+(frame==1?PX:0),headTop=y-34*PX;
  rect(c,p,HAIR,x-7*PX,headTop,x+7*PX,y-18*PX);
  rect(c,p,SKIN,x-4*PX,headTop+5*PX,x+5*PX,headTop+14*PX);
  rect(c,p,EYE,x+1*PX,headTop+8*PX,x+2*PX,headTop+9*PX);
  rect(c,p,HAIR,x-5*PX,headTop+2*PX,x+5*PX,headTop+6*PX);
  rect(c,p,TOP,x-5*PX,y-20*PX,x+6*PX,y-9*PX);
  rect(c,p,BOTTOM,x-4*PX,y-9*PX,x+5*PX,y-4*PX);
  rect(c,p,SKIN,x+4*PX,y-12*PX,x+11*PX,y-9*PX);
  rect(c,p,SKIN,x-2*PX,y-4*PX,x+2*PX,y-PX);
  rect(c,p,SKIN,x+3*PX,y-4*PX,x+8*PX,y-PX);
  rect(c,p,SHOE,x-3*PX,y-2*PX,x+2*PX,y);
  rect(c,p,SHOE,x+6*PX,y-2*PX,x+11*PX,y);
  drawDivinePixels(c,p,x,headTop,divine);
 }

 private static void drawSleep(Canvas c,Paint p,float x,float g,int frame,float divine){
  float breath=(frame==1||frame==2)?PX:0,y=g-4*PX-breath;
  rect(c,p,Color.argb(42,0,0,0),x-14*PX,g-PX,x+14*PX,g+PX);
  rect(c,p,HAIR,x-13*PX,y-8*PX,x-3*PX,y+PX);
  rect(c,p,SKIN,x-10*PX,y-6*PX,x-4*PX,y);
  rect(c,p,HAIR,x-12*PX,y-9*PX,x-4*PX,y-6*PX);
  rect(c,p,TOP,x-4*PX,y-7*PX,x+8*PX,y);
  rect(c,p,BOTTOM,x+6*PX,y-6*PX,x+14*PX,y);
  rect(c,p,SKIN,x+12*PX,y-3*PX,x+17*PX,y);
  rect(c,p,SHOE,x+15*PX,y-3*PX,x+20*PX,y);
  drawDivinePixels(c,p,x,y-10*PX,divine);
 }

 private static void drawDivinePixels(Canvas c,Paint p,float x,float top,float divine){
  float d=Math.max(0,Math.min(1,divine));if(d<.04f)return;
  int a=(int)(45+120*d);p.setColor(Color.argb(a,244,222,159));
  c.drawRect(x-9*PX,top+2*PX,x-8*PX,top+3*PX,p);
  c.drawRect(x+8*PX,top-2*PX,x+9*PX,top-PX,p);
  if(d>.45f)c.drawRect(x+11*PX,top+8*PX,x+12*PX,top+9*PX,p);
 }

 private static float bob(GirlAnimationController.State state,int frame){
  if(state==GirlAnimationController.State.WALK_LEFT||state==GirlAnimationController.State.WALK_RIGHT)return(frame==1||frame==3)?-PX:0;
  if(state==GirlAnimationController.State.REACT)return frame==1?-PX:0;
  if(state==GirlAnimationController.State.SEARCH_LEFT||state==GirlAnimationController.State.SEARCH_RIGHT)return frame==2?-PX:0;
  return 0;
 }

 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
}
