package com.aicharacter.v3;

import android.graphics.*;

/**
 * Procedural pixel feline renderer.
 * Uses cat physics, rig and social state; never writes back to simulation.
 */
public final class CatVisualRenderer {
 private static final int COAT=Color.rgb(111,96,82),COAT_DARK=Color.rgb(72,62,56),COAT_LIGHT=Color.rgb(166,139,109);
 private static final int MUZZLE=Color.rgb(210,184,147),EYE=Color.rgb(219,207,117),INK=Color.rgb(43,39,38);
 private CatVisualRenderer(){}

 public static void draw(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float x,float ground,float anim,float cameraZoom,float divinePresence){
  if(c==null||p==null||s==null||v==null||v.state==CatAnimationController.State.ATTACHED)return;
  float close=Math.max(0,Math.min(1,(cameraZoom-1f)/.56f)),scale=1.05f+close*.08f;
  double gp=s.catRig==null?0:s.catRig.gaitPhase;float phase=(float)(gp*Math.PI*2),bob=v.moving()?(float)Math.sin(phase*2)*1.3f:idleBreath(s,anim);
  float bodyY=ground-38*scale+bob;
  if(v.state==CatAnimationController.State.SETTLE)bodyY+=8*scale;
  if(v.state==CatAnimationController.State.BRACE)bodyY+=5*scale;

  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setColorFilter(null);p.setAlpha(255);
  drawShadow(c,p,x,ground,v,close);
  c.save();c.scale(v.facingRight?1f:-1f,1f,x,ground);
  if(v.state==CatAnimationController.State.SLEEP)drawSleep(c,p,s,v,x,ground,anim,scale,divinePresence);
  else drawAwake(c,p,s,v,x,bodyY,ground,anim,phase,scale,close,divinePresence);
  c.restore();
 }

 public static void drawAttached(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float haruX,float haruGround,float anim,float cameraZoom,float divinePresence){
  if(v==null||v.state!=CatAnimationController.State.ATTACHED)return;
  float close=Math.max(0,Math.min(1,(cameraZoom-1f)/.56f)),sc=.86f+close*.10f;
  float x=haruX+41f,y=haruGround-HaruVisualRenderer.authoredFrameHeight()*HaruVisualRenderer.fixedBodyScale()*.57f+(float)Math.sin(anim*.72f)*1.1f;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setColorFilter(null);p.setAlpha(255);
  p.setColor(Color.argb((int)(24+18*close),0,0,0));c.drawOval(x-14*sc,y+15*sc,x+14*sc,y+21*sc,p);
  drawHead(c,p,x,y,sc,true,.72f,.22f,divinePresence);
  p.setColor(COAT_DARK);c.drawRect(x-9*sc,y+11*sc,x+10*sc,y+19*sc,p);
  p.setColor(COAT);c.drawRect(x-7*sc,y+10*sc,x+8*sc,y+16*sc,p);
 }

 private static void drawAwake(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float x,float bodyY,float ground,float anim,float phase,float sc,float close,float divine){
  float guarded=(float)v.guardedness,attention=(float)v.attention;
  float crouch=v.state==CatAnimationController.State.RETREAT?5.5f:v.state==CatAnimationController.State.BRACE?7f:v.state==CatAnimationController.State.APPROACH?2f:v.state==CatAnimationController.State.RUB?1.2f:0;
  float bodyH=(23-crouch*.45f)*sc,bodyW=(52+(v.state==CatAnimationController.State.BRACE?4:0))*sc;
  float headX=x+31*sc,headY=bodyY-7*sc+crouch*.28f*sc;
  float lean=s.catRig==null?0:(float)Math.max(-.10,Math.min(.10,s.catRig.trunkLean))*34f*sc;
  headX+=lean;if(v.state==CatAnimationController.State.RUB){headX+=(5.2f+(float)Math.sin(anim*1.9f)*3.0f)*sc;headY+=(float)Math.sin(anim*3.8f)*1.1f*sc;}

  drawTail(c,p,v,x-bodyW*.44f,bodyY+5*sc,anim,sc,guarded,attention);
  p.setColor(COAT_DARK);c.drawRect(x-bodyW*.50f,bodyY-bodyH*.47f,x+bodyW*.48f,bodyY+bodyH*.48f,p);
  p.setColor(COAT);c.drawRect(x-bodyW*.44f,bodyY-bodyH*.42f,x+bodyW*.43f,bodyY+bodyH*.37f,p);
  p.setColor(COAT_LIGHT);c.drawRect(x-bodyW*.30f,bodyY-bodyH*.37f,x+bodyW*.16f,bodyY-bodyH*.22f,p);

  drawLegs(c,p,s,v,x,bodyY,ground,phase,sc);
  double gaze=s.catSocial!=null&&"girl".equals(s.catSocial.gazeTarget)?Math.signum(s.haruX-s.catState.x):1;
  float earBack=v.state==CatAnimationController.State.RETREAT||guarded>.62f?.72f:v.state==CatAnimationController.State.BRACE?.42f:0;
  drawHead(c,p,headX,headY,sc,false,attention,earBack,divine);
  drawFace(c,p,headX,headY,sc,gaze,attention,guarded,close);

  if(v.state==CatAnimationController.State.RUB){p.setColor(COAT_LIGHT);float rub=(float)Math.sin(anim*1.9f)*2.2f*sc;c.drawRect(headX-10*sc+rub,headY+7*sc,headX-2*sc+rub,headY+12*sc,p);}
  if(v.state==CatAnimationController.State.SETTLE){
   p.setColor(COAT_DARK);c.drawRect(x-18*sc,bodyY+9*sc,x+17*sc,ground-3*sc,p);
   p.setColor(COAT);c.drawRect(x-14*sc,bodyY+7*sc,x+14*sc,ground-5*sc,p);
  }
 }

