package com.aicharacter.v3;
import org.json.JSONObject;
public final class MoodState { public double pleasantness,arousal,loneliness;
 public void absorb(double valence,double intensity){pleasantness=clamp(pleasantness+valence*.08*intensity);arousal=clamp(arousal+Math.abs(valence)*.04*intensity);}
 public void decay(double factor){pleasantness*=factor;arousal*=factor;loneliness*=factor;}
 public String label(){if(loneliness>.45)return"lonely";if(pleasantness>.25)return"content";if(pleasantness<-.25)return"uneasy";return"settled";}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("pleasantness",pleasantness);j.put("arousal",arousal);j.put("loneliness",loneliness);}catch(Exception ignored){}return j;}
 public static MoodState fromJson(JSONObject j){MoodState m=new MoodState();if(j!=null){m.pleasantness=j.optDouble("pleasantness");m.arousal=j.optDouble("arousal");m.loneliness=j.optDouble("loneliness");}return m;}
 private static double clamp(double v){return Math.max(-1,Math.min(1,v));}
}
