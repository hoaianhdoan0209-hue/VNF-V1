package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.text.Normalizer;

/**
 * Hidden System-Reality evolutionary catalog.
 * Exactly 500 terminal base species descend from one common ancestor.
 * Hybrids, mutations and individual variants are deliberately NOT counted here.
 * Haru does not read this catalog directly; it is reserved for System/God knowledge.
 */
public final class SpeciesEvolutionCatalog {
 public static final String ROOT_ANCESTOR_ID="first_lumen_ancestor";
 public static final int BASE_SPECIES_COUNT=500;
 public static final int CLADE_COUNT=10;
 public static final int LINEAGE_COUNT=50;

 public static final class Species {
  public final String key,displayName,cladeId,lineageId,bodyPlan,niche,locomotion,senseMode,reproduction,defense,sociality,lifeCycle,traitFingerprint;
  Species(String key,String displayName,String cladeId,String lineageId,String bodyPlan,String niche,String locomotion,String senseMode,String reproduction,String defense,String sociality,String lifeCycle){
   this.key=key;this.displayName=displayName;this.cladeId=cladeId;this.lineageId=lineageId;this.bodyPlan=bodyPlan;this.niche=niche;this.locomotion=locomotion;this.senseMode=senseMode;this.reproduction=reproduction;this.defense=defense;this.sociality=sociality;this.lifeCycle=lifeCycle;
   this.traitFingerprint=bodyPlan+"|"+niche+"|"+locomotion+"|"+senseMode+"|"+reproduction+"|"+defense+"|"+sociality+"|"+lifeCycle;
  }
  JSONObject toGodJson(){JSONObject j=new JSONObject();try{j.put("key",key);j.put("displayName",displayName);j.put("lineagePath",lineagePath(key));j.put("bodyPlan",bodyPlan);j.put("niche",niche);j.put("locomotion",locomotion);j.put("senseMode",senseMode);j.put("reproduction",reproduction);j.put("defense",defense);j.put("sociality",sociality);j.put("lifeCycle",lifeCycle);j.put("traitFingerprint",traitFingerprint);}catch(Exception ignored){}return j;}
 }

