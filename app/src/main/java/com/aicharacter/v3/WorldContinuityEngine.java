package com.aicharacter.v3;

/**
 * Advances persistent System Reality independently from the Activity lifecycle.
 * The Activity is only a window into this state; opening the app is not what
 * creates or starts the world.
 */
public final class WorldContinuityEngine {
 private WorldContinuityEngine(){}

 public static String advanceBackground(WorldState s,long now){
  return advance(s,now,false);
 }

 public static String advanceForPlayerOpen(WorldState s,long now){
  return advance(s,now,true);
 }

 private static String advance(WorldState s,long now,boolean playerPresent){
  if(s==null)return "";
  StateInvariantChecker.normalize(s,now);
  DivineMaintenanceEngine.maintain(s,now);
  LifeCycleEngine.apply(s,now);
  AgeDevelopmentEngine.apply(s,now);
  StateInvariantChecker.repairOrReport(s,now);
  if(!s.catState.awake)CatOfflineEngine.followAttachment(s);

  String trace=playerPresent
    ? OfflineLifeEngine.reconstruct(s,now)
    : OfflineLifeEngine.reconstructBackground(s,now);

  StateInvariantChecker.repairOrReport(s,now);
  GirlCatSearchEngine.advance(s,now);
  CatOfflineEngine.followAttachment(s);
  if(playerPresent)CatOfflineEngine.wakeForPlayer(s,now);
  return trace==null?"":trace;
 }
}
