package com.aicharacter.v3;

/** Deterministic physical locomotion for authored fictional creatures.
 * Ground fauna obey traction/slope/collision. Suspended fauna counter gravity with bounded lift.
 * No random teleports: local targets are deterministic world-time waypoints.
 */
public final class CreaturePhysicsEngine{
 private CreaturePhysicsEngine(){}

 public static void advanceLocal(WorldState s,WorldObject o,CreatureLifeState c,double minutes,long now){
  if(s==null||s.world==null||o==null||c==null||minutes<=0)return;
  WorldArea area=s.world.area(c.areaId);
  if(area==null){area=s.world.area(o.areaId);if(area==null)return;c.areaId=area.id;c.x=o.x;}
  double seconds=Math.max(.001,minutes*60.0);
  boolean flyer=isFlyer(o);
  float margin=Math.max(22f,o.width*.48f);
  float left=area.left+margin,right=area.right-margin;
  if(right<=left){left=area.left;right=area.right;}

  long bucket=Math.max(0L,now/waypointIntervalMs(o));
  double unit=unitHash(o.id,bucket);
  float target=(float)(left+(right-left)*(.12+.76*unit));
  float dxPx=target-c.x;
  if(Math.abs(dxPx)<10)target=(float)(left+(right-left)*(.18+.64*unitHash(o.id,bucket+1)));
  dxPx=target-c.x;

  double moisture=EcologyEngine.localMoisture(s,area);
  double slope=Math.abs(GroundGeometry.slope(s,c.x));
  double traction=flyer?1.0:clamp(.88-moisture*.16-slope*.42,.30,.96);
  double speciesSpeed=locomotionSpeedMps(o,c.locomotionDrive);
  if("withdraw".equals(c.activity))speciesSpeed*=1.45;
  else if("rest".equals(c.activity))speciesSpeed*=.12;
  else if("forage".equals(c.activity))speciesSpeed*=.72;
  double desired=Math.signum(dxPx)*speciesSpeed;
  double maxAccel=WholeBodyPhysicsEngine.GRAVITY*traction*(flyer?.30:.48);
  double dv=desired-c.velocityXMps;
  double applied=Math.max(-maxAccel*seconds,Math.min(maxAccel*seconds,dv));
  c.velocityXMps+=applied;
  double drag=flyer?.42:.72;
  c.velocityXMps*=Math.exp(-drag*Math.min(seconds,4.0)*(.35+.65*(1-c.locomotionDrive)));
  if(Math.abs(c.velocityXMps)<.006)c.velocityXMps=0;
  c.heading=c.velocityXMps<-.006?-1:c.velocityXMps>.006?1:c.heading==0?1:c.heading;

  double travelM=c.velocityXMps*seconds;
  double maxToTarget=Math.abs(WorldUnits.pxToM(target-c.x));
  if(Math.abs(travelM)>maxToTarget)travelM=Math.signum(travelM)*maxToTarget;
  float desiredX=(float)(c.x+WorldUnits.mToPx(travelM));
  desiredX=Math.max(left,Math.min(right,desiredX));
  desiredX=limitAgainstWorld(s,o,c.areaId,c.x,desiredX);
  if(Math.abs(desiredX-c.x)<.01&&Math.abs(travelM)>.001)c.velocityXMps=0;
  c.x=desiredX;

  if(flyer)advanceVerticalFlight(s,o,c,area,seconds,now);
  else{c.verticalOffsetM=0;c.velocityYMps=0;c.grounded=true;}
 }

 public static void advanceReedlingLocal(WorldState s,WorldObject o,CreatureState c,double minutes,long now){
  if(s==null||s.world==null||o==null||c==null||minutes<=0)return;WorldArea area=s.world.area(c.areaId);if(area==null)return;
  double seconds=Math.max(.001,minutes*60.0),moisture=EcologyEngine.localMoisture(s,area),slope=Math.abs(GroundGeometry.slope(s,c.x));
  float margin=Math.max(22f,o.width*.45f),left=area.left+margin,right=area.right-margin;if(right<=left){left=area.left;right=area.right;}
  long bucket=Math.max(0L,now/30000L);float target=(float)(left+(right-left)*(.14+.72*unitHash(o.id,bucket)));
  double traction=clamp(.90-moisture*.14-slope*.40,.34,.97),speed=.10+.28*c.energy+.18*(1-c.fear);
  if("rest".equals(c.activity))speed*=.10;else if("forage".equals(c.activity))speed*=.65;
  double desired=Math.signum(target-c.x)*speed,maxAccel=WholeBodyPhysicsEngine.GRAVITY*traction*.46,dv=desired-c.velocityXMps;
  c.velocityXMps+=Math.max(-maxAccel*seconds,Math.min(maxAccel*seconds,dv));c.velocityXMps*=Math.exp(-.74*Math.min(seconds,4.0)*.45);
  c.heading=c.velocityXMps<-.006?-1:c.velocityXMps>.006?1:c.heading==0?1:c.heading;
  double travelM=c.velocityXMps*seconds,maxToTarget=Math.abs(WorldUnits.pxToM(target-c.x));if(Math.abs(travelM)>maxToTarget)travelM=Math.signum(travelM)*maxToTarget;
  float desiredX=(float)(c.x+WorldUnits.mToPx(travelM));desiredX=Math.max(left,Math.min(right,desiredX));
  desiredX=limitAgainstWorld(s,o,c.areaId,c.x,desiredX);if(Math.abs(desiredX-c.x)<.01)c.velocityXMps=0;c.x=desiredX;
  c.verticalOffsetM=0;c.velocityYMps=0;c.grounded=true;
 }

