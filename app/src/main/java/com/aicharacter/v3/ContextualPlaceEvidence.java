package com.aicharacter.v3;
import org.json.JSONObject;
/** Repeated context-specific evidence. A single cold/rain/breathing episode is not a timeless place belief. */
public final class ContextualPlaceEvidence{
 public String key="";public int samples;public double accumulated;public long firstAt,lastAt;
 public void observe(double strength,long now){if(samples==0)firstAt=now;samples++;accumulated+=Math.max(0,Math.min(1,strength));lastAt=now;}
 public double confidence(){if(samples<3)return 0;double mean=accumulated/Math.max(1,samples),diversity=Math.min(1,(lastAt-firstAt)/(6.0*3600000.0));return Math.min(.9,mean*(.45+.35*diversity)+Math.min(.2,samples*.025));}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("key",key);j.put("samples",samples);j.put("accumulated",accumulated);j.put("firstAt",firstAt);j.put("lastAt",lastAt);}catch(Exception ignored){}return j;}
 public static ContextualPlaceEvidence fromJson(String key,JSONObject j){ContextualPlaceEvidence e=new ContextualPlaceEvidence();e.key=key;if(j!=null){e.samples=j.optInt("samples");e.accumulated=j.optDouble("accumulated");e.firstAt=j.optLong("firstAt");e.lastAt=j.optLong("lastAt");}return e;}
}