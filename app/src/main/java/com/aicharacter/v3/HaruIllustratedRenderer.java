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

  float hipY=ground-(sit||crouch?74f:114f)-bounce;
  float shoulderY=hipY-(sit||crouch?73f:94f);
  float profile=(v.isWalk()||search)?.62f:0f;
  float torsoLean=v.isWalk()?stride*2.6f:0f;
  float headY=shoulderY-53f+(think?2f:0f);float headTilt=think?-4f:react?4f:search?-3f:torsoLean*.18f;

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
  else drawStandingLegs(c,p,x,hipY,ground,stride,bodyAlpha,v.isWalk());

  // Torso outline then clothing.
  drawTorso(c,p,x+torsoLean,shoulderY,hipY,breath,bodyAlpha,phase,area,profile);

  // Arms are stateful rather than generic mirrored sticks.
  if(think)drawThinkArms(c,p,x,shoulderY,headY,bodyAlpha);
  else if(search)drawSearchArms(c,p,x,shoulderY,headY,bodyAlpha);
  else if(react)drawReactArms(c,p,x,shoulderY,hipY,stride,bodyAlpha);
  else drawNaturalArms(c,p,x+torsoLean,shoulderY,hipY,stride,v.isWalk(),profile,bodyAlpha);

  // Neck and head.
  p.setColor(withAlpha(Color.rgb(222,174,143),bodyAlpha));c.drawRoundRect(x+torsoLean-8,headY+25,x+torsoLean+8,shoulderY+13,7,7,p);
  p.setColor(withAlpha(Color.rgb(198,145,122),Math.min(bodyAlpha,92)));c.drawRoundRect(x+torsoLean+1,headY+28,x+torsoLean+7,shoulderY+12,4,4,p);
  drawHead(c,p,s,x+torsoLean*.72f,headY,headTilt,anim,v,bodyAlpha,closeT,profile);

  // Small cloth details make the silhouette read as a person rather than a mannequin.
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f);p.setColor(withAlpha(Color.rgb(202,177,151),Math.min(210,bodyAlpha)));
  c.drawLine(x-22,shoulderY+40,x+19,shoulderY+40,p);
  p.setStyle(Paint.Style.FILL);

  restore(p);
 }

 private static void drawStandingLegs(Canvas c,Paint p,float x,float hipY,float ground,float stride,int alpha,boolean walking){
  if(!walking){
   drawLimb(c,p,x-12,hipY+8,x-14,hipY+57,x-13,ground-16,Color.rgb(55,59,72),17,alpha);
   drawLimb(c,p,x+12,hipY+8,x+14,hipY+57,x+13,ground-16,Color.rgb(55,59,72),17,alpha);
   drawBoot(c,p,x-13,ground-8,-1,alpha);drawBoot(c,p,x+13,ground-8,1,alpha);
   return;
  }
  float phase=Math.max(-1f,Math.min(1f,stride));
  float forward=phase*24f;
  float liftA=Math.max(0f,phase)*13f, liftB=Math.max(0f,-phase)*13f;
  float hipSpread=9f;
  float kneeAX=x-hipSpread+forward*.42f,kneeBX=x+hipSpread-forward*.42f;
  float footAX=x-7+forward,footBX=x+7-forward;
  // Far leg first, slightly slimmer/darker to create depth.
  drawLimb(c,p,x+hipSpread,hipY+9,kneeBX,hipY+56-liftB*.25f,footBX,ground-16-liftB,Color.rgb(47,52,64),14.5f,Math.min(alpha,220));
  drawBoot(c,p,footBX,ground-8-liftB,phase>0?-1:1,Math.min(alpha,225));
  drawLimb(c,p,x-hipSpread,hipY+8,kneeAX,hipY+54-liftA*.32f,footAX,ground-16-liftA,Color.rgb(59,64,78),17,alpha);
  drawBoot(c,p,footAX,ground-8-liftA,phase>0?1:-1,alpha);
  // Knee highlights keep the silhouette from reading as two tubes.
  p.setColor(withAlpha(Color.rgb(86,91,104),Math.min(alpha,92)));
  c.drawCircle(kneeAX,hipY+54-liftA*.32f,4.3f,p);
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

 private static void drawTorso(Canvas c,Paint p,float x,float shoulderY,float hipY,float breath,int alpha,String phase,String area,float profile){
  float waistY=shoulderY+(hipY-shoulderY)*.60f;
  float far=1f-profile*.16f;
  // Soft silhouette shadow rather than a thick cartoon outline.
  p.setColor(withAlpha(Color.rgb(46,37,41),Math.min(alpha,170)));
  Path shadow=new Path();shadow.moveTo(x-31,shoulderY+5);shadow.cubicTo(x-36,shoulderY+26,x-29,waistY,x-24,hipY+7);
  shadow.quadTo(x,hipY+14,x+24*far,hipY+7);shadow.cubicTo(x+29*far,waistY,x+35*far,shoulderY+25,x+30*far,shoulderY+5);
  shadow.quadTo(x,shoulderY-7,x-31,shoulderY+5);shadow.close();c.drawPath(shadow,p);

  // Main blouse with a visible waist and gentle breathing.
  p.setColor(withAlpha(Color.rgb(168,99,108),alpha));
  Path shirt=new Path();shirt.moveTo(x-27,shoulderY+5);shirt.cubicTo(x-30,shoulderY+28,x-22,waistY,x-19,hipY+3);
  shirt.quadTo(x,hipY+9+breath*.10f,x+19*far,hipY+3);shirt.cubicTo(x+22*far,waistY,x+30*far,shoulderY+28,x+27*far,shoulderY+5);
  shirt.quadTo(x,shoulderY-5,x-27,shoulderY+5);shirt.close();c.drawPath(shirt,p);

  // Light cloth plane gives volume instead of flat fill.
  p.setColor(withAlpha(Color.rgb(203,129,134),Math.min(alpha,94)));
  Path light=new Path();light.moveTo(x-18,shoulderY+10);light.cubicTo(x-16,shoulderY+32,x-13,waistY,x-10,hipY);
  light.lineTo(x-2,hipY+3);light.cubicTo(x-5,waistY,x-5,shoulderY+30,x-7,shoulderY+8);light.close();c.drawPath(light,p);

  // Open moss-green vest: narrower, tapered, not two rigid planks.
  int vest=Color.rgb(70,101,78),vestHi=Color.rgb(111,137,103);
  p.setColor(withAlpha(vest,alpha));
  Path lv=new Path();lv.moveTo(x-23,shoulderY+15);lv.quadTo(x-15,shoulderY+24,x-7,shoulderY+31);lv.lineTo(x-9,hipY+2);lv.lineTo(x-20,hipY);lv.close();c.drawPath(lv,p);
  Path rv=new Path();rv.moveTo(x+23*far,shoulderY+15);rv.quadTo(x+15*far,shoulderY+24,x+7*far,shoulderY+31);rv.lineTo(x+9*far,hipY+2);rv.lineTo(x+20*far,hipY);rv.close();c.drawPath(rv,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(withAlpha(vestHi,Math.min(alpha,126)));
  c.drawLine(x-16,shoulderY+25,x-13,hipY-3,p);c.drawLine(x+16*far,shoulderY+25,x+13*far,hipY-3,p);p.setStyle(Paint.Style.FILL);

  // Collar/upper chest breaks the neck-to-torso mannequin joint.
  p.setColor(withAlpha(Color.rgb(228,187,160),alpha));c.drawRoundRect(x-18,shoulderY-4,x+18*far,shoulderY+10,7,7,p);
  p.setColor(withAlpha(Color.rgb(194,125,130),alpha));c.drawRoundRect(x-16,shoulderY,x+16*far,shoulderY+5,3,3,p);

  int hi="NIGHT".equals(phase)?Color.rgb(149,164,170):"quiet_grove".equals(area)?Color.rgb(136,156,118):Color.rgb(149,163,126);
  p.setColor(withAlpha(hi,Math.min(alpha,72)));c.drawRoundRect(x+9,shoulderY+32,x+12,hipY-8,2,2,p);
 }

 private static void drawNaturalArms(Canvas c,Paint p,float x,float shoulderY,float hipY,float stride,boolean walking,float profile,int alpha){
  if(!walking){
   drawSleevedArm(c,p,x-29,shoulderY+11,x-36,shoulderY+49,x-30,hipY-3,alpha);
   drawSleevedArm(c,p,x+29,shoulderY+11,x+34,shoulderY+54,x+28,hipY-7,Math.min(alpha,238));
   return;
  }
  float swing=stride*24f,far=1f-profile*.18f;
  // Far arm first; elbows bend naturally and swing opposite the near leg.
  drawSleevedArm(c,p,x+27*far,shoulderY+11,x+33*far+swing*.22f,shoulderY+48,x+24*far+swing*.72f,hipY-8,Math.min(alpha,218));
  drawSleevedArm(c,p,x-29,shoulderY+10,x-34-swing*.24f,shoulderY+46,x-24-swing*.78f,hipY-9,alpha);
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

 private static void drawHead(Canvas c,Paint p,WorldState s,float x,float y,float tilt,float anim,GirlAnimationController.Visual v,int alpha,float closeT,float profile){
  float faceX=x+profile*3.2f;c.save();c.rotate(tilt,x,y);
  float far=1f-profile*.16f;

  // Hair silhouette first; slightly asymmetric so the head is not a perfect doll oval.
  p.setColor(withAlpha(Color.rgb(55,37,39),alpha));
  Path hairMass=new Path();hairMass.moveTo(x-34,y-17);hairMass.cubicTo(x-34,y-49,x-15,y-58,x+3,y-57);
  hairMass.cubicTo(x+26,y-57,x+36,y-40,x+35,y-14);hairMass.lineTo(x+31,y+35);
  hairMass.cubicTo(x+17,y+43,x-19,y+42,x-32,y+31);hairMass.close();c.drawPath(hairMass,p);

  // Ears are mostly tucked behind hair.
  p.setColor(withAlpha(Color.rgb(219,171,142),alpha));
  c.drawOval(x-32,y-9,x-25,y+8,p);c.drawOval(x+26,y-9,x+33,y+8,p);

  // Softer jaw/cheek contour, no heavy black face outline.
  p.setColor(withAlpha(Color.rgb(232,188,157),alpha));
  Path face=new Path();face.moveTo(faceX-26,y-29);face.cubicTo(faceX-31,y-8,faceX-27,y+19,faceX-15,y+31);
  face.quadTo(faceX,y+40,faceX+15*far,y+31);face.cubicTo(faceX+27*far,y+18,faceX+30*far,y-8,faceX+25*far,y-29);
  face.quadTo(faceX,y-38,faceX-26,y-29);face.close();c.drawPath(face,p);

  // Temple/cheek shading adds volume.
  p.setColor(withAlpha(Color.rgb(199,145,126),Math.min(alpha,42)));
  c.drawOval(faceX+13,y-18,faceX+27*far,y+24,p);
  p.setColor(withAlpha(Color.rgb(244,207,177),Math.min(alpha,52)));
  c.drawOval(faceX-20,y-22,faceX-7,y+15,p);

  // Fringe and crown, with a restrained warm highlight.
  p.setColor(withAlpha(Color.rgb(58,39,40),alpha));
  Path fringe=new Path();fringe.moveTo(x-29,y-24);fringe.cubicTo(x-25,y-50,x+23,y-52,x+31,y-23);
  fringe.cubicTo(x+20,y-31,x+14,y-23,x+6,y-35);fringe.cubicTo(x-2,y-23,x-11,y-31,x-18,y-18);
  fringe.cubicTo(x-23,y-16,x-27,y-19,x-29,y-24);fringe.close();c.drawPath(fringe,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f);p.setStrokeCap(Paint.Cap.ROUND);
  p.setColor(withAlpha(Color.rgb(105,73,68),Math.min(alpha,86)));
  c.drawArc(x-20,y-46,x+21,y-18,205,95,false,p);p.setStyle(Paint.Style.FILL);

  boolean blink=((anim+0.37f)%4.7f)<.11f;
  float mood=s.mood==null?0f:(float)Math.max(-1,Math.min(1,(s.mood.pleasantness-.5)*2.0));
  double sadness=s.emotion==null?0:s.emotion.sadness,fear=s.emotion==null?0:s.emotion.fear,pain=s.body==null?0:s.body.pain;
  boolean genuinelyDown=sadness>.42||fear>.44||pain>30;
  float eyeY=y-3f,gaze=profile*1.9f;
  try{HaruExpressionEngine.Visual e=HaruExpressionEngine.derive(s,Math.max(s.lastSimulatedAt,s.lastOpenedAt));gaze+=(float)Math.max(-1.7,Math.min(1.7,e.gazeX*1.55));}catch(Throwable ignored){}

  // Eyes: smaller whites, warm iris, upper-lid line and always-present catchlight.
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.0f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(withAlpha(Color.rgb(76,50,48),alpha));
  if(blink){
   c.drawLine(faceX-17,eyeY,faceX-8,eyeY,p);c.drawLine(faceX+8,eyeY,faceX+17*far,eyeY,p);
  }else{
   p.setStyle(Paint.Style.FILL);p.setColor(withAlpha(Color.rgb(247,235,218),alpha));
   c.drawOval(faceX-18,eyeY-3.4f,faceX-8,eyeY+4.1f,p);c.drawOval(faceX+8,eyeY-3.4f,faceX+8+10*far,eyeY+4.1f,p);
   p.setColor(withAlpha(Color.rgb(103,76,58),alpha));c.drawCircle(faceX-13+gaze,eyeY+.5f,3.45f,p);c.drawCircle(faceX+13*far+gaze,eyeY+.5f,3.25f,p);
   p.setColor(withAlpha(Color.rgb(45,39,35),alpha));c.drawCircle(faceX-13+gaze,eyeY+.7f,1.65f,p);c.drawCircle(faceX+13*far+gaze,eyeY+.7f,1.55f,p);
   p.setColor(withAlpha(Color.WHITE,Math.min(alpha,205)));c.drawCircle(faceX-12.1f+gaze,eyeY-.7f,1.05f,p);c.drawCircle(faceX+13.8f*far+gaze,eyeY-.7f,.95f,p);
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setColor(withAlpha(Color.rgb(70,47,46),alpha));
   c.drawArc(faceX-19,eyeY-5.2f,faceX-7,eyeY+4.5f,202,132,false,p);
   c.drawArc(faceX+7,eyeY-5.2f,faceX+20*far,eyeY+4.5f,206,128,false,p);
  }

  // Brows remain relaxed unless the state is genuinely negative.
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.75f);p.setColor(withAlpha(Color.rgb(78,52,50),Math.min(alpha,215)));
  float reactLift=v.state==GirlAnimationController.State.REACT?-2.2f:v.state==GirlAnimationController.State.THINK?.6f:0f;
  if(genuinelyDown){
   c.drawLine(faceX-18,eyeY-10+reactLift,faceX-8,eyeY-12+reactLift,p);
   c.drawLine(faceX+8,eyeY-12+reactLift,faceX+18*far,eyeY-10+reactLift,p);
  }else{
   // Calm brows: inner ends sit slightly lower than the outer arch; avoid a permanent worried face.
   c.drawLine(faceX-18,eyeY-12.6f+reactLift,faceX-8,eyeY-11.6f+reactLift,p);
   c.drawLine(faceX+8,eyeY-11.6f+reactLift,faceX+18*far,eyeY-12.6f+reactLift,p);
  }

  // Small nose and stateful mouth.
  p.setStrokeWidth(1.2f);p.setColor(withAlpha(Color.rgb(184,129,110),Math.min(alpha,150)));
  c.drawLine(faceX+profile*1.7f,y+1,faceX-1+profile*2.7f,y+8,p);
  float mouthY=y+19;p.setStrokeWidth(1.65f);p.setColor(withAlpha(Color.rgb(132,76,78),alpha));
  Path mouth=new Path();
  if(v.state==GirlAnimationController.State.REACT){mouth.moveTo(faceX-6,mouthY);mouth.quadTo(faceX,mouthY+4,faceX+6,mouthY);}
  else if(genuinelyDown){mouth.moveTo(faceX-6,mouthY+2);mouth.quadTo(faceX,mouthY-1.5f,faceX+6,mouthY+2);}
  else if(mood>.12f){mouth.moveTo(faceX-7,mouthY-1);mouth.quadTo(faceX,mouthY+3.2f,faceX+7,mouthY-1);}
  else{mouth.moveTo(faceX-6.5f,mouthY-.7f);mouth.quadTo(faceX,mouthY+2.0f,faceX+6.5f,mouthY-.7f);}
  c.drawPath(mouth,p);

  // Warm cheek tone only when the face is visible enough.
  p.setStyle(Paint.Style.FILL);p.setColor(withAlpha(Color.rgb(215,130,126),Math.min(alpha,(int)(30+18*closeT))));
  c.drawOval(faceX-24,y+8,faceX-14,y+13,p);c.drawOval(faceX+14*far,y+8,faceX+24*far,y+13,p);
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
