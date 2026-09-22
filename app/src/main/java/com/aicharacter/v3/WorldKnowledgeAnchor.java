package com.aicharacter.v3;

import org.json.JSONObject;
import java.util.*;

/**
 * Read-only reference anchors for turning human knowledge into fictional VNF worldbuilding.
 *
 * These anchors are a REFERENCE LAYER only. They are not Haru memories, are not
 * world history, and do not become VNF truth until fictional content is
 * explicitly authored/committed through the world-content path.
 */
public final class WorldKnowledgeAnchor {
    public final String id,domain,realAnchor,fantasyRule,sourceFamily,sourceRef,retrievedAt,limits;
    public final double confidence;
    public final List<String> tags;

    /** Backward-compatible constructor for older authored callers. */
    public WorldKnowledgeAnchor(String id,String domain,String realAnchor,String fantasyRule,
                                String sourceFamily,String sourceRef,List<String> tags){
        this(id,domain,realAnchor,fantasyRule,sourceFamily,sourceRef,
                "UNKNOWN_BUNDLED_REFERENCE",-1,
                "Exact retrieval time/confidence unavailable; reference only, never VNF world truth.",tags);
    }

    public WorldKnowledgeAnchor(String id,String domain,String realAnchor,String fantasyRule,
                                String sourceFamily,String sourceRef,String retrievedAt,
                                double confidence,String limits,List<String> tags){
        this.id=safe(id);this.domain=safe(domain);this.realAnchor=safe(realAnchor);
        this.fantasyRule=safe(fantasyRule);this.sourceFamily=safe(sourceFamily);
        this.sourceRef=safe(sourceRef);
        this.retrievedAt=blank(retrievedAt)?"UNKNOWN_BUNDLED_REFERENCE":retrievedAt.trim();
        this.confidence=Double.isFinite(confidence)?Math.max(-1,Math.min(1,confidence)):-1;
        this.limits=blank(limits)
                ?"Reference-only anchor; uncertainty/coverage limits were not supplied."
                :limits.trim();
        this.tags=tags==null?Collections.emptyList():Collections.unmodifiableList(new ArrayList<>(tags));
    }

    public static WorldKnowledgeAnchor fromJson(JSONObject j){
        if(j==null)return new WorldKnowledgeAnchor("","","","","","",Collections.emptyList());
        List<String> tags=new ArrayList<>();org.json.JSONArray a=j.optJSONArray("tags");
        if(a!=null)for(int i=0;i<a.length();i++){String x=a.optString(i,"").trim();if(!x.isEmpty())tags.add(x);}
        return new WorldKnowledgeAnchor(
                j.optString("id",""),j.optString("domain",""),j.optString("realAnchor",""),
                j.optString("fantasyRule",""),j.optString("sourceFamily",""),j.optString("sourceRef",""),
                j.optString("retrievedAt","UNKNOWN_BUNDLED_REFERENCE"),
                j.has("confidence")?j.optDouble("confidence",-1):-1,
                j.optString("limits","Reference-only anchor; uncertainty/coverage limits were not supplied."),
                tags);
    }

    public boolean hasCompleteProvenance(){
        if(blank(id)||blank(domain)||blank(realAnchor)||blank(fantasyRule)||blank(sourceFamily)||blank(sourceRef))return false;
        String retrieved=retrievedAt==null?"":retrievedAt.trim();
        if(retrieved.isEmpty()||"UNKNOWN_BUNDLED_REFERENCE".equalsIgnoreCase(retrieved)||
                "UNKNOWN".equalsIgnoreCase(retrieved)||"UNSPECIFIED".equalsIgnoreCase(retrieved))return false;
        if(!Double.isFinite(confidence)||confidence<0||confidence>1)return false;
        String limitation=limits==null?"":limits.trim();
        if(limitation.isEmpty())return false;
        String normalized=limitation.toLowerCase(Locale.ROOT);
        if(normalized.contains("not supplied")||normalized.contains("unavailable")||
                normalized.equals("reference only"))return false;
        return true;
    }

    public static String diagnostic(WorldModel w){
        if(w==null)return"WORLD LIBRARY <no world>";
        StringBuilder b=new StringBuilder("WORLD LIBRARY V1\nanchors=").append(w.knowledgeAnchors.size()).append('\n');
        Map<String,Integer> counts=new LinkedHashMap<>();
        for(WorldKnowledgeAnchor a:w.knowledgeAnchors)counts.put(a.domain,counts.getOrDefault(a.domain,0)+1);
        for(Map.Entry<String,Integer>e:counts.entrySet())b.append(e.getKey()).append('=').append(e.getValue()).append(' ');
        b.append("\nRule: REAL-WORLD REFERENCE != VNF WORLD TRUTH; fictional reality must be explicitly authored.\n");
        for(int i=0;i<Math.min(8,w.knowledgeAnchors.size());i++){
            WorldKnowledgeAnchor a=w.knowledgeAnchors.get(i);
            b.append("• ").append(a.id).append(" [").append(a.domain).append("] ")
             .append(a.sourceFamily).append(" / ").append(a.sourceRef)
             .append(" retrieved=").append(a.retrievedAt)
             .append(" confidence=").append(a.confidence<0?"unspecified":String.format(Locale.US,"%.2f",a.confidence))
             .append(" -> ").append(a.fantasyRule).append('\n');
        }
        return b.toString();
    }

    private static String safe(String s){return s==null?"":s;}
    private static boolean blank(String s){return s==null||s.trim().isEmpty();}
}
