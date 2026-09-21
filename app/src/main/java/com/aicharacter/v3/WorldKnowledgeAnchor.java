package com.aicharacter.v3;

import org.json.JSONObject;
import java.util.*;

/** Read-only reference anchors for turning human knowledge into fictional VNF worldbuilding.
 * These anchors are NOT Haru memories and do not become world truth until authored into the world definition.
 */
public final class WorldKnowledgeAnchor {
    public final String id,domain,realAnchor,fantasyRule,sourceFamily,sourceRef;
    public final List<String> tags;
    public WorldKnowledgeAnchor(String id,String domain,String realAnchor,String fantasyRule,String sourceFamily,String sourceRef,List<String> tags){
        this.id=id==null?"":id;this.domain=domain==null?"":domain;this.realAnchor=realAnchor==null?"":realAnchor;
        this.fantasyRule=fantasyRule==null?"":fantasyRule;this.sourceFamily=sourceFamily==null?"":sourceFamily;
        this.sourceRef=sourceRef==null?"":sourceRef;this.tags=tags==null?Collections.emptyList():Collections.unmodifiableList(new ArrayList<>(tags));
    }
    public static WorldKnowledgeAnchor fromJson(JSONObject j){
        if(j==null)return new WorldKnowledgeAnchor("","","","","","",Collections.emptyList());
        List<String> tags=new ArrayList<>();org.json.JSONArray a=j.optJSONArray("tags");
        if(a!=null)for(int i=0;i<a.length();i++){String x=a.optString(i,"").trim();if(!x.isEmpty())tags.add(x);}
        return new WorldKnowledgeAnchor(j.optString("id",""),j.optString("domain",""),j.optString("realAnchor",""),
                j.optString("fantasyRule",""),j.optString("sourceFamily",""),j.optString("sourceRef",""),tags);
    }
    public static String diagnostic(WorldModel w){
        if(w==null)return"WORLD LIBRARY <no world>";
        StringBuilder b=new StringBuilder("WORLD LIBRARY V1\nanchors=").append(w.knowledgeAnchors.size()).append('\n');
        Map<String,Integer> counts=new LinkedHashMap<>();
        for(WorldKnowledgeAnchor a:w.knowledgeAnchors)counts.put(a.domain,counts.getOrDefault(a.domain,0)+1);
        for(Map.Entry<String,Integer>e:counts.entrySet())b.append(e.getKey()).append('=').append(e.getValue()).append(' ');
        b.append("\nRule: real knowledge is reference/provenance only; fictional VNF reality must be explicitly authored.\n");
        for(int i=0;i<Math.min(8,w.knowledgeAnchors.size());i++){
            WorldKnowledgeAnchor a=w.knowledgeAnchors.get(i);
            b.append("• ").append(a.id).append(" [").append(a.domain).append("] ")
             .append(a.sourceFamily).append(" -> ").append(a.fantasyRule).append('\n');
        }
        return b.toString();
    }
}
