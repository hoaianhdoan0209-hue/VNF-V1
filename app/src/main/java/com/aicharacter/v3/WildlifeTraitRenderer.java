package com.aicharacter.v3;

import android.graphics.*;

/**
 * Visible phenotype renderer for dynamically manifested wildlife.
 * Hidden species metadata drives morphology, locomotion motion, senses and defense,
 * while Haru still receives only ordinary visual evidence from the rendered body.
 */
public final class WildlifeTraitRenderer {
 private static final int[][] PALETTE={
  {92,132,111},{122,111,150},{85,142,153},{148,118,91},{105,142,91},
  {158,112,130},{91,126,157},{157,143,93},{100,151,137},{139,105,151}
 };
 private WildlifeTraitRenderer(){}

 public static boolean draw(Canvas c,Paint p,WorldState world,WorldObject o,CreatureLifeState life,float x,float anim){
  if(c==null||p==null||o==null||life==null||!WildlifeManifestationEngine.isDynamic(o))return false;
  SpeciesEvolutionCatalog.Species species=SpeciesEvolutionCatalog.get(o.dictionaryRef);
  if(species==null)return false;

  x=PixelArtRenderPolicy.snapLogical(x);
  float ground=PixelArtRenderPolicy.snapLogical(CreaturePhysicsEngine.renderGroundY(world,o,life));
  float y=PixelArtRenderPolicy.snapLogical(ground-(float)WorldUnits.mToPx(life.verticalOffsetM));
  float w=PixelArtRenderPolicy.snapLogical(Math.max(24f,o.width*1.42f)),h=PixelArtRenderPolicy.snapLogical(Math.max(18f,o.height*1.32f));
  float motion=motionOffset(species,life,anim),bodyY=PixelArtRenderPolicy.snapLogical(y+motion);
  float rhythm=(float)Math.max(.15,Math.min(1,life.body==null?.45:life.body.rhythm));
  float alert=(float)Math.max(0,Math.min(1,life.sense==null?.2:life.sense.alertness));
  int clade=parseTail(species.cladeId)%PALETTE.length;
  int[] rgb=PALETTE[clade];
  int base=Color.rgb(rgb[0],rgb[1],rgb[2]);
  int light=Color.rgb(Math.min(255,rgb[0]+52),Math.min(255,rgb[1]+52),Math.min(255,rgb[2]+46));
  int dark=Color.rgb(Math.max(0,rgb[0]-40),Math.max(0,rgb[1]-40),Math.max(0,rgb[2]-40));

  p.setShader(null);p.setAntiAlias(false);p.setDither(false);p.setStrokeCap(Paint.Cap.SQUARE);p.setStrokeJoin(Paint.Join.MITER);
  p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(34,0,0,0));
  float shadowScale=CreaturePhysicsEngine.isFlyer(o)?.72f:1f;
  c.drawOval(x-w*.52f*shadowScale,ground-5,x+w*.52f*shadowScale,ground+5,p);

  c.save();
  if(life.heading<0)c.scale(-1,1,x,bodyY);
  drawBodyPlan(c,p,species.bodyPlan,x,bodyY,w,h,anim,rhythm,base,light,dark);
  drawSense(c,p,species.senseMode,x,bodyY,w,h,anim,alert,light);
  drawDefense(c,p,species.defense,x,bodyY,w,h,anim,alert,dark,light);
  c.restore();

