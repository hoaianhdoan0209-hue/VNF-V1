package com.aicharacter.v3;
/** Causal inter-species ecology for the original VNF world. No Earth food-chain assumptions. */
public final class FantasyEcologyEngine{
 private FantasyEcologyEngine(){}
 public static double areaUtility(WorldState s,WorldObject self,WorldArea area){
  if(s==null||s.world==null||self==null||area==null)return 0;SpeciesEcologyProfile p=FantasyEcologyDictionary.forObject(self);if(p==null)return EcologyEngine.creatureSuitability(s,area,self);
  double habitat=EcologyEngine.creatureSuitability(s,area,self),resource=LivingWorldEngine.field(s,area.id).resourcePulse,preferred=tagAffinity(p.preferredAreaTags,area.tags)+tagAffinity(p.preferredAreaTags,s.world.biome(area.biomeId)==null?"":s.world.biome(area.biomeId).tags);
  double relation=relationSignal(s,self,area,p);return cl(habitat*.52+resource*p.resourceAffinity*.26+preferred*.18+relation*.22);
 }
 public static double relationPressure(WorldState s,WorldObject self,WorldArea area){
  SpeciesEcologyProfile p=FantasyEcologyDictionary.forObject(self);if(p==null||s==null||area==null)return 0;double avoid=0;
  if(s.reedling!=null&&!self.id.equals(s.reedling.objectId)&&area.id.equals(s.reedling.areaId)){WorldObject ro=s.world.object(s.reedling.objectId);if(ro!=null&&FantasyEcologyDictionary.anyTag(p.avoidCreatureTags,ro.tags))avoid=Math.max(avoid,.72);}
  if(s.livingWorld!=null)for(WorldObject o:s.world.objects){if(!LivingWorldEngine.isGenericCreature(o,s)||o.id.equals(self.id))continue;CreatureLifeState c=s.livingWorld.creatures.get(o.id);if(c!=null&&area.id.equals(c.areaId)&&FantasyEcologyDictionary.anyTag(p.avoidCreatureTags,o.tags))avoid=Math.max(avoid,.62+.28*c.sense.alertness);}
  return cl(avoid);
 }
 public static String bestAdjacentArea(WorldState s,WorldObject self,String currentAreaId){
  if(s==null||s.world==null)return"";WorldArea cur=s.world.area(currentAreaId);if(cur==null)return"";double here=areaUtility(s,self,cur),best=here;String bestId="";
  for(String id:cur.connections){WorldArea a=s.world.area(id);if(a==null)continue;double u=areaUtility(s,self,a);if(u>best+.08){best=u;bestId=id;}}
  return bestId;
 }
 public static boolean stepReedlingMigration(WorldState s,WorldObject self,CreatureState c,double minutes){
  if(s==null||self==null||c==null||minutes<=0)return false;if(c.body!=null&&(c.body.strain>.72||c.body.vitalReserve<.26)||c.cycle!=null&&("WEAK".equals(c.cycle.phase)||"RECOVER".equals(c.cycle.phase)))return false;SpeciesEcologyProfile p=FantasyEcologyDictionary.forObject(self);if(p==null)return false;WorldArea cur=s.world.area(c.areaId);if(cur==null)return false;
  c.relationPressure=follow(c.relationPressure,relationPressure(s,self,cur),minutes,2.5);String candidate=bestAdjacentArea(s,self,cur);double here=areaUtility(s,self,cur),drive=cl((candidate.isEmpty()?0:.40)+c.relationPressure*.36+(1-here)*.24+c.hunger*.26)*p.migrationDrive;
  c.migrationIntent=follow(c.migrationIntent,drive,minutes,5);if(!candidate.isEmpty()&&(c.ecologyTargetAreaId.isEmpty()||c.migrationIntent>.44))c.ecologyTargetAreaId=candidate;
  if(c.ecologyTargetAreaId.isEmpty()||c.migrationIntent<.30)return false;WorldArea target=s.world.area(c.ecologyTargetAreaId);if(target==null||!cur.connections.contains(target.id)){c.ecologyTargetAreaId="";return false;}
  float dir=target.left>=cur.right?1f:target.right<=cur.left?-1f:(target.left+target.right>cur.left+cur.right?1f:-1f);double speed=8+24*c.energy+16*c.migrationIntent;float nx=c.x+(float)(dir*speed*minutes);
  if(dir>0&&nx>=cur.right-2){c.areaId=target.id;c.x=Math.min(target.right-24,target.left+24);c.ecologyTargetAreaId="";c.migrationIntent*=.45;return true;}
  if(dir<0&&nx<=cur.left+2){c.areaId=target.id;c.x=Math.max(target.left+24,target.right-24);c.ecologyTargetAreaId="";c.migrationIntent*=.45;return true;}
  c.x=Math.max(cur.left+22,Math.min(cur.right-22,nx));return true;
 }
 public static boolean stepMigration(WorldState s,WorldObject self,CreatureLifeState c,double minutes){
  if(s==null||self==null||c==null||minutes<=0)return false;if(c.body!=null&&(c.body.strain>.72||c.body.vitalReserve<.26)||c.cycle!=null&&("WEAK".equals(c.cycle.phase)||"RECOVER".equals(c.cycle.phase)))return false;SpeciesEcologyProfile p=FantasyEcologyDictionary.forObject(self);if(p==null)return false;WorldArea cur=s.world.area(c.areaId);if(cur==null)return false;
  c.relationPressure=follow(c.relationPressure,relationPressure(s,self,cur),minutes,2.5);String candidate=bestAdjacentArea(s,self,cur);double here=areaUtility(s,self,cur),away=c.relationPressure*.38+(1-here)*.24,hunger=c.hunger*.25,targetDrive=cl((candidate.isEmpty()?0:.42)+away+hunger)*p.migrationDrive;
  c.migrationIntent=follow(c.migrationIntent,targetDrive,minutes,5);if(!candidate.isEmpty()&&(c.ecologyTargetAreaId.isEmpty()||c.migrationIntent>.44))c.ecologyTargetAreaId=candidate;
  if(c.ecologyTargetAreaId.isEmpty()||c.migrationIntent<.30)return false;WorldArea target=s.world.area(c.ecologyTargetAreaId);if(target==null||!cur.connections.contains(target.id)){c.ecologyTargetAreaId="";return false;}
  float dir=target.left>=cur.right?1f:target.right<=cur.left?-1f:(target.left+target.right>cur.left+cur.right?1f:-1f);double speed=9+26*c.locomotionDrive+18*c.migrationIntent;float nx=c.x+(float)(dir*speed*minutes);
  if(dir>0&&nx>=cur.right-2){c.areaId=target.id;c.x=Math.min(target.right-24,target.left+24);c.ecologyTargetAreaId="";c.migrationIntent*=.45;return true;}
  if(dir<0&&nx<=cur.left+2){c.areaId=target.id;c.x=Math.max(target.left+24,target.right-24);c.ecologyTargetAreaId="";c.migrationIntent*=.45;return true;}
  c.x=Math.max(cur.left+22,Math.min(cur.right-22,nx));return true;
 }
 public static void stimulateFlora(WorldState s,WorldObject self,String areaId,double minutes){
  if(s==null||s.world==null||minutes<=0)return;SpeciesEcologyProfile p=FantasyEcologyDictionary.forObject(self);if(p==null||p.stimulateFloraTags.isEmpty())return;
  for(WorldObject o:s.world.objects){if(!LivingWorldEngine.isLivingFlora(o)||!areaId.equals(o.areaId))continue;String identity=(o.tags==null?"":o.tags)+","+(o.dictionaryRef==null?"":o.dictionaryRef);if(!FantasyEcologyDictionary.anyTag(p.stimulateFloraTags,identity))continue;FloraLifeState f=LivingWorldEngine.flora(s,o.id);double pulse=p.stimulusStrength*minutes/18.0;f.sense.vibrationSense=cl(f.sense.vibrationSense+pulse*.22);f.growthPulse=cl(f.growthPulse+pulse*.15);f.body.strain=cl(f.body.strain-pulse*.04);if(f.cycle==null)f.cycle=new OrganismCycleState();f.cycle.growthReserve=cl(f.cycle.growthReserve+pulse*.10);}
 }
 public static String relationCue(WorldState s,WorldObject self){
  if(s==null||self==null)return"";String areaId=HaruVisionEngine.actualAreaId(s,self);WorldArea a=s.world.area(areaId);SpeciesEcologyProfile p=FantasyEcologyDictionary.forObject(self);if(a==null||p==null)return"";
  double pressure=relationPressure(s,self,a);if(pressure>.58)return"đang giữ khoảng cách với một sinh vật khác";
  double relation=relationSignal(s,self,a,p);if(relation>.28)return"đang nghiêng chuyển động về phía tín hiệu sống quen thuộc";
  return"";
 }
 public static String diagnostic(WorldState s){StringBuilder b=new StringBuilder("FANTASY ECOLOGY TRACE\n");if(s==null||s.world==null)return b.append("<no world>").toString();for(WorldObject o:s.world.objects){if(!"creature".equals(o.type))continue;SpeciesEcologyProfile p=FantasyEcologyDictionary.forObject(o);if(p==null)continue;String areaId=HaruVisionEngine.actualAreaId(s,o);WorldArea a=s.world.area(areaId);b.append(o.id).append(" area=").append(areaId);if(a!=null)b.append(" utility=").append(fmt(areaUtility(s,o,a))).append(" pressure=").append(fmt(relationPressure(s,o,a))).append(" bestNext=").append(bestAdjacentArea(s,o,areaId));if(s.reedling!=null&&o.id.equals(s.reedling.objectId))b.append(" target=").append(s.reedling.ecologyTargetAreaId).append(" intent=").append(fmt(s.reedling.migrationIntent));else{CreatureLifeState c=s.livingWorld==null?null:s.livingWorld.creatures.get(o.id);if(c!=null)b.append(" target=").append(c.ecologyTargetAreaId).append(" intent=").append(fmt(c.migrationIntent));}b.append('\n');}return b.toString();}
 private static String fmt(double v){return String.format(java.util.Locale.US,"%.2f",v);}
 private static double relationSignal(WorldState s,WorldObject self,WorldArea area,SpeciesEcologyProfile p){double signal=0;
  if(s.reedling!=null&&!self.id.equals(s.reedling.objectId)&&area.id.equals(s.reedling.areaId)){WorldObject ro=s.world.object(s.reedling.objectId);if(ro!=null){if(FantasyEcologyDictionary.anyTag(p.attractCreatureTags,ro.tags))signal+=.34;if(FantasyEcologyDictionary.anyTag(p.avoidCreatureTags,ro.tags))signal-=.48;}}
  if(s.livingWorld!=null)for(WorldObject o:s.world.objects){if(!LivingWorldEngine.isGenericCreature(o,s)||o.id.equals(self.id))continue;CreatureLifeState c=s.livingWorld.creatures.get(o.id);if(c==null||!area.id.equals(c.areaId))continue;if(FantasyEcologyDictionary.anyTag(p.attractCreatureTags,o.tags))signal+=.22*p.groupTolerance;if(FantasyEcologyDictionary.anyTag(p.avoidCreatureTags,o.tags))signal-=.40*(.7+.3*c.sense.alertness);}
  return Math.max(-1,Math.min(1,signal));
 }
 private static double tagAffinity(String wanted,String actual){if(wanted==null||actual==null)return 0;int total=0,hit=0;for(String q:wanted.split(",")){if(q.trim().isEmpty())continue;total++;if(FantasyEcologyDictionary.hasTag(actual,q))hit++;}return total==0?0:(double)hit/total;}
 private static double follow(double v,double target,double minutes,double tau){double k=1-Math.exp(-minutes/Math.max(.05,tau));return cl(v+(target-v)*k);}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}