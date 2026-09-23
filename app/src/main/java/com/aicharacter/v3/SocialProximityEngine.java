package com.aicharacter.v3;

/**
 * Haru-owned social spacing around a locally perceived cat.
 *
 * Relationship/emotion produce a preferred distance band and a decision bias.
 * Actual movement only occurs if autonomy selects social_adjust; the target is frozen
 * from the locally perceived cat position at plan start, so this never tracks hidden catX.
 */
public final class SocialProximityEngine {
 public static final class Assessment {
  public final boolean perceived,adjustmentNeeded;public final String mode,reason;public final double desiredMin,desiredMax,distance,warmth,guardedness,utility;public final float targetX;
  Assessment(boolean perceived,boolean need,String mode,String reason,double min,double max,double distance,double warmth,double guard,double utility,float targetX){this.perceived=perceived;adjustmentNeeded=need;this.mode=mode;this.reason=reason;desiredMin=min;desiredMax=max;this.distance=distance;this.warmth=warmth;guardedness=guard;this.utility=utility;this.targetX=targetX;}
 }
 private static final long ADJUSTMENT_COOLDOWN_MS=18_000L;
 private SocialProximityEngine(){}

 public static Assessment assess(WorldState s,long now){
  if(s==null||s.world==null||s.catState==null||s.relationship==null)return none("social state unavailable");
  if("girl".equals(s.catState.attachedToEntity))return none("cat is already physically attached to Haru");
  boolean perceived=s.catState.awake&&GirlCatSearchEngine.canPerceiveCat(s);if(!perceived)return none("cat is not locally perceived");
  double warmth=warmth(s),guard=guardedness(s),distance=Math.abs(s.haruX-s.catState.x),min,max;String mode;
  if(guard>.46&&guard>warmth*.78){mode="GUARDED";min=112+guard*92;max=min+52;}
  else if(warmth>.36&&guard<.58){mode="APPROACH";min=50+guard*24;max=86+guard*28;}
  else{mode="NEUTRAL";min=78+guard*22;max=132+guard*34;}
  min=clamp(min,42,215);max=clamp(Math.max(min+20,max),68,275);
  double deviation=distance<min?min-distance:distance>max?distance-max:0;
  boolean cooldown=s.socialProximity!=null&&s.socialProximity.lastAdjustmentAt>0&&now>=s.socialProximity.lastAdjustmentAt&&now-s.socialProximity.lastAdjustmentAt<ADJUSTMENT_COOLDOWN_MS;
  boolean bodyBlocked=s.body==null||s.body.energy<22||s.body.pain>34||s.body.sleepiness>84;
  boolean committed=s.planState!=null&&s.planState.active()&&!"social_adjust".equals(s.planState.intentionId);
  boolean need=deviation>15&&!cooldown&&!bodyBlocked&&!committed&&(s.girlTravel==null||!s.girlTravel.active);
  double drive="GUARDED".equals(mode)?guard:warmth*.72+cl01(s.emotion==null?0:s.emotion.loneliness)*.24;
  double utility=need?Math.min(18,deviation*.105)+drive*7.5:0;
  float target=targetX(s,min,max);
  String reason="GUARDED".equals(mode)?"hurt/irritation/fear favor more personal space":"APPROACH".equals(mode)?"trust/affection/comfort make a nearer distance feel acceptable":"current relationship favors a moderate distance";
  updateState(s,mode,reason,min,max,distance,warmth,guard,utility,target,now);
  return new Assessment(true,need,mode,reason,min,max,distance,warmth,guard,utility,target);
 }

 public static boolean beginPlan(WorldState s,LifeDecision d,long now){
  if(s==null||d==null||d.intention==null||!"social_adjust".equals(d.intention.id))return false;Assessment a=assess(s,now);if(!a.perceived||!a.adjustmentNeeded)return false;
  PlanState p=new PlanState();p.planId="plan_social_"+Long.toHexString(now);p.intentionId="social_adjust";p.goal="adjust social distance without overriding other needs";p.status="ACTIVE";p.origin="SOCIAL_MICRO";p.commitment=.28;p.createdAt=now;p.lastProgressAt=now;p.destination="cat";p.plannedAction="SOCIAL_SPACING";p.steps.add("ASSESS_SOCIAL_DISTANCE:"+a.mode);p.steps.add("MOVE_TO_OBSERVED_OFFSET:"+(int)a.targetX);p.steps.add("REVIEW_SOCIAL_COMFORT");
  s.planState=p;s.currentIntention="social_adjust";s.persistentIntentionTarget="cat";s.intentionStartedAt=now;s.haruActivity=activity(a.mode);
  s.socialProximity.activePlanId=p.planId;s.socialProximity.lastAdjustmentAt=now;s.socialProximity.lastTargetX=a.targetX;
  WorldEventBus.publishId(s,now,"social_spacing_start_"+p.planId,"HARU_SOCIAL_SPACING_STARTED",p.planId,"mode="+a.mode+" observedDistance="+fmt(a.distance)+" desired="+fmt(a.desiredMin)+"-"+fmt(a.desiredMax)+" fixedTargetX="+(int)a.targetX);
  if(TravelEngine.startLocal(s,s.girlTravel,"girl","observed_cat_offset",a.targetX,12,p.planId,now))return true;
  p.status="FAILED";p.lastOutcome="could not make bounded local social adjustment";p.lastProgressAt=now;s.currentIntention="";s.persistentIntentionTarget="";s.intentionStartedAt=now;s.socialProximity.activePlanId="";PlanExecutor.learnTerminalOutcome(s,p,now,"social_spacing_blocked",p.lastOutcome,false);return false;
 }

