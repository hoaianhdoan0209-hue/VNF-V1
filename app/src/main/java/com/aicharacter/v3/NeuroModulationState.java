package com.aicharacter.v3;

import org.json.JSONObject;

/**
 * Persistent neuromodulation state.
 * Values are normalized gameplay signals, not literal neurotransmitter concentrations.
 * Dopamine changes salience/drive transiently; it never rewrites memories, beliefs or personality.
 */
public final class NeuroModulationState {
 public double dopamineTonic=.42,dopaminePhasic,rewardSalience=.36,executiveNoise,instinctBias;
 public long lastUpdatedAt,lastPulseAt;
 public String lastPulseSource="";

 public double dopamine(){return cl(dopamineTonic+dopaminePhasic*.62);}
 public double overdrive(){return cl((dopamine()-.62)/.38);}
 public double logicalControl(){
  double arousalPenalty=executiveNoise*.24;
  return Math.max(.45,Math.min(1,1-overdrive()*.42-arousalPenalty));
 }

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("dopamineTonic",finite(dopamineTonic,.42));j.put("dopaminePhasic",finite(dopaminePhasic,0));j.put("rewardSalience",finite(rewardSalience,.36));
  j.put("executiveNoise",finite(executiveNoise,0));j.put("instinctBias",finite(instinctBias,0));j.put("lastUpdatedAt",Math.max(0,lastUpdatedAt));j.put("lastPulseAt",Math.max(0,lastPulseAt));j.put("lastPulseSource",lastPulseSource==null?"":lastPulseSource);
 }catch(Exception ignored){}return j;}

 public static NeuroModulationState fromJson(JSONObject j){NeuroModulationState n=new NeuroModulationState();if(j==null)return n;
  n.dopamineTonic=cl(finite(j.optDouble("dopamineTonic",.42),.42));n.dopaminePhasic=cl(finite(j.optDouble("dopaminePhasic",0),0));n.rewardSalience=cl(finite(j.optDouble("rewardSalience",.36),.36));
  n.executiveNoise=cl(finite(j.optDouble("executiveNoise",0),0));n.instinctBias=cl(finite(j.optDouble("instinctBias",0),0));n.lastUpdatedAt=Math.max(0,j.optLong("lastUpdatedAt"));n.lastPulseAt=Math.max(0,j.optLong("lastPulseAt"));n.lastPulseSource=j.optString("lastPulseSource","");
  return n;
 }
 private static double finite(double v,double fallback){return Double.isFinite(v)?v:fallback;}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
