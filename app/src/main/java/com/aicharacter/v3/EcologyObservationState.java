package com.aicharacter.v3;
import org.json.*;import java.util.*;
/** Persistent observation scaffolding for Haru. Stores only what she actually saw, never hidden ecology truth. */
public final class EcologyObservationState{
 public static final class FloraSeen{
  public double opening,growthPulse;public long seenAt;
  JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("opening",opening);j.put("growthPulse",growthPulse);j.put("seenAt",seenAt);}catch(Exception ignored){}return j;}
  static FloraSeen fromJson(JSONObject j){FloraSeen f=new FloraSeen();if(j==null)return f;f.opening=cl(j.optDouble("opening"));f.growthPulse=cl(j.optDouble("growthPulse"));f.seenAt=j.optLong("seenAt");return f;}
 }
 public final Map<String,FloraSeen> flora=new LinkedHashMap<>();public final Map<String,Long> creatureSeen=new LinkedHashMap<>();public final Map<String,Long> evidenceCooldown=new LinkedHashMap<>();public long lastObservedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{JSONObject f=new JSONObject();for(Map.Entry<String,FloraSeen>e:flora.entrySet())f.put(e.getKey(),e.getValue().toJson());j.put("flora",f);JSONObject c=new JSONObject();for(Map.Entry<String,Long>e:creatureSeen.entrySet())c.put(e.getKey(),e.getValue());j.put("creatureSeen",c);JSONObject cd=new JSONObject();for(Map.Entry<String,Long>e:evidenceCooldown.entrySet())cd.put(e.getKey(),e.getValue());j.put("evidenceCooldown",cd);j.put("lastObservedAt",lastObservedAt);}catch(Exception ignored){}return j;}
 public static EcologyObservationState fromJson(JSONObject j){EcologyObservationState s=new EcologyObservationState();if(j==null)return s;JSONObject f=j.optJSONObject("flora");if(f!=null){Iterator<String>it=f.keys();while(it.hasNext()){String k=it.next();s.flora.put(k,FloraSeen.fromJson(f.optJSONObject(k)));}}JSONObject c=j.optJSONObject("creatureSeen");if(c!=null){Iterator<String>it=c.keys();while(it.hasNext()){String k=it.next();s.creatureSeen.put(k,c.optLong(k));}}JSONObject cd=j.optJSONObject("evidenceCooldown");if(cd!=null){Iterator<String>it=cd.keys();while(it.hasNext()){String k=it.next();s.evidenceCooldown.put(k,cd.optLong(k));}}s.lastObservedAt=j.optLong("lastObservedAt");return s;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}