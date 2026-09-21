package com.aicharacter.v3;
/**
 * Automatic Cat-POV framing director.
 * The player does not manually pan the camera. Vertical framing follows Haru's real airborne body height
 * while preserving ground context for the trailing cat.
 */
public final class CatCameraDirector{
 public static final class Frame{
  public float cameraX,elevation,lookX,lookY,airFollowPx,girlScreenLiftPx;
  public String mode,reason;
  Frame(float x,float e,float l,float ly,float air,float lift,String m,String r){cameraX=x;elevation=e;lookX=l;lookY=ly;airFollowPx=air;girlScreenLiftPx=lift;mode=m;reason=r;}
 }
 private static float smoothElev=0,smoothAir=0;
 private static String lastMode="CHARACTER_FRAME";
 private static int stableTicks=0;
 private CatCameraDirector(){}
 public static void resetForTest(){smoothElev=0;smoothAir=0;lastMode="CHARACTER_FRAME";stableTicks=0;}
 public static Frame direct(WorldState s){
  float dx=s.haruX-s.catX;
  float elev=26f,lookY=420f;
  String wanted="CHARACTER_FRAME";
  String reason="automatic framing follows Haru while preserving the cat's ground context";
  if("girl".equals(s.catState.attachedToEntity)){
   elev=92f;lookY=390f;wanted="REST_CONTEXT";reason="cat is physically carried: shared elevated framing";
  }else if(Math.abs(dx)<260f){
   elev=108f;lookY=385f;wanted="CLOSE_SAFE_INTERACTION";reason="cat is close: safe upper-body framing";
  }else if(s.girlTravel.active){
   elev=58f;lookY=415f;wanted="CHARACTER_TRAVEL";reason="cat trails Haru while the frame preserves travel context";
  }else if("SIT".equalsIgnoreCase(s.haruActivity)||"REST".equalsIgnoreCase(s.haruActivity)){
   elev=72f;lookY=430f;wanted="CHARACTER_REST";reason="Haru is resting: calmer medium framing";
  }
  double girlClear=s.girlPhysics==null?0:s.girlPhysics.groundClearanceM,catClear=s.catPhysics==null?0:s.catPhysics.groundClearanceM;
  float girlLift=(float)WorldUnits.mToPx(Math.max(0,girlClear)),catLift=(float)WorldUnits.mToPx(Math.max(0,catClear));
  boolean airborne=girlClear>.025||(s.girlPhysics!=null&&!s.girlPhysics.grounded);
  float preserveGround=Math.abs(dx)<560f?.82f:.94f;
  float airTarget=Math.min(180f,girlLift*preserveGround+catLift*.18f);
  float rate=airTarget>smoothAir?.30f:.16f;
  smoothAir+=(airTarget-smoothAir)*rate;
  if(Math.abs(smoothAir)<.15f&&airTarget==0)smoothAir=0;
  if(airborne){wanted="AIRBORNE_FOLLOW";lookY=Math.max(330f,405f-Math.min(75f,girlLift*.55f));reason="Haru is airborne: camera rises with her while leaving enough ground to keep the trailing cat readable";}
  if(!wanted.equals(lastMode)){
   stableTicks++;
   int needed="AIRBORNE_FOLLOW".equals(wanted)||"CLOSE_SAFE_INTERACTION".equals(wanted)?1:2;
   if(stableTicks>=needed){lastMode=wanted;stableTicks=0;}else wanted=lastMode;
  }else stableTicks=0;
  float targetElev=elev+smoothAir;
  smoothElev+=(targetElev-smoothElev)*(airborne?.24f:.16f);
  float cameraX=0f,lookX=s.haruX;
  s.cameraTrace="mode="+wanted+" subject=girl haru="+(int)s.haruX+" cat="+(int)s.catX+
          " camera=0,"+(int)smoothElev+" air="+(int)smoothAir+" girlLift="+(int)girlLift+
          " look="+(int)lookX+","+(int)lookY+" manual=false reason="+reason;
  return new Frame(cameraX,smoothElev,lookX,lookY,smoothAir,girlLift,wanted,reason);
 }
}