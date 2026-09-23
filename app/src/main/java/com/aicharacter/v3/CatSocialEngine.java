package com.aicharacter.v3;

/**
 * Cat-owned social response to locally observed Haru behavior.
 * This does not read Haru's private relationship/belief state.
 */
public final class CatSocialEngine {
 private static final double PERCEPTION_RANGE=420.0;
 private static final long PLAYER_OVERRIDE_MS=6500L,RESPONSE_COOLDOWN_MS=9000L,LEARNING_INTERVAL_MS=8000L;
 private CatSocialEngine(){}

 public static boolean tick(WorldState s,long now){
  if(s==null||s.world==null||s.catState==null)return false;if(s.catSocial==null)s.catSocial=new CatSocialState();CatSocialState cs=s.catSocial;cs.normalize();CatState cat=s.catState;
  if(!cat.awake||"girl".equals(cat.attachedToEntity)){clearTravelOwnership(s);cs.attention=0;cs.gazeTarget="";return false;}
  if(ownsTravel(s,s.catTravel)){
   cs.attention=Math.max(cs.attention,.72);cs.gazeTarget="girl";
   if(!s.catTravel.active){interrupt(s,now,"social travel stopped before arrival");return false;}
   return true;
  }
  if(s.catTravel!=null&&s.catTravel.active)return false;
  if(playerOverrideActive(s,now)){cs.mode="WATCH";cs.reason="recent player activity keeps autonomous cat response passive";return false;}

  Observation o=observe(s,now);
  if(!o.seesGirl){cs.attention=follow(cs.attention,0,.22);cs.gazeTarget="";cs.lastApproachPressure=0;return false;}
  learn(s,o,now);
  cs.attention=follow(cs.attention,Math.max(.28,cs.curiosity*.62+cs.familiarity*.30),.35);cs.gazeTarget="girl";
  if(cat.energy<15||cat.sleepiness>94||nervousLoad(s)>.78){cs.mode="STAY";cs.reason="body/balance state outweighs optional social movement";return false;}

  double affinity=cl(cs.familiarity*.43+cs.comfort*.40+cs.curiosity*.17),guard=cl(cs.wariness*.68+o.approachPressure*.58+nervousLoad(s)*.34);
  if(guard>.56&&o.distance<205){
   float target=retreatTarget(s,o,guard);if(Float.isFinite(target)&&Math.abs(target-cat.x)>20)return startMove(s,now,"RETREAT",target,"Haru approached within the cat's current comfort boundary",o);
   cs.mode="STAY";cs.reason="wary but no safer bounded local retreat is available";return false;
  }
  if(cs.lastResponseAt>0&&now>=cs.lastResponseAt&&now-cs.lastResponseAt<RESPONSE_COOLDOWN_MS){cs.mode="WATCH";cs.reason="watching Haru during social response cooldown";return false;}
  double tired=Math.max(cat.sleepiness/100.0,(100-cat.energy)/100.0);
  if(cs.comfort>.56&&cs.familiarity>.48&&guard<.42&&tired>.38&&o.distance>=55&&o.distance<=145){
   cs.mode="SETTLE_NEAR";cs.reason="familiar calm proximity and tiredness make staying nearby comfortable";cs.settles++;cs.lastResponseAt=now;WorldEventBus.publishId(s,now,"cat_social_settle_"+Long.toHexString(now),"CAT_SOCIAL_SETTLE_NEAR","cat","Cat chose to settle near Haru without attaching or being controlled.");return false;
  }
  if(affinity>.48&&guard<.46&&o.distance>125){
   float target=approachTarget(s,o,affinity);if(Float.isFinite(target)&&Math.abs(target-cat.x)>18)return startMove(s,now,"APPROACH",target,"familiarity/comfort/curiosity support a voluntary closer distance",o);
  }
  cs.mode=affinity>.34?"WATCH":"STAY";cs.reason=affinity>.34?"curiosity keeps attention on Haru without requiring movement":"current cat state does not favor changing distance";cs.lastResponseAt=now;return false;
 }

