package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory authored definition. It is not serialized into player save. */
public final class WorldModel {
    public int definitionVersion=1;
    public String worldId="vnf_lakeside_home_v1";
    public final List<WorldArea> areas=new ArrayList<>();
    public final List<WorldObject> objects=new ArrayList<>();
    public final Map<String,BiomeProfile> biomes=new LinkedHashMap<>();

    public WorldArea areaAt(float x){ for(WorldArea a:areas) if(a.contains(x)) return a; return null; }
    public WorldArea area(String id){ for(WorldArea a:areas) if(a.id.equals(id)) return a; return null; }
    public WorldObject object(String id){ for(WorldObject o:objects) if(o.id.equals(id)) return o; return null; }
    public BiomeProfile biome(String id){return id==null?null:biomes.get(id);}
    public WorldObject firstTagged(String tag){ for(WorldObject o:objects) if(o.enabled&&o.tags.contains(tag)) return o; return null; }

    public static WorldModel fromDefinitionJson(JSONObject j){
        WorldModel w=new WorldModel();
        w.definitionVersion=j.optInt("worldDefinitionVersion",1);
        w.worldId=j.optString("worldId",w.worldId);
        JSONArray bj=j.optJSONArray("biomes");if(bj!=null)for(int i=0;i<bj.length();i++){BiomeProfile b=BiomeProfile.fromJson(bj.optJSONObject(i));if(b.id!=null&&!b.id.isEmpty())w.biomes.put(b.id,b);}
        JSONArray areas=j.optJSONArray("areas");
        if(areas!=null) for(int i=0;i<areas.length();i++) w.areas.add(WorldArea.fromJson(areas.optJSONObject(i)));
        JSONArray objects=j.optJSONArray("objects");
        if(objects!=null) for(int i=0;i<objects.length();i++) w.objects.add(WorldObject.fromJson(objects.optJSONObject(i)));
        if(w.areas.isEmpty()) throw new IllegalArgumentException("World definition has no areas");
        return w;
    }
}
