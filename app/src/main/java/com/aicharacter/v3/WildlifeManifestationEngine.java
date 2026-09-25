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
  String tags="creature,dynamic_wildlife,"+species+(eco==null?"":","+eco.preferredAreaTags);
  WorldObject o=new WorldObject(objectId(area.id,species),"creature",area.id,"","một sinh vật chưa quen thuộc",x,area.groundY,w,hh,tags);
  o.renderLayer="WORLD_PROPS";o.habitat=eco==null?"":eco.preferredAreaTags;o.dictionaryRef=hidden==null?"":hidden.key;
  o.interactable=false;o.collision=false;o.safeEditable="position,enabled";
  return o;
 }

 private static void bindLifeState(WorldState s,WorldObject o,WorldArea fallback,long now){
  CreatureLifeState c=s.livingWorld.creature(o.id);
  if(c.areaId==null||c.areaId.isEmpty()||s.world.area(c.areaId)==null){c.areaId=fallback.id;c.x=o.x;}
  if(!Float.isFinite(c.x))c.x=o.x;
  if(c.lastUpdatedAt<=0){c.activity="drift";c.curiosity=Math.max(c.curiosity,.30);c.lastUpdatedAt=Math.max(0,now);}
 }

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
