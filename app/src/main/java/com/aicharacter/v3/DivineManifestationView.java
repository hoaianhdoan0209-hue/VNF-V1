package com.aicharacter.v3;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * Full-screen non-human manifestation.
 * Presentation-only: visualizes session phase without creating world/cognition state.
 */
public final class DivineManifestationView extends View {
 public enum Phase { CONNECTING, PRESENT, THINKING, SPEAKING, WARNING, OBSERVING, APPLYING, DEGRADED, DISAPPEARING }
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private Phase phase=Phase.CONNECTING;private long phaseAt=System.currentTimeMillis();

 public DivineManifestationView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 public Phase phase(){return phase;}
 public void setPhase(Phase next){if(next==null)next=Phase.DEGRADED;if(phase!=next){phase=next;phaseAt=System.currentTimeMillis();}invalidate();}

 @Override protected void onDraw(Canvas c){
  super.onDraw(c);long now=System.currentTimeMillis();float t=(now-phaseAt)/1000f,d=getResources().getDisplayMetrics().density,cx=getWidth()*.5f,cy=getHeight()*.405f;
  float pulse=(float)(.5+.5*Math.sin(now/610.0)),breath=(float)(.5+.5*Math.sin(now/1180.0)),strength=strength();
  if(phase==Phase.DISAPPEARING)strength=Math.max(0f,1f-t/.60f);
  Palette pal=palette();
  drawAtmosphericField(c,cx,cy,d,pulse,breath,strength,pal);
  drawLightColumn(c,cx,cy,d,breath,strength,pal);
  drawFloorEcho(c,cx,cy,d,t,pulse,strength,pal);
  drawRays(c,cx,cy,d,t,strength,pal);
  drawHaloLattice(c,cx,cy,d,t,pulse,strength,pal);
  drawOrbitBands(c,cx,cy,d,t,pulse,strength,pal);
  drawSigil(c,cx,cy,d,t,pulse,strength,pal);
  drawMotes(c,cx,cy,d,t,breath,strength,pal);
  drawPhaseAccent(c,cx,cy,d,t,pulse,strength,pal);
  if((phase!=Phase.DISAPPEARING&&phase!=Phase.DEGRADED)||(phase==Phase.DISAPPEARING&&strength>0))postInvalidateDelayed(33);
 }

 private float strength(){switch(phase){case DEGRADED:return .30f;case CONNECTING:return .60f;case WARNING:return 1f;case APPLYING:return .98f;case OBSERVING:return .92f;case THINKING:return .94f;case SPEAKING:return .96f;default:return .90f;}}

 private Palette palette(){
  switch(phase){
   case WARNING:return new Palette(Color.rgb(255,214,178),Color.rgb(236,153,136),Color.rgb(117,177,190),Color.rgb(255,235,193));
   case APPLYING:return new Palette(Color.rgb(224,240,203),Color.rgb(159,214,183),Color.rgb(111,181,190),Color.rgb(244,232,181));
   case OBSERVING:return new Palette(Color.rgb(207,234,228),Color.rgb(143,198,205),Color.rgb(116,167,203),Color.rgb(243,229,184));
   case THINKING:return new Palette(Color.rgb(222,224,246),Color.rgb(166,177,224),Color.rgb(116,192,199),Color.rgb(246,226,181));
   case SPEAKING:return new Palette(Color.rgb(246,232,196),Color.rgb(195,207,201),Color.rgb(132,191,194),Color.rgb(255,236,187));
   case DEGRADED:return new Palette(Color.rgb(188,201,203),Color.rgb(110,135,145),Color.rgb(84,112,128),Color.rgb(214,204,175));
   default:return new Palette(Color.rgb(239,229,195),Color.rgb(176,214,207),Color.rgb(120,181,193),Color.rgb(255,236,187));
  }
 }

