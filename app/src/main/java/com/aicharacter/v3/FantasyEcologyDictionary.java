package com.aicharacter.v3;
import java.util.*;
/**
 * Original VNF ecological relations.
 * The System-Reality catalog contains exactly 500 base species; only sparse populations are materialized at runtime.
 * Haru never receives the hidden evolutionary catalog directly.
 */
public final class FantasyEcologyDictionary{
 private static final Map<String,SpeciesEcologyProfile> P=new LinkedHashMap<>();
 static{
  // Preserve the five already-authored species exactly.
  add(new SpeciesEcologyProfile("reedling","lumenmere,wet_margin,lam_thread","lumenmere,lam_thread,blue_reed","driftwing","root_husher","lam_thread,blue_reed",.82,.48,.62,.12));
  add(new SpeciesEcologyProfile("driftwing","verge,mist,rootmat","mist,glow_seed,mistleaf,silverfold","reedling","root_husher","mistleaf,silverfold,vegetation",.58,.72,.74,.22));
  add(new SpeciesEcologyProfile("root_husher","veilroot,shade,echo_frond","echo_frond,veilroot,ember_moss","","driftwing","veilroot,ember_moss,vegetation",.72,.36,.42,.28));
  add(new SpeciesEcologyProfile("ripplekin","lumenmere,wet_margin,water","shimmer_mat,lam_thread,blue_reed","reedling","","lam_thread,shimmer_mat,vegetation",.76,.44,.68,.18));
  add(new SpeciesEcologyProfile("hearthmote","interior,dry,quiet,warm","hearth,bloom,quiet","","","hearth,bloom,vegetation",.38,.10,.82,.10));

  // Fill the remainder from the hidden evolutionary catalog. These are base species,
  // not mutation/hybrid variants. Runtime population materialization remains sparse.
  for(SpeciesEvolutionCatalog.Species s:SpeciesEvolutionCatalog.all())if(!P.containsKey(s.key))add(SpeciesEvolutionCatalog.ecologyProfile(s));
  if(P.size()!=SpeciesEvolutionCatalog.BASE_SPECIES_COUNT)throw new IllegalStateException("Fantasy ecology must expose exactly 500 base species.");
 }
 private FantasyEcologyDictionary(){}
 private static void add(SpeciesEcologyProfile p){if(p!=null)P.put(p.key,p);}
 public static SpeciesEcologyProfile forObject(WorldObject o){String key=keyForObject(o);return key.isEmpty()?null:P.get(key);}
 public static String keyForObject(WorldObject o){
  if(o==null)return"";
  if(o.tags!=null)for(String raw:o.tags.split(",")){String k=raw.trim().toLowerCase(Locale.ROOT);if(P.containsKey(k))return k;}
  String id=o.id==null?"":o.id.toLowerCase(Locale.ROOT);for(String k:P.keySet())if(id.startsWith(k))return k;
  return"";
 }
 public static SpeciesEcologyProfile get(String k){return k==null?null:P.get(k);}
 public static Collection<SpeciesEcologyProfile> all(){return Collections.unmodifiableCollection(P.values());}
 public static int baseSpeciesCount(){return P.size();}
 public static boolean anyTag(String csv,String tags){if(csv==null||tags==null)return false;for(String q:csv.split(","))if(hasTag(tags,q))return true;return false;}
 public static boolean hasTag(String csv,String tag){if(csv==null||tag==null)return false;String q=tag.trim().toLowerCase(Locale.ROOT);for(String s:csv.split(","))if(s.trim().toLowerCase(Locale.ROOT).equals(q))return true;return false;}
}