 private static void drawLegs(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float x,float bodyY,float ground,float phase,float sc){
  float gait=v.moving()?1f:0f,step=(float)Math.sin(phase),step2=(float)Math.sin(phase+Math.PI);
  float frontX=x+21*sc,hindX=x-20*sc;
  float fA=gait*step*5.2f*sc,hA=gait*step2*5.2f*sc;
  if(v.state==CatAnimationController.State.BRACE){frontX+=5*sc;hindX-=5*sc;fA=hA=0;}
  drawLeg(c,p,frontX+fA,bodyY+8*sc,ground,sc,COAT);
  drawLeg(c,p,frontX-4*sc-fA*.65f,bodyY+8*sc,ground,sc,COAT_DARK);
  drawLeg(c,p,hindX+hA,bodyY+8*sc,ground,sc,COAT);
  drawLeg(c,p,hindX-4*sc-hA*.65f,bodyY+8*sc,ground,sc,COAT_DARK);
 }
 private static void drawLeg(Canvas c,Paint p,float x,float y,float ground,float sc,int color){
  p.setColor(color);float knee=Math.min(ground-8*sc,y+13*sc);c.drawRect(x-2.2f*sc,y,x+2.2f*sc,knee,p);c.drawRect(x-2.5f*sc,knee,x+3.4f*sc,ground-3*sc,p);p.setColor(COAT_DARK);c.drawRect(x-2.8f*sc,ground-4*sc,x+4.5f*sc,ground-1.4f*sc,p);
 }

