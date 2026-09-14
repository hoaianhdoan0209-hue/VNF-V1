package com.aicharacter.v3;
import org.json.JSONObject;
public final class PersonalityState {public double curiosity=.52,caution=.48,sociability=.45,independence=.55,patience=.5;
 public void slowlyLearn(String tag,double direction,double significance){double d=.004*Math.max(.1,significance)*Math.signum(direction);if("explore".equals(tag))curiosity=clamp(curiosity+d);if("danger".equals(tag))caution=clamp(caution+d);if("social".equals(tag))sociability=clamp(sociability+d);if("alone".equals(tag))independence=clamp(independence+d);if("wait".equals(tag))patience=clamp(patience+d);}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("curiosity",curiosity);j.put("caution",caution);j.put("sociability",sociability);j.put("independence",independence);j.put("patience",patience);}catch(Exception ignored){}return j;}
 public static PersonalityState fromJson(JSONObject j){PersonalityState p=new PersonalityState();if(j!=null){p.curiosity=j.optDouble("curiosity",p.curiosity);p.caution=j.optDouble("caution",p.caution);p.sociability=j.optDouble("sociability",p.sociability);p.independence=j.optDouble("independence",p.independence);p.patience=j.optDouble("patience",p.patience);}return p;}
 private static double clamp(double v){return Math.max(0,Math.min(1,v));}
}
