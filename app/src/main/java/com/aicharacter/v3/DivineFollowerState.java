package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/** Individual worshipper/chosen state. Species is metadata only and is never a score input. */
public final class DivineFollowerState {
 public String entityId="",speciesKey="",areaId="",rank="WORSHIPPER",lastEvidenceEventId="";
 public double devotionSnapshot=.18,influence=0,reliability=.50,service=0,offeringCommitment=0,candidateScore=0;
 public boolean chosen=false;
 public long lastEvaluatedAt=0,chosenAt=0;
 public int giftCount=0;
 public final LinkedHashSet<String> evidenceEventIds=new LinkedHashSet<>();

 public void clamp(){
  if(entityId==null)entityId="";if(speciesKey==null)speciesKey="";if(areaId==null)areaId="";if(rank==null)rank="WORSHIPPER";if(lastEvidenceEventId==null)lastEvidenceEventId="";
  devotionSnapshot=unit(devotionSnapshot,.18);influence=unit(influence,0);reliability=unit(reliability,.50);service=unit(service,0);offeringCommitment=unit(offeringCommitment,0);candidateScore=unit(candidateScore,0);
  if(lastEvaluatedAt<0)lastEvaluatedAt=0;if(chosenAt<0)chosenAt=0;if(giftCount<0)giftCount=0;
  while(evidenceEventIds.size()>96){Iterator<String>it=evidenceEventIds.iterator();if(it.hasNext()){it.next();it.remove();}else break;}
 }
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{j.put("entityId",entityId);j.put("speciesKey",speciesKey);j.put("areaId",areaId);j.put("rank",rank);j.put("lastEvidenceEventId",lastEvidenceEventId);j.put("devotionSnapshot",devotionSnapshot);j.put("influence",influence);j.put("reliability",reliability);j.put("service",service);j.put("offeringCommitment",offeringCommitment);j.put("candidateScore",candidateScore);j.put("chosen",chosen);j.put("lastEvaluatedAt",lastEvaluatedAt);j.put("chosenAt",chosenAt);j.put("giftCount",giftCount);j.put("evidenceEventIds",new JSONArray(evidenceEventIds));}catch(Exception ignored){}return j;}
 public static DivineFollowerState fromJson(JSONObject j){DivineFollowerState s=new DivineFollowerState();if(j==null){s.clamp();return s;}s.entityId=j.optString("entityId","");s.speciesKey=j.optString("speciesKey","");s.areaId=j.optString("areaId","");s.rank=j.optString("rank","WORSHIPPER");s.lastEvidenceEventId=j.optString("lastEvidenceEventId","");s.devotionSnapshot=j.optDouble("devotionSnapshot",.18);s.influence=j.optDouble("influence",0);s.reliability=j.optDouble("reliability",.50);s.service=j.optDouble("service",0);s.offeringCommitment=j.optDouble("offeringCommitment",0);s.candidateScore=j.optDouble("candidateScore",0);s.chosen=j.optBoolean("chosen",false);s.lastEvaluatedAt=j.optLong("lastEvaluatedAt");s.chosenAt=j.optLong("chosenAt");s.giftCount=j.optInt("giftCount");JSONArray a=j.optJSONArray("evidenceEventIds");if(a!=null)for(int i=0;i<a.length();i++){String id=a.optString(i,"");if(!id.isEmpty())s.evidenceEventIds.add(id);}s.clamp();return s;}
 private static double unit(double v,double f){if(!Double.isFinite(v))v=f;return Math.max(0,Math.min(1,v));}
}