 private void drawAtmosphericField(Canvas c,float cx,float cy,float d,float pulse,float breath,float s,Palette q){
  p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,0,getHeight(),Color.argb((int)(34*s),7,13,24),Color.argb((int)(88*s),4,10,18),Shader.TileMode.CLAMP));c.drawRect(0,0,getWidth(),getHeight(),p);
  float r=(260+36*pulse)*d;c.save();c.scale(1.18f,.82f,cx,cy);p.setShader(new RadialGradient(cx,cy,r,new int[]{alpha(q.core,(int)(62*s)),alpha(q.mid,(int)(30*s)),Color.TRANSPARENT},new float[]{0f,.42f,1f},Shader.TileMode.CLAMP));c.drawCircle(cx,cy,r,p);c.restore();
  float outer=(390+24*breath)*d;c.save();c.scale(1.35f,.70f,cx,cy);p.setShader(new RadialGradient(cx,cy,outer,alpha(q.cool,(int)(20*s)),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,outer,p);c.restore();p.setShader(null);
  p.setColor(Color.argb((int)(12*s),12,25,37));c.drawRect(0,0,getWidth(),getHeight(),p);
 }

 private void drawLightColumn(Canvas c,float cx,float cy,float d,float breath,float s,Palette q){
  float half=(56+18*breath)*d,top=Math.max(0,cy-330*d),bottom=Math.min(getHeight(),cy+310*d);
  p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(cx,top,cx,bottom,new int[]{Color.TRANSPARENT,alpha(q.cool,(int)(22*s)),alpha(q.core,(int)(12*s)),Color.TRANSPARENT},new float[]{0f,.27f,.63f,1f},Shader.TileMode.CLAMP));
  Path beam=new Path();beam.moveTo(cx-half*.28f,top);beam.lineTo(cx+half*.28f,top);beam.lineTo(cx+half,bottom);beam.lineTo(cx-half,bottom);beam.close();c.drawPath(beam,p);p.setShader(null);
 }

 private void drawFloorEcho(Canvas c,float cx,float cy,float d,float t,float pulse,float s,Palette q){
  float y=cy+218*d,w=(164+16*pulse)*d,h=(35+4*pulse)*d;
  p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(cx,y,w,alpha(q.cool,(int)(26*s)),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.save();c.scale(1f,.28f,cx,y);c.drawCircle(cx,y,w,p);c.restore();p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.05f*d);for(int i=0;i<3;i++){float phase=(float)((t*(10+i*4)+i*71)%360),rw=w*(.56f+i*.18f),rh=h*(.55f+i*.28f);p.setColor(alpha(i==1?q.core:q.cool,(int)((62-i*12)*s)));c.drawArc(new RectF(cx-rw,y-rh,cx+rw,y+rh),phase,120+i*26,false,p);}p.setStyle(Paint.Style.FILL);
 }

 private void drawRays(Canvas c,float cx,float cy,float d,float t,float s,Palette q){
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);int count=phase==Phase.DEGRADED?6:11;
  for(int i=0;i<count;i++){float drift=(float)Math.sin(t*(.28+i*.012)+i*.72f)*8*d,x=cx+(i-(count-1)*.5f)*22*d+drift,top=cy-(238+(i%4)*31)*d,bottom=cy+(158+(i+2)%4*23)*d;p.setStrokeWidth((.55f+(i%3)*.34f)*d);p.setColor(alpha((i&1)==0?q.cool:q.mid,(int)((18+(i%5)*7)*s)));c.drawLine(x,top,x+(float)Math.sin(t*.54+i)*11*d,bottom,p);}p.setStrokeCap(Paint.Cap.BUTT);
 }

 private void drawHaloLattice(Canvas c,float cx,float cy,float d,float t,float pulse,float s,Palette q){
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);for(int i=0;i<5;i++){float rx=(78+i*31+pulse*(i%2==0?5:-3))*d,ry=rx*(.56f+i*.025f);p.setStrokeWidth((.8f+i*.28f)*d);p.setColor(alpha((i&1)==0?q.core:q.cool,(int)((104-i*13)*s)));float spin=t*(10+i*5)*(i%2==0?1:-1)+i*39;c.drawArc(new RectF(cx-rx,cy-ry,cx+rx,cy+ry),spin,92+i*17,false,p);c.drawArc(new RectF(cx-rx,cy-ry,cx+rx,cy+ry),spin+177,48+i*11,false,p);}p.setStrokeCap(Paint.Cap.BUTT);
 }

 private void drawOrbitBands(Canvas c,float cx,float cy,float d,float t,float pulse,float s,Palette q){
  p.setStyle(Paint.Style.STROKE);for(int i=0;i<3;i++){float rx=(118+i*41+pulse*4*(i==1?-1:1))*d,ry=rx*(.46f+i*.04f),speed=(14+i*7)*(i%2==0?1:-1),start=t*speed+i*83;p.setStrokeWidth((1.0f+i*.34f)*d);p.setColor(alpha(i==1?q.mid:q.core,(int)((92-i*12)*s)));c.drawArc(new RectF(cx-rx,cy-ry,cx+rx,cy+ry),start,138+i*18,false,p);
   for(int n=0;n<2;n++){double a=Math.toRadians(start+34+n*(72+i*11));float x=cx+(float)Math.cos(a)*rx,y=cy+(float)Math.sin(a)*ry;p.setStyle(Paint.Style.FILL);p.setColor(alpha(q.spark,(int)((160-i*20)*s)));c.drawCircle(x,y,(1.8f+i*.45f)*d,p);p.setStyle(Paint.Style.STROKE);}
  }p.setStyle(Paint.Style.FILL);
 }

 private void drawSigil(Canvas c,float cx,float cy,float d,float t,float pulse,float s,Palette q){
  float r=(43+pulse*4)*d;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.0f*d);p.setColor(alpha(q.spark,(int)(228*s)));Path diamond=new Path();diamond.moveTo(cx,cy-r);diamond.lineTo(cx+r*.72f,cy);diamond.lineTo(cx,cy+r);diamond.lineTo(cx-r*.72f,cy);diamond.close();c.drawPath(diamond,p);
  p.setStrokeWidth(1.05f*d);p.setColor(alpha(q.cool,(int)(178*s)));c.drawCircle(cx,cy,r*.56f,p);Path inner=new Path();inner.moveTo(cx,cy-r*.40f);inner.lineTo(cx+r*.30f,cy);inner.lineTo(cx,cy+r*.40f);inner.lineTo(cx-r*.30f,cy);inner.close();c.drawPath(inner,p);
  float spin=t*(phase==Phase.THINKING?48:phase==Phase.APPLYING?34:24);for(int i=0;i<8;i++){double a=Math.toRadians(spin+i*45);float x1=cx+(float)Math.cos(a)*r*.22f,y1=cy+(float)Math.sin(a)*r*.22f,x2=cx+(float)Math.cos(a)*r*.50f,y2=cy+(float)Math.sin(a)*r*.50f;c.drawLine(x1,y1,x2,y2,p);}
  p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(cx,cy,24*d,new int[]{alpha(q.spark,(int)(250*s)),alpha(q.core,(int)(96*s)),Color.TRANSPARENT},new float[]{0f,.42f,1f},Shader.TileMode.CLAMP));c.drawCircle(cx,cy,24*d,p);p.setShader(null);
 }

 private void drawMotes(Canvas c,float cx,float cy,float d,float t,float breath,float s,Palette q){
  int count=phase==Phase.DEGRADED?10:26;p.setStyle(Paint.Style.FILL);
  for(int i=0;i<count;i++){double a=i*2.399+t*(.18+(i%4)*.045)*(i%2==0?1:-1);float r=(74+(i*13)%235)*d,x=cx+(float)Math.cos(a)*r,y=cy+(float)Math.sin(a)*r*(.48f+(i%3)*.08f)+(float)Math.sin(t*.7+i)*4*d,sz=(.9f+(i%5)*.42f)*(1+.14f*breath)*d;p.setColor(alpha((i%3)==0?q.spark:(i%3)==1?q.core:q.cool,(int)((54+(i%5)*22)*s)));c.drawCircle(x,y,sz,p);}
  if(phase==Phase.DEGRADED&&((int)(t*5)%2==0)){p.setColor(alpha(q.cool,46));c.drawRect(cx-70*d,cy+26*d,cx-22*d,cy+28*d,p);c.drawRect(cx+28*d,cy-42*d,cx+62*d,cy-40*d,p);}
 }

 private void drawPhaseAccent(Canvas c,float cx,float cy,float d,float t,float pulse,float s,Palette q){
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
  if(phase==Phase.WARNING){float r=(66+5*pulse)*d;p.setStrokeWidth(1.5f*d);p.setColor(alpha(q.mid,(int)(220*s)));c.drawCircle(cx,cy,r*1.42f,p);c.drawArc(new RectF(cx-r*1.75f,cy-r*1.18f,cx+r*1.75f,cy+r*1.18f),t*32,86,false,p);}
  else if(phase==Phase.APPLYING){float r=(68+4*pulse)*d;p.setStrokeWidth(1.35f*d);p.setColor(alpha(q.core,(int)(208*s)));for(int i=0;i<3;i++)c.drawArc(new RectF(cx-r*(1.18f+i*.20f),cy-r*(.74f+i*.11f),cx+r*(1.18f+i*.20f),cy+r*(.74f+i*.11f)),-24+t*(22+i*7),188-i*18,false,p);}
  else if(phase==Phase.SPEAKING){p.setStrokeWidth(1.1f*d);for(int i=0;i<4;i++){float rr=(78+i*19+pulse*3)*d;p.setColor(alpha(q.spark,(int)((92-i*12)*s)));c.drawArc(new RectF(cx-rr,cy-rr*.52f,cx+rr,cy+rr*.52f),12+i*31+t*8,44,false,p);}}
  else if(phase==Phase.OBSERVING){p.setStrokeWidth(1.15f*d);p.setColor(alpha(q.cool,(int)(180*s)));float rr=82*d;c.drawArc(new RectF(cx-rr*1.7f,cy-rr*.72f,cx+rr*1.7f,cy+rr*.72f),-34+t*16,72,false,p);c.drawArc(new RectF(cx-rr*1.7f,cy-rr*.72f,cx+rr*1.7f,cy+rr*.72f),142+t*16,72,false,p);}
  p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
 }

 private static int alpha(int color,int a){return Color.argb(Math.max(0,Math.min(255,a)),Color.red(color),Color.green(color),Color.blue(color));}
 private static final class Palette{final int core,mid,cool,spark;Palette(int c,int m,int cool,int spark){core=c;mid=m;this.cool=cool;this.spark=spark;}}
}
