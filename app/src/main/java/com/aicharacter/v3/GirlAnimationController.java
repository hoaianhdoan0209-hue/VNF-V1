package com.aicharacter.v3;
/** Presentation-only mapping: simulation state -> visual state. Never changes cognition. */
public final class GirlAnimationController{
 public enum State{IDLE,WALK_LEFT,WALK_RIGHT,SIT,CROUCH,SLEEP,THINK,REACT,SEARCH_LEFT,SEARCH_RIGHT}
 public static final class Visual{
  public final State state;public final String asset;public final int frames;public final float fps;public final float anchorX,anchorY;public final String reason;
  Visual(State s,String a,int f,float p,float ax,float ay,String r){state=s;asset=a;frames=f;fps=p;anchorX=ax;anchorY=ay;reason=r;}
  public boolean isWalk(){return state==State.WALK_LEFT||state==State.WALK_RIGHT;}
 }
 private GirlAnimationController(){}
 public static Visual select(WorldState s){
  String a=s.haruActivity==null?"":s.haruActivity.toLowerCase();
  boolean facingRight=!s.girlTravel.active||Float.isNaN(s.girlTravel.segmentEndX)||s.girlTravel.segmentEndX>=s.haruX;
  if(a.contains("sleep"))return v(State.SLEEP,"girl_sleep_right",12,1.8f,.50f,.82f,"body/activity sleeping");
  if(a.contains("crouch")||a.contains("lean"))return v(State.CROUCH,"girl_crouch_right",12,2.5f,.50f,.94f,"real close interaction");
  if("find_cat".equals(s.currentIntention)||s.catSearch.active)return facingRight?v(State.SEARCH_RIGHT,"girl_search_right",12,3.2f,.50f,.94f,"active search plan"):v(State.SEARCH_LEFT,"girl_search_left",12,3.2f,.50f,.94f,"active search plan");
  if(s.girlTravel.active){float bodyFactor=s.body.pain>20||s.body.energy<25?.72f:1f;float speed=(float)Math.max(2.2,Math.min(8.5,(3.4+s.girlTravel.lastSpeed/80.0)*bodyFactor));return facingRight?v(State.WALK_RIGHT,"girl_walk_right",12,speed,.50f,.94f,"travel expressed through current body state"):v(State.WALK_LEFT,"girl_walk_left",12,speed,.50f,.94f,"travel expressed through current body state");}
  if(s.body.pain>20)return v(State.REACT,"girl_react_right",12,1.9f,.50f,.94f,"pain is visibly affecting movement");
  if(s.body.energy<22||s.body.sleepiness>78)return v(State.SIT,"girl_sit_right",12,1.45f,.50f,.94f,"body pressure is visibly dominant");
  if(a.contains("keeping some distance")||a.contains("unresolved hurt"))return v(State.IDLE,"girl_idle_right",12,1.35f,.50f,.94f,"distance expressed without exposing relationship scores");
  if(a.contains("softening")||a.contains("familiar attention"))return v(State.REACT,"girl_react_right",12,2.15f,.50f,.94f,"warmth toward the cat becomes a small visible response");
  if(a.contains("watching the cat")||a.contains("noticing the cat"))return v(State.REACT,"girl_react_right",12,1.9f,.50f,.94f,"local cat perception becomes visible attention");
  if(a.contains("hesitat")||a.contains("changing her mind")||a.contains("notice")||a.contains("reunion")||a.contains("wait"))return v(State.REACT,"girl_react_right",12,2.8f,.50f,.94f,"decision/reaction transition");
  ThoughtState thought=lastThought(s);if(thought!=null&&thought.uncertainty>.58)return v(State.THINK,"girl_think_right",12,1.75f,.50f,.94f,"current thought remains uncertain");
  if(a.contains("think")||a.contains("looking")||a.contains("observe")||a.contains("finishing a quiet thought"))return v(State.THINK,"girl_think_right",12,2.0f,.50f,.94f,"attention/thought transition");
  if(a.contains("sitting")||a.equals("resting")||a.contains("taking a quiet rest"))return v(State.SIT,"girl_sit_right",12,1.8f,.50f,.94f,"body still needs rest");
  if(s.relationship!=null&&s.relationship.hurt+s.relationship.irritation>32)return v(State.IDLE,"girl_idle_right",12,1.55f,.50f,.94f,"relationship tension keeps her visually reserved");
  return facingRight?v(State.IDLE,"girl_idle_right",12,2.0f,.50f,.94f,"between committed actions"):v(State.IDLE,"girl_idle_left",12,2.0f,.50f,.94f,"between committed actions");
 }
 private static ThoughtState lastThought(WorldState s){return s.thoughts==null||s.thoughts.isEmpty()?null:s.thoughts.get(s.thoughts.size()-1);}
 public static float renderTop(float ground,float frameHeight,float scale,float anchorY){return ground-anchorY*frameHeight*scale;}
 private static Visual v(State s,String a,int f,float fps,float ax,float ay,String r){return new Visual(s,a,f,fps,ax,ay,r);}
}
