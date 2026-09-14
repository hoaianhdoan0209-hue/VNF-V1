package com.aicharacter.v3;
/**
 * Automatic Cat-POV framing director.
 * The player does not manually pan the camera. The authored 2400x1080 stage remains stable;
 * framing reacts to the girl's position/state through look target and safe elevation.
 */
public final class CatCameraDirector{
 public static final class Frame{
  public float cameraX,elevation,lookX,lookY;
  public String mode,reason;
  Frame(float x,float e,float l,float ly,String m,String r){cameraX=x;elevation=e;lookX=l;lookY=ly;mode=m;reason=r;}
 }
 private static float smoothElev=0;
 private static String lastMode="CHARACTER_FRAME";
 private static int stableTicks=0;
 private CatCameraDirector(){}
 public static void resetForTest(){smoothElev=0;lastMode="CHARACTER_FRAME";stableTicks=0;}
 public static Frame direct(WorldState s){
  float dx=s.haruX-s.catX;
  float elev=26f;
  float lookY=420f;
  String wanted="CHARACTER_FRAME";
  String reason="automatic framing follows the girl; no manual camera controls";

  if("girl".equals(s.catState.attachedToEntity)){
   elev=92f; lookY=390f; wanted="REST_CONTEXT";
   reason="cat is physically carried: elevated safe character framing";
  }else if(Math.abs(dx)<260f){
   elev=108f; lookY=385f; wanted="CLOSE_SAFE_INTERACTION";
   reason="girl is close: frame face/upper body and block unsafe low rear angles";
  }else if(s.girlTravel.active){
   elev=58f; lookY=415f; wanted="CHARACTER_TRAVEL";
   reason="girl is travelling: keep her as the visual subject while preserving the authored stage";
  }else if("SIT".equalsIgnoreCase(s.haruActivity)||"REST".equalsIgnoreCase(s.haruActivity)){
   elev=72f; lookY=430f; wanted="CHARACTER_REST";
   reason="girl is resting: calmer medium framing";
  }

  if(!wanted.equals(lastMode)){
   stableTicks++;
   int needed="CLOSE_SAFE_INTERACTION".equals(wanted)?1:2;
   if(stableTicks>=needed){lastMode=wanted;stableTicks=0;} else wanted=lastMode;
  }else stableTicks=0;

  smoothElev+=(elev-smoothElev)*.16f;
  // cameraX stays at 0 because the current authored backgrounds are exactly one 2400px stage.
  // This prevents legacy tiling/sea fallback while the look target still follows the girl.
  float cameraX=0f;
  float lookX=s.haruX;
  s.cameraTrace="mode="+wanted+" subject=girl haru="+(int)s.haruX+" cat="+(int)s.catX+
          " camera=0,"+(int)smoothElev+" look="+(int)lookX+","+(int)lookY+
          " manual=false reason="+reason;
  return new Frame(cameraX,smoothElev,lookX,lookY,wanted,reason);
 }
}
