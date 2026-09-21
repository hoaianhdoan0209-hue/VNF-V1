package com.aicharacter.v3;
/** Lightweight non-human ecology. Habitat and biome bound where creatures can live; weather only modulates activity. */
public final class CreatureLifeEngine {private CreatureLifeEngine(){}
 public static void advance(WorldState s,double minutes,long now){if(s==null||s.world==null||minutes<=0)return;CreatureState c=s.reedling;if(c==null){LivingWorldEngine.advance(s,minutes,now);return;}WorldObject o=s.world.object(c.objectId);if(o==null)return;WorldArea a=s.world.area(c.areaId);if(a==null)a=s.world.area(o.areaId);if(a==null)return;
  double suitability=EcologyEngine.creatureSuitability(s,a,o);if(suitability<.28){WorldArea better=bestHabitat(s,o);if(better!=null&&EcologyEngine.creatureSuitability(s,better,o)>suitability+.18){a=better;c.areaId=a.id;c.x=(a.left+a.right)*.5f;suitability=EcologyEngine.creatureSuitability(s,a,o);}}
  c.hunger=Math.min(1,c.hunger+minutes*(.0012+.0007*(1-suitability)));double alert=c.sense==null?0:c.sense.alertness,bodyStrain=c.body==null?0:c.body.strain;BiomeProfile b=EcologyEngine.biome(s,a);double exposure=WorldSemantics.exposure(a),rain="RAIN".equals(s.environment.weather)?s.environment.weatherIntensity:0,canopy=b==null?0:b.canopy,risk=rain*exposure*(1-canopy*.55);
  if(suitability<.32||bodyStrain>.72){c.activity="rest";c.energy=Math.max(0,c.energy-minutes*.0012);}
  else if(c.hunger>.65&&suitability>.45){c.activity="forage";double gathered=LivingWorldEngine.consumeFieldResource(s,a.id,minutes*.0025*suitability);c.energy=Math.min(1,Math.max(0,c.energy-minutes*.0012)+gathered*.22);c.hunger=Math.max(0,c.hunger-gathered*.78);}
  else if(risk>.62||c.energy<.25||alert>.78){c.activity="rest";c.energy=Math.min(1,c.energy+minutes*.003);}
  else{c.activity="wander";c.energy=Math.max(0,c.energy-minutes*.0009);float span=Math.max(20,a.right-a.left-80);double phase=((now/60000L)+(c.id.hashCode()&31))*.17;c.x=(float)(a.left+40+(Math.sin(phase)*.5+.5)*span);}
  c.areaId=a.id;c.lastUpdate=now;LivingWorldEngine.advance(s,minutes,now);
 }
 private static WorldArea bestHabitat(WorldState s,WorldObject o){WorldArea best=null;double score=-1;for(WorldArea a:s.world.areas){double x=EcologyEngine.creatureSuitability(s,a,o);if(x>score){score=x;best=a;}}return best;}
 public static boolean perceivable(WorldState s){WorldObject o=s.world==null?null:s.world.object(s.reedling.objectId);if(o==null)return false;WorldArea ga=s.world.areaAt(s.haruX);if(ga==null||!ga.id.equals(s.reedling.areaId))return false;double range=160*WorldSemantics.visibility(s,ga);return Math.abs(s.haruX-s.reedling.x)<=range;}
 public static MemoryEntry encounter(WorldState s,long now){if(!perceivable(s))return null;s.reedling.meetHaru(true,now);int k=s.knowledge.getOrDefault("reedling",0);s.knowledge.put("reedling",Math.min(4,k+1));return CognitionEngine.process(s,new Experience("creature_encounter","noticed a small creature nearby",.08,.45).id("creature_"+s.reedling.id+"_"+now).at(s.reedling.areaId,s.reedling.objectId).tag("observe").tag("creature"));}
}
