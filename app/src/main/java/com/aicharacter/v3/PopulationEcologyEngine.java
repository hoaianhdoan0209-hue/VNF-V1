package com.aicharacter.v3;
import java.util.*;

/**
 * Population-level ecology. It simulates bounded relative abundance rather than spawning thousands of objects.
 * Authored/visible creatures remain representative individuals of these fields.
 */
public final class PopulationEcologyEngine {
 private static final double SEED_SUITABILITY=.52;
 private static final double VIABLE_SUITABILITY=.36;
 private PopulationEcologyEngine(){}

 public static void advance(WorldState s,double minutes,long now){
  if(s==null||s.world==null||!Double.isFinite(minutes)||minutes<=0)return;if(s.livingWorld==null)s.livingWorld=new LivingWorldState();
  Collection<SpeciesEcologyProfile> profiles=FantasyEcologyDictionary.all();if(profiles.isEmpty()||s.world.areas.isEmpty())return;
  for(SpeciesEcologyProfile p:profiles)for(WorldArea a:s.world.areas){
   SpeciesPopulationState x=s.livingWorld.population(p.key,a.id);x.clamp();
   if(x.lastUpdatedAt<=0){
    WorldObject rep=representative(s,p.key);double suitability=unit(rep==null?0:EcologyEngine.creatureSuitability(s,a,rep),0);int visible=visibleRepresentatives(s,p.key,a.id);boolean authoredHere=visible>0,seed=authoredHere||suitability>=SEED_SUITABILITY;
    x.relativeAbundance=seed?unit((authoredHere?.08:.02)+Math.max(0,suitability-.45)*.38+Math.min(.18,visible*.06),0):0;
    x.carryingCapacity=suitability>=VIABLE_SUITABILITY?unit(.06+suitability*.58,0):0;
    if(x.relativeAbundance>0){x.birthPressure=.12;x.recoveryPressure=.22;x.resourcePressure=.12;x.weatherPressure=.04;x.migrationPressure=.04;}
    x.speciesKey=p.key;x.areaId=a.id;x.lastUpdatedAt=now;x.clamp();
   }
  }
  Map<String,Double> deltas=new LinkedHashMap<>();
  for(SpeciesEcologyProfile p:profiles)for(WorldArea a:s.world.areas){
   SpeciesPopulationState x=s.livingWorld.population(p.key,a.id);x.clamp();WorldObject rep=representative(s,p.key);double suitability=unit(rep==null?0:EcologyEngine.creatureSuitability(s,a,rep),0);
   double resource=unit(rep==null?LivingWorldEngine.field(s,a.id).resourcePulse:FantasyEcologyEngine.resourceAvailability(s,rep,a),0);
   double competition=competition(s,p,a),weather=weatherPressure(s,p,a),season=signed(x.seasonalInfluence);
   double capacityTarget=suitability>=VIABLE_SUITABILITY?unit(.08+suitability*.52+resource*.30-competition*.18-weather*.14-Math.max(0,season)*.08,0):0;
   x.carryingCapacity=follow(x.carryingCapacity,capacityTarget,minutes,720);
   x.competitionPressure=follow(x.competitionPressure,competition,minutes,180);
   x.resourcePressure=follow(x.resourcePressure,unit(1-resource,1),minutes,120);
   x.weatherPressure=follow(x.weatherPressure,weather,minutes,90);
   double gap=finite(x.carryingCapacity-x.relativeAbundance,0);
   double birth=unit(Math.max(0,gap)*1.35*resource*(1-x.competitionPressure*.55)*(1-x.weatherPressure*.45),0);
   double recovery=unit((1-x.resourcePressure)*.46+(1-x.weatherPressure)*.28+(1-x.competitionPressure)*.16,0);
   double mortality=unit(Math.max(0,-gap)*1.10+x.resourcePressure*.36+x.weatherPressure*.28+x.competitionPressure*.16,0);
   double migration=unit(Math.max(0,-gap)*1.35+x.resourcePressure*.42+x.weatherPressure*.30+x.competitionPressure*.24,0);
   x.birthPressure=follow(x.birthPressure,birth,minutes,360);x.recoveryPressure=follow(x.recoveryPressure,recovery,minutes,240);x.mortalityPressure=follow(x.mortalityPressure,mortality,minutes,240);x.migrationPressure=follow(x.migrationPressure,migration,minutes,120);
   double days=finite(minutes/1440.0,0);double localDelta=finite(x.relativeAbundance*(x.birthPressure*.16+x.recoveryPressure*.025-x.mortalityPressure*.20)*days,0);
   addDelta(deltas,key(p.key,a.id),localDelta);
   WorldArea target=bestMigrationTarget(s,p,a,x);if(target!=null&&x.relativeAbundance>0&&x.migrationPressure>.08){
    SpeciesPopulationState dst=s.livingWorld.population(p.key,target.id);dst.clamp();double opportunity=Math.max(0,finite(dst.carryingCapacity-dst.relativeAbundance,0));double transfer=Math.min(x.relativeAbundance*.22*x.migrationPressure*days,opportunity*.30*days+.02*days);transfer=Math.max(0,finite(transfer,0));
    if(transfer>0){addDelta(deltas,key(p.key,a.id),-transfer);addDelta(deltas,key(p.key,target.id),transfer);}
   }
   x.lastUpdatedAt=now;x.clamp();
  }
  for(Map.Entry<String,Double> e:deltas.entrySet()){SpeciesPopulationState p=s.livingWorld.populations.get(e.getKey());if(p!=null){p.clamp();p.relativeAbundance=unit(p.relativeAbundance+finite(e.getValue(),0),p.relativeAbundance);p.lastUpdatedAt=now;p.clamp();}}
  for(SpeciesPopulationState p:s.livingWorld.populations.values())p.clamp();
 }