 public static String complete(WorldState s,PlanState p,long now){
  if(s==null||p==null)return"social spacing completed";double distance=Math.abs(s.haruX-s.catState.x);SocialProximityState st=s.socialProximity;st.lastCompletedAt=now;st.lastObservedDistance=distance;st.activePlanId="";
  String outcome="settled at a self-chosen "+st.mode.toLowerCase()+" social distance after a locally perceived encounter";
  s.haruActivity="GUARDED".equals(st.mode)?"staying nearby while keeping comfortable space":"APPROACH".equals(st.mode)?"settling a little nearer the cat":"sharing the area without forcing closeness";
  WorldEventBus.publishId(s,now,"social_spacing_done_"+p.planId,"HARU_SOCIAL_SPACING_COMPLETED",p.planId,"mode="+st.mode+" resultingDistance="+fmt(distance));
  return outcome;
 }

 public static void abandon(WorldState s,PlanState p,long now,String why){
  if(s==null||p==null)return;if(s.socialProximity!=null)s.socialProximity.activePlanId="";s.girlTravel.active=false;p.status="ABANDONED";p.lastOutcome="social adjustment yielded to a more important need: "+why;p.lastProgressAt=now;s.currentIntention="";s.persistentIntentionTarget="";s.intentionStartedAt=now;s.haruActivity="letting the small social adjustment go";WorldEventBus.publishId(s,now,"social_spacing_abandon_"+p.planId,"HARU_SOCIAL_SPACING_ABANDONED",p.planId,p.lastOutcome);
 }

 public static boolean shouldOrientToCat(WorldState s){
  if(s==null||s.catState==null||s.world==null||"girl".equals(s.catState.attachedToEntity))return false;WorldArea ga=s.world.areaAt(s.haruX),ca=s.world.areaAt(s.catState.x);if(ga==null||ca==null||!ga.id.equals(ca.id))return false;double d=Math.abs(s.haruX-s.catState.x);return d<=320&&(GirlCatSearchEngine.canPerceiveCat(s)||(s.socialProximity!=null&&s.socialProximity.lastEvaluatedAt>0));
 }
 public static double gazeEngagement(WorldState s){
  if(s==null)return 0;double warm=warmth(s),guard=guardedness(s);double curiosity=s.emotion==null?0:cl01(s.emotion.curiosity),joy=s.emotion==null?0:cl01(s.emotion.joy);return cl01(.24+warm*.58+curiosity*.16+joy*.10-guard*.62);
 }
 public static double gazeDownBias(WorldState s){double guard=guardedness(s),sad=s==null||s.emotion==null?0:cl01(s.emotion.sadness),lonely=s==null||s.emotion==null?0:cl01(s.emotion.loneliness);return cl01(guard*.54+sad*.34+lonely*.18);}
 public static boolean preferFacingRight(WorldState s,boolean fallback){if(!shouldOrientToCat(s))return fallback;return s.catState.x>=s.haruX;}

 private static double warmth(WorldState s){if(s==null||s.relationship==null)return 0;RelationshipState r=s.relationship;return cl01((safe(r.affection)*.25+safe(r.trust)*.27+safe(r.comfort)*.28+safe(r.attachment)*.20)/101.0);}
 private static double guardedness(WorldState s){if(s==null)return 0;RelationshipState r=s.relationship;double relational=r==null?0:(safe(r.hurt)*.52+safe(r.irritation)*.48)/101.0,anger=s.emotion==null?0:cl01(s.emotion.anger),fear=s.emotion==null?0:cl01(s.emotion.fear);return cl01(relational*.66+anger*.22+fear*.28);}
 private static float targetX(WorldState s,double min,double max){WorldArea a=s.world.areaAt(s.haruX);if(a==null)return s.haruX;double desired=(min+max)*.5,side=Math.signum(s.haruX-s.catState.x);if(side==0)side=s.socialProximity!=null&&Float.isFinite(s.socialProximity.lastTargetX)&&s.socialProximity.lastTargetX>s.catState.x?1:-1;float raw=(float)(s.catState.x+side*desired);float margin=20;return Math.max(a.left+margin,Math.min(a.right-margin,raw));}
 private static void updateState(WorldState s,String mode,String reason,double min,double max,double distance,double warmth,double guard,double utility,float target,long now){if(s.socialProximity==null)s.socialProximity=new SocialProximityState();SocialProximityState st=s.socialProximity;st.mode=mode;st.reason=reason;st.desiredMin=min;st.desiredMax=max;st.lastObservedDistance=distance;st.lastWarmth=warmth;st.lastGuardedness=guard;st.lastUtility=utility;st.lastTargetX=target;st.lastEvaluatedAt=now;}
 private static Assessment none(String why){return new Assessment(false,false,"NEUTRAL",why,82,132,0,0,0,0,Float.NaN);}
 private static String activity(String mode){if("GUARDED".equals(mode))return"quietly making a little more room around the cat";if("APPROACH".equals(mode))return"choosing to move a little nearer the cat";return"adjusting where she stands near the cat";}
 private static double safe(double v){return Double.isFinite(v)?Math.max(0,Math.min(101,v)):0;}private static double cl01(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}private static double clamp(double v,double a,double b){return Double.isFinite(v)?Math.max(a,Math.min(b,v)):a;}private static String fmt(double v){return String.format(java.util.Locale.US,"%.1f",v);}
}
