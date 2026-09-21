package com.aicharacter.v3;
/** Shared semantic interaction layer used by autonomy and future cat interactions. */
public final class WorldInteractionEngine {
 private WorldInteractionEngine(){}
 public static boolean interact(WorldState s,WorldObject o,String action){if(o==null||!o.enabled||!o.interactable||!PhysicalInteraction.inRangeForAction(s,o,"REST".equals(action)?"REST_PROTECT":action))return false;String id=o.id;
  if("EAT".equals(action)&&o.tags.contains("food_source")){long now=Math.max(System.currentTimeMillis(),Math.max(s.lastOpenedAt,s.lastSimulatedAt));if(!DigestionHydrationEngine.eat(s,now))return false;s.haruActivity="eating a simple meal";remember(s,o,"ate a simple meal from the food available at home","eat");return true;}
  if("DRINK".equals(action)&&o.tags.contains("water_source")){long now=Math.max(System.currentTimeMillis(),Math.max(s.lastOpenedAt,s.lastSimulatedAt));if(!DigestionHydrationEngine.drink(s,now))return false;s.haruActivity="drinking water";remember(s,o,"drank water at home","drink");return true;}
  if("TOILET".equals(action)&&o.tags.contains("toilet")){long now=Math.max(System.currentTimeMillis(),Math.max(s.lastOpenedAt,s.lastSimulatedAt));if(!DigestionHydrationEngine.urinate(s,now))return false;s.haruActivity="taking care of a bodily need";remember(s,o,"used the private sanitation space at home","toilet");return true;}
  if("REST".equals(action)&&(o.tags.contains("rest")||o.tags.contains("sit"))){if(o.tags.contains("shelter")&&o.hasInteriorZone()&&!o.containsInterior(s.haruX))return false;s.haruActivity=o.tags.contains("shelter")?"resting":"taking a quiet rest";remember(s,o,"rested here for a while","rest");return true;}
  if("REFLECT".equals(action)&&o.tags.contains("reflect")){s.haruActivity="sitting by the lake and reflecting";remember(s,o,"spent a quiet moment reflecting here","reflect");return true;}
  if("OBSERVE".equals(action)){s.haruActivity="looking closely at "+PerceptionBoundary.perceive(s,o).description;remember(s,o,"noticed "+PerceptionBoundary.perceive(s,o).description,"observe");if("reedling_01".equals(o.id))s.reedling.meetHaru(true,System.currentTimeMillis());return true;}return false;}
 private static void remember(WorldState s,WorldObject o,String text,String tag){for(int i=Math.max(0,s.memories.size()-8);i<s.memories.size();i++)if(o.id.equals(s.memories.get(i).location)&&s.memories.get(i).hasTag(tag))return;CognitionEngine.experienceAt(s,"world_interaction",text,0.05,.42,o.id,"world",tag,o.id);}
}
