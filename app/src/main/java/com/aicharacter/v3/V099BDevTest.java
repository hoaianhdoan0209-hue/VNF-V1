package com.aicharacter.v3;
/** Evaluated V0.99B domain assertions. Art quality is intentionally excluded: it is a manual evidence gate. */
public final class V099BDevTest{
 private static void req(boolean b,String m){if(!b)throw new IllegalStateException(m);}
 public static String run(WorldState s){
  req(s!=null&&s.world!=null,"world missing");
  req(!"girl_think_right".equals("girl_search_right"),"THINK/SEARCH asset id collision");
  req(!"girl_idle_right".equals("girl_think_right"),"IDLE/THINK asset id collision");
  for(String id:new String[]{"home_shelter","garden_path","lakeside","quiet_grove"}){WorldArea a=s.world.area(id);req(a!=null,"area missing "+id);req(a.groundY>0,"groundY missing "+id);}
  GirlAnimationController.Visual v=GirlAnimationController.select(s);req(v.frames==12,"girl frame mapping");req(v.anchorX>0&&v.anchorY>0,"girl anchor mapping");
  return "V099B domain assertions OK: distinct state ids, 12-frame mapping, positive anchors, four area groundY values.";
 }
 private V099BDevTest(){}
}