 public static boolean ownsTravel(WorldState s,TravelState t){return s!=null&&s.catSocial!=null&&t!=null&&!s.catSocial.activeTravelId.isEmpty()&&s.catSocial.activeTravelId.equals(t.currentPlanId);}

 public static void onArrival(WorldState s,TravelState t,long now){
  if(s==null||s.catSocial==null||t==null)return;CatSocialState cs=s.catSocial;String mode=cs.mode;cs.activeTravelId="";cs.frozenTargetX=Float.NaN;cs.lastResponseAt=now;cs.lastObservedDistance=locallySeesGirl(s)?Math.abs(s.catState.x-s.haruX):cs.lastObservedDistance;
  if("APPROACH".equals(mode)){cs.approaches++;cs.comfort=cl(cs.comfort+.018);cs.familiarity=cl(cs.familiarity+.012);}
  else if("RETREAT".equals(mode)){cs.retreats++;cs.wariness=cl(cs.wariness+.010);}
  cs.reason=("APPROACH".equals(mode)?"cat completed a voluntary closer approach":"RETREAT".equals(mode)?"cat completed a self-protective retreat":"cat completed social repositioning");
  WorldEventBus.publishId(s,now,"cat_social_arrive_"+Long.toHexString(now),"CAT_SOCIAL_RESPONSE_COMPLETED","cat","mode="+mode+" distance="+(locallySeesGirl(s)?fmt(Math.abs(s.catState.x-s.haruX)):"girl no longer locally perceived"));
 }

 public static void notePlayerActive(WorldState s,long now){
  if(s==null||s.catState==null)return;if(s.catSocial==null)s.catSocial=new CatSocialState();s.catState.lastPlayerActiveAt=Math.max(s.catState.lastPlayerActiveAt,now);s.catSocial.lastPlayerOverrideAt=Math.max(s.catSocial.lastPlayerOverrideAt,now);
  if(ownsTravel(s,s.catTravel)){s.catTravel.active=false;s.catTravel.interruption="player_override";clearTravelOwnership(s);WorldEventBus.publishId(s,now,"cat_social_player_override_"+Long.toHexString(now),"CAT_SOCIAL_RESPONSE_YIELDED","cat","Recent player activity immediately yielded autonomous social movement.");}
 }

 public static boolean locallySeesGirl(WorldState s){
  if(s==null||s.world==null||s.catState==null||!s.catState.awake)return false;WorldArea ca=s.world.areaAt(s.catState.x),ga=s.world.areaAt(s.haruX);if(ca==null||ga==null||!ca.id.equals(ga.id))return false;double visibility=s.visibility<=0?1:s.visibility,range=PERCEPTION_RANGE*Math.max(.48,visibility);return Math.abs(s.catState.x-s.haruX)<=range;
 }

 private static Observation observe(WorldState s,long now){
  CatSocialState cs=s.catSocial;double dist=Math.abs(s.catState.x-s.haruX);boolean sees=locallySeesGirl(s);double pressure=0;if(sees&&cs.lastObservedAt>0&&now>cs.lastObservedAt&&cs.lastObservedDistance>0){double closing=Math.max(0,cs.lastObservedDistance-dist),dt=Math.max(.25,(now-cs.lastObservedAt)/1000.0);pressure=cl((closing/dt)/72.0);}
  cs.lastObservedDistance=sees?dist:cs.lastObservedDistance;cs.lastObservedAt=now;cs.lastApproachPressure=pressure;return new Observation(sees,dist,pressure);
 }

