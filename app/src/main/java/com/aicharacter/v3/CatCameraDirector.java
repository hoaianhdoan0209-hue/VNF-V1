package com.aicharacter.v3;
/** Cat POV director: low/present by default, but safety framing always wins near the young girl. */
public final class CatCameraDirector{
 public static final class Frame{public float cameraX,elevation,lookX,lookY;public String mode,reason;Frame(float x,float e,float l,float ly,String m,String r){cameraX=x;elevation=e;lookX=l;lookY=ly;mode=m;reason=r;}}
 private static float smoothX=Float.NaN,smoothElev=0;private static String lastMode="WORLD_EXPLORATION";private static int stableTicks=0;
 private CatCameraDirector(){}
 public static void resetForTest(){smoothX=Float.NaN;smoothElev=0;lastMode="WORLD_EXPLORATION";stableTicks=0;}
 public static Frame direct(WorldState s){
  float dx=s.haruX-s.catX;float target=s.catX-360,elev=8,look=s.catX+520,lookY=510;String wanted="WORLD_EXPLORATION",reason="environment-first cat POV";
  if("girl".equals(s.catState.attachedToEntity)){target=s.haruX-300;elev=90;look=s.haruX+150;lookY=390;wanted="REST_CONTEXT";reason="physically carried cat: safe elevated contextual view";}
  else if(Math.abs(dx)<240){target+=dx>=0?-145:145;elev=112;look=s.haruX;lookY=390;wanted="CLOSE_SAFE_INTERACTION";reason="close girl: raise/lateral frame toward face/upper body; low rear angle blocked";}
  else if(dx>0&&dx<760){target-=80;elev=62;look=s.haruX;lookY=420;wanted="WALKING_PARALLEL";reason="girl ahead: parallel 3/4 framing with upper-body look target";}
  else if(s.girlTravel.active){look=s.girlTravel.segmentEndX;lookY=520;reason="travel direction is current look target";}
  if(!wanted.equals(lastMode)){
   stableTicks++; int needed="CLOSE_SAFE_INTERACTION".equals(wanted)?1:3;
   if(stableTicks>=needed){lastMode=wanted;stableTicks=0;} else wanted=lastMode;
  }else stableTicks=0;
  String mode=wanted;
  if(Float.isNaN(smoothX))smoothX=target;float dead=Math.abs(target-smoothX);if(dead>18)smoothX+=Math.max(-36,Math.min(36,(target-smoothX)*.16f));smoothElev+=(elev-smoothElev)*.14f;
  s.cameraTrace="mode="+mode+" cat="+(int)s.catX+" camera="+(int)smoothX+","+(int)smoothElev+" look="+(int)look+","+(int)lookY+" safe="+(!mode.equals("WORLD_EXPLORATION"))+" reason="+reason;
  return new Frame(smoothX,smoothElev,look,lookY,mode,reason);
 }
}
