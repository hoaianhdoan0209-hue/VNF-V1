package com.aicharacter.v3;

import java.util.*;

/**
 * Authored current-life registry. Exactly 500 BASE species live in the present era.
 * Hybrids, mutations, regional variants and future speciation must use a separate lineage layer.
 */
public final class SpeciesRegistryV2 {
 public static final int BASE_SPECIES_COUNT=500;
 private static final LinkedHashMap<String,SpeciesDefinition> ALL=new LinkedHashMap<>();

 private static final String[] LIFE={"motile_fauna","rooted_flora","drifting_flora","fungal_mesh","aquatic_fauna","aerial_fauna","burrow_fauna","colonial_life","symbiotic_life","softbody_fauna"};
 private static final String[] BODY={"reed_frame","ribbon_shell","radial_petal","segmented_arch","hollow_spindle","root_crown","membrane_fan","tripod_core","spiral_carapace","floating_bladder","woven_plate","hinged_frond","soft_lattice","six_limb_arc","fin_disc","branch_body","ring_crawler","glass_sac","folded_mantle","rooted_column","paired_sail","ciliate_mat","jointed_orb","tapered_skiff","cluster_body"};
 private static final String[] LOC={"rooted","slow_creep","pulse_hop","four_point_walk","six_point_walk","glide","hover","burrow","swim_undulate","swim_jet","climb","drift","roll","spring","surface_skate"};
 private static final String[] META={"lumen_capture","mineral_filter","mist_exchange","detritus_cycle","thermal_ferment","root_sap","micrograze","predatory_absorb","symbiotic_exchange","water_filter","airborne_spore_feed","stored_reserve"};
 private static final String[] SENSE={"vibration_web","polarized_light","humidity_gradient","thermal_pits","chemical_thread","pressure_wave","electric_field","magnetic_tilt","lumen_flicker","surface_ripple","echo_resonance","root_tension","airflow_fan","nearfield_motion","mineral_trace","multi_spectrum_glow"};
 private static final String[] NICHE={"mist_grazer","lumen_browser","detritus_recycler","root_sap_feeder","micro_predator","filter_feeder","spore_carrier","seed_disperser","mineral_licker","algae_scraper","canopy_browser","bank_forager","deepwater_hunter","surface_skimmer","symbiotic_cleaner","fungal_partner","nectar_probe","thermal_scavenger","sediment_sifter","ambush_feeder"};
 private static final String[] REPRO={"paired_bud","spore_cloud","root_split","egg_cluster","live_brood","seasonal_cyst","fragment_clone","water_larvae","airborne_seed","brood_pouch","colony_fission","resonant_spawn"};
 private static final String[] SOCIAL={"solitary","loose_pair","small_cluster","seasonal_school","territorial_pair","mat_colony","cooperative_band","signal_network","mother_brood","roving_swarm","resource_aggregation","stable_group"};
 private static final String[] COG={"reflexive","associative","spatial_learner","social_learner","tool_potential","symbol_potential","long_memory","culture_potential"};
 private static final String[] COMM={"touch_pulse","lumen_flash","air_click","vibration_phrase","chemical_mark","water_wave","posture_signal","thermal_pulse","root_signal","wing_hum","surface_tap","color_shift"};
 private static final String[] HAB={
  "lumenmere,wet_margin,water","verge,mist,rootmat","veilroot,shade,vegetation","interior,dry,quiet,warm",
  "skyreef,high_wind,stone","deepmere,dark_water,pressure","amberfen,warm_wet,reeds","glassplain,dry,open,mineral",
  "cloudwood,canopy,mist","underroot,burrow,dark","saltfold,brine,flat","emberfield,warm,ash",
  "frostveil,cold,mist","tidecave,water,cave","sunsteppe,dry,grass","mossvault,humid,cave",
  "stormridge,wind,rock","moonmarsh,wet,night","crystalwood,mineral,forest","redbasin,warm,sediment",
  "floating_isles,air,canopy","deep_root,soil,dark","shallow_reef,water,lumen","high_meadow,cool,open"
 };

