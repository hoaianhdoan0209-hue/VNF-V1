package com.aicharacter.v3;

import org.json.JSONObject;

/** Worship expands cognitive bandwidth; it never grants omniscience or rewrites evidence. */
public final class DivineCognitiveCapacityState {
 public double intelligenceLevel=.34,attentionBandwidth=.36,memoryIntegration=.34,learningRate=.32,referenceReach=.30,selfCheckStrength=.46,presenceStability=.40;
 public int hypothesisBreadth=2,reasoningDepth=2;

 public void recompute(double currentWorship,double accumulatedWorship){
  double current=unit(currentWorship,0),history=unit(accumulatedWorship,0);
  double level=unit(.20+.34*Math.sqrt(history)+.30*current,0);
  intelligenceLevel=level;
  attentionBandwidth=unit(.18+.76*level,0);
  memoryIntegration=unit(.20+.72*level,0);
  learningRate=unit(.16+.70*level,0);
  referenceReach=unit(.12+.78*level,0);
  selfCheckStrength=unit(.42+.50*level,0); // humility/self-check grows with intelligence.
  presenceStability=unit(.08+.90*current,0); // worship sustains active presence, not truth creation.
  hypothesisBreadth=1+(int)Math.floor(level*5.0);
  reasoningDepth=1+(int)Math.floor(level*6.0);
  clamp();
 }
 public int referenceBudget(){return Math.max(2,Math.min(10,2+(int)Math.floor(referenceReach*8)));}
 public void clamp(){
  intelligenceLevel=unit(intelligenceLevel,.34);attentionBandwidth=unit(attentionBandwidth,.36);memoryIntegration=unit(memoryIntegration,.34);learningRate=unit(learningRate,.32);referenceReach=unit(referenceReach,.30);selfCheckStrength=unit(selfCheckStrength,.46);presenceStability=unit(presenceStability,.40);
  hypothesisBreadth=Math.max(1,Math.min(6,hypothesisBreadth));reasoningDepth=Math.max(1,Math.min(7,reasoningDepth));
 }
 public JSONObject toJson(){clamp();JSONObject j=new JSONObject();try{
  j.put("intelligenceLevel",intelligenceLevel);j.put("attentionBandwidth",attentionBandwidth);j.put("memoryIntegration",memoryIntegration);j.put("learningRate",learningRate);j.put("referenceReach",referenceReach);j.put("selfCheckStrength",selfCheckStrength);j.put("presenceStability",presenceStability);j.put("hypothesisBreadth",hypothesisBreadth);j.put("reasoningDepth",reasoningDepth);
 }catch(Exception ignored){}return j;}
 public JSONObject qualitativeJson(){clamp();JSONObject j=new JSONObject();try{
  j.put("intelligence",band(intelligenceLevel));j.put("attention",band(attentionBandwidth));j.put("memoryIntegration",band(memoryIntegration));j.put("learning",band(learningRate));j.put("referenceReach",band(referenceReach));j.put("selfCheck",band(selfCheckStrength));j.put("presenceStability",band(presenceStability));j.put("hypothesisBreadth",hypothesisBreadth);j.put("reasoningDepth",reasoningDepth);
 }catch(Exception ignored){}return j;}
 public static DivineCognitiveCapacityState fromJson(JSONObject j){DivineCognitiveCapacityState s=new DivineCognitiveCapacityState();if(j==null)return s;s.intelligenceLevel=j.optDouble("intelligenceLevel",.34);s.attentionBandwidth=j.optDouble("attentionBandwidth",.36);s.memoryIntegration=j.optDouble("memoryIntegration",.34);s.learningRate=j.optDouble("learningRate",.32);s.referenceReach=j.optDouble("referenceReach",.30);s.selfCheckStrength=j.optDouble("selfCheckStrength",.46);s.presenceStability=j.optDouble("presenceStability",.40);s.hypothesisBreadth=j.optInt("hypothesisBreadth",2);s.reasoningDepth=j.optInt("reasoningDepth",2);s.clamp();return s;}
 private static double unit(double v,double f){if(!Double.isFinite(v))v=f;return Math.max(0,Math.min(1,v));}
 private static String band(double v){return v<.2?"very_low":v<.4?"low":v<.65?"moderate":v<.85?"high":"very_high";}
}
