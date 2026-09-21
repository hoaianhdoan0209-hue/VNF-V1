package com.aicharacter.v3;
/**
 * Basic motivational state. Needs are pressures, not commands or a rigid hierarchy.
 * World/body signals raise or lower them; deliberation decides what to do about them.
 */
public final class NeedState {
 public double rest,safety,physicalComfort,solitude,connection,curiosity,hunger,thirst,elimination;
 public static NeedState evaluate(WorldState s){
  NeedState n=new NeedState();
  WorldArea here=s.world==null?null:s.world.areaAt(s.haruX);
  double exposure=WorldSemantics.exposure(here);
  boolean rain="RAIN".equals(s.environment.weather);
  double rainExposure=rain?s.environment.weatherIntensity*exposure:0;
  double wet=Math.max(0,Math.min(1,s.worldWetness));\n  double cold=s.thermal==null?0:s.thermal.coldLoad,heat=s.thermal==null?0:s.thermal.heatLoad,breath=s.respiration==null?0:s.respiration.breathingLoad,hypoxia=s.respiration==null?0:Math.max(0,.95-s.respiration.oxygenSaturation);
  n.hunger=DigestionHydrationEngine.hunger(s)*100;n.thirst=DigestionHydrationEngine.thirst(s)*100;n.elimination=DigestionHydrationEngine.bladderUrgency(s)*100;
  n.rest=(100-s.body.energy)*.65+s.body.sleepiness*.55+s.body.pain*.5+s.body.injuryBurden()*18+cold*12+heat*10+breath*9+n.hunger*.10+n.thirst*.18;
  n.physicalComfort=wet*55+rainExposure*42+s.body.pain*.25+Math.max(0,35-s.body.energy)*.12+cold*48+heat*42+breath*28+n.thirst*.22+n.elimination*.32;
  n.safety=s.body.pain*.8+s.body.injuryBurden()*28+s.emotion.fear*45+rainExposure*18+wet*8+cold*24+heat*22+breath*35+hypoxia*220+Math.max(0,n.thirst-70)*.45;
  n.solitude=s.relationship.irritation*.45+s.relationship.hurt*.55+s.emotion.anger*35;
  n.connection=s.relationship.attachment*.3+s.emotion.loneliness*60+Math.max(0,18-s.relationship.comfort);
  n.curiosity=s.emotion.curiosity*55+Math.max(0,55-s.body.sleepiness)*.3;
  return n;
 }
}
