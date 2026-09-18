package com.aicharacter.v3;
/** Range gate shared by planned object actions. Interaction is evaluated from an actor-reachable contact point, not visual sprite center. */
public final class PhysicalInteraction{private PhysicalInteraction(){}
 public static float range(WorldObject o){return o==null?0:(o.interactionRadius>0?o.interactionRadius:Math.max(28,Math.min(72,o.width*.35f+20)));}
 public static float reachableTargetX(WorldState s,WorldObject o,String actor,float actorX){return o==null?actorX:o.nearestReachableInteractionX(s,actor,actorX);}
 public static boolean inRange(WorldState s,WorldObject o){if(s==null||o==null||s.world==null)return false;WorldArea a=s.world.areaAt(s.haruX);if(a==null||!a.id.equals(o.areaId))return false;float tx=reachableTargetX(s,o,"girl",s.haruX);return !WholeBodyPhysicsEngine.blockedBetween(s,"girl",s.haruX,tx)&&Math.abs(s.haruX-tx)<=range(o);}
 public static String trace(WorldState s,WorldObject o){if(s==null||o==null)return"target=<null> execute=false";float tx=reachableTargetX(s,o,"girl",s.haruX),d=Math.abs(s.haruX-tx),r=range(o);boolean clear=!WholeBodyPhysicsEngine.blockedBetween(s,"girl",s.haruX,tx);return"target="+o.id+" reachableX="+(int)tx+" currentX="+(int)s.haruX+" distance="+(int)d+" range="+(int)r+" clear="+clear+" execute="+(clear&&d<=r);}
}
