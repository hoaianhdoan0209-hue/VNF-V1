package com.aicharacter.v3;
import org.json.JSONException; import org.json.JSONObject;
public final class EmotionState { public double joy,sadness,anger,fear,curiosity,loneliness,calm=0.55;
 public void decay(double amount){joy*=amount;sadness*=amount;anger*=amount;fear*=amount;curiosity*=amount;loneliness*=amount;calm=Math.min(1,calm+(1-amount)*0.08);}
 public String dominant(){double m=calm;String n="calm";double[]v={joy,sadness,anger,fear,curiosity,loneliness};String[]s={"joyful","sad","angry","afraid","curious","lonely"};for(int i=0;i<v.length;i++)if(v[i]>m){m=v[i];n=s[i];}return n;}
 public JSONObject toJson()throws JSONException{JSONObject j=new JSONObject();j.put("joy",joy);j.put("sadness",sadness);j.put("anger",anger);j.put("fear",fear);j.put("curiosity",curiosity);j.put("loneliness",loneliness);j.put("calm",calm);return j;}
 public static EmotionState fromJson(JSONObject j){EmotionState e=new EmotionState();if(j==null)return e;e.joy=j.optDouble("joy");e.sadness=j.optDouble("sadness");e.anger=j.optDouble("anger");e.fear=j.optDouble("fear");e.curiosity=j.optDouble("curiosity");e.loneliness=j.optDouble("loneliness");e.calm=j.optDouble("calm",.55);return e;}
}
