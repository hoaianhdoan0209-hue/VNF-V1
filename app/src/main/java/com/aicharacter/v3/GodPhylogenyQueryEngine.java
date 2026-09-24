package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/**
 * God-only view into authored origin-of-life truth.
 * This never writes Haru memory/beliefs; revelation must use the teaching/revelation pipeline separately.
 */
public final class GodPhylogenyQueryEngine {
 private GodPhylogenyQueryEngine(){}

 public static boolean relevant(String q){
  String x=q==null?"":q.toLowerCase(Locale.ROOT);
  return x.contains("tổ tiên")||x.contains("to tien")||x.contains("tiến hóa")||x.contains("tien hoa")||x.contains("evolution")||x.contains("lineage")||x.contains("nguồn gốc")||x.contains("nguon goc")||x.contains("cùng loài")||x.contains("chung tổ");
 }

 public static JSONObject query(WorldState s,String text){
  JSONObject out=new JSONObject();try{
   out.put("available",false);out.put("layer","DIVINE_ORIGIN_TRUTH");out.put("worldTruth",true);out.put("autoRevealedToHaru",false);
   if(!relevant(text)){out.put("reason","query not about ancestry/evolution");return out;}
   int budget=s!=null&&s.divineOntology!=null?s.divineOntology.capacity.reasoningDepth:2;
   ArrayList<String> species=mentionedSpecies(text,Math.max(2,Math.min(6,budget)));
   out.put("available",true);out.put("primordialAncestorId",DivinePhylogenyTruth.ROOT_ID);out.put("extantBaseSpecies",DivinePhylogenyTruth.extantSpeciesCount());out.put("knownExtinctLineages",DivinePhylogenyTruth.extinctLineageCount());
   JSONArray entries=new JSONArray();
   for(String key:species){
    SpeciesDefinition d=SpeciesRegistryV2.get(key);EvolutionaryLineageNode leaf=DivinePhylogenyTruth.lineageOf(key);if(d==null||leaf==null)continue;
    JSONObject e=new JSONObject();e.put("speciesKey",key);e.put("systemName",d.systemName);e.put("lineageId",leaf.nodeId);e.put("divergenceAgeMyr",leaf.divergenceAgeMyr);
    JSONArray path=new JSONArray();for(EvolutionaryLineageNode n:DivinePhylogenyTruth.ancestry(key,Math.max(3,budget+2)))path.put(n.nodeId);e.put("ancestry",path);entries.put(e);
   }
   out.put("species",entries);
   if(species.size()>=2){EvolutionaryLineageNode ca=DivinePhylogenyTruth.commonAncestor(species.get(0),species.get(1));if(ca!=null){JSONObject c=new JSONObject();c.put("nodeId",ca.nodeId);c.put("label",ca.label);c.put("divergenceAgeMyr",ca.divergenceAgeMyr);out.put("commonAncestor",c);}}
   out.put("provenance","DIVINE_ORIGIN_TRUTH:authored_phylogeny_v1");
   out.put("epistemicLimit","This is origin-history truth known to Thần. It does not imply omniscience about unobserved present events.");
  }catch(Exception ignored){}
  return out;
 }

 private static ArrayList<String> mentionedSpecies(String text,int max){
  ArrayList<String> out=new ArrayList<>();String x=text==null?"":text.toLowerCase(Locale.ROOT);
  for(SpeciesDefinition d:SpeciesRegistryV2.all()){
   if(out.size()>=max)break;
   String name=d.systemName.toLowerCase(Locale.ROOT);
   if(x.contains(d.key.toLowerCase(Locale.ROOT))||(!name.isEmpty()&&x.contains(name)))out.add(d.key);
  }
  return out;
 }
}