 static{
  add(new SpeciesDefinition("reedling","Reedling","lineage_reedling","motile_fauna","reed_frame","four_point_walk","micrograze","vibration_web","bank_forager","egg_cluster","small_cluster","social_learner","vibration_phrase","lumenmere,wet_margin,lam_thread","lumenmere,lam_thread,blue_reed","driftwing","root_husher","lam_thread,blue_reed",.82,.48,.62,.12,true));
  add(new SpeciesDefinition("driftwing","Driftwing","lineage_driftwing","aerial_fauna","paired_sail","glide","mist_exchange","airflow_fan","spore_carrier","airborne_seed","roving_swarm","spatial_learner","wing_hum","verge,mist,rootmat","mist,glow_seed,mistleaf,silverfold","reedling","root_husher","mistleaf,silverfold,vegetation",.58,.72,.74,.22,true));
  add(new SpeciesDefinition("root_husher","Root Husher","lineage_root_husher","burrow_fauna","ring_crawler","burrow","root_sap","root_tension","root_sap_feeder","seasonal_cyst","territorial_pair","long_memory","root_signal","veilroot,shade,echo_frond","echo_frond,veilroot,ember_moss","","driftwing","veilroot,ember_moss,vegetation",.72,.36,.42,.28,true));
  add(new SpeciesDefinition("ripplekin","Ripplekin","lineage_ripplekin","aquatic_fauna","fin_disc","swim_undulate","water_filter","surface_ripple","filter_feeder","water_larvae","seasonal_school","associative","water_wave","lumenmere,wet_margin,water","shimmer_mat,lam_thread,blue_reed","reedling","","lam_thread,shimmer_mat,vegetation",.76,.44,.68,.18,true));
  add(new SpeciesDefinition("hearthmote","Hearthmote","lineage_hearthmote","colonial_life","floating_bladder","hover","thermal_ferment","thermal_pits","detritus_recycler","fragment_clone","mat_colony","associative","thermal_pulse","interior,dry,quiet,warm","hearth,bloom,quiet","","","hearth,bloom,vegetation",.38,.10,.82,.10,true));

  for(int i=5;i<BASE_SPECIES_COUNT;i++){
   int n=i+1;
   String key=String.format(Locale.US,"vns_%03d",n);
   String life=LIFE[(i*7+3)%LIFE.length],body=BODY[(i*11+5)%BODY.length],loc=LOC[(i*13+2)%LOC.length],meta=META[(i*5+7)%META.length],sense=SENSE[(i*9+1)%SENSE.length],niche=NICHE[(i*17+4)%NICHE.length],repro=REPRO[(i*7+8)%REPRO.length],social=SOCIAL[(i*11+6)%SOCIAL.length],cog=COG[(i*5+3)%COG.length],comm=COMM[(i*7+1)%COMM.length];
   int habitatIndex=(i*19+3)%HAB.length;String habitat=HAB[habitatIndex];
   String resources=resourceTags(niche,habitat),attract="guild_"+((i*7+1)%31),avoid="guild_"+((i*13+9)%31),flora="flora_guild_"+((i*11+4)%29);
   double resource=.24+.68*((i*37)%499)/498.0;
   double migrate=.05+.90*((i*71+17)%499)/498.0;
   double group=.08+.86*((i*113+29)%499)/498.0;
   double stimulus=.04+.92*(i-5)/494.0;
   boolean local=habitatIndex<4 && i%4==0;
   add(new SpeciesDefinition(key,"VNF Species "+String.format(Locale.US,"%03d",n),"lineage_"+key,life,body,loc,meta,sense,niche,repro,social,cog,comm,habitat,resources,attract,avoid,flora,resource,migrate,group,stimulus,local));
  }
  if(ALL.size()!=BASE_SPECIES_COUNT)throw new IllegalStateException("VNF base species registry must contain exactly "+BASE_SPECIES_COUNT+" species, got "+ALL.size());
 }

 private SpeciesRegistryV2(){}
 private static void add(SpeciesDefinition d){if(d==null||d.key.isEmpty()||ALL.put(d.key,d)!=null)throw new IllegalStateException("Duplicate species "+(d==null?"null":d.key));}
 private static String resourceTags(String niche,String habitat){
  String base=niche==null?"resource":niche.replace("_feeder","").replace("_grazer","").replace("_browser","").replace("_hunter","").replace("_forager","").replace("_recycler","").replace("_scraper","").replace("_sifter","").replace("_carrier","").replace("_partner","").replace("_cleaner","");
  String first=habitat==null?"world":habitat.split(",")[0];
  return base+","+first+",resource";
 }
 public static SpeciesDefinition get(String key){return key==null?null:ALL.get(key);}
 public static Collection<SpeciesDefinition> all(){return Collections.unmodifiableCollection(ALL.values());}
 public static Set<String> keys(){return Collections.unmodifiableSet(ALL.keySet());}
 public static int size(){return ALL.size();}
 public static boolean isBaseSpecies(String key){return key!=null&&ALL.containsKey(key);}
 public static boolean locallySeedable(String key){SpeciesDefinition d=get(key);return d!=null&&d.localPresenceEligible;}
 public static boolean isLegacyVisibleSpecies(String key){return "reedling".equals(key)||"driftwing".equals(key)||"root_husher".equals(key)||"ripplekin".equals(key)||"hearthmote".equals(key);}
}
