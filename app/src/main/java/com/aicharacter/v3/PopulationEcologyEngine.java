package com.aicharacter.v3;
import java.util.*;

/**
 * Population-level ecology. It simulates bounded relative abundance rather than spawning thousands of objects.
 * Authored/visible creatures remain representative individuals of these fields.
 */
public final class PopulationEcologyEngine {
 private PopulationEcologyEngine(){}

 public static void advance(WorldState s,double minutes,long now){
  if(s==null||s.world==null||minutes<=0)return;if(s.livingWorld==null)s.livingWorld=new LivingWorldState();
  Collection<SpeciesEcologyProfile> profiles=FantasyEcologyDictionary.all();if(profiles.isEmpty()||s.world.areas.isEmpty())return;
  for(SpeciesEcologyProfile p:profiles)for(WorldArea a:s.world.areas){SpeciesPopulationState x=s.livingWorld.population(p.key,a.id);if(x.lastUpdatedAt<=0){WorldObject rep=representative(s,p.key);double suitability=rep==null?.25:EcologyEngine.creatureSuitability(s,a,rep);int visible=visibleRepresentatives(s,p.key,a.id);x.relativeAbundance=cl(.05+suitability*.24+Math.min(.18,visible*.06));x.carryingCapacity=cl(.10+suitability*.56);x.speciesKey=p.key;x.areaId=a.id;}}
  Map<String,Double> deltas=new LinkedHashMap<>();
  for(SpeciesEcologyProfile p:profiles)for(WorldArea a:s.world.areas){
   SpeciesPopulationState x=s.livingWorld.population(p.key,a.id);WorldObject rep=representative(s,p.key);double suitability=rep==null?.25:EcologyEngine.creatureSuitability(s,a,rep);
   double resource=rep==null?LivingWorldEngine.field(s,a.id).resourcePulse:FantasyEcologyEngine.resourceAvailability(s,rep,a);
   double competition=competition(s,p,a),weather=weatherPressure(s,p,a),season=x.seasonalInfluence;
   double capacityTarget=cl(.08+suitability*.52+resource*.30-competition*.18-weather*.14-Math.max(0,season)*.08);
   x.carryingCapacity=follow(x.carryingCapacity,capacityTarget,minutes,720);
   x.competitionPressure=follow(x.competitionPressure,competition,minutes,180);
   x.resourcePressure=follow(x.resourcePressure,cl(1-resource),minutes,120);
   x.weatherPressure=follow(x.weatherPressure,weather,minutes,90);
   double gap=x.carryingCapacity-x.relativeAbundance;
   double birth=cl(Math.max(0,gap)*1.35*resource*(1-x.competitionPressure*.55)*(1-x.weatherPressure*.45));
   double recovery=cl((1-x.resourcePressure)*.46+(1-x.weatherPressure)*.28+(1-x.competitionPressure)*.16);
   double mortality=cl(Math.max(0,-gap)*1.10+x.resourcePressure*.36+x.weatherPressure*.28+x.competitionPressure*.16);
   double migration=cl(Math.max(0,-gap)*1.35+x.resourcePressure*.42+x.weatherPressure*.30+x.competitionPressure*.24);
   x.birthPressure=follow(x.birthPressure,birth,minutes,360);x.recoveryPressure=follow(x.recoveryPressure,recovery,minutes,240);x.mortalityPressure=follow(x.mortalityPressure,mortality,minutes,240);x.migrationPressure=follow(x.migrationPressure,migration,minutes,120);
   double days=minutes/1440.0;double localDelta=x.relativeAbundance*(x.birthPressure*.16+x.recoveryPressure*.025-x.mortalityPressure*.20)*days;
   deltas.put(key(p.key,a.id),deltas.getOrDefault(key(p.key,a.id),0.0)+localDelta);
   WorldArea target=bestMigrationTarget(s,p,a,x);if(target!=null&&x.migrationPressure>.08){
    SpeciesPopulationState dst=s.livingWorld.population(p.key,target.id);double opportunity=Math.max(0,dst.carryingCapacity-dst.relativeAbundance);double transfer=Math.min(x.relativeAbundance*.22*x.migrationPressure*days,opportunity*.30*days+.02*days);
    if(transfer>0){deltas.put(key(p.key,a.id),deltas.getOrDefault(key(p.key,a.id),0.0)-transfer);deltas.put(key(p.key,target.id),deltas.getOrDefault(key(p.key,target.id),0.0)+transfer);}
   }
   x.lastUpdatedAt=now;
  }
  for(Map.Entry<String,Double> e:deltas.entrySet()){SpeciesPopulationState p=s.livingWorld.populations.get(e.getKey());if(p!=null){p.relativeAbundance=cl(p.relativeAbundance+e.getValue());p.clamp();p.lastUpdatedAt=now;}}
 }