 public static float renderGroundY(WorldState s,WorldObject o,CreatureLifeState c){
  if(s==null||o==null||c==null)return o==null?0:o.y;
  return (float)GroundGeometry.heightPx(s,c.x);
 }

 public static boolean isFlyer(WorldObject o){
  if(o==null)return false;
  String locomotion=locomotion(o);
  if("air-drift".equals(locomotion)||"membrane-flight".equals(locomotion))return true;
  return o.id.startsWith("driftwing")||o.id.startsWith("hearthmote")||has(o.tags,"glide")||has(o.tags,"wing");
 }
 static double locomotionSpeedMps(WorldObject o,double drive){
  drive=clamp(drive,0,1);String locomotion=locomotion(o);
  if("four-point walk".equals(locomotion))return .16+.34*drive;
  if("six-beat scuttle".equals(locomotion))return .20+.44*drive;
  if("short-hop".equals(locomotion))return .17+.40*drive;
  if("air-drift".equals(locomotion))return .12+.34*drive;
  if("membrane-flight".equals(locomotion))return .23+.50*drive;
  if("water-pulse".equals(locomotion))return .11+.28*drive;
  if("surface-skate".equals(locomotion))return .21+.46*drive;
  if("burrow-wave".equals(locomotion))return .08+.24*drive;
  if("climb-coil".equals(locomotion))return .09+.27*drive;
  if("root-step".equals(locomotion))return .07+.18*drive;
  if("fin-undulate".equals(locomotion))return .13+.32*drive;
  if("ground-glide".equals(locomotion))return .14+.32*drive;
  return .18+.42*drive;
 }
 private static long waypointIntervalMs(WorldObject o){
  String locomotion=locomotion(o);
  if("six-beat scuttle".equals(locomotion))return 12000L;
  if("short-hop".equals(locomotion))return 14000L;
  if("surface-skate".equals(locomotion))return 16000L;
  if("water-pulse".equals(locomotion))return 20000L;
  if("membrane-flight".equals(locomotion))return 22000L;
  if("fin-undulate".equals(locomotion))return 24000L;
  if("air-drift".equals(locomotion))return 45000L;
  if("climb-coil".equals(locomotion))return 48000L;
  if("burrow-wave".equals(locomotion))return 60000L;
  if("root-step".equals(locomotion))return 65000L;
  return 30000L;
 }
 private static String locomotion(WorldObject o){
  if(o==null)return"";
  SpeciesEvolutionCatalog.Species species=SpeciesEvolutionCatalog.get(o.dictionaryRef);
  return species==null||species.locomotion==null?"":species.locomotion;
 }

 private static void advanceVerticalFlight(WorldState s,WorldObject o,CreatureLifeState c,WorldArea area,double seconds,long now){
  double wind=s.environment==null?0:s.environment.wind;
  double vitality=c.body==null?.7:c.body.vitalReserve;
  double strain=c.body==null?0:c.body.strain;
  String locomotion=locomotion(o);double base=("air-drift".equals(locomotion)?.86:"membrane-flight".equals(locomotion)?1.34:1.10)+(Math.abs(o.id.hashCode())%7)*.06;
  double current=.18*Math.sin(now/4200.0+(o.id.hashCode()&31))+.12*wind;
  double target=Math.max(.42,base+current-.38*strain);
  double liftSupport=clamp(.91+.11*vitality+.10*c.locomotionDrive+.05*wind,.72,1.10);
  double spring=(target-c.verticalOffsetM)*2.1;
  double net=WholeBodyPhysicsEngine.GRAVITY*(liftSupport-1.0)+spring;
  double step=Math.min(seconds,1.25);
  c.velocityYMps+=net*step;
  c.velocityYMps*=Math.exp(-1.15*step);
  c.verticalOffsetM+=c.velocityYMps*step;
  c.verticalOffsetM=Math.max(0,Math.min(2.8,c.verticalOffsetM));
  if(c.verticalOffsetM<=.02){c.verticalOffsetM=0;c.velocityYMps=Math.max(0,c.velocityYMps);c.grounded=true;}
  else c.grounded=false;
 }

 private static float limitAgainstWorld(WorldState s,WorldObject self,String areaId,float from,float desired){
  if(s==null||s.world==null||desired==from)return desired;
  float radius=Math.max(8f,self.width*.32f),dir=Math.signum(desired-from),out=desired;
  for(WorldObject o:s.world.objects){
   if(o==null||o==self||!o.enabled||!o.collision||areaId==null||!areaId.equals(o.areaId))continue;
   for(float[]seg:o.collisionIntervals()){
    if(seg==null||seg.length<2)continue;
    float l=seg[0]-radius,r=seg[1]+radius;
    if(dir>0&&from<=l&&desired>l)out=Math.min(out,l);
    else if(dir<0&&from>=r&&desired<r)out=Math.max(out,r);
   }
  }
  return out;
 }
 private static double unitHash(String id,long bucket){
  long x=1469598103934665603L;
  String k=(id==null?"":id)+"#"+bucket;
  for(int i=0;i<k.length();i++){x^=k.charAt(i);x*=1099511628211L;}
  long positive=x&0x7fffffffffffffffL;
  return (positive%1000003L)/1000003.0;
 }
 private static boolean has(String tags,String q){if(tags==null)return false;for(String x:tags.split(","))if(q.equalsIgnoreCase(x.trim()))return true;return false;}
 private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
}