  p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);p.setStrokeWidth(1f);p.setAntiAlias(false);
  return true;
 }

 public static String visualSignature(WorldObject o){
  SpeciesEvolutionCatalog.Species s=o==null?null:SpeciesEvolutionCatalog.get(o.dictionaryRef);
  return s==null?"":s.bodyPlan+"|"+s.locomotion+"|"+s.senseMode+"|"+s.defense+"|"+s.cladeId;
 }

 private static void drawBodyPlan(Canvas c,Paint p,String body,float x,float y,float w,float h,float anim,float rhythm,int base,int light,int dark){
  p.setStyle(Paint.Style.FILL);p.setColor(base);
  if("ribbon-frond".equals(body)){
   Path q=new Path();q.moveTo(x-w*.55f,y-h*.22f);q.cubicTo(x-w*.18f,y-h*1.05f,x+w*.10f,y+h*.10f,x+w*.60f,y-h*.62f);q.cubicTo(x+w*.28f,y-h*.12f,x-w*.08f,y-h*.78f,x-w*.55f,y-h*.22f);q.close();c.drawPath(q,p);
   p.setColor(light);c.drawOval(x-w*.10f,y-h*.62f,x+w*.20f,y-h*.30f,p);
  }else if("hollow-shell".equals(body)){
   c.drawOval(x-w*.50f,y-h*.82f,x+w*.50f,y,p);p.setColor(dark);c.drawOval(x-w*.28f,y-h*.64f,x+w*.28f,y-h*.16f,p);p.setColor(light);c.drawArc(x-w*.40f,y-h*.73f,x+w*.40f,y-h*.08f,205,215,false,p);
  }else if("tripod-reed".equals(body)){
   p.setStrokeWidth(Math.max(3f,w*.08f));p.setStyle(Paint.Style.STROKE);p.setColor(dark);for(int i=-1;i<=1;i++)c.drawLine(x+i*w*.10f,y-h*.42f,x+i*w*.30f,y,p);p.setStyle(Paint.Style.FILL);p.setColor(base);c.drawOval(x-w*.23f,y-h*.86f,x+w*.23f,y-h*.32f,p);
  }else if("veil-wing".equals(body)){
   float flap=(float)Math.sin(anim*(2.0+rhythm*2.2))*h*.15f;p.setColor(base);Path q=new Path();q.moveTo(x,y-h*.24f);q.quadTo(x-w*.72f,y-h*.92f-flap,x-w*.58f,y-h*.14f);q.quadTo(x,y-h*.38f,x+w*.58f,y-h*.14f);q.quadTo(x+w*.72f,y-h*.92f+flap,x,y-h*.24f);q.close();c.drawPath(q,p);p.setColor(light);c.drawOval(x-w*.10f,y-h*.58f,x+w*.10f,y-h*.08f,p);
  }else if("root-mantle".equals(body)){
   Path q=new Path();q.moveTo(x-w*.50f,y-h*.16f);q.quadTo(x,y-h*.94f,x+w*.50f,y-h*.16f);q.lineTo(x+w*.34f,y-h*.02f);q.lineTo(x-w*.34f,y-h*.02f);q.close();c.drawPath(q,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3f);p.setColor(dark);for(int i=-2;i<=2;i++)c.drawLine(x+i*w*.12f,y-h*.08f,x+i*w*.18f,y+h*.12f,p);
  }else if("glass-fin".equals(body)){
   c.drawOval(x-w*.45f,y-h*.68f,x+w*.38f,y-h*.08f,p);Path tail=new Path();tail.moveTo(x-w*.40f,y-h*.38f);tail.lineTo(x-w*.72f,y-h*.70f);tail.lineTo(x-w*.67f,y-h*.10f);tail.close();c.drawPath(tail,p);p.setColor(light);Path fin=new Path();fin.moveTo(x-w*.04f,y-h*.58f);fin.lineTo(x+w*.15f,y-h*.98f);fin.lineTo(x+w*.23f,y-h*.50f);fin.close();c.drawPath(fin,p);
  }else if("spiral-back".equals(body)){
   c.drawOval(x-w*.48f,y-h*.70f,x+w*.48f,y,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3f);p.setColor(light);for(int i=0;i<3;i++)c.drawArc(x-w*(.34f-i*.08f),y-h*(.58f-i*.07f),x+w*(.20f-i*.05f),y-h*(.10f+i*.03f),190+i*25,235-i*25,false,p);
  }else if("lantern-bell".equals(body)){
   Path q=new Path();q.moveTo(x-w*.38f,y-h*.26f);q.quadTo(x-w*.25f,y-h*.94f,x,y-h);q.quadTo(x+w*.25f,y-h*.94f,x+w*.38f,y-h*.26f);q.quadTo(x,y-h*.08f,x-w*.38f,y-h*.26f);q.close();c.drawPath(q,p);p.setColor(light);c.drawCircle(x,y-h*.48f,Math.max(3,h*.11f),p);
  }else if("jointed-stilt".equals(body)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3f);p.setColor(dark);for(int side=-1;side<=1;side+=2){c.drawLine(x+side*w*.16f,y-h*.55f,x+side*w*.36f,y-h*.25f,p);c.drawLine(x+side*w*.36f,y-h*.25f,x+side*w*.46f,y,p);}p.setStyle(Paint.Style.FILL);p.setColor(base);c.drawOval(x-w*.24f,y-h*.78f,x+w*.24f,y-h*.40f,p);
  }else if("moss-armor".equals(body)){
   c.drawRoundRect(x-w*.48f,y-h*.66f,x+w*.48f,y,8,8,p);p.setColor(dark);for(int i=-2;i<=2;i++)c.drawRect(x+i*w*.16f-w*.055f,y-h*.72f,x+i*w*.16f+w*.055f,y-h*.45f,p);p.setColor(light);c.drawOval(x+w*.24f,y-h*.52f,x+w*.48f,y-h*.26f,p);
  }else if("silk-glider".equals(body)){
   float flap=(float)Math.sin(anim*(1.6+rhythm))*h*.10f;Path q=new Path();q.moveTo(x,y-h*.78f);q.lineTo(x-w*.70f,y-h*.26f-flap);q.lineTo(x,y-h*.12f);q.lineTo(x+w*.70f,y-h*.26f+flap);q.close();c.drawPath(q,p);p.setColor(light);c.drawRect(x-w*.05f,y-h*.76f,x+w*.05f,y-h*.08f,p);
  }else if("petal-crown".equals(body)){
   p.setColor(base);c.drawCircle(x,y-h*.38f,h*.20f,p);p.setColor(light);for(int i=0;i<6;i++){double a=i*Math.PI/3+anim*.06;float px=x+(float)Math.cos(a)*w*.28f,py=y-h*.40f+(float)Math.sin(a)*h*.28f;c.drawOval(px-w*.13f,py-h*.10f,px+w*.13f,py+h*.10f,p);}
  }else if("ring-body".equals(body)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(5f,w*.13f));p.setColor(base);c.drawOval(x-w*.42f,y-h*.72f,x+w*.42f,y-h*.02f,p);p.setStrokeWidth(2f);p.setColor(light);c.drawOval(x-w*.28f,y-h*.58f,x+w*.28f,y-h*.16f,p);
  }else if("fan-tail".equals(body)){
   c.drawOval(x-w*.35f,y-h*.60f,x+w*.38f,y-h*.05f,p);p.setColor(light);for(int i=-2;i<=2;i++){Path q=new Path();q.moveTo(x-w*.30f,y-h*.28f);q.lineTo(x-w*(.58f+Math.abs(i)*.04f),y-h*(.28f+i*.16f));q.lineTo(x-w*.43f,y-h*.12f);q.close();c.drawPath(q,p);}
  }else if("stone-pad".equals(body)){
   c.drawOval(x-w*.50f,y-h*.42f,x+w*.50f,y,p);p.setColor(dark);c.drawOval(x-w*.28f,y-h*.48f,x+w*.20f,y-h*.12f,p);p.setColor(light);c.drawCircle(x+w*.28f,y-h*.20f,3f,p);
  }else if("mist-bladder".equals(body)){
   float pulse=1f+(float)Math.sin(anim*(.8+rhythm))*.06f;c.drawOval(x-w*.34f*pulse,y-h*.90f,x+w*.34f*pulse,y-h*.20f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(light);for(int i=-2;i<=2;i++)c.drawLine(x+i*w*.10f,y-h*.20f,x+i*w*.14f,y+h*.06f+(i%2)*4,p);
  }else if("needle-limb".equals(body)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.4f);p.setColor(dark);for(int i=-2;i<=2;i++){c.drawLine(x+i*w*.08f,y-h*.48f,x+i*w*.28f,y,p);c.drawLine(x+i*w*.05f,y-h*.45f,x-i*w*.18f,y-h*.08f,p);}p.setStyle(Paint.Style.FILL);p.setColor(base);c.drawOval(x-w*.16f,y-h*.78f,x+w*.16f,y-h*.30f,p);
  }else if("disk-crest".equals(body)){
   c.drawOval(x-w*.42f,y-h*.55f,x+w*.42f,y,p);p.setColor(light);c.drawOval(x-w*.08f,y-h*.96f,x+w*.45f,y-h*.34f,p);p.setColor(dark);c.drawOval(x+w*.08f,y-h*.77f,x+w*.31f,y-h*.50f,p);
  }else if("branch-antler".equals(body)){
   c.drawOval(x-w*.38f,y-h*.56f,x+w*.38f,y,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3f);p.setColor(light);for(int side=-1;side<=1;side+=2){c.drawLine(x+side*w*.22f,y-h*.50f,x+side*w*.42f,y-h*.96f,p);c.drawLine(x+side*w*.34f,y-h*.77f,x+side*w*.55f,y-h*.82f,p);}
  }else if("soft-spine".equals(body)){
   for(int i=0;i<6;i++){float px=x-w*.42f+i*w*.16f,py=y-h*.22f-(float)Math.sin(anim*.7+i*.7)*h*.10f;p.setColor(i%2==0?base:light);c.drawOval(px-w*.12f,py-h*.25f,px+w*.12f,py+h*.08f,p);}
  }else if("coil-body".equals(body)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(5f,w*.12f));p.setColor(base);c.drawArc(x-w*.44f,y-h*.72f,x+w*.44f,y+h*.06f,18,310,false,p);p.setStrokeWidth(2f);p.setColor(light);c.drawArc(x-w*.28f,y-h*.56f,x+w*.24f,y-h*.02f,15,270,false,p);
  }else if("leaf-sail".equals(body)){
   Path q=new Path();q.moveTo(x-w*.18f,y);q.quadTo(x-w*.46f,y-h*.80f,x+w*.12f,y-h);q.quadTo(x+w*.50f,y-h*.48f,x+w*.20f,y-h*.08f);q.close();c.drawPath(q,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(light);c.drawLine(x,y-h*.08f,x+w*.08f,y-h*.84f,p);
  }else if("drum-thorax".equals(body)){
   c.drawOval(x-w*.36f,y-h*.72f,x+w*.30f,y-h*.06f,p);p.setColor(light);c.drawOval(x-w*.10f,y-h*.66f,x+w*.42f,y-h*.15f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(dark);for(int i=-2;i<=2;i++)c.drawLine(x+i*w*.08f,y-h*.25f,x+i*w*.20f,y,p);
  }else if("twin-keel".equals(body)){
   c.drawOval(x-w*.46f,y-h*.64f,x+w*.02f,y-h*.08f,p);p.setColor(light);c.drawOval(x-w*.02f,y-h*.64f,x+w*.46f,y-h*.08f,p);p.setColor(dark);c.drawRect(x-w*.05f,y-h*.70f,x+w*.05f,y-h*.02f,p);
  }else if("orbital-frill".equals(body)){
   c.drawCircle(x,y-h*.38f,h*.20f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(light);c.drawOval(x-w*.48f,y-h*.70f,x+w*.48f,y-h*.06f,p);c.drawOval(x-w*.34f,y-h*.86f,x+w*.34f,y+h*.08f,p);
  }else{
   c.drawOval(x-w*.42f,y-h*.58f,x+w*.42f,y,p);
  }
 }

 private static void drawSense(Canvas c,Paint p,String sense,float x,float y,float w,float h,float anim,float alert,int light){
  p.setColor(light);p.setStyle(Paint.Style.FILL);
  float front=x+w*.31f,eyeY=y-h*.46f;
  if("lumen-gradient".equals(sense)||"polarized-glow".equals(sense)){
   float pulse=2.3f+alert*2.2f+(float)(.5+.5*Math.sin(anim*2.1));c.drawCircle(front,eyeY,pulse,p);c.drawCircle(front-w*.14f,eyeY+h*.09f,pulse*.62f,p);
  }else if("vibration-map".equals(sense)||"chemical-thread".equals(sense)||"echo-pressure".equals(sense)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);float len=10+alert*14;c.drawLine(front,eyeY,front+len,eyeY-h*.16f,p);c.drawLine(front,eyeY+h*.06f,front+len*.84f,eyeY+h*.18f,p);
  }else if("humidity-field".equals(sense)||"airflow-vortex".equals(sense)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);c.drawArc(front-8,eyeY-9,front+18,eyeY+9,-65,115,false,p);c.drawArc(front-5,eyeY-5,front+12,eyeY+5,-65,115,false,p);
  }else if("thermal-edge".equals(sense)||"surface-ripple".equals(sense)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);for(int i=0;i<3;i++)c.drawLine(front+i*4,eyeY-h*.08f,front+8+i*4,eyeY-h*(.20f+i*.04f),p);
  }else if("electrostatic-drift".equals(sense)){
   for(int i=0;i<3;i++){double a=anim*(.7+i*.13)+i*2.1;c.drawCircle(front+(float)Math.cos(a)*(8+i*3),eyeY+(float)Math.sin(a)*(5+i*2),1.5f,p);}
  }else if("root-tension".equals(sense)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);c.drawLine(x-w*.16f,y-h*.12f,x-w*.22f,y+5,p);c.drawLine(x+w*.08f,y-h*.10f,x+w*.18f,y+6,p);
  }else{
   c.drawCircle(front,eyeY,2.6f,p);
  }
 }

 private static void drawDefense(Canvas c,Paint p,String defense,float x,float y,float w,float h,float anim,float alert,int dark,int light){
  if(defense==null)return;
  if("warning shimmer".equals(defense)||"group flare".equals(defense)){
   int alpha=(int)(42+70*(.5+.5*Math.sin(anim*2.4)));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(Color.argb(alpha,220,238,196));c.drawOval(x-w*.55f,y-h*.92f,x+w*.55f,y+h*.04f,p);
  }else if("root-anchor".equals(defense)||"rapid burrow".equals(defense)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(dark);for(int i=-2;i<=2;i++)c.drawLine(x+i*w*.12f,y-h*.06f,x+i*w*.18f,y+h*(.10f+alert*.08f),p);
  }else if("shell-fold".equals(defense)||"bitter surface film".equals(defense)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3f);p.setColor(light);c.drawArc(x-w*.45f,y-h*.75f,x+w*.45f,y,188,164,false,p);
  }else if("mist discharge".equals(defense)){
   p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(60,218,231,224));for(int i=0;i<4;i++){float dx=(float)Math.sin(anim*.35+i)*w*.55f,dy=-h*.25f-i*5;c.drawCircle(x+dx,y+dy,3+i,p);}
  }else if("air-brake dart".equals(defense)){
   p.setStyle(Paint.Style.FILL);p.setColor(dark);Path q=new Path();q.moveTo(x-w*.43f,y-h*.30f);q.lineTo(x-w*.76f,y-h*.52f);q.lineTo(x-w*.55f,y-h*.16f);q.close();c.drawPath(q,p);
  }else if("vibration decoy".equals(defense)){
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.6f);p.setColor(Color.argb(100,190,212,185));float pulse=8+(float)(.5+.5*Math.sin(anim*2))*7;c.drawCircle(x-w*.52f,y-h*.18f,pulse,p);
  }else if("stillness camouflage".equals(defense)){
   p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(90,45,66,54));for(int i=0;i<5;i++)c.drawCircle(x-w*.30f+i*w*.15f,y-h*(.25f+(i%2)*.18f),2.2f,p);
  }
 }

 private static float motionOffset(SpeciesEvolutionCatalog.Species s,CreatureLifeState life,float anim){
  String loc=s.locomotion==null?"":s.locomotion;float drive=(float)Math.max(0,Math.min(1,life.locomotionDrive)),raw;
  if("short-hop".equals(loc))raw=-(float)Math.abs(Math.sin(anim*(2.4+drive*2.8)))*(3+7*drive);
  else if("six-beat scuttle".equals(loc))raw=(float)Math.sin(anim*(5.0+drive*5))*1.6f;
  else if("surface-skate".equals(loc))raw=(float)Math.sin(anim*(2.6+drive*2))*1.2f;
  else if("air-drift".equals(loc))raw=(float)Math.sin(anim*(.72+drive*.8))*4.5f;
  else if("membrane-flight".equals(loc))raw=(float)Math.sin(anim*(1.6+drive*1.7))*3.0f;
  else if("burrow-wave".equals(loc))raw=(float)Math.sin(anim*(1.5+drive))*1.0f;
  else if("root-step".equals(loc))raw=(float)Math.sin(anim*(.65+drive*.5))*.7f;
  else raw=(float)Math.sin(anim*(.9+drive*1.4))*1.5f;
  return PixelArtRenderPolicy.snapLogical(raw);
 }

 private static int parseTail(String id){
  if(id==null)return 0;int n=0;for(int i=0;i<id.length();i++){char ch=id.charAt(i);if(ch>='0'&&ch<='9')n=n*10+(ch-'0');}return n;
 }
}