 public static SpeciesPopulationState state(WorldState s,String speciesKey,String areaId){if(s==null||s.livingWorld==null)return null;SpeciesPopulationState p=s.livingWorld.populations.get(key(speciesKey,areaId));if(p!=null)p.clamp();return p;}
 public static SpeciesPopulationState state(WorldState s,WorldObject creature,WorldArea area){String species=FantasyEcologyDictionary.keyForObject(creature);return species.isEmpty()||area==null?null:state(s,species,area.id);}
 public static double overcrowding(WorldState s,WorldObject creature,WorldArea area){SpeciesPopulationState p=state(s,creature,area);if(p==null)return 0;return unit(Math.max(0,p.relativeAbundance-p.carryingCapacity)*1.5+p.competitionPressure*.34+p.resourcePressure*.24,0);}

 private static WorldArea bestMigrationTarget(WorldState s,SpeciesEcologyProfile p,WorldArea from,SpeciesPopulationState source){WorldArea best=null;double bestGain=.08;source.clamp();for(String id:from.connections){WorldArea a=s.world.area(id);if(a==null)continue;SpeciesPopulationState t=s.livingWorld.population(p.key,a.id);t.clamp();if(t.carryingCapacity<=0)continue;double gain=finite((t.carryingCapacity-t.relativeAbundance)-(source.carryingCapacity-source.relativeAbundance)+(.5-t.resourcePressure)*.16-(t.weatherPressure-source.weatherPressure)*.18,0);if(gain>bestGain){bestGain=gain;best=a;}}return best;}
 private static double competition(WorldState s,SpeciesEcologyProfile p,WorldArea a){double sum=0;for(SpeciesEcologyProfile q:FantasyEcologyDictionary.all()){if(q.key.equals(p.key))continue;double overlap=resourceOverlap(p.resourceTags,q.resourceTags);if(overlap<=0)continue;SpeciesPopulationState other=s.livingWorld.population(q.key,a.id);other.clamp();sum=finite(sum+other.relativeAbundance*overlap*(.55+.45*unit(q.resourceAffinity,0)),sum);}return unit(sum*.55,0);}
 private static double weatherPressure(WorldState s,SpeciesEcologyProfile p,WorldArea a){if(s.environment==null)return 0;double pressure=0;if("RAIN".equals(s.environment.weather)){double tolerant=hasAny(p.preferredAreaTags,"wet_margin,water,mist,rain_tolerant")?.35:1;pressure+=unit(s.environment.weatherIntensity,0)*unit(WorldSemantics.exposure(a),0)*.28*tolerant;}double temp=finite(EcologyEngine.localTemperatureC(s,a),24);pressure+=Math.max(0,Math.abs(temp-24)-12)/26.0;return unit(pressure,0);}
 private static int visibleRepresentatives(WorldState s,String species,String areaId){int n=0;if(s==null||s.world==null)return 0;for(WorldObject o:s.world.objects)if(o!=null&&o.enabled&&"creature".equals(o.type)&&areaId!=null&&areaId.equals(o.areaId)&&species.equals(FantasyEcologyDictionary.keyForObject(o)))n++;return n;}
 private static WorldObject representative(WorldState s,String species){if(s==null||s.world==null)return null;for(WorldObject o:s.world.objects)if(o!=null&&o.enabled&&"creature".equals(o.type)&&species.equals(FantasyEcologyDictionary.keyForObject(o)))return o;return null;}
 private static double resourceOverlap(String a,String b){if(a==null||b==null)return 0;Set<String>x=tokens(a),y=tokens(b);if(x.isEmpty()||y.isEmpty())return 0;int hit=0;for(String q:x)if(y.contains(q))hit++;return unit((double)hit/Math.max(1,Math.min(x.size(),y.size())),0);}
 private static Set<String> tokens(String csv){Set<String>s=new LinkedHashSet<>();if(csv!=null)for(String q:csv.split(",")){q=q.trim().toLowerCase(Locale.ROOT);if(!q.isEmpty())s.add(q);}return s;}
 private static boolean hasAny(String csv,String wanted){if(csv==null||wanted==null)return false;for(String q:wanted.split(","))if(FantasyEcologyDictionary.hasTag(csv,q))return true;return false;}
 private static void addDelta(Map<String,Double> d,String k,double v){v=finite(v,0);d.put(k,finite(d.getOrDefault(k,0.0)+v,0));}
 private static String key(String species,String area){return LivingWorldState.populationKey(species,area);}
 private static double follow(double v,double target,double minutes,double tau){v=unit(v,0);target=unit(target,0);minutes=Math.max(0,finite(minutes,0));tau=Math.max(.05,finite(tau,.05));double k=unit(1-Math.exp(-minutes/tau),0);return unit(v+(target-v)*k,v);}
 private static double finite(double v,double fallback){return Double.isFinite(v)?v:fallback;}
 private static double unit(double v,double fallback){v=finite(v,fallback);return Math.max(0,Math.min(1,v));}
 private static double signed(double v){if(!Double.isFinite(v))return 0;return Math.max(-1,Math.min(1,v));}
}
