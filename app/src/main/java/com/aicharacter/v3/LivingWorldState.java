package com.aicharacter.v3;
import org.json.*;import java.util.*;
/** Persistent non-human living layer. Authored flora have individual state; decorative biome vegetation is a population visual layer. */
public final class LivingWorldState{
 public final Map<String,FloraLifeState> flora=new LinkedHashMap<>();public final Map<String,BiomeLifeFieldState> fields=new LinkedHashMap<>();
 public FloraLifeState flora(String objectId){return flora.computeIfAbsent(objectId,k->new FloraLifeState());}public BiomeLifeFieldState field(String areaId){return fields.computeIfAbsent(areaId,k->new BiomeLifeFieldState());}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{JSONObject f=new JSONObject();for(Map.Entry<String,FloraLifeState>e:flora.entrySet())f.put(e.getKey(),e.getValue().toJson());j.put("flora",f);JSONObject bf=new JSONObject();for(Map.Entry<String,BiomeLifeFieldState>e:fields.entrySet())bf.put(e.getKey(),e.getValue().toJson());j.put("fields",bf);}catch(Exception ignored){}return j;}
 public static LivingWorldState fromJson(JSONObject j){LivingWorldState w=new LivingWorldState();if(j==null)return w;JSONObject f=j.optJSONObject("flora");if(f!=null){Iterator<String>it=f.keys();while(it.hasNext()){String id=it.next();w.flora.put(id,FloraLifeState.fromJson(f.optJSONObject(id)));}}JSONObject bf=j.optJSONObject("fields");if(bf!=null){Iterator<String>it=bf.keys();while(it.hasNext()){String id=it.next();w.fields.put(id,BiomeLifeFieldState.fromJson(bf.optJSONObject(id)));}}return w;}
}