package com.aicharacter.v3;

import org.json.JSONObject;

/** Power reserve is fueled by accepted offerings; it is separate from divine knowledge/intelligence. */
public final class DivinePowerState {
 public double reserve=0,lifetimeYield=0,reservedForGifts=0;
 public int acceptedOfferings=0,refusedOfferings=0;
 public long lastOfferingAt=0;

 public double available(){clamp();return Math.max(0,reserve-reservedForGifts);}
 public void clamp(){reserve=range(reserve,0,0,100);lifetimeYield=range(lifetimeYield,0,0,100000);reservedForGifts=range(reservedForGifts,0,0,reserve);if(acceptedOfferings<0)acceptedOfferings=0;if(refusedOfferings<0)refusedOfferings=0;if(lastOfferingAt<0)lastOfferingAt=0;}
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{j.put("reserve",reserve);j.put("lifetimeYield",lifetimeYield);j.put("reservedForGifts",reservedForGifts);j.put("acceptedOfferings",acceptedOfferings);j.put("refusedOfferings",refusedOfferings);j.put("lastOfferingAt",lastOfferingAt);}catch(Exception ignored){}return j;}
 public static DivinePowerState fromJson(JSONObject j){DivinePowerState s=new DivinePowerState();if(j==null)return s;s.reserve=j.optDouble("reserve",0);s.lifetimeYield=j.optDouble("lifetimeYield",0);s.reservedForGifts=j.optDouble("reservedForGifts",0);s.acceptedOfferings=j.optInt("acceptedOfferings");s.refusedOfferings=j.optInt("refusedOfferings");s.lastOfferingAt=j.optLong("lastOfferingAt");s.clamp();return s;}
 private static double range(double v,double f,double lo,double hi){if(!Double.isFinite(v))v=f;return Math.max(lo,Math.min(hi,v));}
}
