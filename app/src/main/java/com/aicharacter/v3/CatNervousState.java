package com.aicharacter.v3;
import org.json.JSONObject;
/** Fast feline sensorimotor state. It constrains player intent before cognition or presentation. */
public final class CatNervousState{
 public double proprioceptiveError,balanceAlarm,motorCorrection,recoveryDrive,fallBrace;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("proprioceptiveError",proprioceptiveError);j.put("balanceAlarm",balanceAlarm);j.put("motorCorrection",motorCorrection);j.put("recoveryDrive",recoveryDrive);j.put("fallBrace",fallBrace);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static CatNervousState fromJson(JSONObject j){CatNervousState n=new CatNervousState();if(j==null)return n;n.proprioceptiveError=cl(j.optDouble("proprioceptiveError"));n.balanceAlarm=cl(j.optDouble("balanceAlarm"));n.motorCorrection=cl(j.optDouble("motorCorrection"));n.recoveryDrive=cl(j.optDouble("recoveryDrive"));n.fallBrace=cl(j.optDouble("fallBrace"));n.lastUpdatedAt=j.optLong("lastUpdatedAt");return n;}private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
