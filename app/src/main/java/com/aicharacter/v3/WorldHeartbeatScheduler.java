package com.aicharacter.v3;

import android.content.Context;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

/**
 * Best-effort Android heartbeat. Android may defer work while the device sleeps,
 * so WorldContinuityEngine still performs a bounded timestamp reconcile later.
 */
public final class WorldHeartbeatScheduler {
 private static final String PERIODIC="vnf_persistent_world_heartbeat";
 private static final String SOON="vnf_persistent_world_heartbeat_soon";
 private WorldHeartbeatScheduler(){}

 public static void ensureScheduled(Context context){
  Context app=context.getApplicationContext();
  PeriodicWorkRequest periodic=new PeriodicWorkRequest.Builder(
      WorldHeartbeatWorker.class,15,TimeUnit.MINUTES,5,TimeUnit.MINUTES)
    .addTag(PERIODIC)
    .build();
  WorkManager.getInstance(app).enqueueUniquePeriodicWork(
    PERIODIC,ExistingPeriodicWorkPolicy.UPDATE,periodic);
 }

 public static void requestSoon(Context context){
  Context app=context.getApplicationContext();
  OneTimeWorkRequest work=new OneTimeWorkRequest.Builder(WorldHeartbeatWorker.class)
    .setInitialDelay(1,TimeUnit.MINUTES)
    .addTag(SOON)
    .build();
  WorkManager.getInstance(app).enqueueUniqueWork(SOON,ExistingWorkPolicy.REPLACE,work);
 }
}
