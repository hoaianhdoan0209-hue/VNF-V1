package com.aicharacter.v3;
import org.json.*;import java.util.*;
/** Authored ecological identity. A biome describes climate/terrain/vegetation/fauna, never Haru beliefs. */
public final class BiomeProfile{
 public String id="",label="",climate="",terrain="",waterRegime="",vegetation="",fauna="",tags="";public double baseMoisture=.5,canopy=.35;public int elevationMinM=0,elevationMaxM=500;
 public boolean hasTag(String t){return token(tags,t);}
 public boolean supports(String habitat){if(habitat==null||habitat.trim().isEmpty())return true;for(String x:habitat.split(","))if(token(tags,x)||token(vegetation,x)||token(fauna,x)||waterRegime.equalsIgnoreCase(x.trim()))return true;return false;}
 public static BiomeProfile fromJson(JSONObject j){BiomeProfile b=new BiomeProfile();if(j==null)return b;b.id=j.optString("id");b.label=j.optString("label",b.id);b.climate=j.optString("climate");b.terrain=j.optString("terrain");b.waterRegime=j.optString("waterRegime");b.vegetation=j.optString("vegetation");b.fauna=j.optString("fauna");b.tags=j.optString("tags");b.baseMoisture=cl(j.optDouble("baseMoisture",.5));b.canopy=cl(j.optDouble("canopy",.35));b.elevationMinM=j.optInt("elevationMinM",0);b.elevationMaxM=Math.max(b.elevationMinM,j.optInt("elevationMaxM",500));return b;}
 private static boolean token(String csv,String x){if(csv==null||x==null)return false;String q=x.trim().toLowerCase(Locale.ROOT);for(String s:csv.split(","))if(s.trim().toLowerCase(Locale.ROOT).equals(q))return true;return false;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}