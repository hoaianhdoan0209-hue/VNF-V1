package com.aicharacter.v3;
/** Evaluated V0.99C integration invariants. Visual quality remains a manual evidence gate. */
public final class V099CDevTest{
 private static void req(boolean b,String m){if(!b)throw new IllegalStateException(m);}
 public static String run(WorldState s){
  req(s!=null&&s.world!=null,"world missing");
  for(String id:new String[]{"home_shelter","garden_path","lakeside","quiet_grove"}){WorldArea a=s.world.area(id);req(a!=null,"area missing "+id);req(a.groundY>0,"groundY missing "+id);}
  GirlAnimationController.Visual v=GirlAnimationController.select(s);req(v.frames==12,"girl frames");req(v.anchorX>0&&v.anchorY>0,"girl anchors");
  req(!"girl_think_right".equals("girl_search_right"),"THINK/SEARCH collision");
  req(s.reedling!=null,"creature state missing");
  return "V099C integration assertions OK";
 }
 private V099CDevTest(){}
}
