package com.aicharacter.v3;
/** DEV-only runtime graphics facts. Asset byte/hash/placeholder facts are measured by tools/v092_asset_audit.py. */
public final class GraphicsQualityReport{
 private GraphicsQualityReport(){}
 public static String summarize(WorldState s){
  if(s==null)return"GRAPHICS QUALITY REPORT\nstate=NOT AVAILABLE";
  GirlAnimationController.Visual v=GirlAnimationController.select(s);CatCameraDirector.Frame c=CatCameraDirector.direct(s);
  WorldArea a=s.world.areaAt(s.haruX);
  return"GRAPHICS QUALITY REPORT\narea="+(a==null?"unknown":a.id)+"\ngroundY="+(a==null?"unknown":a.groundY)+"\nweather="+s.environment.weather+"\ndayPhase="+s.environment.dayPhase(s.worldMinutes)+"\nanimation="+v.state+" asset="+v.asset+" anchor="+v.anchorX+","+v.anchorY+"\ncamera="+c.mode+" look="+(int)c.lookX+","+(int)c.lookY+"\nlayers=sky,distant,mid,ground,objects,actors,foreground,atmosphere\nassetAudit=run tools/v092_asset_audit.py for measured file/hash/frame/placeholder facts";
 }
}
