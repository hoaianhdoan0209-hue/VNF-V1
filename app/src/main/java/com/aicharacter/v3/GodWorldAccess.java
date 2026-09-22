package com.aicharacter.v3;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;

/** Bounded System-Reality view for the online God world-caretaker channel. */
public final class GodWorldAccess {
    private GodWorldAccess(){}

    public static JSONObject snapshot(Context c, WorldState s){
        JSONObject out=new JSONObject();
        try{
            WorldArea area=s.world==null?null:s.world.areaAt(s.haruX);
            out.put("girlArea",area==null?"unknown":area.id);
            out.put("girlX",Math.round(s.haruX));
            out.put("catX",Math.round(s.catX));
            out.put("weather",s.environment==null?"UNKNOWN":s.environment.weather);
            out.put("worldMinutes",s.worldMinutes);
            out.put("runtimeRevision",RuntimeContentStore.revision(c));
            out.put("contentUpdaterConfigured",VnfOnlineConfig.contentUpdaterConfigured());
            out.put("checkpointAvailable",RuntimeContentStore.latestCheckpoint(c)!=null);
            out.put("capabilities",GodCapabilityModel.toJson());
            out.put("worldObservation",GodObservationSnapshot.build(s));

            JSONArray objects=new JSONArray();
            if(s.world!=null && s.world.objects!=null){
                int count=0;
                for(WorldObject o:s.world.objects){
                    if(o==null||!o.enabled)continue;
                    JSONObject j=new JSONObject();
                    j.put("id",o.id);j.put("type",o.type);j.put("assetRef",o.assetRef);j.put("renderLayer",o.renderLayer);
                    objects.put(j); if(++count>=24)break;
                }
            }
            out.put("visibleWorldObjects",objects);

            JSONArray overrides=new JSONArray();
            File dir=new File(RuntimeContentStore.current(c),"assets");
            File[] xs=dir.listFiles((d,n)->n.endsWith(".png"));
            if(xs!=null)for(File f:xs){JSONObject j=new JSONObject();j.put("file",f.getName());j.put("bytes",f.length());overrides.put(j);}
            out.put("runtimeAssetOverrides",overrides);

            out.put("repairCapabilities",new JSONArray()
                    .put("install signed graphics/world-content patch")
                    .put("checkpoint before activation")
                    .put("rollback latest checkpoint")
                    .put("reload runtime asset cache automatically"));
            out.put("forbiddenCapabilities",new JSONArray()
                    .put("overwrite APK/DEX/Java core")
                    .put("rewrite girl mind/personality")
                    .put("change security/API secrets")
                    .put("change save schema through content patch"));
        }catch(Exception ignored){}
        return out;
    }
}
