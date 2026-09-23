package com.aicharacter.v3;
/** Presentation-only mapping: simulation state -> visual state. Never changes cognition. */
public final class GirlAnimationController{
 public enum State{IDLE,WALK_LEFT,WALK_RIGHT,SIT,CROUCH,SLEEP,THINK,REACT,SEARCH_LEFT,SEARCH_RIGHT}
 public static final class Visual{
  public final State state;public final String asset;public final int frames;public final float fps;public final float anchorX,anchorY;public final String reason;public final boolean flipX;
  Visual(State s,String a,int f,float p,float ax,float ay,String r){this(s,a,f,p,ax,ay,r,false);}Visual(State s,String a,int f,float p,float ax,float ay,String r,boolean flip){state=s;asset=a;frames=f;fps=p;anchorX=ax;anchorY=ay;reason=r;flipX=flip;}
  public boolean isWalk(){return state==State.WALK_LEFT||state==State.WALK_RIGHT;}public Visual flipped(){return flipX?this:new Visual(state,asset,frames,fps,anchorX,anchorY,reason,true);}
 }
 private GirlAnimationController(){}
 public static Visual select(WorldState s){
  String a=s.haruActivity==null?"":s.haruActivity.toLowerCase();WorldArea here=s.world==null?null:s.world.areaAt(s.haruX);double exposure=WorldSemantics.exposure(here),rain="RAIN".equals(s.environment.weather)?s.environment.weatherIntensity*exposure:0,wind=s.environment.wind*exposure;boolean socialLeft=SocialProximityEngine.shouldOrientToCat(s)&&s.catState!=null&&s.catState.x<s.haruX;
  if(s.girlPhysics!=null&&s.girlPhysics.falling)return v(State.REACT,"girl_react_right",12,2.4f,.50f,.94f,"whole body lost physical support");
  boolean facingRight=s.girlTravel.active?(Float.isNaN(s.girlTravel.segmentEndX)||s.girlTravel.segmentEndX>=s.haruX):SocialProximityEngine.preferFacingRight(s,true);
  if(a.contains("sleep"))return v(State.SLEEP,"girl_sleep_right",12,1.8f,.50f,.82f,"body/activity sleeping");
  if(a.contains("crouch")||a.contains("lean"))return social(v(State.CROUCH,"girl_crouch_right",12,2.5f,.50f,.94f,"real close interaction"),socialLeft);
  if("find_cat".equals(s.currentIntention)||s.catSearch.active)return facingRight?v(State.SEARCH_RIGHT,"girl_search_right",12,3.2f,.50f,.94f,"active search plan"):v(State.SEARCH_LEFT,"girl_search_left",12,3.2f,.50f,.94f,"active search plan");
  if(s.girlTravel.active){float bodyFactor=s.body.pain>20||s.body.energy<25?.72f:1f;float weatherFactor=(float)Math.max(.62,1-rain*.18-wind*.12-s.worldWetness*.08);float speed=(float)Math.max(2.2,Math.min(8.5,(3.4+s.girlTravel.lastSpeed/80.0)*bodyFactor*weatherFactor));String reason=rain>.18||wind>.35?"travel cadence responds to rain/wind and current body state":"travel expressed through current body state";return facingRight?v(State.WALK_RIGHT,"girl_walk_right",12,speed,.50f,.94f,reason):v(State.WALK_LEFT,"girl_walk_left",12,speed,.50f,.94f,reason);}
  if(rain>.60&&(wind>.42||(s.thermal!=null&&s.thermal.coldLoad>.48)))return social(v(State.REACT,"girl_react_right",12,1.7f,.50f,.94f,"heavy exposed weather visibly changes posture"),socialLeft);
  if(s.body.pain>20)return social(v(State.REACT,"girl_react_right",12,1.9f,.50f,.94f,"pain is visibly affecting movement"),socialLeft);
  if(s.body.energy<22||s.body.sleepiness>78)return social(v(State.SIT,"girl_sit_right",12,1.45f,.50f,.94f,"body pressure is visibly dominant"),socialLeft);
  EmotionEpisodeState emotion=latestEmotion(s);if(emotion!=null&&emotion.intensity>=.30){String e=emotion.primaryEmotion==null?"":emotion.primaryEmotion;if("afraid".equals(e)||"angry".equals(e))return social(v(State.REACT,"girl_react_right",12,2.15f,.50f,.94f,"current "+e+" episode remains visibly active"),socialLeft);if("sad".equals(e)||"lonely".equals(e))return social(v(State.SIT,"girl_sit_right",12,1.35f,.50f,.94f,"current "+e+" episode softens posture"),socialLeft);if("curious".equals(e))return social(v(State.THINK,"girl_think_right",12,1.85f,.50f,.94f,"current curiosity episode keeps attention visibly engaged"),socialLeft);if("joyful".equals(e)||"relieved".equals(e))return social(v(State.REACT,"girl_react_right",12,1.85f,.50f,.94f,"current "+e+" episode becomes a brief open reaction"),socialLeft);}
  HaruVisibleBehaviorBridge.Cue cue=HaruVisibleBehaviorBridge.observe(s,Math.max(s.lastSimulatedAt,s.lastOpenedAt));
  if(cue.mode==HaruVisibleBehaviorBridge.Mode.THINK)return social(v(State.THINK,"girl_think_right",12,1.9f,.50f,.94f,cue.reason),socialLeft);
  if(cue.mode==HaruVisibleBehaviorBridge.Mode.SETTLE)return social(v(State.SIT,"girl_sit_right",12,1.6f,.50f,.94f,cue.reason),socialLeft);
  if(cue.mode==HaruVisibleBehaviorBridge.Mode.REACT)return social(v(State.REACT,"girl_react_right",12,2.25f,.50f,.94f,cue.reason),socialLeft);
  if(a.contains("keeping some distance")||a.contains("unresolved hurt"))return v(State.IDLE,"girl_idle_right",12,1.35f,.50f,.94f,"distance expressed without exposing relationship scores");
  if(a.contains("softening")||a.contains("familiar attention"))return social(v(State.REACT,"girl_react_right",12,2.15f,.50f,.94f,"warmth toward the cat becomes a small visible response"),socialLeft);
  if(a.contains("watching the cat")||a.contains("noticing the cat"))return social(v(State.REACT,"girl_react_right",12,1.9f,.50f,.94f,"local cat perception becomes visible attention"),socialLeft);
  if(a.contains("hesitat")||a.contains("changing her mind")||a.contains("notice")||a.contains("reunion")||a.contains("wait"))return social(v(State.REACT,"girl_react_right",12,2.8f,.50f,.94f,"decision/reaction transition"),socialLeft);
  ThoughtState thought=lastThought(s);long now=Math.max(s.lastSimulatedAt,s.lastOpenedAt);boolean currentThought=thought!=null&&thought.isCurrent(now,s.currentIntention);if(currentThought&&thought.uncertainty>.58&&!s.girlTravel.active)return social(v(State.THINK,"girl_think_right",12,1.75f,.50f,.94f,"current thought remains uncertain"),socialLeft);
  if(a.contains("think")||a.contains("looking")||a.contains("observe")||a.contains("finishing a quiet thought"))return social(v(State.THINK,"girl_think_right",12,2.0f,.50f,.94f,"attention/thought transition"),socialLeft);
  if(a.contains("sitting")||a.equals("resting")||a.contains("taking a quiet rest"))return social(v(State.SIT,"girl_sit_right",12,1.8f,.50f,.94f,"body still needs rest"),socialLeft);
  if(s.relationship!=null&&s.relationship.hurt+s.relationship.irritation>32)return social(v(State.IDLE,"girl_idle_right",12,1.55f,.50f,.94f,"relationship tension keeps her visually reserved"),socialLeft);
  return facingRight?v(State.IDLE,"girl_idle_right",12,2.0f,.50f,.94f,"between committed actions"):v(State.IDLE,"girl_idle_left",12,2.0f,.50f,.94f,"between committed actions");
 }
 private static EmotionEpisodeState latestEmotion(WorldState s){if(s==null||s.emotionEpisodes==null)return null;long now=Math.max(s.lastSimulatedAt,s.lastOpenedAt);for(int i=s.emotionEpisodes.size()-1;i>=0;i--){EmotionEpisodeState e=s.emotionEpisodes.get(i);if(e==null||!"ACTIVE".equals(e.status)||e.intensity<.18)continue;if(e.updatedAt>0&&now>=e.updatedAt&&now-e.updatedAt>12L*60L*1000L)continue;return e;}return null;}
 private static Visual social(Visual x,boolean left){return left&&x!=null?x.flipped():x;}
 private static ThoughtState lastThought(WorldState s){return s.thoughts==null||s.thoughts.isEmpty()?null:s.thoughts.get(s.thoughts.size()-1);}
 public static float renderTop(float ground,float frameHeight,float scale,float anchorY){return ground-anchorY*frameHeight*scale;}
 private static Visual v(State s,String a,int f,float fps,float ax,float ay,String r){return new Visual(s,a,f,fps,ax,ay,r);}
}
