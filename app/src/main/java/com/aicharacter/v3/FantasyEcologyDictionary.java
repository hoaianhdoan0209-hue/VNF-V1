package com.aicharacter.v3;
import java.util.*;
/** Original VNF ecological relations. These are fictional and intentionally do not model Earth food chains. */
public final class FantasyEcologyDictionary{
 private static final Map<String,SpeciesEcologyProfile> P=new LinkedHashMap<>();
 static{
  add(new SpeciesEcologyProfile("reedling","lumenmere,wet_margin,lam_thread","driftwing","root_husher","lam_thread,blue_reed",.82,.48,.62,.12));
  add(new SpeciesEcologyProfile("driftwing","verge,mist,rootmat","reedling","root_husher","mistleaf,silverfold,vegetation",.58,.72,.74,.22));
  add(new SpeciesEcologyProfile("root_husher","veilroot,shade,echo_frond","","driftwing","veilroot,ember_moss,vegetation",.72,.36,.42,.28));
 }
 private FantasyEcologyDictionary(){}
 private static void add(SpeciesEcologyProfile p){P.put(p.key,p);}
 public static SpeciesEcologyProfile forObject(WorldObject o){if(o==null)return null;for(String k:P.keySet())if(hasTag(o.tags,k)||o.id.startsWith(k))return P.get(k);return null;}
 public static SpeciesEcologyProfile get(String k){return P.get(k);}
 public static boolean anyTag(String csv,String tags){if(csv==null||tags==null)return false;for(String q:csv.split(","))if(hasTag(tags,q))return true;return false;}
 public static boolean hasTag(String csv,String tag){if(csv==null||tag==null)return false;String q=tag.trim().toLowerCase(Locale.ROOT);for(String s:csv.split(","))if(s.trim().toLowerCase(Locale.ROOT).equals(q))return true;return false;}
}