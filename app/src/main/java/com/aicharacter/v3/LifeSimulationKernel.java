package com.aicharacter.v3;
import java.util.Calendar;

/**
 * V3 foundation: one causal slice contract shared by active and offline life.
 *
 * Active/offline may choose different step sizes and cognition cadence, but the
 * physical world, body rhythm, perception and learning phases must pass through
 * this kernel in the same order.
 */
public final class LifeSimulationKernel {
 public enum Mode { ACTIVE, OFFLINE }
 private LifeSimulationKernel(){}

 public static void beginSlice(WorldState s,double seconds,long now,Mode mode){
  if(s==null||seconds<=0)return;
  syncClock(s,now);
  AtmosphereEvolutionEngine.advance(s,now);
  RespirationEngine.advance(s,now);
  ThermalEngine.advance(s,now);
  BodyInstinctEngine.advance(s,now);
  HaruAutonomyEngine.advanceMindBody(s,seconds,now);
  BodyPerceptionEngine.observe(s,now);
 }

 public static void endSlice(WorldState s,double seconds,long now,boolean advanceGirlTravel,boolean advanceCatTravel,Mode mode){
  if(s==null||seconds<=0)return;
  double minutes=seconds/60.0;
  boolean socialCatTravel=CatSocialEngine.tick(s,now);
  if(mode==Mode.OFFLINE)PhysicalLifeStepEngine.advanceOffline(s,seconds,now,advanceGirlTravel,advanceCatTravel||socialCatTravel);else PhysicalLifeStepEngine.advance(s,seconds,now,advanceGirlTravel,advanceCatTravel||socialCatTravel);
  BodyPerceptionEngine.observe(s,now);
  EnvironmentConsequences.advance(s,minutes);
  CreatureLifeEngine.advance(s,minutes,now);
  DivineWorshipEngine.advance(s,minutes,now);
  EcologyObservationLearningEngine.observe(s,now);
  HaruNamingEngine.observe(s,now);
  HaruConceptEngine.observe(s,now);
  HaruAbstractConceptEngine.observe(s,now);
  BodyPerceptionEngine.observe(s,now);
  HaruProactiveSpeechEngine.advance(s,now,mode);
 }

 public static void syncClock(WorldState s,long timestamp){
  if(s==null||s.environment==null)return;
  Calendar c=Calendar.getInstance();
  c.setTimeInMillis(timestamp);
  s.worldMinutes=c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE)+c.get(Calendar.SECOND)/60.0;
  s.environment.updateForTime(s.worldMinutes);
 }
}