 private static final String[] PREFIX={
  "avel","brin","cyr","dara","elun","fenn","glo","hesh","iora","june",
  "kesh","luma","mori","neth","oriv","pala","quill","rava","syl","taren",
  "ulmo","vesh","wyna","xeri","yaro"
 };
 private static final String[] SUFFIX={
  "ling","wing","husher","kin","mote","veil","drift","root","flare","bell",
  "skate","coil","whorl","fin","bloom","strider","wisp","crest","shell","ray"
 };
 private static final String[] BODY={
  "ribbon-frond","hollow-shell","tripod-reed","veil-wing","root-mantle",
  "glass-fin","spiral-back","lantern-bell","jointed-stilt","moss-armor",
  "silk-glider","petal-crown","ring-body","fan-tail","stone-pad",
  "mist-bladder","needle-limb","disk-crest","branch-antler","soft-spine",
  "coil-body","leaf-sail","drum-thorax","twin-keel","orbital-frill"
 };
 private static final String[] NICHE={
  "lumenmere film-browser","mist filter-feeder","rootmat grazer","canopy spore-browser","shallow-water pulse-feeder",
  "warm-hollow gleaner","echo-frond symbiont","blue-reed sapper","shimmer-mat scraper","rain-edge scavenger",
  "night bloom-feeder","mineral-film browser","floating-seed hunter","shade detritivore","wind-plankton hunter",
  "wet-margin burrower","grove resin-feeder","thermal-pocket browser","quiet-wall growth grazer","stormfall opportunist"
 };
 private static final String[] AREA={
  "lumenmere,wet_margin,water,lam_thread","verge,mist,rootmat,vegetation","veilroot,grove,shade,echo_frond","interior,dry,warm,quiet","lumenmere,water,shimmer_mat,wet_margin",
  "interior,warm,hearth,bloom","grove,echo_frond,veilroot,vegetation","wet_margin,blue_reed,lumenmere,rain_tolerant","lumenmere,shimmer_mat,water","verge,rain_tolerant,vegetation",
  "grove,shade,bloom,vegetation","veilroot,mineral,shade","verge,seed,mist","grove,shade,rootmat","verge,wind,mist",
  "wet_margin,water,rootmat","grove,veilroot,resin,vegetation","interior,warm,dry","interior,quiet,hearth","wet_margin,rain_tolerant,verge"
 };
 private static final String[] RESOURCE={
  "lam_thread,blue_reed,shimmer_mat","mist,mistleaf,silverfold","rootmat,veilroot,echo_frond","echo_frond,mistleaf,vegetation","shimmer_mat,lam_thread,water",
  "hearth,bloom,quiet","echo_frond,veilroot,ember_moss","blue_reed,lam_thread","shimmer_mat,water","vegetation,rootmat,mistleaf",
  "bloom,mistleaf,silverfold","veilroot,ember_moss,rootmat","glow_seed,mistleaf","echo_frond,ember_moss","mist,glow_seed",
  "rootmat,lam_thread,blue_reed","veilroot,echo_frond,vegetation","hearth,bloom","hearth,vegetation,quiet","rootmat,mistleaf,blue_reed"
 };
 private static final String[] LOC={"ground-glide","four-point walk","six-beat scuttle","short-hop","air-drift","membrane-flight","water-pulse","surface-skate","burrow-wave","climb-coil","root-step","fin-undulate"};
 private static final String[] SENSE={"lumen-gradient","vibration-map","humidity-field","thermal-edge","echo-pressure","electrostatic-drift","chemical-thread","polarized-glow","surface-ripple","airflow-vortex","root-tension"};
 private static final String[] REPRO={"paired-spore brood","seasonless bud clutch","gel-capsule clutch","carried seed brood","root-nursery brood","mist-egg chain","hollow-shell nursery","split-cocoon brood","floating sac brood"};
 private static final String[] DEFENSE={"stillness camouflage","warning shimmer","root-anchor","shell-fold","mist discharge","group flare","rapid burrow","air-brake dart","bitter surface film","vibration decoy"};
 private static final String[] SOCIAL={"solitary-range","loose-pair","small-cluster","seasonal-gathering","trail-network","nursery-colony","resource-truce","call-and-response"};
 private static final String[] CYCLE={"short-fast","short-cyclical","medium-steady","medium-dormant","long-slow","long-pulse","rain-linked","lumen-linked"};

 private static final List<Species> SPECIES;
 private static final Map<String,Species> BY_KEY;
 static{
  ArrayList<Species> all=new ArrayList<>(BASE_SPECIES_COUNT);LinkedHashMap<String,Species> by=new LinkedHashMap<>();
  String[] legacyKey={"reedling","driftwing","root_husher","ripplekin","hearthmote"};
  String[] legacyName={"Reedling","Driftwing","Root Husher","Ripplekin","Hearthmote"};
  for(int i=0;i<BASE_SPECIES_COUNT;i++){
   int body=i%BODY.length,niche=i/BODY.length,lineage=(body*2+niche*7)%LINEAGE_COUNT,clade=lineage/5;
   String key=i<legacyKey.length?legacyKey[i]:"vnf_"+PREFIX[body]+"_"+SUFFIX[niche];
   String display=i<legacyName.length?legacyName[i]:title(PREFIX[body]+SUFFIX[niche]);
   Species s=new Species(key,display,String.format(Locale.ROOT,"clade_%02d",clade),String.format(Locale.ROOT,"lineage_%02d",lineage),BODY[body],NICHE[niche],
    LOC[(i*7+body)%LOC.length],SENSE[(i*11+niche)%SENSE.length],REPRO[(i*13+body+niche)%REPRO.length],DEFENSE[(i*17+body)%DEFENSE.length],SOCIAL[(i*19+niche)%SOCIAL.length],CYCLE[(i*23+body+niche)%CYCLE.length]);
   all.add(s);by.put(s.key,s);
  }
  validate(all,by);SPECIES=Collections.unmodifiableList(all);BY_KEY=Collections.unmodifiableMap(by);
 }
 private SpeciesEvolutionCatalog(){}

