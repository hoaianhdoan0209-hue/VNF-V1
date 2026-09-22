package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/** Read-only knowledge lookup with explicit layer/provenance separation. */
public final class GodKnowledgeQueryEngine {
 public static final class Result{
  public final String id,layer,summary,sourceFamily,sourceRef,retrievedAt,limits;public final double confidence;
  Result(String id,String layer,String summary,String family,String ref,String retrieved,double confidence,String limits){this.id=id;this.layer=layer;this.summary=summary;this.sourceFamily=family;this.sourceRef=ref;this.retrievedAt=retrieved;this.confidence=confidence;this.limits=limits;}
  public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("layer",layer);j.put("summary",summary);j.put("sourceFamily",sourceFamily);j.put("sourceRef",sourceRef);j.put("retrievedAt",retrievedAt);j.put("confidence",confidence<0?JSONObject.NULL:confidence);j.put("limits",limits);j.put("worldTruth","VNF_WORLD_TRUTH".equals(layer));}catch(Exception ignored){}return j;}
 }
 private GodKnowledgeQueryEngine(){}

 public static List<Result> query(WorldState s,String raw,int limit){
  List<Result>out=new ArrayList<>();if(s==null||s.world==null)return out;String q=norm(raw);int max=Math.max(1,Math.min(8,limit));
  if(!q.isEmpty()){
   for(WorldObject o:s.world.objects){
    if(out.size()>=max)break;if(o==null||!o.enabled)continue;String hay=norm(o.id+" "+o.type+" "+o.haruDescription+" "+o.tags);
    if(overlap(q,hay)>0)out.add(new Result(o.id,"VNF_WORLD_TRUTH",o.haruDescription==null||o.haruDescription.isEmpty()?o.type:o.haruDescription,"VNF World Definition",o.id,"authored",1.0,"Exists because it is authored in the loaded VNF world definition."));
   }
  }
  List<Scored> refs=new ArrayList<>();
  for(WorldKnowledgeAnchor a:s.world.knowledgeAnchors){
   String hay=norm(a.id+" "+a.domain+" "+a.realAnchor+" "+a.fantasyRule+" "+String.join(" ",a.tags));double score=q.isEmpty()?0:overlap(q,hay);
   if(score>0)refs.add(new Scored(a,score));
  }
  refs.sort((a,b)->Double.compare(b.score,a.score));
  for(Scored x:refs){
   if(out.size()>=max)break;WorldKnowledgeAnchor a=x.anchor;
   out.add(new Result(a.id,"REAL_REFERENCE",a.realAnchor,a.sourceFamily,a.sourceRef,a.retrievedAt,a.confidence,a.limits));
  }
  return out;
 }
 public static JSONArray toJson(List<Result>rs){JSONArray a=new JSONArray();if(rs!=null)for(Result r:rs)a.put(r.toJson());return a;}
 private static final class Scored{final WorldKnowledgeAnchor anchor;final double score;Scored(WorldKnowledgeAnchor a,double s){anchor=a;score=s;}}
 private static String norm(String s){return (s==null?"":s).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+"," ").trim();}
 private static double overlap(String q,String hay){if(q.isEmpty()||hay.isEmpty())return 0;double score=0;for(String t:q.split("\\s+"))if(t.length()>2&&hay.contains(t))score+=1;return score;}
}
