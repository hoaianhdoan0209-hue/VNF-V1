package com.aicharacter.v3;

import android.content.Context;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Loads immutable authored world content once. Runtime state is a separate overlay. */
public final class WorldDefinitionLoader {
    private WorldDefinitionLoader() {}
    public static WorldModel load(Context context) {
        try (InputStream in=context.getAssets().open("world/world_v1.json")) {
            String json=new String(in.readAllBytes(), StandardCharsets.UTF_8);
            WorldModel world=WorldModel.fromDefinitionJson(new JSONObject(json));
            try(InputStream kin=context.getAssets().open("world/world_knowledge_v1.json")){
                JSONObject root=new JSONObject(new String(kin.readAllBytes(),StandardCharsets.UTF_8));
                JSONArray anchors=root.optJSONArray("anchors");
                if(anchors!=null)for(int i=0;i<anchors.length();i++){
                    WorldKnowledgeAnchor a=WorldKnowledgeAnchor.fromJson(anchors.optJSONObject(i));
                    if(!a.id.isEmpty())world.knowledgeAnchors.add(a);
                }
            }
            return world;
        } catch (Exception e) {
            throw new IllegalStateException("World definition could not be loaded", e);
        }
    }
}
