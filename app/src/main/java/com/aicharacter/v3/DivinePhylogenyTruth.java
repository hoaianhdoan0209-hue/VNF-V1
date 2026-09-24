package com.aicharacter.v3;

import java.util.*;

/**
 * Hidden authored World Truth: every present VNF species descends from one primordial ancestor.
 * Ordinary Haru/creature cognition must never read this class directly. Access is reserved for God knowledge/revelation.
 */
public final class DivinePhylogenyTruth {
 public static final String ROOT_ID="primordial_life_0";
 private static final LinkedHashMap<String,EvolutionaryLineageNode> NODES=new LinkedHashMap<>();
 private static final LinkedHashMap<String,String> SPECIES_TO_NODE=new LinkedHashMap<>();

 static{
  add(new EvolutionaryLineageNode(ROOT_ID,"","The First Living Pattern","",false,920.0,"self-maintaining membrane + heritable rhythm"));
  for(int c=0;c<20;c++){
   String id=String.format(Locale.US,"ancient_clade_%02d",c);
   add(new EvolutionaryLineageNode(id,ROOT_ID,"Ancient Clade "+c,"",false,760-c*5.2,"major body chemistry split "+c));
   add(new EvolutionaryLineageNode(id+"_extinct",id,"Lost Ancient Sister "+c,"",true,690-c*4.1,"extinct early experiment "+c));
  }
  for(int b=0;b<100;b++){
   String id=String.format(Locale.US,"branch_%03d",b),parent=String.format(Locale.US,"ancient_clade_%02d",b%20);
   add(new EvolutionaryLineageNode(id,parent,"Deep Branch "+b,"",false,520-b*2.1,"ecological radiation "+b));
   add(new EvolutionaryLineageNode(id+"_lost",id,"Extinct Sister Branch "+b,"",true,420-b*1.5,"lost adaptation "+b));
  }
  int i=0;
  for(SpeciesDefinition d:SpeciesRegistryV2.all()){
   int branch=i/5;
   double age=Math.max(.8,180.0-(i%5)*17.0-(branch%13)*2.3);
   String adapt=d.bodyPlan+";"+d.locomotion+";"+d.metabolism+";"+d.sensorySignature+";"+d.trophicNiche;
   EvolutionaryLineageNode leaf=new EvolutionaryLineageNode(d.lineageId,String.format(Locale.US,"branch_%03d",branch),d.systemName,d.key,false,age,adapt);
   add(leaf);SPECIES_TO_NODE.put(d.key,leaf.nodeId);i++;
  }
  if(SPECIES_TO_NODE.size()!=SpeciesRegistryV2.BASE_SPECIES_COUNT)throw new IllegalStateException("Every base species must have one extant lineage leaf.");
  for(String species:SPECIES_TO_NODE.keySet())if(!reachesRoot(species))throw new IllegalStateException("Species does not reach primordial ancestor: "+species);
 }

 private DivinePhylogenyTruth(){}
 private static void add(EvolutionaryLineageNode n){if(NODES.put(n.nodeId,n)!=null)throw new IllegalStateException("Duplicate lineage node "+n.nodeId);}
 public static EvolutionaryLineageNode root(){return NODES.get(ROOT_ID);}
 public static EvolutionaryLineageNode lineageOf(String speciesKey){String id=SPECIES_TO_NODE.get(speciesKey);return id==null?null:NODES.get(id);}
 public static boolean reachesRoot(String speciesKey){
  EvolutionaryLineageNode n=lineageOf(speciesKey);int guard=0;
  while(n!=null&&guard++<64){if(ROOT_ID.equals(n.nodeId))return true;if(n.parentId.isEmpty())return false;n=NODES.get(n.parentId);}
  return false;
 }
 public static List<EvolutionaryLineageNode> ancestry(String speciesKey,int maxDepth){
  ArrayList<EvolutionaryLineageNode> out=new ArrayList<>();EvolutionaryLineageNode n=lineageOf(speciesKey);int limit=Math.max(1,Math.min(64,maxDepth));
  while(n!=null&&out.size()<limit){out.add(n);if(ROOT_ID.equals(n.nodeId)||n.parentId.isEmpty())break;n=NODES.get(n.parentId);}
  return Collections.unmodifiableList(out);
 }
 public static EvolutionaryLineageNode commonAncestor(String a,String b){
  if(a==null||b==null)return null;LinkedHashSet<String> path=new LinkedHashSet<>();EvolutionaryLineageNode x=lineageOf(a);int guard=0;while(x!=null&&guard++<64){path.add(x.nodeId);if(x.parentId.isEmpty())break;x=NODES.get(x.parentId);}
  EvolutionaryLineageNode y=lineageOf(b);guard=0;while(y!=null&&guard++<64){if(path.contains(y.nodeId))return y;if(y.parentId.isEmpty())break;y=NODES.get(y.parentId);}return null;
 }
 public static int extantSpeciesCount(){return SPECIES_TO_NODE.size();}
 public static int extinctLineageCount(){int n=0;for(EvolutionaryLineageNode x:NODES.values())if(x.extinct)n++;return n;}
 public static int totalNodeCount(){return NODES.size();}
 static Collection<EvolutionaryLineageNode> allNodesForGod(){return Collections.unmodifiableCollection(NODES.values());}
}
