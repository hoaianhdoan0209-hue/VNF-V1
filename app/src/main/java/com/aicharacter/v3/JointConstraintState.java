package com.aicharacter.v3;
import org.json.JSONObject;
/** Effective human joint chain. Angles are normalized mechanical demands, bounded before rendering. */
public final class JointConstraintState{
 public double hip,knee,ankle,spine,shoulder,neck,limitStress;
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("hip",hip);j.put("knee",knee);j.put("ankle",ankle);j.put("spine",spine);j.put("shoulder",shoulder);j.put("neck",neck);j.put("limitStress",limitStress);}catch(Exception ignored){}return j;}
 public static JointConstraintState fromJson(JSONObject j){JointConstraintState x=new JointConstraintState();if(j==null)return x;x.hip=j.optDouble("hip");x.knee=j.optDouble("knee");x.ankle=j.optDouble("ankle");x.spine=j.optDouble("spine");x.shoulder=j.optDouble("shoulder");x.neck=j.optDouble("neck");x.limitStress=Math.max(0,Math.min(1,j.optDouble("limitStress")));return x;}
}