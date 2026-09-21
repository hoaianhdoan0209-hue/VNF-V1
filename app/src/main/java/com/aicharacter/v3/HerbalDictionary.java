package com.aicharacter.v3;
import java.util.*;
/** World-dictionary entries for fictional medicinal flora. Species are generated from VNF biome semantics, not real-world herbal medicine. */
public final class HerbalDictionary{
 private static final Map<String,HerbDefinition> ALL=new LinkedHashMap<>();
 static{
  add(new HerbDefinition("mistleaf","phiến sương","silverfold_verge","những phiến mềm ánh bạc khép mở theo rung động của nền đất","DEW_FOLD",.12,.28,.05,.08,.10));
  add(new HerbDefinition("blue_reed","lam ty","lumenmere_margin","những sợi thân lam rỗng phát nhịp mờ khi không khí ẩm lên cao","LUMEN_REST",.30,.06,.12,.16,.18));
  add(new HerbDefinition("ember_moss","hồng mạch","veilroot_grove","mạng mô thấp màu tối có những điểm hồng co giãn như mạch sáng dưới bóng râm","ECHO_DRY",.10,.16,.26,.14,.15));
 }
 private HerbalDictionary(){}
 private static void add(HerbDefinition d){ALL.put(d.id,d);}
 public static HerbDefinition get(String id){return ALL.get(id);}
 public static Collection<HerbDefinition> all(){return Collections.unmodifiableCollection(ALL.values());}
 public static List<HerbDefinition> forBiome(String biomeId){List<HerbDefinition>x=new ArrayList<>();for(HerbDefinition d:ALL.values())if(d.biomeId.equals(biomeId))x.add(d);return x;}
}