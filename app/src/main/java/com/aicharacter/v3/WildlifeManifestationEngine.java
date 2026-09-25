package com.aicharacter.v3;

import java.util.*;

/**
 * Turns sparse population fields into a tiny number of visible representative creatures.
 * The 500-species catalog remains population-level; this class never materializes the whole catalog.
 */
public final class WildlifeManifestationEngine {
 public static final String DYNAMIC_PREFIX="eco_";
 public static final int MAX_DYNAMIC_PER_AREA=2;
 public static final double MIN_VISIBLE_ABUNDANCE=.035;
 private WildlifeManifestationEngine(){}

 public static int sync(WorldState s,long now){
  if(s==null||s.world==null||s.livingWorld==null)return 0;
  LinkedHashSet<String> desired=new LinkedHashSet<>();
  int added=0;
  for(WorldArea area:s.world.areas){
   ArrayList<SpeciesPopulationState> candidates=new ArrayList<>();
   for(SpeciesPopulationState p:s.livingWorld.populations.values()){
    if(p==null||!area.id.equals(p.areaId))continue;p.clamp();
    if(p.relativeAbundance<MIN_VISIBLE_ABUNDANCE||p.carryingCapacity<=0)continue;
    if(SpeciesEvolutionCatalog.get(p.speciesKey)==null||authoredSpeciesHere(s,area.id,p.speciesKey))continue;
    candidates.add(p);
   }
   candidates.sort((a,b)->{
    int d=Double.compare(score(b),score(a));
    return d!=0?d:a.speciesKey.compareTo(b.speciesKey);
   });
   int count=0;
   for(SpeciesPopulationState p:candidates){
    if(count>=MAX_DYNAMIC_PER_AREA)break;
    String id=objectId(area.id,p.speciesKey);desired.add(id);
    WorldObject o=s.world.object(id);
    if(o==null){
     o=createRepresentative(area,p.speciesKey);
     s.world.objects.add(o);added++;
     recordFirstVisibleEncounter(s,area,p,now);
    }
    bindLifeState(s,o,area,now);
    count++;
   }
  }
  for(Iterator<WorldObject> it=s.world.objects.iterator();it.hasNext();){
   WorldObject o=it.next();
   if(o!=null&&o.id!=null&&o.id.startsWith(DYNAMIC_PREFIX)&&!desired.contains(o.id))it.remove();
  }
  return added;
 }

 public static boolean isDynamic(WorldObject o){return o!=null&&o.id!=null&&o.id.startsWith(DYNAMIC_PREFIX);}

 private static double score(SpeciesPopulationState p){
  return p.relativeAbundance*.62+p.carryingCapacity*.28+(1-p.resourcePressure)*.05+(1-p.weatherPressure)*.05;
 }

 private static boolean authoredSpeciesHere(WorldState s,String areaId,String species){
  for(WorldObject o:s.world.objects){
   if(o==null||!o.enabled||isDynamic(o)||!"creature".equals(o.type))continue;
   if(!areaId.equals(HaruVisionEngine.actualAreaId(s,o)))continue;
   if(species.equals(FantasyEcologyDictionary.keyForObject(o)))return true;
  }
  return false;
 }

 private static WorldObject createRepresentative(WorldArea area,String species){
  SpeciesEvolutionCatalog.Species hidden=SpeciesEvolutionCatalog.get(species);
  SpeciesEcologyProfile eco=FantasyEcologyDictionary.get(species);
  int h=(area.id+"|"+species).hashCode()&0x7fffffff;
  float margin=70f,span=Math.max(30f,(area.right-area.left)-margin*2f);
  float x=area.left+margin+(h%1000)/999f*span;
  float w=30f+(h%17),hh=22f+((h/17)%15);
  String traitTags=hidden==null?"":",body_"+traitTag(hidden.bodyPlan)+",move_"+traitTag(hidden.locomotion)+",sense_"+traitTag(hidden.senseMode)+",social_"+traitTag(hidden.sociality);
  String tags="creature,dynamic_wildlife,"+species+traitTags+(eco==null?"":","+eco.preferredAreaTags);
  if(hidden!=null){if(hidden.bodyPlan.contains("wing")||hidden.bodyPlan.contains("glider")||hidden.bodyPlan.contains("sail"))w*=1.22f;if(hidden.bodyPlan.contains("stilt")||hidden.bodyPlan.contains("limb")||hidden.bodyPlan.contains("antler"))hh*=1.18f;if(hidden.bodyPlan.contains("shell")||hidden.bodyPlan.contains("disk")||hidden.bodyPlan.contains("ring"))w*=1.12f;}
  WorldObject o=new WorldObject(objectId(area.id,species),"creature",area.id,"","một sinh vật chưa quen thuộc",x,area.groundY,w,hh,tags);
  o.renderLayer="WORLD_PROPS";o.habitat=eco==null?"":eco.preferredAreaTags;o.dictionaryRef=hidden==null?"":hidden.key;
  o.interactable=false;o.collision=false;o.safeEditable="position,enabled";
  return o;
 }

