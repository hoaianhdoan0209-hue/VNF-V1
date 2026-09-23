package com.aicharacter.v3;
import org.json.JSONObject;
public final class MoodState { public double pleasantness,arousal,loneliness;
 public void absorb(double valence,double intensity){pleasantness=clamp(pleasantness+finite(valence)*.08*Math.max(0,finite(intensity)));arousal=clamp(arousal+Math.abs(finite(valence))*.04*Math.max(0,finite(intensity)));normalize();}
 public void decay(double factor){double f=Double.isFinite(factor)?Math.max(0,Math.min(1,factor)):1;pleasantness*=f;arousal*=f;loneliness*=f;normalize();}
 public void normalize(){pleasantness=clamp(pleasantness);arousal=clamp(arousal);loneliness=clamp(loneliness);}
 public String label(){normalize();if(loneliness>.45)return"lonely";if(pleasantness>.25)return"content";if(pleasantness<-.25)return"uneasy";return"settled";}
 public JSONObject toJson(){normalize();JSONObject j=new JSONObject();try{j.put("pleasantness",pleasantness);j.put("arousal",arousal);j.put("loneliness",loneliness);}catch(Exception ignored){}return j;}
 public static MoodState fromJson(JSONObject j){MoodState m=new MoodState();if(j!=null){m.pleasantness=j.optDouble("pleasantness");m.arousal=j.optDouble("arousal");m.loneliness=j.optDouble("loneliness");}m.normalize();return m;}
 private static double finite(double v){return Double.isFinite(v)?v:0;}private static double clamp(double v){return Double.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
}