 public static SpeciesPopulationState state(WorldState s,String speciesKey,String areaId){return s==null||s.livingWorld==null?null:s.livingWorld.populations.get(key(speciesKey,areaId));}
 public static SpeciesPopulationState state(WorldState s,WorldObject creature,WorldArea area){String species=FantasyEcologyDictionary.keyForObject(creature);return species.isEmpty()||area==null?null:state(s,species,area.id);}
 public static double overcrowding(WorldState s,WorldObject creature,WorldArea area){SpeciesPopulationState p=state(s,creature,area);if(p==null)return 0;return cl(Math.max(0,p.relativeAbundance-p.carryingCapacity)*1.5+p.competitionPressure*.34+p.resourcePressure*.24);}

 private static WorldArea bestMigrationTarget(WorldState s,SpeciesEcologyProfile p,WorldArea from,SpeciesPopulationState source){WorldArea best=null;double bestGain=.08;for(String id:from.connections){WorldArea a=s.world.area(id);if(a==null)continue;SpeciesPopulationState t=s.livingWorld.population(p.key,a.id);double gain=(t.carryingCapacity-t.relativeAbundance)-(source.carryingCapacity-source.relativeAbundance)+(.5-t.resourcePressure)*.16-(t.weatherPressure-source.weatherPressure)*.18;if(gain>bestGain){bestGain=gain;best=a;}}return best;}
 private static double competition(WorldState s,SpeciesEcologyProfile p,WorldArea a){double sum=0;for(SpeciesEcologyProfile q:FantasyEcologyDictionary.all()){if(q.key.equals(p.key))continue;double overlap=resourceOverlap(p.resourceTags,q.resourceTags);if(overlap<=0)continue;SpeciesPopulationState other=s.livingWorld.population(q.key,a.id);sum+=other.relativeAbundance*overlap*(.55+.45*q.resourceAffinity);}return cl(sum*.55);}
 private static double weatherPressure(WorldState s,SpeciesEcologyProfile p,WorldArea a){if(s.environment==null)return 0;double pressure=0;if("RAIN".equals(s.environment.weather)){double tolerant=hasAny(p.preferredAreaTags,"wet_margin,water,mist,rain_tolerant")?.35:1;pressure+=s.environment.weatherIntensity*WorldSemantics.exposure(a)*.28*tolerant;}double temp=EcologyEngine.localTemperatureC(s,a);pressure+=Math.max(0,Math.abs(temp-24)-12)/26.0;return cl(pressure);}
 private static int visibleRepresentatives(WorldState s,String species,String areaId){int n=0;for(WorldObject o:s.world.objects)if(o.enabled&&"creature".equals(o.type)&&areaId.equals(o.areaId)&&species.equals(FantasyEcologyDictionary.keyForObject(o)))n++;return n;}
 private static WorldObject representative(WorldState s,String species){for(WorldObject o:s.world.objects)if(o.enabled&&"creature".equals(o.type)&&species.equals(FantasyEcologyDictionary.keyForObject(o)))return o;return null;}
 private static double resourceOverlap(String a,String b){if(a==null||b==null)return 0;Set<String>x=tokens(a),y=tokens(b);if(x.isEmpty()||y.isEmpty())return 0;int hit=0;for(String q:x)if(y.contains(q))hit++;return (double)hit/Math.max(1,Math.min(x.size(),y.size()));}
 private static Set<String> tokens(String csv){Set<String>s=new LinkedHashSet<>();if(csv!=null)for(String q:csv.split(",")){q=q.trim().toLowerCase(Locale.ROOT);if(!q.isEmpty())s.add(q);}return s;}
 private static boolean hasAny(String csv,String wanted){if(csv==null||wanted==null)return false;for(String q:wanted.split(","))if(FantasyEcologyDictionary.hasTag(csv,q))return true;return false;}
 private static String key(String species,String area){return (species==null?"":species)+"@"+(area==null?"":area);}
 private static double follow(double v,double target,double minutes,double tau){double k=1-Math.exp(-minutes/Math.max(.05,tau));return cl(v+(target-v)*k);}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
