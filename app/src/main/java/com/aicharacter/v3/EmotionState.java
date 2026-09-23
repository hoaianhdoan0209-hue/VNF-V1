package com.aicharacter.v3;
import org.json.JSONException; import org.json.JSONObject;
public final class EmotionState { public double joy,sadness,anger,fear,curiosity,loneliness,calm=0.55;
 public void decay(double amount){double a=finite(amount,1);a=Math.max(0,Math.min(1,a));joy*=a;sadness*=a;anger*=a;fear*=a;curiosity*=a;loneliness*=a;calm=Math.min(1,calm+(1-a)*0.08);normalize();}
 public void normalize(){joy=c(joy);sadness=c(sadness);anger=c(anger);fear=c(fear);curiosity=c(curiosity);loneliness=c(loneliness);calm=c(calm);}
 public String dominant(){normalize();double m=calm;String n="calm";double[]v={joy,sadness,anger,fear,curiosity,loneliness};String[]s={"joyful","sad","angry","afraid","curious","lonely"};for(int i=0;i<v.length;i++)if(v[i]>m){m=v[i];n=s[i];}return n;}
 public JSONObject toJson()throws JSONException{normalize();JSONObject j=new JSONObject();j.put("joy",joy);j.put("sadness",sadness);j.put("anger",anger);j.put("fear",fear);j.put("curiosity",curiosity);j.put("loneliness",loneliness);j.put("calm",calm);return j;}
 public static EmotionState fromJson(JSONObject j){EmotionState e=new EmotionState();if(j==null)return e;e.joy=c(j.optDouble("joy"));e.sadness=c(j.optDouble("sadness"));e.anger=c(j.optDouble("anger"));e.fear=c(j.optDouble("fear"));e.curiosity=c(j.optDouble("curiosity"));e.loneliness=c(j.optDouble("loneliness"));e.calm=c(finite(j.optDouble("calm",.55),.55));e.normalize();return e;}
 private static double c(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}private static double finite(double v,double f){return Double.isFinite(v)?v:f;}
}
