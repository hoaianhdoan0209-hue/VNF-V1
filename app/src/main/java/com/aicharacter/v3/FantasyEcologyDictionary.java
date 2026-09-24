package com.aicharacter.v3;
import java.util.*;
/**
 * Original VNF ecological relations for every present-era BASE species.
 * Biology identity comes from SpeciesRegistryV2; this dictionary exposes the ecology projection only.
 */
public final class FantasyEcologyDictionary{
 private static final Map<String,SpeciesEcologyProfile> P=new LinkedHashMap<>();
 static{
  for(SpeciesDefinition d:SpeciesRegistryV2.all())add(d.ecologyProfile());
  if(P.size()!=SpeciesRegistryV2.BASE_SPECIES_COUNT)throw new IllegalStateException("Ecology projection must cover every base species.");
 }
 private FantasyEcologyDictionary(){}
 private static void add(SpeciesEcologyProfile p){P.put(p.key,p);}
 public static SpeciesEcologyProfile forObject(WorldObject o){String key=keyForObject(o);return key.isEmpty()?null:P.get(key);}
 public static String keyForObject(WorldObject o){
  if(o==null)return"";
  String tags=o.tags==null?"":o.tags;
  for(String token:tags.split(",")){String k=token.trim().toLowerCase(Locale.ROOT);if(P.containsKey(k))return k;}
  String id=o.id==null?"":o.id.toLowerCase(Locale.ROOT);
  for(String k:P.keySet())if(id.startsWith(k))return k;
  return"";
 }
 public static SpeciesEcologyProfile get(String k){return P.get(k);}
 public static Collection<SpeciesEcologyProfile> all(){return Collections.unmodifiableCollection(P.values());}
 public static boolean anyTag(String csv,String tags){if(csv==null||tags==null)return false;for(String q:csv.split(","))if(hasTag(tags,q))return true;return false;}
 public static boolean hasTag(String csv,String tag){if(csv==null||tag==null)return false;String q=tag.trim().toLowerCase(Locale.ROOT);for(String s:csv.split(","))if(s.trim().toLowerCase(Locale.ROOT).equals(q))return true;return false;}
}