 private static void learn(WorldState s,Observation o,long now){
  CatSocialState cs=s.catSocial;double intrusion=o.distance<52?cl((52-o.distance)/35.0):0,pressure=o.approachPressure;
  if(intrusion>0||pressure>.52){double shock=Math.max(intrusion,pressure);cs.wariness=cl(cs.wariness+.045+.055*shock);cs.comfort=cl(cs.comfort-.025-.025*shock);cs.lastCalmExposure=0;cs.lastLearningAt=now;return;}
  if(cs.lastLearningAt>0&&now>=cs.lastLearningAt&&now-cs.lastLearningAt<LEARNING_INTERVAL_MS)return;cs.lastLearningAt=now;
  boolean calm=o.distance>=65&&o.distance<=230&&pressure<.24&&nervousLoad(s)<.42;
  if(calm){cs.calmEncounters++;cs.lastCalmExposure=cl(.45+(1-cs.wariness)*.40);cs.familiarity=cl(cs.familiarity+.014*(1-cs.wariness));cs.comfort=cl(cs.comfort+.018*(.65+cs.familiarity*.35));cs.wariness=cl(cs.wariness-.012*(.5+cs.comfort*.5));}
 }

 private static boolean startMove(WorldState s,long now,String mode,float target,String reason,Observation o){
  CatSocialState cs=s.catSocial;String id="cat_social_"+mode.toLowerCase()+"_"+Long.toHexString(now);cs.mode=mode;cs.reason=reason;cs.activeTravelId=id;cs.frozenTargetX=target;cs.lastResponseAt=now;boolean ok=TravelEngine.startLocal(s,s.catTravel,"cat","haru_social_offset",target,10,id,now);if(!ok){cs.activeTravelId="";cs.frozenTargetX=Float.NaN;cs.mode="STAY";cs.reason="chosen social movement was not physically reachable";return false;}
  WorldEventBus.publishId(s,now,"cat_social_start_"+Long.toHexString(now),"CAT_SOCIAL_RESPONSE_STARTED","cat","mode="+mode+" observedDistance="+fmt(o.distance)+" approachPressure="+fmt(o.approachPressure)+" fixedTargetX="+(int)target);
  return s.catTravel.active;
 }
 private static float approachTarget(WorldState s,Observation o,double affinity){WorldArea a=s.world.areaAt(s.catState.x);if(a==null)return Float.NaN;double desired=96-affinity*18,side=Math.signum(s.catState.x-s.haruX);if(side==0)side=1;float raw=(float)(s.haruX+side*desired);return clampToArea(a,raw);}
 private static float retreatTarget(WorldState s,Observation o,double guard){WorldArea a=s.world.areaAt(s.catState.x);if(a==null)return Float.NaN;double desired=155+guard*55,side=Math.signum(s.catState.x-s.haruX);if(side==0)side=1;float raw=(float)(s.haruX+side*desired);return clampToArea(a,raw);}
 private static float clampToArea(WorldArea a,float x){float margin=24;float lo=a.left+margin,hi=a.right-margin;if(hi<lo)return Float.NaN;return Math.max(lo,Math.min(hi,x));}
 private static boolean playerOverrideActive(WorldState s,long now){long at=Math.max(s.catState.lastPlayerActiveAt,s.catSocial.lastPlayerOverrideAt);return at>0&&now>=at&&now-at<PLAYER_OVERRIDE_MS;}
 private static double nervousLoad(WorldState s){if(s==null||s.catNervous==null)return 0;return cl(Math.max(s.catNervous.balanceAlarm,Math.max(s.catNervous.recoveryDrive,s.catNervous.fallBrace)));}
 private static void interrupt(WorldState s,long now,String why){CatSocialState cs=s.catSocial;cs.activeTravelId="";cs.frozenTargetX=Float.NaN;cs.mode="WATCH";cs.reason=why;cs.lastResponseAt=now;WorldEventBus.publishId(s,now,"cat_social_interrupt_"+Long.toHexString(now),"CAT_SOCIAL_RESPONSE_INTERRUPTED","cat",why);}
 private static void clearTravelOwnership(WorldState s){if(s!=null&&s.catSocial!=null){s.catSocial.activeTravelId="";s.catSocial.frozenTargetX=Float.NaN;}}
 private static double follow(double a,double b,double k){return cl(a+(b-a)*k);}private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}private static String fmt(double v){return String.format(java.util.Locale.US,"%.2f",v);}
 private static final class Observation{final boolean seesGirl;final double distance,approachPressure;Observation(boolean s,double d,double p){seesGirl=s;distance=d;approachPressure=p;}}
}
