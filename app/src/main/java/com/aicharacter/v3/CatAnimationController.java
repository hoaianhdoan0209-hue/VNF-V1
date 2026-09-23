package com.aicharacter.v3;

/** Presentation-only feline animation selection from real cat physics/social state. */
public final class CatAnimationController {
 public enum State{IDLE,WATCH,WALK,APPROACH,RETREAT,SETTLE,SLEEP,ATTACHED,BRACE}
 public static final class Visual{
  public final State state;public final boolean facingRight;public final double attention,guardedness,energy;public final String reason;
  Visual(State state,boolean facingRight,double attention,double guardedness,double energy,String reason){this.state=state;this.facingRight=facingRight;this.attention=cl(attention);this.guardedness=cl(guardedness);this.energy=cl(energy);this.reason=reason==null?"":reason;}
  public boolean moving(){return state==State.WALK||state==State.APPROACH||state==State.RETREAT;}
 }
 private CatAnimationController(){}

 public static Visual select(WorldState s){
  if(s==null||s.catState==null)return new Visual(State.IDLE,true,0,0,.5,"no cat state");
  CatState c=s.catState;CatSocialState social=s.catSocial==null?new CatSocialState():s.catSocial;boolean right=facingRight(s);
  double nervous=s.catNervous==null?0:max(s.catNervous.balanceAlarm,s.catNervous.recoveryDrive,s.catNervous.fallBrace),guard=cl(social.wariness*.72+nervous*.28),energy=cl(c.energy/100.0);
  if("girl".equals(c.attachedToEntity))return new Visual(State.ATTACHED,right,social.attention,guard,energy,"cat is physically carried with Haru");
  if(!c.awake&&!s.catTravel.active)return new Visual(State.SLEEP,right,0,guard,energy,"cat is sleeping in its persisted location");
  if(s.catPhysics!=null&&(s.catPhysics.falling||!s.catPhysics.grounded)||nervous>.70)return new Visual(State.BRACE,right,social.attention,guard,energy,"balance/fall recovery is physically dominant");
  if(s.catTravel!=null&&s.catTravel.active){
   if("RETREAT".equals(social.mode))return new Visual(State.RETREAT,right,social.attention,guard,energy,"cat is voluntarily increasing social distance");
   if("APPROACH".equals(social.mode))return new Visual(State.APPROACH,right,social.attention,guard,energy,"cat is voluntarily approaching");
   return new Visual(State.WALK,right,social.attention,guard,energy,"cat is physically travelling");
  }
  if("SETTLE_NEAR".equals(social.mode)||c.sleepiness>72)return new Visual(State.SETTLE,right,social.attention,guard,energy,"cat has chosen a settled/resting posture");
  if("girl".equals(social.gazeTarget)||social.attention>.26)return new Visual(State.WATCH,right,social.attention,guard,energy,"cat attention is currently on Haru");
  return new Visual(State.IDLE,right,social.attention,guard,energy,"cat is locally idle");
 }

 private static boolean facingRight(WorldState s){
  if(s.catTravel!=null&&s.catTravel.active&&Float.isFinite(s.catTravel.segmentEndX)&&s.catState!=null&&Math.abs(s.catTravel.segmentEndX-s.catState.x)>1)return s.catTravel.segmentEndX>s.catState.x;
  if(s.catSocial!=null&&"girl".equals(s.catSocial.gazeTarget)&&s.catState!=null)return s.haruX>=s.catState.x;
  return true;
 }
 private static double max(double...v){double m=0;for(double x:v)if(Double.isFinite(x)&&x>m)m=x;return m;}
 private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
