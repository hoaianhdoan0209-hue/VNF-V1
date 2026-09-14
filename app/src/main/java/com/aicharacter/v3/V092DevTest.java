package com.aicharacter.v3;
/** V0.9.2 runtime/state assertions. File/hash/image audits live in tools/v092_asset_audit.py. */
public final class V092DevTest{
 private V092DevTest(){}
 private static void line(StringBuilder b,String id,boolean pass,String detail){b.append(id).append(' ').append(pass?"PASS":"FAIL").append(" — ").append(detail).append('\n');}
 public static String run(WorldState s){
  StringBuilder b=new StringBuilder("DEV V092 TEST\n");
  boolean oldActive=s.girlTravel.active;float oldEnd=s.girlTravel.segmentEndX;String oldAct=s.haruActivity,oldInt=s.currentIntention;boolean oldSearch=s.catSearch.active;float oldCat=s.catX,oldGirl=s.haruX;
  try{
   s.haruActivity="idle";s.currentIntention="";s.catSearch.active=false;s.girlTravel.active=true;s.girlTravel.segmentEndX=s.haruX+100;
   GirlAnimationController.Visual walk=GirlAnimationController.select(s);line(b,"A",walk.isWalk(),"travel active -> "+walk.state);
   s.girlTravel.active=false;GirlAnimationController.Visual idle=GirlAnimationController.select(s);line(b,"A2",!idle.isWalk(),"travel inactive -> "+idle.state);
   s.haruActivity="think";GirlAnimationController.Visual think=GirlAnimationController.select(s);line(b,"B",!think.asset.equals(idle.asset),"THINK="+think.asset+" IDLE="+idle.asset);
   s.haruActivity="idle";s.currentIntention="find_cat";s.catSearch.active=true;GirlAnimationController.Visual search=GirlAnimationController.select(s);line(b,"C",!search.asset.equals(think.asset)&&search.state.name().startsWith("SEARCH"),"SEARCH="+search.asset+" THINK="+think.asset);
   float ground=846,top=GirlAnimationController.renderTop(ground,260,1.34f,idle.anchorY);float foot=top+260*1.34f*idle.anchorY;line(b,"E",Math.abs(foot-ground)<0.01f,"rendered foot="+foot+" ground="+ground);
   float topA=GirlAnimationController.renderTop(810,260,1.34f,idle.anchorY),topB=GirlAnimationController.renderTop(880,260,1.34f,idle.anchorY);line(b,"F",Math.abs((topB-topA)-70)<0.01f,"render Y follows area ground delta 70 -> "+(topB-topA));
   s.haruActivity="idle";s.currentIntention="";s.catSearch.active=false;s.catX=1000;s.haruX=1120;CatCameraDirector.resetForTest();CatCameraDirector.Frame safe=null;for(int ci=0;ci<12;ci++)safe=CatCameraDirector.direct(s);line(b,"M","CLOSE_SAFE_INTERACTION".equals(safe.mode)&&safe.elevation>50,"mode="+safe.mode+" elevation="+safe.elevation);
   CatCameraDirector.resetForTest();s.catX=1000;s.haruX=1740;String first=CatCameraDirector.direct(s).mode;s.haruX=1765;String second=CatCameraDirector.direct(s).mode;s.haruX=1740;String third=CatCameraDirector.direct(s).mode;line(b,"N",!( !first.equals(second)&&!second.equals(third) ),"modes="+first+","+second+","+third);
   s.girlTravel.active=false;s.haruActivity="sitting near bench";GirlAnimationController.Visual sit=GirlAnimationController.select(s);line(b,"O",sit.state==GirlAnimationController.State.SIT,"sit visual only when simulation activity is sitting/resting");
  }finally{s.girlTravel.active=oldActive;s.girlTravel.segmentEndX=oldEnd;s.haruActivity=oldAct;s.currentIntention=oldInt;s.catSearch.active=oldSearch;s.catX=oldCat;s.haruX=oldGirl;}
  b.append("D/H/I/J/K/L/P/Q/R/S/T/U: use measured asset/static/render audit output; no hardcoded PASS.\n");
  return b.toString();
 }
}
