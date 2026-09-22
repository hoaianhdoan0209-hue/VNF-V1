package com.aicharacter.v3;
import org.json.JSONObject;
/** Whole-body mechanics shared by human/cat. Support and COM determine balance under gravity. */
public final class PhysicsBodyState{
 public double massKg=50,velocityX,velocityY,centerOfMassX,centerOfMassY=.55,groundClearanceM,groundReaction,traction=1,balance=1,supportLeft=-.18,supportRight=.18,landingInstability,slipVelocity,slipSeverity,fallBodyHeightM;public boolean grounded=true,falling=false;public long lastUpdatedAt;

 public void normalizeFinite(double defaultMass){
  double defaultCom=defaultMass<10?.24:.55;
  massKg=Math.max(.5,finiteOr(massKg,defaultMass));
  velocityX=finiteOr(velocityX,0);
  velocityY=finiteOr(velocityY,0);
  centerOfMassX=finiteOr(centerOfMassX,0);
  centerOfMassY=finiteOr(centerOfMassY,defaultCom);
  groundClearanceM=Math.max(0,finiteOr(groundClearanceM,0));
  groundReaction=Math.max(0,finiteOr(groundReaction,0));
  traction=clamp(finiteOr(traction,1),.05,1);
  balance=clamp(finiteOr(balance,1),0,1);
  supportLeft=finiteOr(supportLeft,-.18);
  supportRight=finiteOr(supportRight,.18);
  if(supportRight<=supportLeft){supportLeft=-.18;supportRight=.18;}
  landingInstability=clamp(finiteOr(landingInstability,0),0,1);
  slipVelocity=finiteOr(slipVelocity,0);
  slipSeverity=clamp(finiteOr(slipSeverity,0),0,1);
  fallBodyHeightM=Math.max(0,finiteOr(fallBodyHeightM,0));
  if(lastUpdatedAt<0)lastUpdatedAt=0;
  if(grounded){
   falling=false;
   groundClearanceM=0;
   velocityY=0;
  }
 }

 public JSONObject toJson(){return toJson(50);}
 public JSONObject toJson(double defaultMass){double safeDefault=Double.isFinite(defaultMass)&&defaultMass>=.5?defaultMass:50;normalizeFinite(safeDefault);JSONObject j=new JSONObject();try{j.put("massKg",massKg);j.put("velocityX",velocityX);j.put("velocityY",velocityY);j.put("centerOfMassX",centerOfMassX);j.put("centerOfMassY",centerOfMassY);j.put("groundClearanceM",groundClearanceM);j.put("groundReaction",groundReaction);j.put("traction",traction);j.put("balance",balance);j.put("supportLeft",supportLeft);j.put("supportRight",supportRight);j.put("landingInstability",landingInstability);j.put("slipVelocity",slipVelocity);j.put("slipSeverity",slipSeverity);j.put("fallBodyHeightM",fallBodyHeightM);j.put("grounded",grounded);j.put("falling",falling);j.put("lastUpdatedAt",lastUpdatedAt);}catch(Exception ignored){}return j;}
 public static PhysicsBodyState fromJson(JSONObject j,double mass){PhysicsBodyState p=new PhysicsBodyState();p.massKg=mass;if(j==null){p.normalizeFinite(mass);return p;}p.massKg=j.optDouble("massKg",mass);p.velocityX=j.optDouble("velocityX");p.velocityY=j.optDouble("velocityY");p.centerOfMassX=j.optDouble("centerOfMassX");double defaultCom=mass<10?.24:.55;p.centerOfMassY=j.optDouble("centerOfMassY",defaultCom);p.groundClearanceM=j.optDouble("groundClearanceM",0);p.groundReaction=j.optDouble("groundReaction");p.traction=j.optDouble("traction",1);p.balance=j.optDouble("balance",1);p.supportLeft=j.optDouble("supportLeft",-.18);p.supportRight=j.optDouble("supportRight",.18);p.landingInstability=j.optDouble("landingInstability",0);p.slipVelocity=j.optDouble("slipVelocity",0);p.slipSeverity=j.optDouble("slipSeverity",0);p.fallBodyHeightM=j.optDouble("fallBodyHeightM",0);p.grounded=j.optBoolean("grounded",true);p.falling=j.optBoolean("falling",false);p.lastUpdatedAt=j.optLong("lastUpdatedAt");p.normalizeFinite(mass);return p;}
 private static double finiteOr(double v,double fallback){return Double.isFinite(v)?v:fallback;}
 private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
}
