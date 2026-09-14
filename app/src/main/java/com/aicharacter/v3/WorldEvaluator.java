package com.aicharacter.v3;
import java.util.*;
public final class WorldEvaluator {
 private WorldEvaluator(){}
 public static List<WorldIssue> evaluate(WorldState s){List<WorldIssue> out=new ArrayList<>();
  if(s.world==null||s.world.areas.isEmpty()){out.add(new WorldIssue("WORLD_DEFINITION_CORRUPT","CRITICAL","CORE_CONFIG","world","World definition is missing areas.","DEVELOPER_REPORT","",100,false,1));return out;}
  if(s.world.areaAt(s.haruX)==null)out.add(new WorldIssue("HARU_OUT_OF_BOUNDS","HIGH","HARU_PROTECTION","haru","Girl entity is outside navigable world.","CLAMP_HARU","",95,true,1));
  if(s.world.areaAt(s.catX)==null)out.add(new WorldIssue("CAT_OUT_OF_BOUNDS","HIGH","POSITION","cat","Player cat is outside navigable world.","CLAMP_CAT","",75,true,1));
  for(WorldObject o:s.world.objects){WorldArea a=s.world.area(o.areaId);if(a==null)out.add(new WorldIssue("BROKEN_AREA_REF","HIGH","WORLD_CONFIG",o.id,"Object references missing area.","DEVELOPER_REPORT","worldDefinition",90,false,1));else if(o.x<a.left||o.x>a.right)out.add(new WorldIssue("PROP_OUTSIDE_AREA","MEDIUM","LAYOUT",o.id,"Object is outside owning area.","MOVE_PROP_INSIDE","",55,true,1));if(o.enabled&&(o.assetRef==null||o.assetRef.trim().isEmpty()))out.add(new WorldIssue("MISSING_ASSET_REF","MEDIUM","ASSET",o.id,"Enabled object has no asset reference.","DEVELOPER_REPORT","",60,false,1));}
  if(s.devForceRepairRegression && s.environment.ambientBrightness>=0 && s.environment.ambientBrightness<=1)out.add(new WorldIssue("DEV_REPAIR_REGRESSION","CRITICAL","DEV_TEST","environment","Injected verification regression after repair.","DEVELOPER_REPORT","",100,false,1));
  if(s.environment.ambientBrightness<0||s.environment.ambientBrightness>1)out.add(new WorldIssue("LIGHT_RANGE","MEDIUM","ENVIRONMENT","environment","Ambient brightness outside 0..1.","CLAMP_LIGHT","",50,true,1));
  if(!Arrays.asList("CLEAR","CLOUDY","RAIN").contains(s.environment.weather))out.add(new WorldIssue("WEATHER_INVALID","HIGH","ENVIRONMENT","environment","Unsupported weather state.","RESET_WEATHER","",70,true,1));
  if(s.body.health<0||s.body.health>100||s.body.energy<0||s.body.energy>100||s.body.pain<0||s.body.pain>100)out.add(new WorldIssue("BODY_CORRUPT","HIGH","HARU_PROTECTION","haru","Biological metrics outside simulation range.","CLAMP_BODY","",98,true,1));
  out.sort((a,b)->Integer.compare(b.priorityScore(),a.priorityScore()));return out;}
 public static boolean containsKey(List<WorldIssue> xs,String key){for(WorldIssue x:xs)if(x.key().equals(key))return true;return false;}
 public static int worstSeverity(List<WorldIssue> xs){int w=0;for(WorldIssue x:xs)w=Math.max(w,"CRITICAL".equals(x.severity)?4:"HIGH".equals(x.severity)?3:"MEDIUM".equals(x.severity)?2:1);return w;}
}
