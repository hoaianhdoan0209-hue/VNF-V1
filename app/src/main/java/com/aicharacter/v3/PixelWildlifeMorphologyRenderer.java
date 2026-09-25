package com.aicharacter.v3;

import android.graphics.*;

/** Authored block-pixel morphology for all 25 base wildlife body plans. */
public final class PixelWildlifeMorphologyRenderer {
 private static final String[] BODY={
  "ribbon-frond","hollow-shell","tripod-reed","veil-wing","root-mantle",
  "glass-fin","spiral-back","lantern-bell","jointed-stilt","moss-armor",
  "silk-glider","petal-crown","ring-body","fan-tail","stone-pad",
  "mist-bladder","needle-limb","disk-crest","branch-antler","soft-spine",
  "coil-body","leaf-sail","drum-thorax","twin-keel","orbital-frill"
 };
 private PixelWildlifeMorphologyRenderer(){}

 public static int bodyPlanCount(){return BODY.length;}
 public static boolean supportsBodyPlan(String body){return index(body)>=0;}

 public static boolean drawBody(Canvas c,Paint p,String body,float x,float y,float w,float h,float anim,float rhythm,int base,int light,int dark){
  if(index(body)<0||c==null||p==null)return false;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setFilterBitmap(false);
  float u=Math.max(2f,PixelArtRenderPolicy.FRAME_SCALE*2f);
  switch(body){
   case "ribbon-frond": ribbon(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "hollow-shell": shell(c,p,x,y,w,h,base,light,dark,u);break;
   case "tripod-reed": tripod(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "veil-wing": veilWing(c,p,x,y,w,h,anim,rhythm,base,light,dark,u);break;
   case "root-mantle": rootMantle(c,p,x,y,w,h,base,light,dark,u);break;
   case "glass-fin": glassFin(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "spiral-back": spiralBack(c,p,x,y,w,h,base,light,dark,u);break;
   case "lantern-bell": lantern(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "jointed-stilt": stilt(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "moss-armor": mossArmor(c,p,x,y,w,h,base,light,dark,u);break;
   case "silk-glider": glider(c,p,x,y,w,h,anim,rhythm,base,light,dark,u);break;
   case "petal-crown": petal(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "ring-body": ring(c,p,x,y,w,h,base,light,dark,u);break;
   case "fan-tail": fanTail(c,p,x,y,w,h,base,light,dark,u);break;
   case "stone-pad": stonePad(c,p,x,y,w,h,base,light,dark,u);break;
   case "mist-bladder": bladder(c,p,x,y,w,h,anim,rhythm,base,light,dark,u);break;
   case "needle-limb": needle(c,p,x,y,w,h,base,light,dark,u);break;
   case "disk-crest": diskCrest(c,p,x,y,w,h,base,light,dark,u);break;
   case "branch-antler": antler(c,p,x,y,w,h,base,light,dark,u);break;
   case "soft-spine": softSpine(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "coil-body": coil(c,p,x,y,w,h,base,light,dark,u);break;
   case "leaf-sail": sail(c,p,x,y,w,h,anim,base,light,dark,u);break;
   case "drum-thorax": drum(c,p,x,y,w,h,base,light,dark,u);break;
   case "twin-keel": twin(c,p,x,y,w,h,base,light,dark,u);break;
   case "orbital-frill": orbital(c,p,x,y,w,h,anim,base,light,dark,u);break;
   default:return false;
  }
  return true;
 }

 public static boolean drawSense(Canvas c,Paint p,String sense,float x,float y,float w,float h,float anim,float alert,int light){
  if(sense==null||c==null||p==null)return false;
  float u=Math.max(2f,PixelArtRenderPolicy.FRAME_SCALE*2f),front=x+w*.31f,eyeY=y-h*.46f;
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);p.setColor(light);
  if("lumen-gradient".equals(sense)||"polarized-glow".equals(sense)){
   int n=alert>.55f?3:2;for(int i=0;i<n;i++)rect(c,p,Color.argb(150-i*32,Color.red(light),Color.green(light),Color.blue(light)),front+i*u,eyeY-i*u,front+(i+1)*u,eyeY+(i+1)*u);
  }else if("vibration-map".equals(sense)||"chemical-thread".equals(sense)||"echo-pressure".equals(sense)){
   float len=10+alert*14;seg(c,p,light,front,eyeY,front+len,eyeY-h*.16f,u);seg(c,p,light,front,eyeY+h*.06f,front+len*.84f,eyeY+h*.18f,u);
  }else if("humidity-field".equals(sense)||"airflow-vortex".equals(sense)){
   for(int i=0;i<3;i++){float xx=front+(8+i*6);rect(c,p,Color.argb(130-i*25,Color.red(light),Color.green(light),Color.blue(light)),xx,eyeY-i*u,xx+u,eyeY+(i+1)*u);}
  }else if("thermal-edge".equals(sense)||"surface-ripple".equals(sense)){
   for(int i=0;i<3;i++)seg(c,p,light,front+i*4,eyeY-h*.08f,front+8+i*4,eyeY-h*(.20f+i*.04f),u);
  }else if("electrostatic-drift".equals(sense)){
   for(int i=0;i<3;i++){double a=anim*(.7+i*.13)+i*2.1;float xx=front+(float)Math.cos(a)*(8+i*3),yy=eyeY+(float)Math.sin(a)*(5+i*2);rect(c,p,light,xx-u*.5f,yy-u*.5f,xx+u*.5f,yy+u*.5f);}
  }else if("root-tension".equals(sense)){
   seg(c,p,light,x-w*.16f,y-h*.12f,x-w*.22f,y+5,u);seg(c,p,light,x+w*.08f,y-h*.10f,x+w*.18f,y+6,u);
  }else return false;
  return true;
 }

 public static boolean drawDefense(Canvas c,Paint p,String defense,float x,float y,float w,float h,float anim,float alert,int dark,int light){
  if(defense==null||c==null||p==null)return false;
  float u=Math.max(2f,PixelArtRenderPolicy.FRAME_SCALE*2f);
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  if("warning shimmer".equals(defense)||"group flare".equals(defense)){
   int a=(int)(42+70*(.5+.5*Math.sin(anim*2.4)));int col=Color.argb(a,220,238,196);
   float l=x-w*.55f,r=x+w*.55f,t=y-h*.92f,b=y+h*.04f;frame(c,p,col,l,t,r,b,u);
  }else if("root-anchor".equals(defense)||"rapid burrow".equals(defense)){
   for(int i=-2;i<=2;i++)seg(c,p,dark,x+i*w*.12f,y-h*.06f,x+i*w*.18f,y+h*(.10f+alert*.08f),u);
  }else if("shell-fold".equals(defense)||"bitter surface film".equals(defense)){
   frame(c,p,light,x-w*.45f,y-h*.75f,x+w*.45f,y,u);
  }else if("mist discharge".equals(defense)){
   int col=Color.argb(60,218,231,224);for(int i=0;i<4;i++){float dx=(float)Math.sin(anim*.35+i)*w*.55f,dy=-h*.25f-i*5;rect(c,p,col,x+dx-u*(1+i*.25f),y+dy-u,x+dx+u*(1+i*.25f),y+dy+u);}
  }else if("air-brake dart".equals(defense)){
   seg(c,p,dark,x-w*.43f,y-h*.30f,x-w*.76f,y-h*.52f,u);seg(c,p,dark,x-w*.76f,y-h*.52f,x-w*.55f,y-h*.16f,u);
  }else if("vibration decoy".equals(defense)){
   float pulse=8+(float)(.5+.5*Math.sin(anim*2))*7;frame(c,p,Color.argb(100,190,212,185),x-w*.52f-pulse,y-h*.18f-pulse,x-w*.52f+pulse,y-h*.18f+pulse,u);
  }else if("stillness camouflage".equals(defense)){
   for(int i=0;i<5;i++){float xx=x-w*.30f+i*w*.15f,yy=y-h*(.25f+(i%2)*.18f);rect(c,p,Color.argb(90,45,66,54),xx-u,yy-u,xx+u,yy+u);}
  }else return false;
  return true;
 }

 private static void ribbon(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  float wave=(float)Math.sin(anim*.8)*u;
  for(int i=0;i<7;i++){float xx=x-w*.48f+i*w*.16f,yy=y-h*.32f-(i%2==0?u:2*u)+wave*(i/7f);rect(c,p,i%2==0?base:light,xx-u*1.6f,yy-u,xx+u*1.8f,yy+u);}
  rect(c,p,dark,x-w*.54f,y-h*.22f,x-w*.42f,y-h*.10f);rect(c,p,light,x+w*.05f,y-h*.60f,x+w*.20f,y-h*.36f);
 }
 private static void shell(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.50f,y-h*.78f,x+w*.48f,y-u);rect(c,p,base,x-w*.42f,y-h*.70f,x+w*.40f,y-2*u);rect(c,p,dark,x-w*.25f,y-h*.58f,x+w*.25f,y-h*.18f);rect(c,p,light,x-w*.16f,y-h*.49f,x+w*.14f,y-h*.26f);
 }
 private static void tripod(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.25f,y-h*.82f,x+w*.25f,y-h*.30f);rect(c,p,base,x-w*.18f,y-h*.75f,x+w*.18f,y-h*.34f);rect(c,p,light,x-w*.10f,y-h*.69f,x+w*.04f,y-h*.50f);
  for(int i=-1;i<=1;i++)seg(c,p,dark,x+i*w*.08f,y-h*.32f,x+i*w*.30f,y,u);
 }
 private static void veilWing(Canvas c,Paint p,float x,float y,float w,float h,float anim,float rhythm,int base,int light,int dark,float u){
  float flap=(float)Math.sin(anim*(2.0+rhythm*2.2))*h*.12f;
  rect(c,p,dark,x-2*u,y-h*.60f,x+2*u,y-h*.08f);rect(c,p,light,x-u,y-h*.55f,x+u,y-h*.12f);
  for(int i=0;i<4;i++){float dy=i*u*1.5f;rect(c,p,base,x-w*(.60f-i*.09f),y-h*.42f+dy-flap,x-2*u,y-h*.28f+dy);rect(c,p,base,x+2*u,y-h*.42f+dy+flap,x+w*(.60f-i*.09f),y-h*.28f+dy);}
 }
 private static void rootMantle(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  for(int i=0;i<5;i++){float half=w*(.14f+i*.075f),yy=y-h*.82f+i*h*.14f;rect(c,p,i==4?dark:base,x-half,yy,x+half,yy+h*.13f);}
  for(int i=-2;i<=2;i++)seg(c,p,dark,x+i*w*.10f,y-h*.18f,x+i*w*.18f,y+u,u);
  rect(c,p,light,x-w*.08f,y-h*.70f,x+w*.08f,y-h*.44f);
 }
 private static void glassFin(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.44f,y-h*.62f,x+w*.36f,y-h*.10f);rect(c,p,base,x-w*.36f,y-h*.56f,x+w*.30f,y-h*.16f);
  rect(c,p,light,x-w*.04f,y-h*.88f,x+w*.16f,y-h*.48f);rect(c,p,dark,x-w*.68f,y-h*.60f,x-w*.38f,y-h*.10f);rect(c,p,light,x-w*.63f,y-h*.50f,x-w*.43f,y-h*.20f);
 }
 private static void spiralBack(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.46f,y-h*.68f,x+w*.46f,y);rect(c,p,base,x-w*.38f,y-h*.60f,x+w*.38f,y-u);
  frame(c,p,light,x-w*.28f,y-h*.52f,x+w*.24f,y-h*.10f,u);frame(c,p,dark,x-w*.16f,y-h*.40f,x+w*.12f,y-h*.20f,u);
 }
 private static void lantern(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  for(int i=0;i<4;i++){float half=w*(.20f+i*.055f),yy=y-h*.82f+i*h*.13f;rect(c,p,i==3?dark:base,x-half,yy,x+half,yy+h*.14f);}
  rect(c,p,light,x-u,y-h*.56f,x+u,y-h*.36f);for(int i=-2;i<=2;i++)seg(c,p,dark,x+i*w*.08f,y-h*.24f,x+i*w*.11f,y+u,u);
 }
 private static void stilt(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.24f,y-h*.76f,x+w*.24f,y-h*.38f);rect(c,p,base,x-w*.17f,y-h*.70f,x+w*.17f,y-h*.43f);rect(c,p,light,x-w*.05f,y-h*.66f,x+w*.08f,y-h*.52f);
  for(int side=-1;side<=1;side+=2){seg(c,p,dark,x+side*w*.14f,y-h*.40f,x+side*w*.32f,y-h*.22f,u);seg(c,p,dark,x+side*w*.32f,y-h*.22f,x+side*w*.44f,y,u);}
 }
 private static void mossArmor(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.48f,y-h*.62f,x+w*.48f,y);rect(c,p,base,x-w*.42f,y-h*.55f,x+w*.42f,y-u);
  for(int i=-2;i<=2;i++)rect(c,p,(i&1)==0?light:dark,x+i*w*.16f-u*1.5f,y-h*.70f,x+i*w*.16f+u*1.5f,y-h*.44f);
 }
 private static void glider(Canvas c,Paint p,float x,float y,float w,float h,float anim,float rhythm,int base,int light,int dark,float u){
  float flap=(float)Math.sin(anim*(1.6+rhythm))*h*.08f;
  rect(c,p,dark,x-u,y-h*.74f,x+u,y-h*.08f);rect(c,p,light,x-u*.5f,y-h*.68f,x+u*.5f,y-h*.14f);
  for(int i=0;i<4;i++){float inset=i*w*.08f;rect(c,p,base,x-w*.66f+inset,y-h*.38f+i*u-flap,x-u,y-h*.22f+i*u);rect(c,p,base,x+u,y-h*.38f+i*u+flap,x+w*.66f-inset,y-h*.22f+i*u);}
 }
 private static void petal(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  rect(c,p,dark,x-3*u,y-h*.48f,x+3*u,y-h*.28f);rect(c,p,base,x-2*u,y-h*.44f,x+2*u,y-h*.32f);
  for(int i=0;i<6;i++){double a=i*Math.PI/3+anim*.06;float px=x+(float)Math.cos(a)*w*.28f,py=y-h*.39f+(float)Math.sin(a)*h*.26f;rect(c,p,light,px-2*u,py-u,px+2*u,py+u);}
 }
 private static void ring(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  float l=x-w*.42f,r=x+w*.42f,t=y-h*.70f,b=y-h*.04f;frame(c,p,dark,l,t,r,b,2*u);frame(c,p,base,l+u,t+u,r-u,b-u,u);rect(c,p,light,x-w*.30f,t+u,x-w*.05f,t+2*u);
 }
 private static void fanTail(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.30f,y-h*.56f,x+w*.36f,y-h*.06f);rect(c,p,base,x-w*.24f,y-h*.49f,x+w*.30f,y-h*.12f);
  for(int i=-2;i<=2;i++)seg(c,p,light,x-w*.24f,y-h*.28f,x-w*(.56f+Math.abs(i)*.05f),y-h*(.30f+i*.14f),u);
 }
 private static void stonePad(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.50f,y-h*.40f,x+w*.50f,y);rect(c,p,base,x-w*.42f,y-h*.34f,x+w*.42f,y-u);rect(c,p,light,x-w*.24f,y-h*.42f,x+w*.16f,y-h*.16f);rect(c,p,dark,x+w*.26f,y-h*.24f,x+w*.34f,y-h*.16f);
 }
 private static void bladder(Canvas c,Paint p,float x,float y,float w,float h,float anim,float rhythm,int base,int light,int dark,float u){
  float pulse=1f+(float)Math.sin(anim*(.8+rhythm))*.05f;
  for(int i=0;i<4;i++){float half=w*(.18f+i*.045f)*pulse,yy=y-h*.82f+i*h*.13f;rect(c,p,i==0?light:base,x-half,yy,x+half,yy+h*.14f);}
  for(int i=-2;i<=2;i++)seg(c,p,dark,x+i*w*.08f,y-h*.25f,x+i*w*.12f,y+u,u);
 }
 private static void needle(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.16f,y-h*.74f,x+w*.16f,y-h*.28f);rect(c,p,base,x-w*.10f,y-h*.68f,x+w*.10f,y-h*.34f);
  for(int i=-2;i<=2;i++){seg(c,p,dark,x+i*w*.06f,y-h*.44f,x+i*w*.28f,y,u);seg(c,p,light,x+i*w*.04f,y-h*.42f,x-i*w*.18f,y-h*.08f,u);}
 }
 private static void diskCrest(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.40f,y-h*.52f,x+w*.40f,y);rect(c,p,base,x-w*.33f,y-h*.45f,x+w*.33f,y-u);
  for(int i=0;i<4;i++){float yy=y-h*.90f+i*h*.10f,half=w*(.10f+i*.08f);rect(c,p,light,x-half,yy,x+half,yy+h*.09f);}rect(c,p,dark,x+w*.08f,y-h*.70f,x+w*.28f,y-h*.50f);
 }
 private static void antler(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.36f,y-h*.54f,x+w*.36f,y);rect(c,p,base,x-w*.29f,y-h*.47f,x+w*.29f,y-u);
  for(int side=-1;side<=1;side+=2){seg(c,p,light,x+side*w*.20f,y-h*.48f,x+side*w*.40f,y-h*.90f,u);seg(c,p,light,x+side*w*.32f,y-h*.72f,x+side*w*.54f,y-h*.78f,u);}
 }
 private static void softSpine(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  for(int i=0;i<6;i++){float px=x-w*.42f+i*w*.16f,py=y-h*.22f-(float)Math.sin(anim*.7+i*.7)*h*.08f;rect(c,p,dark,px-2.5f*u,py-3*u,px+2.5f*u,py+u);rect(c,p,i%2==0?base:light,px-2*u,py-2.5f*u,px+2*u,py+.5f*u);}
 }
 private static void coil(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  float l=x-w*.42f,r=x+w*.42f,t=y-h*.68f,b=y;frame(c,p,dark,l,t,r,b,2*u);frame(c,p,base,l+2*u,t+2*u,r-2*u,b-2*u,2*u);frame(c,p,light,l+4*u,t+4*u,r-4*u,b-4*u,u);
 }
 private static void sail(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  seg(c,p,dark,x-u,y,x+u,y-h*.86f,u);for(int i=0;i<5;i++){float yy=y-h*.80f+i*h*.14f,half=w*(.10f+i*.07f);rect(c,p,base,x-half,yy,x+w*.18f+i*u,yy+h*.12f);}rect(c,p,light,x-u,y-h*.75f,x+u,y-h*.16f);
 }
 private static void drum(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.34f,y-h*.68f,x+w*.28f,y-h*.05f);rect(c,p,base,x-w*.27f,y-h*.60f,x+w*.22f,y-h*.12f);rect(c,p,light,x-w*.08f,y-h*.62f,x+w*.38f,y-h*.18f);
  for(int i=-2;i<=2;i++)seg(c,p,dark,x+i*w*.08f,y-h*.22f,x+i*w*.20f,y,u);
 }
 private static void twin(Canvas c,Paint p,float x,float y,float w,float h,int base,int light,int dark,float u){
  rect(c,p,dark,x-w*.47f,y-h*.62f,x-u,y-h*.08f);rect(c,p,dark,x+u,y-h*.62f,x+w*.47f,y-h*.08f);rect(c,p,base,x-w*.39f,y-h*.55f,x-2*u,y-h*.14f);rect(c,p,light,x+2*u,y-h*.55f,x+w*.39f,y-h*.14f);rect(c,p,dark,x-u,y-h*.68f,x+u,y-h*.02f);
 }
 private static void orbital(Canvas c,Paint p,float x,float y,float w,float h,float anim,int base,int light,int dark,float u){
  rect(c,p,dark,x-3*u,y-h*.48f,x+3*u,y-h*.28f);rect(c,p,base,x-2*u,y-h*.44f,x+2*u,y-h*.32f);
  for(int i=0;i<8;i++){double a=i*Math.PI/4+anim*.18;float px=x+(float)Math.cos(a)*w*.36f,py=y-h*.38f+(float)Math.sin(a)*h*.30f;rect(c,p,i%2==0?light:dark,px-u,py-u,px+u,py+u);}
 }

 private static int index(String body){if(body==null)return-1;for(int i=0;i<BODY.length;i++)if(BODY[i].equals(body))return i;return-1;}
 private static float sn(float v){return PixelArtRenderPolicy.snapLogical(v);}
 private static void rect(Canvas c,Paint p,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(sn(l),sn(t),sn(r),sn(b),p);}
 private static void frame(Canvas c,Paint p,int color,float l,float t,float r,float b,float thick){rect(c,p,color,l,t,r,t+thick);rect(c,p,color,l,b-thick,r,b);rect(c,p,color,l,t,l+thick,b);rect(c,p,color,r-thick,t,r,b);}
 private static void seg(Canvas c,Paint p,int color,float x1,float y1,float x2,float y2,float u){
  int steps=Math.max(1,(int)(Math.max(Math.abs(x2-x1),Math.abs(y2-y1))/Math.max(1,u)));
  for(int i=0;i<=steps;i++){float t=i/(float)steps,x=x1+(x2-x1)*t,y=y1+(y2-y1)*t;rect(c,p,color,x-u*.55f,y-u*.55f,x+u*.55f,y+u*.55f);}
 }
}
