package com.aicharacter.v3;
import org.json.*;
import java.util.*;

/** Persistent non-human living layer: individual representatives plus bounded biome/species population fields. */
public final class LivingWorldState{
 public final Map<String,FloraLifeState> flora=new LinkedHashMap<>();
 public final Map<String,CreatureLifeState> creatures=new LinkedHashMap<>();
 public final Map<String,BiomeLifeFieldState> fields=new LinkedHashMap<>();
 public final Map<String,SpeciesPopulationState> populations=new LinkedHashMap<>();

 public FloraLifeState flora(String objectId){return flora.computeIfAbsent(objectId,k->new FloraLifeState());}
 public CreatureLifeState creature(String objectId){return creatures.computeIfAbsent(objectId,k->new CreatureLifeState());}
 public BiomeLifeFieldState field(String areaId){return fields.computeIfAbsent(areaId,k->new BiomeLifeFieldState());}
 public SpeciesPopulationState population(String speciesKey,String areaId){String key=populationKey(speciesKey,areaId);SpeciesPopulationState p=populations.computeIfAbsent(key,k->new SpeciesPopulationState());p.speciesKey=speciesKey==null?"":speciesKey;p.areaId=areaId==null?"":areaId;return p;}
 public static String populationKey(String speciesKey,String areaId){return (speciesKey==null?"":speciesKey)+"@"+(areaId==null?"":areaId);}

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  JSONObject f=new JSONObject();for(Map.Entry<String,FloraLifeState>e:flora.entrySet())f.put(e.getKey(),e.getValue().toJson());j.put("flora",f);
  JSONObject cr=new JSONObject();for(Map.Entry<String,CreatureLifeState>e:creatures.entrySet())cr.put(e.getKey(),e.getValue().toJson());j.put("creatures",cr);
  JSONObject bf=new JSONObject();for(Map.Entry<String,BiomeLifeFieldState>e:fields.entrySet())bf.put(e.getKey(),e.getValue().toJson());j.put("fields",bf);
  JSONObject pp=new JSONObject();for(Map.Entry<String,SpeciesPopulationState>e:populations.entrySet())pp.put(e.getKey(),e.getValue().toJson());j.put("populations",pp);
 }catch(Exception ignored){}return j;}

 public static LivingWorldState fromJson(JSONObject j){LivingWorldState w=new LivingWorldState();if(j==null)return w;
  JSONObject f=j.optJSONObject("flora");if(f!=null){Iterator<String>it=f.keys();while(it.hasNext()){String id=it.next();w.flora.put(id,FloraLifeState.fromJson(f.optJSONObject(id)));}}
  JSONObject cr=j.optJSONObject("creatures");if(cr!=null){Iterator<String>it=cr.keys();while(it.hasNext()){String id=it.next();w.creatures.put(id,CreatureLifeState.fromJson(cr.optJSONObject(id)));}}
  JSONObject bf=j.optJSONObject("fields");if(bf!=null){Iterator<String>it=bf.keys();while(it.hasNext()){String id=it.next();w.fields.put(id,BiomeLifeFieldState.fromJson(bf.optJSONObject(id)));}}
  JSONObject pp=j.optJSONObject("populations");if(pp!=null){Iterator<String>it=pp.keys();while(it.hasNext()){String id=it.next();SpeciesPopulationState p=SpeciesPopulationState.fromJson(pp.optJSONObject(id));if(p.speciesKey.isEmpty()||p.areaId.isEmpty()){int cut=id.indexOf('@');if(cut>0){p.speciesKey=id.substring(0,cut);p.areaId=id.substring(cut+1);}}p.clamp();w.populations.put(id,p);}}
  return w;
 }
}
