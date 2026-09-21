package com.aicharacter.v3;
import org.json.JSONObject;

/**
 * V3 slow personality state.
 *
 * Traits are long-horizon biases learned from repeated lived evidence. They are
 * not commands, goals or action selectors. Established traits accumulate
 * contradiction pressure before moving strongly in the opposite direction, so
 * one dramatic event cannot instantly rewrite Haru.
 */
public final class PersonalityState {
 public double curiosity=.52,caution=.48,sociability=.45,independence=.55,patience=.5;
 public int curiosityEvidence=0,cautionEvidence=0,sociabilityEvidence=0,independenceEvidence=0,patienceEvidence=0;
 public double curiosityOpposition=0,cautionOpposition=0,sociabilityOpposition=0,independenceOpposition=0,patienceOpposition=0;

 public void slowlyLearn(String tag,double direction,double significance){
  if(tag==null||direction==0||!Double.isFinite(direction)||!Double.isFinite(significance))return;
  double sig=Math.max(.10,Math.min(1.50,significance)),dir=Math.signum(direction);
  Update u;
  if("explore".equals(tag)){u=learn(curiosity,curiosityEvidence,curiosityOpposition,dir,sig);curiosity=u.value;curiosityEvidence=u.evidence;curiosityOpposition=u.opposition;}
  else if("danger".equals(tag)){u=learn(caution,cautionEvidence,cautionOpposition,dir,sig);caution=u.value;cautionEvidence=u.evidence;cautionOpposition=u.opposition;}
  else if("social".equals(tag)){u=learn(sociability,sociabilityEvidence,sociabilityOpposition,dir,sig);sociability=u.value;sociabilityEvidence=u.evidence;sociabilityOpposition=u.opposition;}
  else if("alone".equals(tag)){u=learn(independence,independenceEvidence,independenceOpposition,dir,sig);independence=u.value;independenceEvidence=u.evidence;independenceOpposition=u.opposition;}
  else if("wait".equals(tag)){u=learn(patience,patienceEvidence,patienceOpposition,dir,sig);patience=u.value;patienceEvidence=u.evidence;patienceOpposition=u.opposition;}
 }

 public void normalize(){
  curiosity=clamp(curiosity);caution=clamp(caution);sociability=clamp(sociability);
  independence=clamp(independence);patience=clamp(patience);
  curiosityEvidence=count(curiosityEvidence);cautionEvidence=count(cautionEvidence);sociabilityEvidence=count(sociabilityEvidence);
  independenceEvidence=count(independenceEvidence);patienceEvidence=count(patienceEvidence);
  curiosityOpposition=pressure(curiosityOpposition);cautionOpposition=pressure(cautionOpposition);sociabilityOpposition=pressure(sociabilityOpposition);
  independenceOpposition=pressure(independenceOpposition);patienceOpposition=pressure(patienceOpposition);
 }

 private static Update learn(double current,int evidence,double opposition,double direction,double significance){
  current=clamp(current);evidence=count(evidence);opposition=pressure(opposition);
  double orientation=Math.abs(current-.5)<.035?0:Math.signum(current-.5);
  boolean contrary=orientation!=0&&direction!=orientation;
  int priorEvidence=evidence;
  evidence=count(evidence+1);
  double effective=significance;
  if(contrary){
   double maturity=Math.min(1,priorEvidence/45.0);
   opposition=pressure(opposition+significance*(.55+.45*maturity));
   double threshold=2.0+Math.min(4.0,priorEvidence*.045);
   if(opposition<threshold)effective*=.18;
   else{
    effective*=.72;
    opposition=pressure(opposition-threshold*.62);
   }
  }else{
   opposition=pressure(opposition-significance*.72);
  }
  return new Update(step(current,direction,effective),evidence,opposition);
 }

 private static double step(double current,double direction,double significance){
  double room=direction>0?1-current:current;
  double plasticity=.30+.70*room;
  return clamp(current+direction*.0032*significance*plasticity);
 }

 public JSONObject toJson(){JSONObject j=new JSONObject();try{
  j.put("curiosity",curiosity);j.put("caution",caution);j.put("sociability",sociability);j.put("independence",independence);j.put("patience",patience);
  j.put("curiosityEvidence",curiosityEvidence);j.put("cautionEvidence",cautionEvidence);j.put("sociabilityEvidence",sociabilityEvidence);j.put("independenceEvidence",independenceEvidence);j.put("patienceEvidence",patienceEvidence);
  j.put("curiosityOpposition",curiosityOpposition);j.put("cautionOpposition",cautionOpposition);j.put("sociabilityOpposition",sociabilityOpposition);j.put("independenceOpposition",independenceOpposition);j.put("patienceOpposition",patienceOpposition);
 }catch(Exception ignored){}return j;}

 public static PersonalityState fromJson(JSONObject j){PersonalityState p=new PersonalityState();if(j!=null){
  p.curiosity=j.optDouble("curiosity",p.curiosity);p.caution=j.optDouble("caution",p.caution);p.sociability=j.optDouble("sociability",p.sociability);p.independence=j.optDouble("independence",p.independence);p.patience=j.optDouble("patience",p.patience);
  p.curiosityEvidence=j.optInt("curiosityEvidence",0);p.cautionEvidence=j.optInt("cautionEvidence",0);p.sociabilityEvidence=j.optInt("sociabilityEvidence",0);p.independenceEvidence=j.optInt("independenceEvidence",0);p.patienceEvidence=j.optInt("patienceEvidence",0);
  p.curiosityOpposition=j.optDouble("curiosityOpposition",0);p.cautionOpposition=j.optDouble("cautionOpposition",0);p.sociabilityOpposition=j.optDouble("sociabilityOpposition",0);p.independenceOpposition=j.optDouble("independenceOpposition",0);p.patienceOpposition=j.optDouble("patienceOpposition",0);
 }p.normalize();return p;}

 private static final class Update{final double value,opposition;final int evidence;Update(double v,int e,double o){value=v;evidence=e;opposition=o;}}
 private static int count(int v){return Math.max(0,Math.min(10000,v));}
 private static double pressure(double v){return Double.isFinite(v)?Math.max(0,Math.min(12,v)):0;}
 private static double clamp(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):.5;}
}
