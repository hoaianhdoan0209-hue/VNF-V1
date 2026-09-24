package com.aicharacter.v3;

import android.graphics.*;

/**
 * Smooth illustrated Haru body used by the live world renderer.
 * Presentation only: all pose/expression choices come from simulation state.
 */
public final class HaruIllustratedRenderer {
 private HaruIllustratedRenderer(){}

 public static void draw(Canvas c,Paint p,WorldState s,GirlAnimationController.Visual v,
                         float x,float ground,float anim,int bodyAlpha,float closeT,
                         float divinePresence,String phase,String area){
  p.setShader(null);p.setColorFilter(null);p.setAntiAlias(true);p.setStyle(Paint.Style.FILL);p.setAlpha(bodyAlpha);
  p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);

  if(v.state==GirlAnimationController.State.SLEEP){drawSleep(c,p,s,x,ground,anim,closeT,divinePresence,phase);restore(p);return;}

  float breath=(float)Math.sin(anim*1.55f)*2.0f;
  float stride=v.isWalk()?(float)Math.sin(anim*Math.max(2.5f,v.fps)*1.75f):0f;
  float bounce=v.isWalk()?Math.abs((float)Math.sin(anim*Math.max(2.5f,v.fps)*1.75f))*3.2f:breath*.30f;
  boolean sit=v.state==GirlAnimationController.State.SIT;
  boolean crouch=v.state==GirlAnimationController.State.CROUCH;
  boolean think=v.state==GirlAnimationController.State.THINK;
  boolean react=v.state==GirlAnimationController.State.REACT;
  boolean search=v.state==GirlAnimationController.State.SEARCH_LEFT||v.state==GirlAnimationController.State.SEARCH_RIGHT;

  float hipY=ground-(sit||crouch?74f:111f)-bounce;
  float shoulderY=hipY-(sit||crouch?73f:92f);
  float headY=shoulderY-55f+(think?2f:0f);
  float headTilt=think?-4f:react?4f:search?-3f:0f;

  // Divine presence gets a subtle silhouette glow, never a different body.
  if(divinePresence>.02f){
   p.setColor(Color.argb((int)(18+34*Math.min(1f,divinePresence)),239,224,184));
   c.drawOval(x-54,headY-48,x+54,hipY+46,p);
  }

  // Back hair mass first.
  p.setColor(withAlpha(Color.rgb(54,36,36),bodyAlpha));
  Path backHair=new Path();
  backHair.moveTo(x-34,headY-27);backHair.cubicTo(x-48,headY+2,x-42,shoulderY+42,x-30,shoulderY+63);
  backHair.lineTo(x+31,shoulderY+62);backHair.cubicTo(x+43,shoulderY+32,x+46,headY+4,x+33,headY-27);backHair.close();
  c.drawPath(backHair,p);

  // Legs / seated lower body.
  if(sit)drawSeatedLegs(c,p,x,hipY,ground,bodyAlpha);
  else if(crouch)drawCrouchedLegs(c,p,x,hipY,ground,bodyAlpha);
  else drawStandingLegs(c,p,x,hipY,ground,stride,bodyAlpha);

  // Torso outline then clothing.
  drawTorso(c,p,x,shoulderY,hipY,breath,bodyAlpha,phase,area);

  // Arms are stateful rather than generic mirrored sticks.
  if(think)drawThinkArms(c,p,x,shoulderY,headY,bodyAlpha);
  else if(search)drawSearchArms(c,p,x,shoulderY,headY,bodyAlpha);
  else if(react)drawReactArms(c,p,x,shoulderY,hipY,stride,bodyAlpha);
  else drawNaturalArms(c,p,x,shoulderY,hipY,stride,v.isWalk(),bodyAlpha);

  // Neck and head.
  p.setColor(withAlpha(Color.rgb(226,179,145),bodyAlpha));c.drawRoundRect(x-9,headY+26,x+9,shoulderY+11,7,7,p);
  drawHead(c,p,s,x,headY,headTilt,anim,v,bodyAlpha,closeT);

