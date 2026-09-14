package com.aicharacter.v3;
/** Semantic route edge. Destination remains a cognition concern; this only describes traversal. */
public final class WorldConnection{
 public final String id,fromAreaId,toAreaId,traversalType,tags;public final float exitX,entryX,distance;public final double baseCost,weatherSensitivity,safety;
 public WorldConnection(String from,String to,float exitX,float entryX,double weatherSensitivity,double safety,String tags){this.fromAreaId=from;this.toAreaId=to;this.id=from+"->"+to;this.exitX=exitX;this.entryX=entryX;this.distance=Math.max(40,Math.abs(entryX-exitX));this.baseCost=Math.max(1,distance);this.weatherSensitivity=weatherSensitivity;this.safety=safety;this.traversalType="WALK";this.tags=tags;}
}