 private static void bindLifeState(WorldState s,WorldObject o,WorldArea fallback,long now){
  CreatureLifeState c=s.livingWorld.creature(o.id);
  if(c.areaId==null||c.areaId.isEmpty()||s.world.area(c.areaId)==null){c.areaId=fallback.id;c.x=o.x;}
  if(!Float.isFinite(c.x))c.x=o.x;
  if(c.lastUpdatedAt<=0){SpeciesEvolutionCatalog.Species hidden=SpeciesEvolutionCatalog.get(o.dictionaryRef);c.activity=initialActivity(hidden);c.curiosity=Math.max(c.curiosity,traitCuriosity(hidden));c.avoidance=Math.max(c.avoidance,traitAvoidance(hidden));c.locomotionDrive=Math.max(c.locomotionDrive,traitMotion(hidden));c.lastUpdatedAt=Math.max(0,now);}
 }

 private static String initialActivity(SpeciesEvolutionCatalog.Species s){if(s==null)return"drift";if(s.lifeCycle.contains("dormant"))return"rest";if(s.locomotion.contains("burrow")||s.niche.contains("burrow"))return"forage";return"drift";}
 private static double traitCuriosity(SpeciesEvolutionCatalog.Species s){if(s==null)return.30;double v=.24;if(s.sociality.contains("call-and-response")||s.sociality.contains("trail-network")||s.sociality.contains("small-cluster"))v+=.16;if(s.senseMode.contains("echo")||s.senseMode.contains("vibration")||s.senseMode.contains("chemical"))v+=.08;return clamp(v);}
 private static double traitAvoidance(SpeciesEvolutionCatalog.Species s){if(s==null)return.10;double v=.08;if(s.defense.contains("rapid burrow")||s.defense.contains("dart")||s.defense.contains("stillness"))v+=.18;if(s.sociality.contains("solitary"))v+=.08;return clamp(v);}
 private static double traitMotion(SpeciesEvolutionCatalog.Species s){if(s==null)return.22;double v=.20;if(s.locomotion.contains("flight")||s.locomotion.contains("drift")||s.locomotion.contains("skate")||s.locomotion.contains("undulate"))v+=.20;if(s.locomotion.contains("hop")||s.locomotion.contains("scuttle"))v+=.12;if(s.lifeCycle.contains("dormant")||s.lifeCycle.contains("long-slow"))v-=.08;return clamp(v);}
 private static double clamp(double v){return Math.max(0,Math.min(1,v));}
 private static String traitTag(String s){return(s==null?"unknown":s.toLowerCase(java.util.Locale.ROOT)).replaceAll("[^a-z0-9]+","_");}

 private static void recordFirstVisibleEncounter(WorldState s,WorldArea area,SpeciesPopulationState p,long now){
  WorldArea haru=s.world.areaAt(s.haruX);
  if(haru==null||!area.id.equals(haru.id)||alreadyRecorded(s,area.id,p.speciesKey))return;
  String id="wildlife_manifest_"+clean(area.id)+"_"+clean(p.speciesKey);
  String summary="area="+area.id+" abundance="+String.format(java.util.Locale.US,"%.3f",p.relativeAbundance);
  s.worldHistory.add(new WorldHistoryEntry(Math.max(0,now),id,"ECOLOGY_VISIBLE_MANIFESTATION",p.speciesKey,summary));
  while(s.worldHistory.size()>240)s.worldHistory.remove(0);
 }

 private static boolean alreadyRecorded(WorldState s,String area,String species){
  if(s.worldHistory==null)return false;
  for(WorldHistoryEntry e:s.worldHistory)if(e!=null&&"ECOLOGY_VISIBLE_MANIFESTATION".equals(e.type)&&species.equals(e.entity)&&e.summary!=null&&e.summary.contains("area="+area))return true;
  return false;
 }

 static String objectId(String area,String species){return DYNAMIC_PREFIX+clean(area)+"_"+clean(species);}
 private static String clean(String s){return(s==null?"unknown":s).replaceAll("[^A-Za-z0-9._-]","_");}
}
