package com.aicharacter.v3;
/** SI physics core. Rendering/map units cross the boundary only through WorldUnits. */
public final class WholeBodyPhysicsEngine{
 private WholeBodyPhysicsEngine(){}
 public static final double GRAVITY=9.81;

 public static void prepare(WorldState s,String actor,double dt,long now){
  if(s==null||dt<=0)return;
  PhysicsBodyState p="cat".equals(actor)?s.catPhysics:s.girlPhysics;
  if(p==null)return;
  double actorX="cat".equals(actor)?s.catState.x:s.haruX;
  double wet=Math.max(0,Math.min(1,s.worldWetness));
  double slope=Math.abs(GroundGeometry.slope(s,actorX));
  double humidity=s.atmosphere==null?.55:s.atmosphere.relativeHumidity;
  double rain=s.environment!=null&&"RAIN".equals(s.environment.weather)?s.environment.weatherIntensity:0;
  double localDamp=Math.max(0,humidity-.65)*.18;
  p.traction=Math.max(.18,Math.min(1,.98-wet*.36-rain*.12-localDamp-Math.min(.26,slope*.38)));

  double capability="cat".equals(actor)?catCapability(s):
          BodyInstinctEngine.movementCapacity(s)*MusculoskeletalEngine.locomotorCapacity(s)*NervousSystemEngine.locomotorConstraint(s);
  double support=Math.max(.05,p.supportRight-p.supportLeft),supportCenter=(p.supportLeft+p.supportRight)*.5,supportHalf=support*.5;
  double edge=Math.max(0,Math.abs(p.centerOfMassX-supportCenter)-supportHalf);
  double stability=Math.max(0,1-edge/Math.max(.04,supportHalf*.65));
  p.balance=Math.max(.05,Math.min(1,capability*(.58+.42*p.traction)*stability));

  if(p.grounded&&p.balance<.12){
   double bodyHeight="cat".equals(actor)?.24:.55;
   double loss=Math.max(.10,Math.min(bodyHeight,bodyHeight*(.42+(1-p.balance)*.48)));
   loseGroundSupport(s,actor,loss,p.velocityX,now);
  }

  if(p.grounded){
   p.falling=false;
   p.velocityY=0;
   p.groundClearanceM=0;
   p.centerOfMassY="cat".equals(actor)?.24:.55;
   p.groundReaction=p.massKg*GRAVITY*Math.max(.15,Math.cos(slope));
  }else{
   p.groundReaction=0;
   p.velocityY+=GRAVITY*dt;
   p.falling=p.velocityY>=0;
   double oldX=actorX,airDx=p.velocityX*dt,desiredX=oldX+WorldUnits.mToPx(airDx);
   double newX=limitHorizontalMove(s,actor,oldX,desiredX);
   if(Math.abs(newX-desiredX)>.01){p.velocityX=0;p.slipVelocity=0;p.slipSeverity=0;}
   setActorX(s,actor,newX);
   TravelEngine.syncAirborneProgress(s,actor,(float)newX);
   newX="cat".equals(actor)?s.catState.x:s.haruX;
   double groundDeltaM=WorldUnits.pxToM(GroundGeometry.heightPx(s,newX)-GroundGeometry.heightPx(s,oldX));
   p.groundClearanceM+=groundDeltaM-p.velocityY*dt;
   p.velocityX*=Math.exp(-.08*dt);
   if(p.groundClearanceM<=0){
    ImpactEngine.land(s,actor,Math.max(0,p.velocityY),now);
    if("girl".equals(actor)&&!BodyRhythmEngine.isSleeping(s)&&BodyRhythmEngine.shouldCollapse(s))BodyRhythmEngine.collapse(s,now);
    p.groundClearanceM=0;
    p.fallBodyHeightM=0;
    p.centerOfMassY="cat".equals(actor)?.24:.55;
   }else p.fallBodyHeightM=Math.max(p.fallBodyHeightM,p.groundClearanceM);
  }
  p.lastUpdatedAt=Math.max(p.lastUpdatedAt,now);
 }

 /** Physical loss of support. Does not alter intention, personality, memory or render scale. */
 public static void loseGroundSupport(WorldState s,String actor,double clearanceM,double horizontalVelocityMps,long now){
  if(s==null)return;
  PhysicsBodyState p="cat".equals(actor)?s.catPhysics:s.girlPhysics;
  if(p==null)return;
  p.grounded=false;
  p.falling=p.velocityY>=0;
  p.groundReaction=0;
  p.groundClearanceM=Math.max(p.groundClearanceM,Math.max(0,clearanceM));
  p.fallBodyHeightM=Math.max(p.fallBodyHeightM,p.groundClearanceM);
  p.velocityY=Math.max(0,p.velocityY);
  if(Double.isFinite(horizontalVelocityMps))p.velocityX=horizontalVelocityMps;
  p.lastUpdatedAt=Math.max(p.lastUpdatedAt,now);
 }

