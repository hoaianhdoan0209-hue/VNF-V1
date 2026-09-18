package com.aicharacter.v3;
import org.json.JSONObject;
/** One linked quadruped body: fore/hind contacts share one trunk and center of mass. */
public final class CatRigState{
 public double gaitPhase,trunkLean,spineFlex,frontLeftContact=1,frontRightContact=1,hindLeftContact=1,hindRightContact=1,frontLoad=.5,hindLoad=.5,lastDistanceSample;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("gaitPhase",gaitPhase);j.put("trunkLean",trunkLean);j.put("spineFlex",spineFlex);j.put("frontLeftContact",frontLeftContact);j.put("frontRightContact",frontRightContact);j.put("hindLeftContact",hindLeftContact);j.put("hindRightContact",hindRightContact);j.put("frontLoad",frontLoad);j.put("hindLoad",hindLoad);j.put("lastDistanceSample",lastDistanceSample);}catch(Exception ignored){}return j;}
 public static CatRigState fromJson(JSONObject j){CatRigState c=new CatRigState();if(j==null)return c;c.gaitPhase=j.optDouble("gaitPhase");c.trunkLean=j.optDouble("trunkLean");c.spineFlex=j.optDouble("spineFlex");c.frontLeftContact=j.optDouble("frontLeftContact",1);c.frontRightContact=j.optDouble("frontRightContact",1);c.hindLeftContact=j.optDouble("hindLeftContact",1);c.hindRightContact=j.optDouble("hindRightContact",1);c.frontLoad=j.optDouble("frontLoad",.5);c.hindLoad=j.optDouble("hindLoad",.5);c.lastDistanceSample=Math.max(0,j.optDouble("lastDistanceSample"));return c;}
}