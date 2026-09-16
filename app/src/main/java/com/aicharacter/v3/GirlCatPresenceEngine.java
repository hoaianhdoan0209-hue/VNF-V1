package com.aicharacter.v3;
/**
 * Offline perception-to-presence bridge for ordinary cat proximity.
 * It reacts only when the girl can locally perceive the cat; it never reads hidden
 * coordinates to choose a destination and never changes her high-level intention.
 */
public final class GirlCatPresenceEngine{
 private static long lastReactionAt=0;private static boolean previouslyPerceived=false;
 private GirlCatPresenceEngine(){}
 public static void tick(WorldState s,long now){
  if(s==null||s.catState==null)return;
  boolean perceived=s.catState.awake&&GirlCatSearchEngine.canPerceiveCat(s);
  if(!perceived){previouslyPerceived=false;return;}
  s.lastCatSeenAt=now;
  if(previouslyPerceived||now-lastReactionAt<12000)return;
  previouslyPerceived=true;lastReactionAt=now;
  double tension=s.relationship==null?0:s.relationship.irritation+s.relationship.hurt;
  double warmth=s.relationship==null?0:s.relationship.trust*.35+s.relationship.comfort*.35+s.relationship.affection*.2+s.relationship.attachment*.1;
  if(tension>42){s.haruActivity="noticing the cat but keeping some distance";CognitionEngine.experience(s,"cat_nearby_tense","She noticed the cat nearby while unresolved hurt or irritation was still present.",-.04,.42,"cat","player","relationship");return;}
  if(s.body.pain>20||s.body.energy<22){s.haruActivity="noticing the cat while staying where she can rest";CognitionEngine.experience(s,"cat_nearby_tired","She noticed the cat nearby while her body still needed rest.",.03,.32,"cat","player","body");return;}
  if(warmth>45){s.haruActivity="softening after noticing the cat nearby";CognitionEngine.experience(s,"cat_nearby_warm","She noticed the cat nearby and felt familiar enough to soften a little.",.10,.38,"cat","player","relationship");return;}
  if(s.emotion!=null&&s.emotion.curiosity>.5){s.haruActivity="watching the cat with quiet curiosity";CognitionEngine.experience(s,"cat_nearby_curious","She noticed the cat nearby and became curious about what it was doing.",.06,.34,"cat","player","curiosity");return;}
  s.haruActivity="noticing the cat nearby";CognitionEngine.experience(s,"cat_nearby","She locally noticed the cat nearby.",.03,.26,"cat","player");
 }
}
