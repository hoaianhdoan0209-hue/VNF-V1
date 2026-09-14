package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Mutable overlay: authored definition remains untouched across game updates. */
public final class WorldRuntimeState {
    public int definitionVersionSeen=1;
    public final Map<String,ObjectState> objects=new LinkedHashMap<>();
    public static final class ObjectState {
        public float x; public boolean enabled=true;
        ObjectState(float x,boolean enabled){this.x=x;this.enabled=enabled;}
        JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("x",x);j.put("enabled",enabled);}catch(Exception ignored){}return j;}
    }
    public void mergeDefinition(WorldModel model){
        definitionVersionSeen=model.definitionVersion;
        for(WorldObject o:model.objects){ObjectState r=objects.get(o.id);if(r==null){r=new ObjectState(o.x,o.enabled);objects.put(o.id,r);}o.x=r.x;o.enabled=r.enabled;}
    }
    public void capture(WorldModel model){for(WorldObject o:model.objects){ObjectState r=objects.get(o.id);if(r==null){r=new ObjectState(o.x,o.enabled);objects.put(o.id,r);}r.x=o.x;r.enabled=o.enabled;}}
    public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("definitionVersionSeen",definitionVersionSeen);JSONArray a=new JSONArray();for(Map.Entry<String,ObjectState> e:objects.entrySet()){JSONObject x=e.getValue().toJson();x.put("id",e.getKey());a.put(x);}j.put("objects",a);}catch(Exception ignored){}return j;}
    public static WorldRuntimeState fromJson(JSONObject j){WorldRuntimeState r=new WorldRuntimeState();if(j==null)return r;r.definitionVersionSeen=j.optInt("definitionVersionSeen",1);JSONArray a=j.optJSONArray("objects");if(a!=null)for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null)r.objects.put(x.optString("id"),new ObjectState((float)x.optDouble("x"),x.optBoolean("enabled",true)));}return r;}
}
