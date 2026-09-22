package com.aicharacter.v3;
/**
 * Automatic Cat-POV framing director.
 * Camera reads physical world state only and never drives Haru.
 */
public final class CatCameraDirector{
 public static final class Frame{
  public float cameraX,elevation,lookX,lookY,airFollowPx,girlScreenLiftPx;
  public String mode,reason;
  Frame(float x,float e,float l,float ly,float air,float lift,String m,String r){cameraX=x;elevation=e;lookX=l;lookY=ly;airFollowPx=air;girlScreenLiftPx=lift;mode=m;reason=r;}
 }
 private static float smoothElev=0,smoothAir=0,smoothLookX=Float.NaN;
 private static String lastMode="CHARACTER_FRAME";
 private static int stableTicks=0;
 private CatCameraDirector(){}
 public static void resetForTest(){smoothElev=0;smoothAir=0;smoothLookX=Float.NaN;lastMode="CHARACTER_FRAME";stableTicks=0;}

 public static Frame direct(WorldState s){
  float dx=s.haruX-s.catX;
  float elev=26f,lookY=420f;
  float targetLookX=Math.abs(dx)<520f?s.haruX*.76f+s.catX*.24f:s.haruX;
  String wanted="CHARACTER_FRAME";
  String reason="automatic framing follows physical actor state";
  if("girl".equals(s.catState.attachedToEntity)){
   elev=92f;lookY=390f;targetLookX=s.haruX;wanted="REST_CONTEXT";reason="cat is physically carried";
  }else if(Math.abs(dx)<260f){
   elev=108f;lookY=385f;wanted="CLOSE_SAFE_INTERACTION";reason="cat and Haru share a close physical frame";
  }else if(s.girlTravel.active){
   elev=58f;lookY=415f;wanted="CHARACTER_TRAVEL";reason="frame follows Haru travel while retaining cat context";
  }else if("SIT".equalsIgnoreCase(s.haruActivity)||"REST".equalsIgnoreCase(s.haruActivity)){
   elev=72f;lookY=430f;wanted="CHARACTER_REST";reason="Haru is resting";
  }

  float encounterX=nearestLivingEncounterX(s);
  if(Float.isFinite(encounterX)&&Math.abs(encounterX-s.haruX)<230f&&!(s.girlTravel!=null&&s.girlTravel.active)&&!"girl".equals(s.catState.attachedToEntity)){
   targetLookX=s.haruX*.62f+encounterX*.38f;
   elev=94f;lookY=392f;wanted="LIFE_ENCOUNTER";
   reason="nearby living organism shares the frame without controlling Haru";
  }

  double girlClear=s.girlPhysics==null?0:s.girlPhysics.groundClearanceM,catClear=s.catPhysics==null?0:s.catPhysics.groundClearanceM;
  float girlLift=(float)WorldUnits.mToPx(Math.max(0,girlClear)),catLift=(float)WorldUnits.mToPx(Math.max(0,catClear));
  boolean airborne=girlClear>.025||(s.girlPhysics!=null&&!s.girlPhysics.grounded);
  float preserveGround=Math.abs(dx)<560f?.82f:.94f;
  float airTarget=Math.min(180f,girlLift*preserveGround+catLift*.18f);
  float rate=airTarget>smoothAir?.30f:.16f;
  smoothAir+=(airTarget-smoothAir)*rate;
  if(Math.abs(smoothAir)<.15f&&airTarget==0)smoothAir=0;
  if(airborne){
   wanted="AIRBORNE_FOLLOW";
   lookY=Math.max(330f,405f-Math.min(75f,girlLift*.55f));
   reason="camera follows real airborne height while preserving ground context";
  }

  if(!wanted.equals(lastMode)){
   stableTicks++;
   int needed="AIRBORNE_FOLLOW".equals(wanted)||"CLOSE_SAFE_INTERACTION".equals(wanted)?1:2;
   if(stableTicks>=needed){lastMode=wanted;stableTicks=0;}else wanted=lastMode;
  }else stableTicks=0;

  if(!Float.isFinite(smoothLookX)||Math.abs(targetLookX-smoothLookX)>1000f)smoothLookX=targetLookX;
  else smoothLookX+=(targetLookX-smoothLookX)*(s.girlTravel!=null&&s.girlTravel.active?.20f:.14f);
  smoothLookX=Math.max(s.haruX-220f,Math.min(s.haruX+220f,smoothLookX));

  float targetElev=elev+smoothAir;
  smoothElev+=(targetElev-smoothElev)*(airborne?.24f:.16f);
  s.cameraTrace="mode="+wanted+" subject=girl haru="+(int)s.haruX+" cat="+(int)s.catX+
          " target="+(int)smoothLookX+","+(int)smoothElev+" air="+(int)smoothAir+
          " look="+(int)smoothLookX+","+(int)lookY+" manual=false reason="+reason;
  return new Frame(0f,smoothElev,smoothLookX,lookY,smoothAir,girlLift,wanted,reason);
 }

 private static float nearestLivingEncounterX(WorldState s){
  if(s==null||s.world==null)return Float.NaN;
  WorldArea haruArea=s.world.areaAt(s.haruX);
  if(haruArea==null)return Float.NaN;
  float best=Float.NaN,bestD=Float.MAX_VALUE;
  if(s.reedling!=null&&haruArea.id.equals(s.reedling.areaId)){
   float d=Math.abs(s.reedling.x-s.haruX);if(d<bestD){bestD=d;best=s.reedling.x;}
  }
  if(s.livingWorld!=null)for(WorldObject o:s.world.objects){
   if(!LivingWorldEngine.isGenericCreature(o,s))continue;
   CreatureLifeState x=s.livingWorld.creatures.get(o.id);
   if(x==null||!haruArea.id.equals(x.areaId))continue;
   float d=Math.abs(x.x-s.haruX);if(d<bestD){bestD=d;best=x.x;}
  }
  return bestD<=260f?best:Float.NaN;
 }
}
