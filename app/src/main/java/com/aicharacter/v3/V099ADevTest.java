package com.aicharacter.v3;
/** Source/domain assertions for the V0.99A presentation contract. Resource-byte audits live in tools/v099a_asset_audit.py. */
public final class V099ADevTest{
 private V099ADevTest(){}
 private static void require(boolean v,String m){if(!v)throw new IllegalStateException("V099A: "+m);}
 public static String run(WorldState s){
  String old=s.haruActivity,oi=s.currentIntention;boolean travel=s.girlTravel.active,search=s.catSearch.active;
  s.girlTravel.active=false;s.catSearch.active=false;s.currentIntention="";s.haruActivity="thinking";
  GirlAnimationController.Visual think=GirlAnimationController.select(s);require(think.state==GirlAnimationController.State.THINK,"THINK mapping");require(think.frames==12,"production frame count");
  s.haruActivity="";s.currentIntention="find_cat";GirlAnimationController.Visual sr=GirlAnimationController.select(s);require(sr.state==GirlAnimationController.State.SEARCH_RIGHT||sr.state==GirlAnimationController.State.SEARCH_LEFT,"SEARCH mapping");require(!sr.asset.equals(think.asset),"SEARCH dedicated asset");
  s.currentIntention="";s.haruActivity="crouch near cat";require(GirlAnimationController.select(s).state==GirlAnimationController.State.CROUCH,"CROUCH mapping");
  WorldArea a=s.world==null?null:s.world.areaAt(s.haruX);require(a==null||a.groundY>0,"WorldArea groundY");
  s.haruActivity=old;s.currentIntention=oi;s.girlTravel.active=travel;s.catSearch.active=search;
  return "V099A domain assertions OK: dedicated THINK/SEARCH/CROUCH, 12-frame presentation mapping, WorldArea groundY.";
 }
}