 public static List<Species> all(){return SPECIES;}
 public static Species get(String key){return key==null?null:BY_KEY.get(key);}
 public static boolean isBaseSpecies(String key){return get(key)!=null;}
 public static String lineagePath(String key){Species s=get(key);return s==null?"":ROOT_ANCESTOR_ID+">"+s.cladeId+">"+s.lineageId+">"+s.key;}

 public static SpeciesEcologyProfile ecologyProfile(Species s){
  if(s==null)return null;int body=indexOf(BODY,s.bodyPlan),niche=indexOf(NICHE,s.niche);
  String area=AREA[niche],resources=RESOURCE[niche],flora=RESOURCE[(niche+3)%RESOURCE.length];
  double resource=.42+((body*7+niche*3)%49)/100.0,migrate=.20+((body*11+niche*5)%66)/100.0,group=.18+((body*13+niche*7)%72)/100.0,stimulus=.06+((body*17+niche*11)%31)/100.0;
  return new SpeciesEcologyProfile(s.key,area,resources,"","",flora,cl(resource),cl(migrate),cl(group),cl(stimulus));
 }

 public static JSONObject godContext(String raw,int limit){
  JSONObject root=new JSONObject();try{
   root.put("visibility","GOD_SYSTEM_ONLY");root.put("rootAncestorId",ROOT_ANCESTOR_ID);root.put("sharedCommonAncestor",true);
   root.put("terminalBaseSpeciesCount",SPECIES.size());root.put("cladeCount",CLADE_COUNT);root.put("lineageCount",LINEAGE_COUNT);
   root.put("hybridsCounted",false);root.put("mutationsCounted",false);root.put("individualVariantsCounted",false);
   root.put("rule","These 500 entries are base terminal species, not skins, hybrids or mutation variants. Haru does not automatically know this tree.");
   JSONArray match=new JSONArray();String q=norm(raw);int max=Math.max(1,Math.min(12,limit));
   for(Species s:SPECIES){if(match.length()>=max)break;String hay=norm(s.key+" "+s.displayName+" "+s.bodyPlan+" "+s.niche+" "+s.cladeId+" "+s.lineageId);if(!q.isEmpty()&&overlap(q,hay)>0)match.put(s.toGodJson());}
   if(match.length()==0&&mentionsEvolution(q))for(int i=0;i<Math.min(5,max);i++)match.put(SPECIES.get(i).toGodJson());
   root.put("matchedSpecies",match);
  }catch(Exception ignored){}return root;
 }

 private static boolean mentionsEvolution(String q){return q.contains("evol")||q.contains("tien hoa")||q.contains("to tien")||q.contains("ancestor")||q.contains("species")||q.contains("loai")||q.contains("500");}
 private static double overlap(String q,String hay){double n=0;for(String t:q.split("\\s+"))if(t.length()>2&&hay.contains(t))n++;return n;}
 private static String norm(String s){String x=Normalizer.normalize((s==null?"":s).toLowerCase(Locale.ROOT),Normalizer.Form.NFD).replaceAll("\\p{M}+","");return x.replace('đ','d').replaceAll("[^a-z0-9_]+"," ").trim();}
 private static String title(String s){if(s==null||s.isEmpty())return"";return Character.toUpperCase(s.charAt(0))+s.substring(1);}
 private static int indexOf(String[] a,String v){for(int i=0;i<a.length;i++)if(a[i].equals(v))return i;return 0;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
 private static void validate(List<Species> all,Map<String,Species> by){
  if(all.size()!=BASE_SPECIES_COUNT||by.size()!=BASE_SPECIES_COUNT)throw new IllegalStateException("VNF evolutionary catalog must contain exactly 500 unique base species.");
  HashSet<String> fingerprints=new HashSet<>(),paths=new HashSet<>();
  for(Species s:all){if(s.key==null||s.key.isEmpty()||!fingerprints.add(s.traitFingerprint)||!paths.add(lineagePathUnsafe(s)))throw new IllegalStateException("VNF base species identity/fingerprint collision: "+s.key);}
 }
 private static String lineagePathUnsafe(Species s){return ROOT_ANCESTOR_ID+">"+s.cladeId+">"+s.lineageId+">"+s.key;}
}