 private static void drawTail(Canvas c,Paint p,CatAnimationController.Visual v,float x,float y,float anim,float sc,float guarded,float attention){
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.SQUARE);p.setStrokeJoin(Paint.Join.MITER);p.setStrokeWidth(5.2f*sc);p.setColor(COAT_DARK);
  float sway=(float)Math.sin(anim*(v.moving()?2.0:.72)+.8)*7*sc;
  Path tail=new Path();tail.moveTo(x,y);
  if(v.state==CatAnimationController.State.APPROACH||v.state==CatAnimationController.State.RUB||attention>.62f){tail.cubicTo(x-18*sc,y-7*sc,x-21*sc+sway,y-28*sc,x-8*sc+sway*.35f,y-34*sc);}
  else if(v.state==CatAnimationController.State.RETREAT||guarded>.58f){tail.cubicTo(x-18*sc,y+2*sc,x-28*sc+sway*.25f,y+8*sc,x-39*sc+sway*.45f,y+5*sc);}
  else{tail.cubicTo(x-17*sc,y-5*sc,x-30*sc+sway*.35f,y-10*sc,x-35*sc+sway*.55f,y-2*sc);}
  c.drawPath(tail,p);p.setStrokeWidth(3.0f*sc);p.setColor(COAT);c.drawPath(tail,p);p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
 }

 private static void drawHead(Canvas c,Paint p,float x,float y,float sc,boolean attached,float attention,float earBack,float divine){
  float w=(attached?21:24)*sc,h=(attached?20:23)*sc;
  p.setColor(COAT_DARK);c.drawRect(x-w*.54f,y-h*.47f,x+w*.54f,y+h*.50f,p);
  p.setColor(COAT);c.drawRect(x-w*.47f,y-h*.42f,x+w*.47f,y+h*.42f,p);
  Path le=new Path(),re=new Path();float ey=y-h*.40f,spread=w*.31f,tipY=y-h*(.86f-earBack*.16f),back=earBack*4*sc;
  le.moveTo(x-spread-6*sc+back,ey);le.lineTo(x-spread,tipY);le.lineTo(x-spread+5*sc,ey);le.close();
  re.moveTo(x+spread-5*sc,ey);re.lineTo(x+spread-back,tipY);re.lineTo(x+spread+6*sc-back,ey);re.close();
  p.setColor(COAT_DARK);c.drawPath(le,p);c.drawPath(re,p);
  p.setColor(COAT_LIGHT);Path li=new Path();li.moveTo(x-spread-3*sc+back*.6f,ey-1*sc);li.lineTo(x-spread,tipY+4*sc);li.lineTo(x-spread+2*sc,ey-1*sc);li.close();c.drawPath(li,p);
  if(divine>.05f){int a=(int)(16+28*Math.max(0,Math.min(1,divine)));p.setColor(Color.argb(a,244,226,186));c.drawRect(x-w*.43f,y-h*.40f,x+w*.39f,y-h*.28f,p);}
 }

 private static void drawFace(Canvas c,Paint p,float x,float y,float sc,double gaze,float attention,float guarded,float close){
  float eyeY=y-1.5f*sc,dx=5.6f*sc,open=(.85f+(attention*.35f)-(guarded*.12f))*sc;
  p.setColor(MUZZLE);c.drawRect(x-7*sc,y+3*sc,x+8*sc,y+9*sc,p);
  p.setColor(EYE);c.drawRect(x-dx-2*sc,eyeY-open,x-dx+2*sc,eyeY+open,p);c.drawRect(x+dx-2*sc,eyeY-open,x+dx+2*sc,eyeY+open,p);
  float gx=(float)Math.max(-1,Math.min(1,gaze))*1.15f*sc;p.setColor(INK);c.drawRect(x-dx-.7f*sc+gx,eyeY-1.3f*sc,x-dx+.7f*sc+gx,eyeY+1.3f*sc,p);c.drawRect(x+dx-.7f*sc+gx,eyeY-1.3f*sc,x+dx+.7f*sc+gx,eyeY+1.3f*sc,p);
  p.setColor(Color.rgb(116,75,67));c.drawRect(x-1.5f*sc,y+4*sc,x+1.5f*sc,y+6.2f*sc,p);
  p.setColor(INK);c.drawRect(x-.7f*sc,y+6.2f*sc,x+.7f*sc,y+8.8f*sc,p);
  if(close>.16f){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,sc*.65f));for(int i=-1;i<=1;i+=2){float sy=y+6.5f*sc+i*1.2f*sc;c.drawLine(x+i*3*sc,sy,x+i*13*sc,sy+i*.5f*sc,p);}p.setStyle(Paint.Style.FILL);}
 }

 private static void drawSleep(Canvas c,Paint p,WorldState s,CatAnimationController.Visual v,float x,float ground,float anim,float sc,float divine){
  float breath=(float)Math.sin(anim*.72f)*1.2f*sc,y=ground-20*sc+breath;
  p.setColor(COAT_DARK);c.drawOval(x-30*sc,y-15*sc,x+30*sc,y+15*sc,p);
  p.setColor(COAT);c.drawOval(x-26*sc,y-12*sc,x+25*sc,y+12*sc,p);
  p.setColor(COAT_LIGHT);c.drawRect(x-14*sc,y-9*sc,x+7*sc,y-5*sc,p);
  drawHead(c,p,x+19*sc,y-7*sc,sc*.84f,false,.1f,.18f,divine);
  p.setColor(INK);c.drawRect(x+14*sc,y-8*sc,x+17*sc,y-7*sc,p);c.drawRect(x+22*sc,y-8*sc,x+25*sc,y-7*sc,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.SQUARE);p.setStrokeWidth(4.2f*sc);p.setColor(COAT_DARK);Path t=new Path();t.moveTo(x-20*sc,y+5*sc);t.cubicTo(x-37*sc,y+13*sc,x-7*sc,y+19*sc,x+9*sc,y+10*sc);c.drawPath(t,p);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
 }

 private static void drawShadow(Canvas c,Paint p,float x,float ground,CatAnimationController.Visual v,float close){
  float w=(v.state==CatAnimationController.State.SLEEP?37:v.state==CatAnimationController.State.SETTLE?31:34)*(1+close*.08f),a=v.moving()?30:38;p.setColor(Color.argb((int)(a+close*8),0,0,0));c.drawOval(x-w,ground-4,x+w,ground+6,p);p.setColor(Color.argb(14,23,28,25));c.drawOval(x-w*.66f,ground-2,x+w*.66f,ground+4,p);
 }
 private static float idleBreath(WorldState s,float anim){double drive=s.catRespiration==null?.08:Math.max(0,Math.min(1,s.catRespiration.breathingDrive));return (float)Math.sin(anim*(.72+drive*1.8))*(.35f+(float)drive*.8f);}
}
