package com.aicharacter.v3;
import org.json.JSONObject;

/**
 * V3 slow personality state.
 *
 * Traits are long-horizon biases learned from repeated lived evidence. They are
 * not commands, goals or action selectors. Near an extreme, plasticity falls so
 * a handful of events cannot instantly rewrite who Haru has become.
 */
public final class PersonalityState {
 public double curiosity=.52,caution=.48,sociability=.45,independence=.55,patience=.5;

 public void slowlyLearn(String tag,double direction,double significance){
  if(tag==null||direction==0||!Double.isFinite(direction)||!Double.isFinite(significance))return;
  double sig=Math.max(.10,Math.min(1.50,significance)),dir=Math.signum(direction);
  if("explore".equals(tag))curiosity=learn(curiosity,dir,sig);
  if("danger".equals(tag))caution=learn(caution,dir,sig);
  if("social".equals(tag))sociability=learn(sociability,dir,sig);
  if("alone".equals(tag))independence=learn(independence,dir,sig);
  if("wait".equals(tag))patience=learn(patience,dir,sig);
 }

 public void normalize(){
  curiosity=clamp(curiosity);caution=clamp(caution);sociability=clamp(sociability);
  independence=clamp(independence);patience=clamp(patience);
 }

 private static double learn(double current,double direction,double significance){
  current=clamp(current);
  double room=direction>0?1-current:current;
  // Slow by design: one strong memory nudges a trait by only a few thousandths.
  double plasticity=.30+.70*room;
  return clamp(current+direction*.0032*significance*plasticity);
 }

 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("curiosity",curiosity);j.put("caution",caution);j.put("sociability",sociability);j.put("independence",independence);j.put("patience",patience);}catch(Exception ignored){}return j;}
 public static PersonalityState fromJson(JSONObject j){PersonalityState p=new PersonalityState();if(j!=null){p.curiosity=j.optDouble("curiosity",p.curiosity);p.caution=j.optDouble("caution",p.caution);p.sociability=j.optDouble("sociability",p.sociability);p.independence=j.optDouble("independence",p.independence);p.patience=j.optDouble("patience",p.patience);}p.normalize();return p;}
 private static double clamp(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):.5;}
}
