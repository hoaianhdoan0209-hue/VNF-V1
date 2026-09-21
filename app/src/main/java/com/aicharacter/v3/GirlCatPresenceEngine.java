package com.aicharacter.v3;
/** Offline perception-to-presence bridge for ordinary cat proximity. */
public final class GirlCatPresenceEngine{
 private static final long REACTION_COOLDOWN_MS=12000L;
 private GirlCatPresenceEngine(){}
 public static void tick(WorldState s,long now){
  if(s==null||s.catState==null||s.catSearch==null)return;
  boolean perceived=s.catState.awake&&GirlCatSearchEngine.canPerceiveCat(s);
  if(!perceived)return;
  boolean reunionPending=s.reunionContext!=null&&!s.reunionContext.isEmpty();
  if(reunionPending||s.catSearch.active)return;
  if(s.catSearch.found){
   // Search owns exactly the first visible encounter. Once its wake/wait/search outcome has
   // been staged, release that transient flag so ordinary nearby-cat life can resume later.
   s.lastCatSeenAt=now;s.catSearch.found=false;return;
  }
  long previousSeen=s.lastCatSeenAt;
  s.lastCatSeenAt=now;
  // lastCatSeenAt is durable world state, unlike process-static flags. A restart, Activity
  // recreation or another WorldState therefore cannot manufacture a fresh proximity reaction.
  if(previousSeen>0&&now-previousSeen<REACTION_COOLDOWN_MS)return;
  double tension=s.relationship==null?0:s.relationship.irritation+s.relationship.hurt;
  double warmth=s.relationship==null?0:s.relationship.trust*.35+s.relationship.comfort*.35+s.relationship.affection*.2+s.relationship.attachment*.1;
  if(tension>42){s.haruActivity="noticing the cat but keeping some distance";CognitionEngine.process(s,new Experience("cat_nearby_tense","She noticed the cat nearby while unresolved hurt or irritation was still present.",-.04,.42).atTime(now).with("cat").tag("player").tag("relationship"));return;}
  if(s.body.pain>20||s.body.energy<22){s.haruActivity="noticing the cat while staying where she can rest";CognitionEngine.process(s,new Experience("cat_nearby_tired","She noticed the cat nearby while her body still needed rest.",.03,.32).atTime(now).with("cat").tag("player").tag("body"));return;}
  if(warmth>45){s.haruActivity="softening after noticing the cat nearby";CognitionEngine.process(s,new Experience("cat_nearby_warm","She noticed the cat nearby and felt familiar enough to soften a little.",.10,.38).atTime(now).with("cat").tag("player").tag("relationship"));return;}
  if(s.emotion!=null&&s.emotion.curiosity>.5){s.haruActivity="watching the cat with quiet curiosity";CognitionEngine.process(s,new Experience("cat_nearby_curious","She noticed the cat nearby and became curious about what it was doing.",.06,.34).atTime(now).with("cat").tag("player").tag("curiosity"));return;}
  s.haruActivity="noticing the cat nearby";CognitionEngine.process(s,new Experience("cat_nearby","She locally noticed the cat nearby.",.03,.26).atTime(now).with("cat").tag("player"));
 }
}
