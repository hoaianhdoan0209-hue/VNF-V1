package com.aicharacter.v3;
import java.util.*;

/** Read-only visual acceptance checks. Does not mutate world, Haru, save or simulation state. */
public final class VisualV1DevTest{
 private VisualV1DevTest(){}
 public static String run(WorldState s){
  List<String> ok=new ArrayList<>(),bad=new ArrayList<>();
  check(HaruVisualRenderer.fixedBodyScale()>0,"Haru uses a positive authored sprite scale",ok,bad);
  float sc=HaruVisualRenderer.fixedBodyScale();
  check(Math.abs((192f*sc)/(288f*sc)-(192f/288f))<.0001f,"Haru sprite aspect ratio is preserved",ok,bad);
  check(HaruVisualRenderer.shadowScale(0)==1f,"grounded Haru shadow remains at contact scale",ok,bad);
  check(HaruVisualRenderer.shadowScale(1000)>=.55f,"airborne shadow reduction is bounded",ok,bad);
  check(s!=null&&s.environment!=null,"visuals consume real environment state",ok,bad);
  check(s!=null&&s.world!=null&&s.world.areas!=null&&s.world.areas.size()>=4,"visual world exposes the four launch areas",ok,bad);
  if(s!=null&&s.world!=null){
   boolean home=false,garden=false,lake=false,grove=false;
   for(WorldArea a:s.world.areas){home|="home_shelter".equals(a.id);garden|="garden_path".equals(a.id);grove|="quiet_grove".equals(a.id);lake|=!home&&!garden&&!grove&&a.id!=null&&a.id.toLowerCase(Locale.ROOT).contains("lake");}
   check(home&&garden&&grove,"Home/Garden/Grove visual identities map to authored world areas",ok,bad);
  }
  StringBuilder b=new StringBuilder("V1 VISUAL TEST\nPASS ").append(ok.size()).append(" / FAIL ").append(bad.size()).append('\n');
  for(String x:ok)b.append("✓ ").append(x).append('\n');
  for(String x:bad)b.append("✗ ").append(x).append('\n');
  return b.toString();
 }
 private static void check(boolean pass,String label,List<String>ok,List<String>bad){(pass?ok:bad).add(label);}
}
