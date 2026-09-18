package com.aicharacter.v3;
import org.json.JSONObject;
/** Coarse whole-body mechanics. Coordinates are world-space; pose is derived from one linked body, never independent limbs. */
public final class PhysicsBodyState{
 public double massKg=50,velocityX,velocityY,centerOfMassY=.55,groundReaction,traction=1,balance=1;public boolean grounded=true;public long lastUpdatedAt;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("massKg",massKg);j.put("velocityX",velocityX);j.put("velocityY",velocityY);j.put("centerOfMassY",centerOfMassY);j.put("groundReaction",groundReaction);j.put("traction",traction);j.put("balance",balance);j.put("grounded",grounded);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static PhysicsBodyState fromJson(JSONObject j,double mass){PhysicsBodyState p=new PhysicsBodyState();p.massKg=mass;if(j==null)return p;p.massKg=Math.max(.5,j.optDouble("massKg",mass));p.velocityX=j.optDouble("velocityX");p.velocityY=j.optDouble("velocityY");p.centerOfMassY=j.optDouble("centerOfMassY",.55);p.groundReaction=Math.max(0,j.optDouble("groundReaction"));p.traction=Math.max(.05,Math.min(1,j.optDouble("traction",1)));p.balance=Math.max(0,Math.min(1,j.optDouble("balance",1)));p.grounded=j.optBoolean("grounded",true);p.lastUpdatedAt=j.optLong("lastUpdatedAt");return p;}
}