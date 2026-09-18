package com.aicharacter.v3;
import org.json.JSONObject;
/** One linked human body pose. Values describe coupled whole-body mechanics, not separately animated limbs. */
public final class BodyRigState{
 public double pelvisTilt,spineLean,shoulderCounter,headStabilization,leftLegLoad=.5,rightLegLoad=.5,stridePhase,stanceWidth=.5;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("pelvisTilt",pelvisTilt);j.put("spineLean",spineLean);j.put("shoulderCounter",shoulderCounter);j.put("headStabilization",headStabilization);j.put("leftLegLoad",leftLegLoad);j.put("rightLegLoad",rightLegLoad);j.put("stridePhase",stridePhase);j.put("stanceWidth",stanceWidth);}catch(Exception ignored){}return j;}
 public static BodyRigState fromJson(JSONObject j){BodyRigState r=new BodyRigState();if(j==null)return r;r.pelvisTilt=j.optDouble("pelvisTilt");r.spineLean=j.optDouble("spineLean");r.shoulderCounter=j.optDouble("shoulderCounter");r.headStabilization=j.optDouble("headStabilization");r.leftLegLoad=j.optDouble("leftLegLoad",.5);r.rightLegLoad=j.optDouble("rightLegLoad",.5);r.stridePhase=j.optDouble("stridePhase");r.stanceWidth=j.optDouble("stanceWidth",.5);return r;}
}