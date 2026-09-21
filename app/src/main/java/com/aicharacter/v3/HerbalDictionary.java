package com.aicharacter.v3;
import java.util.*;
/** World-dictionary entries for fictional medicinal flora. Species are generated from VNF biome semantics, not real-world herbal medicine. */
public final class HerbalDictionary{
 private static final Map<String,HerbDefinition> ALL=new LinkedHashMap<>();
 static{
  add(new HerbDefinition("mistleaf","lá sương bạc","humid_meadow_edge","lá nhỏ phủ ánh bạc, mọc sát cỏ ẩm","RINSE_CRUSH",.12,.28,.05,.08,.10));
  add(new HerbDefinition("blue_reed","sậy lam","freshwater_lakeshore","thân sậy mảnh có viền xanh lam nhạt","WARM_STEEP",.30,.06,.12,.16,.18));
  add(new HerbDefinition("ember_moss","rêu hồng ấm","humid_broadleaf_grove","mảng rêu sẫm có chấm hồng, thường ở gốc cây râm","DRY_CRUSH",.10,.16,.26,.14,.15));
 }
 private HerbalDictionary(){}
 private static void add(HerbDefinition d){ALL.put(d.id,d);}
 public static HerbDefinition get(String id){return ALL.get(id);}
 public static Collection<HerbDefinition> all(){return Collections.unmodifiableCollection(ALL.values());}
 public static List<HerbDefinition> forBiome(String biomeId){List<HerbDefinition>x=new ArrayList<>();for(HerbDefinition d:ALL.values())if(d.biomeId.equals(biomeId))x.add(d);return x;}
}