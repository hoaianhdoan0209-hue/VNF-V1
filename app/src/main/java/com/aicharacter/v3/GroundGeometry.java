package com.aicharacter.v3;
/** Continuous effective ground sampled from authored area ground heights. */
public final class GroundGeometry{private GroundGeometry(){}
 public static double heightPx(WorldState s,double x){if(s==null||s.world==null)return 846;WorldArea a=s.world.areaAt((float)x);if(a==null)return 846;double y=a.groundY,blend=80;WorldArea left=s.world.areaAt((float)(x-blend)),right=s.world.areaAt((float)(x+blend));if(left!=null&&left!=a&&x-a.left<blend){double t=cl((x-a.left+blend)/(2*blend));y=lerp(left.groundY,a.groundY,t);}if(right!=null&&right!=a&&a.right-x<blend){double t=cl((a.right-x+blend)/(2*blend));y=lerp(a.groundY,right.groundY,t);}return y;}
 public static double slope(WorldState s,double x){double d=12;double dy=heightPx(s,x+d)-heightPx(s,x-d);return Math.atan2(dy,2*d);}
 private static double lerp(double a,double b,double t){return a+(b-a)*t;}private static double cl(double v){return Math.max(0,Math.min(1,v));}
}