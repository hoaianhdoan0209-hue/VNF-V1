package com.aicharacter.v3;
import android.graphics.*;
/** Lightweight mechanically-readable fallback body. Every segment is derived from one linked rig. */
public final class BlockBodyRenderer{private BlockBodyRenderer(){}
 public static void drawHuman(Canvas c,Paint p,WorldState s,float x,float ground){
  BodyRigState r=s.bodyRig;JointConstraintState j=s.joints;if(r==null||j==null)return;
  float sc=1.15f;float pelvisX=x+(float)r.pelvisTilt*12, pelvisY=ground-82*sc;
  float spine=(float)Math.max(-.22,Math.min(.22,j.spine));float neck=(float)Math.max(-.18,Math.min(.18,j.neck));
  float shoulderX=pelvisX+spine*34,shoulderY=pelvisY-74*sc;NervousState n=s.nervous;float brace=n==null?0:(float)n.fallBrace,headGuard=n==null?0:(float)n.headProtection;shoulderY+=brace*18;float headX=shoulderX+neck*22-brace*8,headY=shoulderY-45*sc+headGuard*12;
  float phase=(float)(r.stridePhase*Math.PI*2),swing=(float)Math.sin(phase)*24;
  float lHip=pelvisX-12,rHip=pelvisX+12,lFoot=r.leftFootContact>.5&&Double.isFinite(r.leftFootWorldX)?(float)(r.leftFootWorldX-s.haruX+x):x-15+swing,rFoot=r.rightFootContact>.5&&Double.isFinite(r.rightFootWorldX)?(float)(r.rightFootWorldX-s.haruX+x):x+15-swing;float lGround=Double.isFinite(r.leftFootGroundY)?(float)r.leftFootGroundY:ground,rGround=Double.isFinite(r.rightFootGroundY)?(float)r.rightFootGroundY:ground;
  float kneeY=ground-42*sc,lKnee=(lHip+lFoot)*.5f+(float)j.knee*9,rKnee=(rHip+rFoot)*.5f-(float)j.knee*9;
  p.setStrokeWidth(13);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.rgb(67,72,78));line(c,p,lHip,pelvisY,lKnee,kneeY);line(c,p,lKnee,kneeY,lFoot,lGround);line(c,p,rHip,pelvisY,rKnee,kneeY);line(c,p,rKnee,kneeY,rFoot,rGround);
  p.setStrokeWidth(25);p.setColor(Color.rgb(92,111,116));line(c,p,pelvisX,pelvisY,shoulderX,shoulderY);
  p.setStrokeWidth(10);p.setColor(Color.rgb(78,84,91));float arm=(float)j.shoulder*22+(n==null?0:(float)n.armProtection*18);line(c,p,shoulderX-18,shoulderY,shoulderX-25-arm,shoulderY+65);line(c,p,shoulderX+18,shoulderY,shoulderX+25+arm,shoulderY+65);
  p.setColor(Color.rgb(205,174,145));c.drawCircle(headX,headY,22,p);p.setStrokeWidth(9);line(c,p,shoulderX,shoulderY-8,headX,headY+18);
  p.setStrokeCap(Paint.Cap.BUTT);
 }
 private static void line(Canvas c,Paint p,float x1,float y1,float x2,float y2){c.drawLine(x1,y1,x2,y2,p);}
}