package com.aicharacter.v3;

import org.json.JSONObject;

/** Persistent delivery state for Haru's autonomous speech. Speech causes come from simulation, not UI. */
public final class HaruProactiveSpeechState {
 public String pendingText="",pendingExpression="",pendingSourceEventId="",lastSourceEventId="";
 public long pendingCreatedAt=0,lastSpokenAt=0,deliveredAt=0;
 public int spokenCount=0;

 public void normalize(){
  if(pendingText==null)pendingText="";if(pendingExpression==null)pendingExpression="";if(pendingSourceEventId==null)pendingSourceEventId="";if(lastSourceEventId==null)lastSourceEventId="";
  if(pendingCreatedAt<0)pendingCreatedAt=0;if(lastSpokenAt<0)lastSpokenAt=0;if(deliveredAt<0)deliveredAt=0;if(spokenCount<0)spokenCount=0;
  if(pendingText.length()>320)pendingText=pendingText.substring(0,320);
  if(pendingExpression.length()>120)pendingExpression=pendingExpression.substring(0,120);
 }
 public boolean pending(){return pendingText!=null&&!pendingText.isEmpty()&&deliveredAt<pendingCreatedAt;}
 public JSONObject toJson(){normalize();JSONObject j=new JSONObject();try{j.put("pendingText",pendingText);j.put("pendingExpression",pendingExpression);j.put("pendingSourceEventId",pendingSourceEventId);j.put("lastSourceEventId",lastSourceEventId);j.put("pendingCreatedAt",pendingCreatedAt);j.put("lastSpokenAt",lastSpokenAt);j.put("deliveredAt",deliveredAt);j.put("spokenCount",spokenCount);}catch(Exception ignored){}return j;}
 public static HaruProactiveSpeechState fromJson(JSONObject j){HaruProactiveSpeechState s=new HaruProactiveSpeechState();if(j==null)return s;s.pendingText=j.optString("pendingText","");s.pendingExpression=j.optString("pendingExpression","");s.pendingSourceEventId=j.optString("pendingSourceEventId","");s.lastSourceEventId=j.optString("lastSourceEventId","");s.pendingCreatedAt=j.optLong("pendingCreatedAt",0);s.lastSpokenAt=j.optLong("lastSpokenAt",0);s.deliveredAt=j.optLong("deliveredAt",0);s.spokenCount=j.optInt("spokenCount",0);s.normalize();return s;}
}
