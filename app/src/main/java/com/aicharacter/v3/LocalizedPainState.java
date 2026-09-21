package com.aicharacter.v3;import org.json.JSONObject;
/** Regional nociceptive load. New saves separate neck/chest/abdomen; legacy core is read/written as a compatibility mirror only. */
public final class LocalizedPainState{
 public double leftLeg,rightLeg,leftArm,rightArm,head,neck,chest,abdomen;@Deprecated public double core;public long lastUpdatedAt;
 public double coreMirror(){return Math.max(chest,abdomen);}public void syncLegacyCore(){core=coreMirror();}
 public JSONObject toJson(){syncLegacyCore();JSONObject j=new JSONObject();try{j.put("leftLeg",leftLeg);j.put("rightLeg",rightLeg);j.put("leftArm",leftArm);j.put("rightArm",rightArm);j.put("head",head);j.put("neck",neck);j.put("chest",chest);j.put("abdomen",abdomen);j.put("core",coreMirror());j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static LocalizedPainState fromJson(JSONObject j){LocalizedPainState p=new LocalizedPainState();if(j==null)return p;p.leftLeg=c(j.optDouble("leftLeg"));p.rightLeg=c(j.optDouble("rightLeg"));p.leftArm=c(j.optDouble("leftArm"));p.rightArm=c(j.optDouble("rightArm"));p.head=c(j.optDouble("head"));double legacy=c(j.optDouble("core"));p.core=legacy;p.neck=c(j.optDouble("neck"));p.chest=j.has("chest")?c(j.optDouble("chest")):legacy;p.abdomen=j.has("abdomen")?c(j.optDouble("abdomen")):legacy;p.lastUpdatedAt=j.optLong("lastUpdatedAt");p.syncLegacyCore();return p;}
 public double maxLoad(){return Math.max(Math.max(leftLeg,rightLeg),Math.max(Math.max(leftArm,rightArm),Math.max(head,Math.max(neck,Math.max(chest,abdomen)))));}
 private static double c(double v){return Math.max(0,Math.min(1,v));}
}
