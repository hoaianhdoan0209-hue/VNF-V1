package com.aicharacter.v3;

/** One hidden System-Reality node in the true tree of life. */
public final class EvolutionaryLineageNode {
 public final String nodeId,parentId,label,extantSpeciesKey,adaptationSignature;
 public final boolean extinct;
 public final double divergenceAgeMyr;
 public EvolutionaryLineageNode(String nodeId,String parentId,String label,String extantSpeciesKey,boolean extinct,double divergenceAgeMyr,String adaptationSignature){
  this.nodeId=nodeId==null?"":nodeId;this.parentId=parentId==null?"":parentId;this.label=label==null?"":label;this.extantSpeciesKey=extantSpeciesKey==null?"":extantSpeciesKey;this.extinct=extinct;this.divergenceAgeMyr=Double.isFinite(divergenceAgeMyr)?Math.max(0,divergenceAgeMyr):0;this.adaptationSignature=adaptationSignature==null?"":adaptationSignature;
 }
 public boolean extantLeaf(){return !extinct&&!extantSpeciesKey.isEmpty();}
}
