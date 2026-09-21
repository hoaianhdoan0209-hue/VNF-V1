package com.aicharacter.v3;
import org.json.*;import java.util.*;
/** Haru's private vocabulary for unknown VNF life and phenomena. Names are personal labels, never System taxonomy. */
public final class PersonalLexiconState{
 public static final class Entry{
  public String key="",kind="",name="",candidate="",lastCue="";public int encounters,candidateVotes,renames;public double confidence,salience;public long firstSeenAt,lastSeenAt,namedAt,lastChangedAt;public final List<String> priorNames=new ArrayList<>();
  JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("key",key);j.put("kind",kind);j.put("name",name);j.put("candidate",candidate);j.put("lastCue",lastCue);j.put("encounters",encounters);j.put("candidateVotes",candidateVotes);j.put("renames",renames);j.put("confidence",confidence);j.put("salience",salience);j.put("firstSeenAt",firstSeenAt);j.put("lastSeenAt",lastSeenAt);j.put("namedAt",namedAt);j.put("lastChangedAt",lastChangedAt);j.put("priorNames",new JSONArray(priorNames));}catch(Exception ignored){}return j;}
  static Entry fromJson(JSONObject j){Entry e=new Entry();if(j==null)return e;e.key=j.optString("key","");e.kind=j.optString("kind","");e.name=j.optString("name","");e.candidate=j.optString("candidate","");e.lastCue=j.optString("lastCue","");e.encounters=Math.max(0,j.optInt("encounters"));e.candidateVotes=Math.max(0,j.optInt("candidateVotes"));e.renames=Math.max(0,j.optInt("renames"));e.confidence=cl(j.optDouble("confidence"));e.salience=cl(j.optDouble("salience"));e.firstSeenAt=j.optLong("firstSeenAt");e.lastSeenAt=j.optLong("lastSeenAt");e.namedAt=j.optLong("namedAt");e.lastChangedAt=j.optLong("lastChangedAt");JSONArray a=j.optJSONArray("priorNames");if(a!=null)for(int i=0;i<a.length();i++){String x=a.optString(i,"");if(!x.isEmpty())e.priorNames.add(x);}return e;}
  public boolean named(){return name!=null&&!name.isEmpty();}
  public String stage(){return!named()?"UNNAMED":confidence<.70?"TENTATIVE":"FAMILIAR";}
 }
 public final Map<String,Entry> entries=new LinkedHashMap<>();public long lastObservedAt;
 public Entry entry(String key,String kind){Entry e=entries.computeIfAbsent(key,k->new Entry());if(e.key.isEmpty())e.key=key;if(e.kind.isEmpty())e.kind=kind;return e;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{JSONObject e=new JSONObject();for(Map.Entry<String,Entry>x:entries.entrySet())e.put(x.getKey(),x.getValue().toJson());j.put("entries",e);j.put("lastObservedAt",lastObservedAt);}catch(Exception ignored){}return j;}
 public static PersonalLexiconState fromJson(JSONObject j){PersonalLexiconState s=new PersonalLexiconState();if(j==null)return s;JSONObject e=j.optJSONObject("entries");if(e!=null){Iterator<String>it=e.keys();while(it.hasNext()){String k=it.next();Entry x=Entry.fromJson(e.optJSONObject(k));if(x.key.isEmpty())x.key=k;s.entries.put(k,x);}}s.lastObservedAt=j.optLong("lastObservedAt");return s;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}