 /** Jump begins only from real ground contact; gravity then determines the airborne arc. */
 public static boolean beginJump(WorldState s,String actor,double upwardSpeedMps,long now){
  if(s==null)return false;
  PhysicsBodyState p="cat".equals(actor)?s.catPhysics:s.girlPhysics;
  if(p==null||!p.grounded||!Double.isFinite(upwardSpeedMps)||upwardSpeedMps<=0)return false;
  p.grounded=false;
  p.falling=false;
  p.groundReaction=0;
  p.groundClearanceM=0;
  p.fallBodyHeightM=0;
  p.velocityY=-Math.min(8.0,upwardSpeedMps);
  p.lastUpdatedAt=Math.max(p.lastUpdatedAt,now);
  return true;
 }

 public static double realizeHorizontalSpeed(WorldState s,String actor,double requestedPx,double dt,long now){
  PhysicsBodyState p="cat".equals(actor)?s.catPhysics:s.girlPhysics;
  if(p==null)return requestedPx;
  if(p.falling||!p.grounded)return 0;
  double requested=WorldUnits.pxPerSecToMps(requestedPx);
  double cap="cat".equals(actor)?catCapability(s):BodyInstinctEngine.movementCapacity(s)*MusculoskeletalEngine.locomotorCapacity(s);
  double slope=GroundGeometry.slope(s,"cat".equals(actor)?s.catState.x:s.haruX),dir=travelDirection(s,actor),grade=slope*dir;
  double up=Math.max(0,-grade),down=Math.max(0,grade),gradeFactor=Math.max(.56,1-up*.72-down*.20),brake=Math.max(.72,1-down*.18);
  double target=requested*cap*p.balance*gradeFactor*brake,maxFrictionAccel=GRAVITY*Math.max(.15,Math.cos(slope))*p.traction;
  double desiredAccel=(target-p.velocityX)/Math.max(.02,dt),appliedAccel=Math.max(-maxFrictionAccel,Math.min(maxFrictionAccel,desiredAccel)),excessAccel=Math.abs(desiredAccel)-maxFrictionAccel;
  p.velocityX+=appliedAccel*dt;
  if(excessAccel>0){
   double slipDir=Math.signum(desiredAccel==0?p.velocityX:desiredAccel);
   p.slipVelocity+=slipDir*excessAccel*dt*.22;
   p.slipSeverity=Math.min(1,p.slipSeverity+excessAccel/Math.max(.01,maxFrictionAccel)*dt*.7);
  }else{
   p.slipVelocity=approach(p.slipVelocity,0,maxFrictionAccel*.55*dt);
   p.slipSeverity=Math.max(0,p.slipSeverity-dt*.9);
  }
  double achieved=Math.abs(p.velocityX),contactLoss=1-Math.min(.55,p.slipSeverity*.42);
  return WorldUnits.mpsToPxPerSec(Math.max(0,Math.min(requested,achieved*contactLoss)));
 }

 public static double limitHorizontalMove(WorldState s,String actor,double fromX,double desiredX){
  if(s==null||s.world==null||desiredX==fromX)return desiredX;
  double radius=WorldUnits.mToPx("cat".equals(actor)?.16:.22),dir=Math.signum(desiredX-fromX),limited=desiredX;
  for(WorldObject o:s.world.objects){
   if(o==null||!o.enabled||!o.collision)continue;
   for(float[]seg:o.collisionIntervals()){
    if(seg==null||seg.length<2)continue;
    double left=seg[0]-radius,right=seg[1]+radius;
    if(dir>0&&fromX<=left&&desiredX>left)limited=Math.min(limited,left);
    else if(dir<0&&fromX>=right&&desiredX<right)limited=Math.max(limited,right);
   }
  }
  return limited;
 }

 public static boolean blockedBetween(WorldState s,String actor,double fromX,double desiredX){return Math.abs(limitHorizontalMove(s,actor,fromX,desiredX)-desiredX)>.01;}
 private static void setActorX(WorldState s,String actor,double x){
  if("cat".equals(actor)){s.catState.x=(float)x;s.catX=(float)x;WorldArea a=s.world==null?null:s.world.areaAt((float)x);if(a!=null)s.catState.areaId=a.id;}
  else{s.haruX=(float)x;CatOfflineEngine.followAttachment(s);}
 }
 private static double travelDirection(WorldState s,String actor){
  TravelState t="cat".equals(actor)?s.catTravel:s.girlTravel;
  if(t==null||!t.active||!Float.isFinite(t.segmentStartX)||!Float.isFinite(t.segmentEndX))return 0;
  return Math.signum(t.segmentEndX-t.segmentStartX);
 }
 private static double catCapability(WorldState s){
  if(s.catState==null)return 1;
  double fatigue=Math.max(s.catState.sleepiness/100.0,Math.max(0,(30-s.catState.energy)/30.0));
  return Math.max(.24,(1-fatigue*.20)*CatCardioMetabolicEngine.locomotorCapacity(s)*CatNervousSystemEngine.locomotorConstraint(s));
 }
 private static double approach(double v,double t,double d){if(v<t)return Math.min(t,v+d);return Math.max(t,v-d);}
}
