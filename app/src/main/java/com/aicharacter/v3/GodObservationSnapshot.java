package com.aicharacter.v3;

import org.json.*;
import java.lang.reflect.Field;
import java.util.*;

/** Qualitative, read-only world summary for Thần. It intentionally avoids exposing raw Haru mind scores. */
public final class GodObservationSnapshot {
 private GodObservationSnapshot(){}

 public static JSONObject build(WorldState s){
  JSONObject root=new JSONObject();if(s==null)return root;
  try{
   JSONObject env=new JSONObject();env.put("weather",s.environment==null?"UNKNOWN":s.environment.weather);env.put("dayPhase",s.environment==null?"UNKNOWN":s.environment.dayPhase(s.worldMinutes));env.put("wind",band(s.environment==null?0:s.environment.wind));env.put("cloudCover",band(s.environment==null?0:s.environment.cloudCover));env.put("wetness",band(s.worldWetness));env.put("visibility",band(s.visibility));root.put("environment",env);
   JSONObject at=new JSONObject();if(s.atmosphere!=null){at.put("temperature",temperatureBand(s.atmosphere.temperatureC));at.put("humidity",humidityBand(s.atmosphere.relativeHumidity));at.put("pressure",pressureBand(s.atmosphere.pressureKPa));at.put("airQuality",band(s.atmosphere.airQuality));}root.put("atmosphere",at);

   JSONObject life=new JSONObject();life.put("trackedFlora",s.livingWorld==null?0:s.livingWorld.flora.size());life.put("trackedFauna",s.livingWorld==null?0:s.livingWorld.creatures.size());life.put("biomeFields",s.livingWorld==null?0:s.livingWorld.fields.size());life.put("populations",populationSummary(s.livingWorld));root.put("livingWorld",life);

   JSONArray events=new JSONArray();int start=Math.max(0,s.worldHistory.size()-12);for(int i=start;i<s.worldHistory.size();i++){WorldHistoryEntry e=s.worldHistory.get(i);JSONObject j=new JSONObject();j.put("type",e.type);j.put("summary",e.summary);j.put("timestamp",e.time);events.put(j);}root.put("recentCausalHistory",events);

   JSONObject haru=new JSONObject();WorldArea area=s.world==null?null:s.world.areaAt(s.haruX);haru.put("area",area==null?"unknown":area.id);haru.put("activity",safe(s.haruActivity));haru.put("energyPresentation",energyBand(s.body==null?50:s.body.energy));haru.put("painPresentation",painBand(s.body==null?0:s.body.pain));haru.put("sleepPressure",sleepBand(s.body==null?0:s.body.sleepiness));haru.put("breathing",breathingBand(s.respiration));root.put("haruVisibleCondition",haru);

   JSONObject plan=new JSONObject();PlanState p=s.planState;if(p!=null){plan.put("active",p.active());plan.put("origin",p.origin);plan.put("goal",p.goal);plan.put("status",p.status);plan.put("currentStep",p.stepIndex);plan.put("stepCount",p.steps.size());plan.put("lastOutcome",p.lastOutcome);plan.put("lastReview",p.lastOutcomeReview);}root.put("currentPlanOutcome",plan);

   JSONObject divine=new JSONObject();if(s.divineOntology!=null){s.divineOntology.clamp();divine.put("ontology","KNOWN_TO_ALL_LIFE");divine.put("worshipSustains","COGNITION_AND_ACTIVE_PRESENCE");divine.put("currentWorship",band(s.divineOntology.currentWorship));divine.put("historicalWorship",band(s.divineOntology.accumulatedWorship));divine.put("evidenceBoundKnowledge",true);divine.put("learnedKnowledgePersists",true);divine.put("cognitiveCapacity",s.divineOntology.capacity.qualitativeJson());divine.put("trackedSpeciesDevotion",s.livingWorld==null?0:s.livingWorld.divineStates.size());}root.put("divineOntology",divine);
   root.put("abnormalities",abnormalities(s));
  }catch(Exception ignored){}
  return root;
 }

 private static JSONObject populationSummary(LivingWorldState living){
  JSONObject j=new JSONObject();try{
   if(living==null){j.put("available",false);j.put("reason","living world unavailable");return j;}
   Field f=living.getClass().getDeclaredField("populations");f.setAccessible(true);Object v=f.get(living);
   if(v instanceof Map){Map<?,?>m=(Map<?,?>)v;j.put("available",true);j.put("trackedPopulationFields",m.size());double total=0;int n=0;for(Object x:m.values()){try{Field a=x.getClass().getField("relativeAbundance");double d=a.getDouble(x);if(Double.isFinite(d)){total+=Math.max(0,Math.min(1,d));n++;}}catch(Exception ignored){}}j.put("aggregateAbundanceBand",band(n==0?0:total/n));}
   else{j.put("available",false);j.put("reason","population producer not connected");}
  }catch(Exception e){try{j.put("available",false);j.put("reason","population producer not connected on this integration base");}catch(Exception ignored){}}
  return j;
 }

 private static JSONArray abnormalities(WorldState s){JSONArray a=new JSONArray();if(!Float.isFinite(s.haruX)||!Float.isFinite(s.catX))a.put("non-finite actor position");if(s.atmosphere!=null&&(!Double.isFinite(s.atmosphere.temperatureC)||!Double.isFinite(s.atmosphere.relativeHumidity)||!Double.isFinite(s.atmosphere.pressureKPa)))a.put("non-finite atmosphere state");if(s.planState!=null&&s.planState.active()&&(s.planState.destination==null||s.planState.destination.isEmpty()))a.put("active plan has no destination");if(s.world==null)a.put("world definition unavailable");return a;}
 private static String safe(String x){return x==null?"":x;}
 private static String band(double v){if(!Double.isFinite(v))return"unknown";return v<.2?"very_low":v<.4?"low":v<.65?"moderate":v<.85?"high":"very_high";}
 private static String energyBand(double v){return v<20?"exhausted":v<40?"tired":v<70?"steady":"energetic";}
 private static String painBand(double v){return v<8?"none_visible":v<25?"mild":v<55?"guarded":"severe";}
 private static String sleepBand(double v){return v<25?"alert":v<55?"somewhat_sleepy":v<80?"sleepy":"very_sleepy";}
 private static String breathingBand(RespirationState r){if(r==null)return"unknown";double v=0;try{v=r.breathingLoad;}catch(Throwable ignored){}return band(v);}
 private static String temperatureBand(double c){if(!Double.isFinite(c))return"unknown";return c<8?"very_cold":c<18?"cool":c<29?"mild":c<37?"hot":"very_hot";}
 private static String humidityBand(double h){if(!Double.isFinite(h))return"unknown";return h<.25?"dry":h<.55?"comfortable":h<.8?"humid":"saturated";}
 private static String pressureBand(double p){if(!Double.isFinite(p))return"unknown";return p<85?"low":p>108?"high":"normal";}
}