  // Small cloth details make the silhouette read as a person rather than a mannequin.
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f);p.setColor(withAlpha(Color.rgb(202,177,151),Math.min(210,bodyAlpha)));
  c.drawLine(x-22,shoulderY+40,x+19,shoulderY+40,p);
  p.setStyle(Paint.Style.FILL);

  restore(p);
 }

 private static void drawStandingLegs(Canvas c,Paint p,float x,float hipY,float ground,float stride,int alpha){
  float swing=stride*15f;
  float kneeLY=hipY+55f,kneeRY=hipY+55f;
  float footLX=x-17+swing,footRX=x+17-swing;
  drawLimb(c,p,x-16,hipY+8,x-17+swing*.45f,kneeLY,footLX,ground-15,Color.rgb(48,55,68),18,alpha);
  drawLimb(c,p,x+16,hipY+8,x+17-swing*.45f,kneeRY,footRX,ground-15,Color.rgb(48,55,68),18,alpha);
  drawBoot(c,p,footLX,ground-8,-1,alpha);drawBoot(c,p,footRX,ground-8,1,alpha);
 }

 private static void drawSeatedLegs(Canvas c,Paint p,float x,float hipY,float ground,int alpha){
  drawLimb(c,p,x-16,hipY+6,x-39,hipY+43,x-44,ground-15,Color.rgb(49,56,69),18,alpha);
  drawLimb(c,p,x+15,hipY+6,x+43,hipY+38,x+49,ground-15,Color.rgb(49,56,69),18,alpha);
  drawBoot(c,p,x-46,ground-8,-1,alpha);drawBoot(c,p,x+51,ground-8,1,alpha);
 }

 private static void drawCrouchedLegs(Canvas c,Paint p,float x,float hipY,float ground,int alpha){
  drawLimb(c,p,x-14,hipY+5,x-38,hipY+35,x-26,ground-14,Color.rgb(49,56,69),19,alpha);
  drawLimb(c,p,x+14,hipY+5,x+35,hipY+38,x+29,ground-14,Color.rgb(49,56,69),19,alpha);
  drawBoot(c,p,x-28,ground-7,-1,alpha);drawBoot(c,p,x+31,ground-7,1,alpha);
 }

 private static void drawTorso(Canvas c,Paint p,float x,float shoulderY,float hipY,float breath,int alpha,String phase,String area){
  // soft outline
  p.setColor(withAlpha(Color.rgb(42,35,37),alpha));
  Path outline=new Path();outline.moveTo(x-36,shoulderY+2);outline.quadTo(x-44,shoulderY+38,x-31,hipY+10);
  outline.quadTo(x,hipY+24,x+31,hipY+10);outline.quadTo(x+44,shoulderY+38,x+36,shoulderY+2);outline.quadTo(x,shoulderY-10,x-36,shoulderY+2);outline.close();c.drawPath(outline,p);
  // dusty rose shirt
  p.setColor(withAlpha(Color.rgb(164,104,108),alpha));
  Path shirt=new Path();shirt.moveTo(x-29,shoulderY+2);shirt.quadTo(x-32,shoulderY+35,x-25,hipY+6);shirt.lineTo(x+25,hipY+6);shirt.quadTo(x+32,shoulderY+35,x+29,shoulderY+2);shirt.close();c.drawPath(shirt,p);
  // olive vest
  p.setColor(withAlpha(Color.rgb(76,105,75),alpha));
  Path vest=new Path();vest.moveTo(x-23,shoulderY+17);vest.lineTo(x-18,hipY+4);vest.lineTo(x+18,hipY+4);vest.lineTo(x+23,shoulderY+17);vest.lineTo(x+14,shoulderY+9);vest.lineTo(x,shoulderY+25);vest.lineTo(x-14,shoulderY+9);vest.close();c.drawPath(vest,p);
  // scarf/collar
  p.setColor(withAlpha(Color.rgb(221,188,165),alpha));c.drawRoundRect(x-25,shoulderY-5,x+25,shoulderY+11+breath*.15f,8,8,p);
  p.setColor(withAlpha(Color.rgb(184,128,130),alpha));c.drawRoundRect(x-22,shoulderY-1,x+22,shoulderY+5,4,4,p);
  // subtle environment key on vest
  int hi="NIGHT".equals(phase)?Color.rgb(126,145,149):"quiet_grove".equals(area)?Color.rgb(128,147,108):Color.rgb(134,151,112);
  p.setColor(withAlpha(hi,Math.min(alpha,105)));c.drawRoundRect(x+10,shoulderY+29,x+14,hipY-5,2,2,p);
 }

 private static void drawNaturalArms(Canvas c,Paint p,float x,float shoulderY,float hipY,float stride,boolean walking,int alpha){
  float swing=walking?stride*17f:0f;
  drawSleevedArm(c,p,x-30,shoulderY+10,x-37-swing*.35f,shoulderY+57,x-34-swing,hipY+1,alpha);
  drawSleevedArm(c,p,x+30,shoulderY+10,x+37+swing*.35f,shoulderY+57,x+34+swing,hipY+1,alpha);
 }

 private static void drawThinkArms(Canvas c,Paint p,float x,float shoulderY,float headY,int alpha){
  drawSleevedArm(c,p,x-29,shoulderY+10,x-36,shoulderY+55,x-24,shoulderY+80,alpha);
  drawSleevedArm(c,p,x+29,shoulderY+10,x+36,shoulderY+43,x+23,headY+31,alpha);
 }

 private static void drawSearchArms(Canvas c,Paint p,float x,float shoulderY,float headY,int alpha){
  drawSleevedArm(c,p,x-29,shoulderY+10,x-35,shoulderY+50,x-30,shoulderY+80,alpha);
  drawSleevedArm(c,p,x+29,shoulderY+10,x+35,shoulderY+35,x+27,headY-19,alpha);
 }

 private static void drawReactArms(Canvas c,Paint p,float x,float shoulderY,float hipY,float stride,int alpha){
  drawSleevedArm(c,p,x-29,shoulderY+10,x-49,shoulderY+43,x-51,hipY-8+stride*3,alpha);
  drawSleevedArm(c,p,x+29,shoulderY+10,x+49,shoulderY+43,x+51,hipY-8-stride*3,alpha);
 }

 private static void drawSleevedArm(Canvas c,Paint p,float sx,float sy,float ex,float ey,float hx,float hy,int alpha){
  // sleeve to elbow
  drawSegment(c,p,sx,sy,ex,ey,Color.rgb(147,91,94),16,alpha);
  // forearm skin
  drawSegment(c,p,ex,ey,hx,hy,Color.rgb(226,179,145),12,alpha);
  p.setColor(withAlpha(Color.rgb(226,179,145),alpha));c.drawCircle(hx,hy,6.8f,p);
 }

 private static void drawLimb(Canvas c,Paint p,float sx,float sy,float kx,float ky,float fx,float fy,int color,float width,int alpha){
  drawSegment(c,p,sx,sy,kx,ky,color,width,alpha);
  drawSegment(c,p,kx,ky,fx,fy,color,width-1,alpha);
 }

 private static void drawSegment(Canvas c,Paint p,float x1,float y1,float x2,float y2,int color,float width,int alpha){
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
  p.setStrokeWidth(width+4);p.setColor(withAlpha(Color.rgb(38,32,36),alpha));c.drawLine(x1,y1,x2,y2,p);
  p.setStrokeWidth(width);p.setColor(withAlpha(color,alpha));c.drawLine(x1,y1,x2,y2,p);
  p.setStyle(Paint.Style.FILL);
 }

 private static void drawBoot(Canvas c,Paint p,float x,float y,int dir,int alpha){
  p.setColor(withAlpha(Color.rgb(43,38,45),alpha));c.drawRoundRect(x-10,y-12,x+10,y+9,5,5,p);
  c.drawRoundRect(x-10+(dir<0?-6:0),y+2,x+10+(dir>0?6:0),y+11,5,5,p);
  p.setColor(withAlpha(Color.rgb(74,66,72),Math.min(alpha,180)));c.drawRoundRect(x-7,y-8,x+7,y-5,2,2,p);
 }

 private static void drawHead(Canvas c,Paint p,WorldState s,float x,float y,float tilt,float anim,GirlAnimationController.Visual v,int alpha,float closeT){
  c.save();c.rotate(tilt,x,y);
  // ears
  p.setColor(withAlpha(Color.rgb(222,173,140),alpha));c.drawOval(x-37,y-12,x-27,y+10,p);c.drawOval(x+27,y-12,x+37,y+10,p);
  // face outline
  p.setColor(withAlpha(Color.rgb(48,34,35),alpha));c.drawOval(x-33,y-40,x+33,y+38,p);
  p.setColor(withAlpha(Color.rgb(232,187,153),alpha));c.drawOval(x-29,y-37,x+29,y+34,p);
  // fringe / hair crown
  p.setColor(withAlpha(Color.rgb(58,38,38),alpha));
  Path hair=new Path();hair.moveTo(x-31,y-23);hair.cubicTo(x-28,y-52,x+26,y-53,x+33,y-21);
  hair.cubicTo(x+21,y-31,x+15,y-23,x+7,y-34);hair.cubicTo(x-2,y-22,x-13,y-31,x-19,y-18);hair.cubicTo(x-24,y-15,x-28,y-18,x-31,y-23);hair.close();c.drawPath(hair,p);

  boolean blink=((anim+0.37f)%4.7f)<.11f;
  float mood=s.mood==null?0f:(float)Math.max(-1,Math.min(1,(s.mood.pleasantness-.5)*2.0));
  float eyeY=y-4f,gaze=0f;
  try{HaruExpressionEngine.Visual e=HaruExpressionEngine.derive(s,Math.max(s.lastSimulatedAt,s.lastOpenedAt));gaze=(float)Math.max(-2.2,Math.min(2.2,e.gazeX*2.1));}catch(Throwable ignored){}

  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.4f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(withAlpha(Color.rgb(64,44,43),alpha));
  if(blink){c.drawLine(x-18,eyeY,x-8,eyeY,p);c.drawLine(x+8,eyeY,x+18,eyeY,p);}
  else{
   p.setStyle(Paint.Style.FILL);p.setColor(withAlpha(Color.rgb(248,239,218),alpha));c.drawOval(x-20,eyeY-5,x-7,eyeY+6,p);c.drawOval(x+7,eyeY-5,x+20,eyeY+6,p);
   p.setColor(withAlpha(Color.rgb(76,61,50),alpha));c.drawCircle(x-13+gaze,eyeY+1,3.8f,p);c.drawCircle(x+13+gaze,eyeY+1,3.8f,p);
   if(closeT>.18f){p.setColor(withAlpha(Color.WHITE,Math.min(alpha,190)));c.drawCircle(x-12+gaze,eyeY-.3f,1.1f,p);c.drawCircle(x+14+gaze,eyeY-.3f,1.1f,p);}
  }
  // brows
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.1f);p.setColor(withAlpha(Color.rgb(73,48,45),alpha));
  float browLift=v.state==GirlAnimationController.State.REACT?-3f:v.state==GirlAnimationController.State.THINK?1f:0f;
  c.drawLine(x-20,eyeY-11+browLift,x-8,eyeY-12-browLift*.2f,p);c.drawLine(x+8,eyeY-12-browLift*.2f,x+20,eyeY-11+browLift,p);
  // nose
  p.setStrokeWidth(1.6f);p.setColor(withAlpha(Color.rgb(177,125,104),Math.min(alpha,175)));c.drawLine(x+1,y+1,x-1,y+9,p);
  // mouth expressive but restrained
  float mouthY=y+19;
  p.setStrokeWidth(2f);p.setColor(withAlpha(Color.rgb(125,72,74),alpha));
  if(v.state==GirlAnimationController.State.REACT)c.drawArc(x-9,mouthY-4,x+9,mouthY+8,196,148,false,p);
  else if(mood>.16f)c.drawArc(x-10,mouthY-6,x+10,mouthY+6,18,144,false,p);
  else if(mood<-.18f)c.drawArc(x-9,mouthY-1,x+9,mouthY+9,200,140,false,p);
  else c.drawLine(x-7,mouthY,x+7,mouthY,p);
  p.setStyle(Paint.Style.FILL);

  // cheek life
  p.setColor(withAlpha(Color.rgb(210,126,124),Math.min(alpha,44)));c.drawOval(x-25,y+8,x-13,y+14,p);c.drawOval(x+13,y+8,x+25,y+14,p);
  c.restore();
 }

 private static void drawSleep(Canvas c,Paint p,WorldState s,float x,float ground,float anim,float closeT,float divinePresence,String phase){
  float y=ground-31f,breath=(float)Math.sin(anim*1.15f)*2f;
  p.setAntiAlias(true);p.setStyle(Paint.Style.FILL);
  if(divinePresence>.02f){p.setColor(Color.argb((int)(14+25*divinePresence),239,224,184));c.drawOval(x-105,y-52,x+105,y+36,p);}
  // legs / boots
  drawSegment(c,p,x-28,y,x+38,y+5,Color.rgb(48,55,68),18,255);drawBoot(c,p,x+51,y+7,1,255);
  drawSegment(c,p,x-42,y-5,x+5,y+1,Color.rgb(48,55,68),18,255);
  // torso
  p.setColor(Color.rgb(46,38,40));c.drawRoundRect(x-55,y-53+breath,x+22,y+8,22,22,p);
  p.setColor(Color.rgb(154,99,103));c.drawRoundRect(x-51,y-49+breath,x+18,y+4,20,20,p);
  p.setColor(Color.rgb(76,105,75));c.drawRoundRect(x-41,y-42+breath,x+9,y+2,15,15,p);
  // head + hair
  float hx=x-70,hy=y-35+breath*.4f;
  p.setColor(Color.rgb(55,36,36));c.drawCircle(hx,hy,35,p);
  p.setColor(Color.rgb(231,186,152));c.drawOval(hx-26,hy-28,hx+27,hy+27,p);
  p.setColor(Color.rgb(57,38,38));c.drawArc(hx-29,hy-33,hx+30,hy+14,190,160,true,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.1f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.rgb(79,53,50));
  c.drawLine(hx-18,hy-2,hx-7,hy-2,p);c.drawLine(hx+7,hy-2,hx+18,hy-2,p);
  p.setColor(Color.rgb(126,75,76));c.drawLine(hx-6,hy+17,hx+7,hy+17,p);
  p.setStyle(Paint.Style.FILL);
 }

 private static int withAlpha(int color,int alpha){return Color.argb(Math.max(0,Math.min(255,alpha)),Color.red(color),Color.green(color),Color.blue(color));}
 private static void restore(Paint p){p.setShader(null);p.setColorFilter(null);p.setAlpha(255);p.setStyle(Paint.Style.FILL);p.setStrokeWidth(1f);p.setStrokeCap(Paint.Cap.BUTT);p.setStrokeJoin(Paint.Join.MITER);p.setAntiAlias(false);}
}
