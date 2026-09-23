package com.aicharacter.v3;

/**
 * Presentation-only expression synthesis from Haru's current affect and attention.
 * Values are derived each frame and never persisted back into cognition.
 */
public final class HaruExpressionEngine {
 public static final class Visual{
  public final double intensity,gazeX,gazeY,eyeOpen,browLift,browPinch,mouthCurve,mouthOpen,cheekWarmth,headTiltDeg,headDropPx;
  public final boolean socialFocus,blink;public final String emotion,reason;
  Visual(double intensity,double gazeX,double gazeY,double eyeOpen,double browLift,double browPinch,double mouthCurve,double mouthOpen,double cheekWarmth,double headTiltDeg,double headDropPx,boolean socialFocus,boolean blink,String emotion,String reason){
   this.intensity=cl01(intensity);this.gazeX=clamp(gazeX,-1,1);this.gazeY=clamp(gazeY,-1,1);this.eyeOpen=cl01(eyeOpen);this.browLift=clamp(browLift,-1,1);this.browPinch=cl01(browPinch);this.mouthCurve=clamp(mouthCurve,-1,1);this.mouthOpen=cl01(mouthOpen);this.cheekWarmth=cl01(cheekWarmth);this.headTiltDeg=clamp(headTiltDeg,-3.2,3.2);this.headDropPx=clamp(headDropPx,-2,5);this.socialFocus=socialFocus;this.blink=blink;this.emotion=emotion==null?"calm":emotion;this.reason=reason==null?"":reason;
  }
 }
 private HaruExpressionEngine(){}

 public static Visual derive(WorldState s,long now){
  if(s==null||s.emotion==null)return neutral();
  EmotionEpisodeState ep=latestEpisode(s,now);
  double joy=cl01(s.emotion.joy),fear=cl01(s.emotion.fear),sad=cl01(s.emotion.sadness),anger=cl01(s.emotion.anger),curious=cl01(s.emotion.curiosity),lonely=cl01(s.emotion.loneliness),calm=cl01(s.emotion.calm),relief=ep==null?0:cl01(ep.relief);
  double intensity=ep==null?max(joy,fear,sad,anger,curious,lonely):Math.max(ep.intensity,max(joy,fear,sad,anger,curious,lonely));
  String emotion=ep!=null&&ep.primaryEmotion!=null&&!ep.primaryEmotion.isEmpty()?ep.primaryEmotion:s.emotion.dominant();
  boolean social=socialFocus(s,ep);
  double gx=gazeX(s,ep,social),gy=0;
  if(social){double engagement=SocialProximityEngine.gazeEngagement(s);gx*=engagement;gy+=SocialProximityEngine.gazeDownBias(s)*.34;}
  if((sad+lonely)>.62&&!social)gy=.46;else if(fear>.55&&!social)gy=-.12;else if(curious>.48&&!social)gy=-.05;

  double eye=.62+fear*.30+curious*.18- sad*.18-lonely*.10-joy*.06-calm*.04;
  double lift=fear*.54+curious*.38+sad*.30+lonely*.18-anger*.25;
  double pinch=anger*.82+fear*.22+sad*.10;
  double mouth=joy*.82+relief*.58-sad*.72-lonely*.48-anger*.46-fear*.18;
  double open=fear*.42+joy*.15+curious*.08-relief*.05;
  double cheeks=joy*.55+relief*.24;
  double drop=sad*3.6+lonely*2.8+fear*.35-joy*.35;
  double tilt=(curious*2.5+joy*.75+lonely*.6-anger*.8)*((gx<-.08)?-1:1);
  if(social&&Math.abs(gx)>.1)tilt+=Math.signum(gx)*.55;

  boolean blink=blink(s,now);
  if(blink)eye=.04;
  String reason=(ep==null?"aggregate affect":"episode "+ep.primaryEmotion+" from "+ep.sourceKind)+(social?"; social gaze is modulated by trust/warmth versus hurt/fear":"; gaze follows current attention");
  return new Visual(intensity,gx,gy,eye,lift,pinch,mouth,open,cheeks,tilt,drop,social,blink,emotion,reason);
 }

 public static boolean supports(GirlAnimationController.State state){
  return state==GirlAnimationController.State.IDLE||state==GirlAnimationController.State.REACT||state==GirlAnimationController.State.THINK||state==GirlAnimationController.State.SIT||state==GirlAnimationController.State.CROUCH;
 }

 private static EmotionEpisodeState latestEpisode(WorldState s,long now){
  if(s.emotionEpisodes==null)return null;for(int i=s.emotionEpisodes.size()-1;i>=0;i--){EmotionEpisodeState e=s.emotionEpisodes.get(i);if(e==null||!"ACTIVE".equals(e.status)||e.intensity<.08)continue;if(e.updatedAt>0&&now>=e.updatedAt&&now-e.updatedAt>12L*60L*1000L)continue;return e;}return null;
 }
 private static boolean socialFocus(WorldState s,EmotionEpisodeState ep){
  boolean near=Math.abs(s.haruX-s.catX)<=460f||s.catState!=null&&"girl".equals(s.catState.attachedToEntity);
  boolean episode=ep!=null&&("cat".equals(ep.targetId)||ep.socialRelevance>=.42||(ep.cause!=null&&ep.cause.toLowerCase().contains("cat")));
  boolean plan="find_cat".equals(s.currentIntention)||"social_adjust".equals(s.currentIntention)||(s.reunionContext!=null&&!s.reunionContext.isEmpty());
  return near&&(episode||plan||SocialProximityEngine.shouldOrientToCat(s));
 }
 private static double gazeX(WorldState s,EmotionEpisodeState ep,boolean social){
  if(social){double d=s.catX-s.haruX;return clamp(d/190.0,-1,1);}
  if(s.planState!=null&&s.world!=null&&s.planState.destination!=null&&!s.planState.destination.isEmpty()){WorldObject o=s.world.object(s.planState.destination);if(o!=null)return clamp((o.x-s.haruX)/260.0,-1,1);}
  if(s.girlTravel!=null&&s.girlTravel.active&&Float.isFinite(s.girlTravel.segmentEndX))return clamp((s.girlTravel.segmentEndX-s.haruX)/180.0,-1,1);
  return 0;
 }
 private static boolean blink(WorldState s,long now){
  long seed=s.createdAt>0?s.createdAt:17L,phase=Math.floorMod(now+seed%1400L,4700L);return phase<105L;
 }
 private static Visual neutral(){return new Visual(0,0,0,.62,0,0,0,0,0,0,0,false,false,"calm","no emotional state");}
 private static double max(double...v){double m=0;for(double x:v)if(Double.isFinite(x)&&x>m)m=x;return m;}
 private static double cl01(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}private static double clamp(double v,double a,double b){return Double.isFinite(v)?Math.max(a,Math.min(b,v)):0;}
}
