package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/**
 * Persistent divine ecology. This is System-Reality state and is deliberately
 * separate from Haru cognition, memory, personality, relationship and plans.
 */
public final class DivineEcologyState {
 public final Map<String,MinorGodState> minorGods=new LinkedHashMap<>();
 public final Map<String,WorshipBondState> bonds=new LinkedHashMap<>();
 public long lastAdvancedAt;

 public String bondKey(String godId,String speciesKey,String areaId){
  return safe(godId)+"@"+safe(speciesKey)+"@"+safe(areaId);
 }
 public WorshipBondState bond(String godId,String speciesKey,String areaId){
  String key=bondKey(godId,speciesKey,areaId);
  WorshipBondState b=bonds.get(key);
  if(b==null){b=new WorshipBondState();b.godId=safe(godId);b.speciesKey=safe(speciesKey);b.areaId=safe(areaId);bonds.put(key,b);}
  return b;
 }
 public List<WorshipBondState> bondsForGod(String godId){
  ArrayList<WorshipBondState> out=new ArrayList<>();
  for(WorshipBondState b:bonds.values())if(b!=null&&safe(godId).equals(b.godId))out.add(b);
  return out;
 }
 public JSONObject toJson(){
  JSONObject j=new JSONObject();try{
   JSONObject gods=new JSONObject();for(Map.Entry<String,MinorGodState>e:minorGods.entrySet())gods.put(e.getKey(),e.getValue().toJson());j.put("minorGods",gods);
   JSONObject ws=new JSONObject();for(Map.Entry<String,WorshipBondState>e:bonds.entrySet())ws.put(e.getKey(),e.getValue().toJson());j.put("bonds",ws);
   j.put("lastAdvancedAt",lastAdvancedAt);
  }catch(Exception ignored){}
  return j;
 }
 public static DivineEcologyState fromJson(JSONObject j){
  DivineEcologyState s=new DivineEcologyState();if(j==null)return s;
  JSONObject gods=j.optJSONObject("minorGods");if(gods!=null){Iterator<String>it=gods.keys();while(it.hasNext()){String k=it.next();MinorGodState g=MinorGodState.fromJson(gods.optJSONObject(k));if(g.id.isEmpty())g.id=k;s.minorGods.put(k,g);}}
  JSONObject ws=j.optJSONObject("bonds");if(ws!=null){Iterator<String>it=ws.keys();while(it.hasNext()){String k=it.next();WorshipBondState b=WorshipBondState.fromJson(ws.optJSONObject(k));s.bonds.put(k,b);}}
  s.lastAdvancedAt=Math.max(0,j.optLong("lastAdvancedAt"));
  return s;
 }
 private static String safe(String x){return x==null?"":x;}
}

final class MinorGodState {
 public String id="",title="Thần Con";
 public double graceReserve=.18,maturity=.08;
 public int followersChosen,totalOfferings;
 public long awakenedAt,lastSelectionAt,lastBlessingAt,lastOfferingAt;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("id",id);j.put("title",title);j.put("graceReserve",unit(graceReserve));j.put("maturity",unit(maturity));
  j.put("followersChosen",Math.max(0,followersChosen));j.put("totalOfferings",Math.max(0,totalOfferings));
  j.put("awakenedAt",Math.max(0,awakenedAt));j.put("lastSelectionAt",Math.max(0,lastSelectionAt));j.put("lastBlessingAt",Math.max(0,lastBlessingAt));j.put("lastOfferingAt",Math.max(0,lastOfferingAt));
 }catch(Exception ignored){}return j;}
 public static MinorGodState fromJson(JSONObject j){MinorGodState g=new MinorGodState();if(j==null)return g;
  g.id=j.optString("id","");g.title=j.optString("title","Thần Con");g.graceReserve=unit(j.optDouble("graceReserve",.18));g.maturity=unit(j.optDouble("maturity",.08));
  g.followersChosen=Math.max(0,j.optInt("followersChosen"));g.totalOfferings=Math.max(0,j.optInt("totalOfferings"));
  g.awakenedAt=Math.max(0,j.optLong("awakenedAt"));g.lastSelectionAt=Math.max(0,j.optLong("lastSelectionAt"));g.lastBlessingAt=Math.max(0,j.optLong("lastBlessingAt"));g.lastOfferingAt=Math.max(0,j.optLong("lastOfferingAt"));return g;}
 private static double unit(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}

final class WorshipBondState {
 public String godId="",speciesKey="",areaId="";
 public double devotion=.08,blessingPower=0,offeringAffinity=0,cumulativePopulationCost=0;
 public long selectedAt,lastBlessedAt,lastOfferingAt;
 public int totalOfferings;

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("godId",godId);j.put("speciesKey",speciesKey);j.put("areaId",areaId);j.put("devotion",unit(devotion));j.put("blessingPower",unit(blessingPower));j.put("offeringAffinity",unit(offeringAffinity));j.put("cumulativePopulationCost",Math.max(0,finite(cumulativePopulationCost)));
  j.put("selectedAt",Math.max(0,selectedAt));j.put("lastBlessedAt",Math.max(0,lastBlessedAt));j.put("lastOfferingAt",Math.max(0,lastOfferingAt));j.put("totalOfferings",Math.max(0,totalOfferings));
 }catch(Exception ignored){}return j;}
 public static WorshipBondState fromJson(JSONObject j){WorshipBondState b=new WorshipBondState();if(j==null)return b;
  b.godId=j.optString("godId","");b.speciesKey=j.optString("speciesKey","");b.areaId=j.optString("areaId","");b.devotion=unit(j.optDouble("devotion",.08));b.blessingPower=unit(j.optDouble("blessingPower",0));b.offeringAffinity=unit(j.optDouble("offeringAffinity",0));b.cumulativePopulationCost=Math.max(0,finite(j.optDouble("cumulativePopulationCost",0)));
  b.selectedAt=Math.max(0,j.optLong("selectedAt"));b.lastBlessedAt=Math.max(0,j.optLong("lastBlessedAt"));b.lastOfferingAt=Math.max(0,j.optLong("lastOfferingAt"));b.totalOfferings=Math.max(0,j.optInt("totalOfferings"));return b;}
 private static double finite(double v){return Double.isFinite(v)?v:0;} private static double unit(double v){return Math.max(0,Math.min(1,finite(v)));}
}
