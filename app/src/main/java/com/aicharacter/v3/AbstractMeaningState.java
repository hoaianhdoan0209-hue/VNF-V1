package com.aicharacter.v3;
import org.json.*;import java.util.*;
/** Haru's learned abstract meanings, grounded only in repeated subjective before/after experience. */
public final class AbstractMeaningState{
 public static final class Meaning{
  public double confidence;public int support,contradictions;public String lastEvidence="";public long lastUpdatedAt,lastMemoryAt;
  JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("confidence",confidence);j.put("support",support);j.put("contradictions",contradictions);j.put("lastEvidence",lastEvidence);j.put("lastUpdatedAt",lastUpdatedAt);j.put("lastMemoryAt",lastMemoryAt);}catch(Exception ignored){}return j;}
  static Meaning fromJson(JSONObject j){Meaning m=new Meaning();if(j==null)return m;m.confidence=cl(j.optDouble("confidence"));m.support=Math.max(0,j.optInt("support"));m.contradictions=Math.max(0,j.optInt("contradictions"));m.lastEvidence=j.optString("lastEvidence","");m.lastUpdatedAt=j.optLong("lastUpdatedAt");m.lastMemoryAt=j.optLong("lastMemoryAt");return m;}
 }
 public static final class Anchor{
  public String key="";public int observations;public long lastSeenAt;public final Map<String,Meaning> meanings=new LinkedHashMap<>();
  public Meaning meaning(String id){return meanings.computeIfAbsent(id,k->new Meaning());}
  JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("key",key);j.put("observations",observations);j.put("lastSeenAt",lastSeenAt);JSONObject m=new JSONObject();for(Map.Entry<String,Meaning>e:meanings.entrySet())m.put(e.getKey(),e.getValue().toJson());j.put("meanings",m);}catch(Exception ignored){}return j;}
  static Anchor fromJson(JSONObject j){Anchor a=new Anchor();if(j==null)return a;a.key=j.optString("key","");a.observations=Math.max(0,j.optInt("observations"));a.lastSeenAt=j.optLong("lastSeenAt");JSONObject m=j.optJSONObject("meanings");if(m!=null){Iterator<String>it=m.keys();while(it.hasNext()){String k=it.next();a.meanings.put(k,Meaning.fromJson(m.optJSONObject(k)));}}return a;}
 }
 public static final class Snapshot{
  public long at;public String placeKey="",targetKey="",intention="";public double safety,discomfort,hunger,thirst,rest,pain,stress,fear,curiosity,energy,thermal,wetness;
  JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("at",at);j.put("placeKey",placeKey);j.put("targetKey",targetKey);j.put("intention",intention);j.put("safety",safety);j.put("discomfort",discomfort);j.put("hunger",hunger);j.put("thirst",thirst);j.put("rest",rest);j.put("pain",pain);j.put("stress",stress);j.put("fear",fear);j.put("curiosity",curiosity);j.put("energy",energy);j.put("thermal",thermal);j.put("wetness",wetness);}catch(Exception ignored){}return j;}
  static Snapshot fromJson(JSONObject j){Snapshot s=new Snapshot();if(j==null)return s;s.at=j.optLong("at");s.placeKey=j.optString("placeKey","");s.targetKey=j.optString("targetKey","");s.intention=j.optString("intention","");s.safety=j.optDouble("safety");s.discomfort=j.optDouble("discomfort");s.hunger=j.optDouble("hunger");s.thirst=j.optDouble("thirst");s.rest=j.optDouble("rest");s.pain=j.optDouble("pain");s.stress=j.optDouble("stress");s.fear=j.optDouble("fear");s.curiosity=j.optDouble("curiosity");s.energy=j.optDouble("energy");s.thermal=j.optDouble("thermal");s.wetness=j.optDouble("wetness");return s;}
 }
 public final Map<String,Anchor> anchors=new LinkedHashMap<>();public final Map<String,Long> evidenceCooldown=new LinkedHashMap<>();public Snapshot last=new Snapshot();public long lastObservedAt;
 public Anchor anchor(String key){Anchor a=anchors.computeIfAbsent(key,k->new Anchor());if(a.key.isEmpty())a.key=key;return a;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{JSONObject a=new JSONObject();for(Map.Entry<String,Anchor>e:anchors.entrySet())a.put(e.getKey(),e.getValue().toJson());j.put("anchors",a);JSONObject cd=new JSONObject();for(Map.Entry<String,Long>e:evidenceCooldown.entrySet())cd.put(e.getKey(),e.getValue());j.put("evidenceCooldown",cd);j.put("last",last.toJson());j.put("lastObservedAt",lastObservedAt);}catch(Exception ignored){}return j;}
 public static AbstractMeaningState fromJson(JSONObject j){AbstractMeaningState s=new AbstractMeaningState();if(j==null)return s;JSONObject a=j.optJSONObject("anchors");if(a!=null){Iterator<String>it=a.keys();while(it.hasNext()){String k=it.next();Anchor x=Anchor.fromJson(a.optJSONObject(k));if(x.key.isEmpty())x.key=k;s.anchors.put(k,x);}}JSONObject cd=j.optJSONObject("evidenceCooldown");if(cd!=null){Iterator<String>it=cd.keys();while(it.hasNext()){String k=it.next();s.evidenceCooldown.put(k,cd.optLong(k));}}s.last=Snapshot.fromJson(j.optJSONObject("last"));s.lastObservedAt=j.optLong("lastObservedAt");return s;}
 public static double cl(double v){return Math.max(0,Math.min(1,v));}
}