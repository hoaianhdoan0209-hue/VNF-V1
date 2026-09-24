package com.aicharacter.v3;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Advances the world while no Activity is visible. This is intentionally
 * network-free: Haru/world autonomy must not depend on an online service.
 */
public final class WorldHeartbeatWorker extends Worker {
 private static final String TAG="VNF-WorldHeartbeat";
 public WorldHeartbeatWorker(@NonNull Context appContext,@NonNull WorkerParameters params){
  super(appContext,params);
 }

 @NonNull @Override public Result doWork(){
  if(WorldRuntimePresence.isForeground())return Result.success();
  try{
   WorldRepository repository=new WorldRepository(getApplicationContext());
   WorldState s=repository.loadOrCreate();
   long now=System.currentTimeMillis();
   long before=s.lastSimulatedAt;
   WorldContinuityEngine.advanceBackground(s,now);
   if(WorldRuntimePresence.isForeground()){
    Log.i(TAG,"foreground resumed during heartbeat; leaving visible world authoritative");
    return Result.success();
   }
   repository.save(s);
   Log.i(TAG,"advanced world from="+before+" to="+s.lastSimulatedAt);
   return Result.success();
  }catch(Throwable e){
   Log.e(TAG,"persistent world heartbeat failed",e);
   return getRunAttemptCount()<3?Result.retry():Result.failure();
  }
 }
}
