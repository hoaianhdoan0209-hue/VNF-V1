package com.aicharacter.v3;
/**
 * Basic motivational state. Needs are pressures, not commands or a rigid hierarchy.
 * World/body signals raise or lower them; deliberation decides what to do about them.
 */
public final class NeedState {
 public double rest,safety,physicalComfort,solitude,connection,curiosity;
 public static NeedState evaluate(WorldState s){
  NeedState n=new NeedState();
  WorldArea here=s.world==null?null:s.world.areaAt(s.haruX);
  double exposure=WorldSemantics.exposure(here);
  boolean rain="RAIN".equals(s.environment.weather);
  double rainExposure=rain?s.environment.weatherIntensity*exposure:0;
  double wet=Math.max(0,Math.min(1,s.worldWetness));
  n.rest=(100-s.body.energy)*.65+s.body.sleepiness*.55+s.body.pain*.5;
  n.physicalComfort=wet*55+rainExposure*42+s.body.pain*.25+Math.max(0,35-s.body.energy)*.12;
  n.safety=s.body.pain*.8+s.emotion.fear*45+rainExposure*18+wet*8;
  n.solitude=s.relationship.irritation*.45+s.relationship.hurt*.55+s.emotion.anger*35;
  n.connection=s.relationship.attachment*.3+s.emotion.loneliness*60+Math.max(0,18-s.relationship.comfort);
  n.curiosity=s.emotion.curiosity*55+Math.max(0,55-s.body.sleepiness)*.3;
  return n;
 }
}
