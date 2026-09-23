package com.aicharacter.v3;

import java.util.*;

/**
 * Transient reward/arousal modulation.
 *
 * Moderate dopamine supports motivation. Strong phasic stimulation increases reward salience,
 * compresses reflective weighting and raises instinct/impulse bias. Evidence truth is untouched.
 */
public final class DopamineModulationEngine {
 private DopamineModulationEngine(){}

 public static void pulse(WorldState s,double intensity,String source,long now){
  if(s==null)return;ensure(s);NeuroModulationState n=s.neuroModulation;double x=finite01(intensity);
  n.dopaminePhasic=cl(n.dopaminePhasic+x*(.58-.28*n.dopaminePhasic));n.lastPulseAt=Math.max(n.lastPulseAt,now);n.lastPulseSource=source==null?"":source;recompute(s,n);
 }

 public static void onExperience(WorldState s,MemoryEntry m,Experience e){
  if(s==null||m==null)return;double positive=Math.max(0,m.valence),sig=Math.max(0,Math.min(1,m.importance));double novelty=(m.hasTag("novelty")||m.hasTag("curiosity")||m.hasTag("success"))?.16:0;
  double x=positive*.62+sig*.18+novelty;if(x>=.10)pulse(s,Math.min(1,x),"experience:"+m.kind,m.time);
 }

 public static void advance(WorldState s,double seconds,long now){
  if(s==null||seconds<=0)return;ensure(s);NeuroModulationState n=s.neuroModulation;
  double dt=Math.min(seconds,6*3600.0),phasicDecay=Math.exp(-dt/210.0),tonicTarget=.40;
  n.dopaminePhasic=cl(n.dopaminePhasic*phasicDecay);n.dopamineTonic=cl(n.dopamineTonic+(tonicTarget-n.dopamineTonic)*(1-Math.exp(-dt/2400.0)));n.lastUpdatedAt=Math.max(n.lastUpdatedAt,now);recompute(s,n);
 }

 public static double logicalControl(WorldState s){ensure(s);return s.neuroModulation.logicalControl();}
 public static double overdrive(WorldState s){ensure(s);return s.neuroModulation.overdrive();}
 public static double instinctBias(WorldState s){ensure(s);return s.neuroModulation.instinctBias;}

 public static void applyDecisionBias(WorldState s,List<LifeDecision> candidates,long now){
  if(s==null||candidates==null||candidates.isEmpty())return;ensure(s);NeuroModulationState n=s.neuroModulation;double over=n.overdrive();if(over<=.001)return;NeedState needs=NeedState.evaluate(s);
  for(LifeDecision d:candidates){
   String id=d.intention.id;double drive=instinctDrive(s,needs,id),immediate=immediateReward(id);
   if(immediate>0)d.reason("dopamine_reward_salience",over*n.rewardSalience*immediate*7.5);
   if(drive>0)d.reason("dopamine_instinct_bias",over*n.instinctBias*drive*8.5);
   double noise=decisionNoise(id,now)*over*n.executiveNoise*3.6;d.reason("dopamine_executive_noise",noise);
   d.intention.utility=sum(d.reasons)+(id.equals(s.currentIntention)?5:0);
  }
 }

 public static double reflectiveWeight(WorldState s){return logicalControl(s);}
 public static double predictionConfidenceBias(WorldState s){return Math.min(.09,overdrive(s)*.09);}

 private static double instinctDrive(WorldState s,NeedState n,String id){
  if("eat".equals(id))return cl(n.hunger/100.0);if("drink".equals(id))return cl(n.thirst/100.0);if("toilet".equals(id))return cl(n.elimination/100.0);
  if("find_cat".equals(id))return cl(Math.max(n.connection/100.0,s.mood==null?0:s.mood.loneliness));
  if("explore_garden".equals(id)||"observe_lake".equals(id)||"watch_reedling".equals(id)||"study_ecology".equals(id))return cl(Math.max(n.curiosity/100.0,s.emotion==null?0:s.emotion.curiosity));
  if("seek_shelter".equals(id)||"recover".equals(id)||"sleep".equals(id))return cl(Math.max(n.safety,n.rest)/100.0);
  return 0;
 }
 private static double immediateReward(String id){if(id==null)return 0;if("eat".equals(id)||"drink".equals(id)||"toilet".equals(id)||"find_cat".equals(id))return1();if("explore_garden".equals(id)||"observe_lake".equals(id)||"watch_reedling".equals(id))return.72;if("quiet_pause".equals(id)||"reflect".equals(id))return.22;return 0;}
 private static double decisionNoise(String id,long now){long bucket=now/12000L,z=bucket*2862933555777941757L+(id==null?0:id.hashCode()*7046029254386353131L);z^=z>>>29;return((Math.abs(z)%2001)/1000.0)-1.0;}
 private static double sum(Map<String,Double>m){double v=0;for(double x:m.values())if(Double.isFinite(x))v+=x;return v;}
 private static void recompute(WorldState s,NeuroModulationState n){double d=n.dopamine(),over=n.overdrive(),ar=s.mood==null?0:cl(Math.abs(s.mood.arousal));n.rewardSalience=cl(.22+d*.58+over*.18);n.executiveNoise=cl(over*(.46+.42*ar));n.instinctBias=cl(over*(.58+.32*ar));}
 private static void ensure(WorldState s){if(s.neuroModulation==null)s.neuroModulation=new NeuroModulationState();}
 private static double finite01(double v){return Double.isFinite(v)?cl(v):0;}private static double cl(double v){return Math.max(0,Math.min(1,v));}private static double return1(){return 1.0;}
}
