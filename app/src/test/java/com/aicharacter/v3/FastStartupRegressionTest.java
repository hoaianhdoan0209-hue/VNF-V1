package com.aicharacter.v3;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public final class FastStartupRegressionTest {
 private static Path appRoot(){
  Path cwd=Paths.get("").toAbsolutePath().normalize();
  if(Files.isDirectory(cwd.resolve("src/main")))return cwd;
  Path app=cwd.resolve("app");
  if(Files.isDirectory(app.resolve("src/main")))return app;
  throw new AssertionError("Cannot locate app module from "+cwd);
 }
 private static String read(Path p)throws Exception{return new String(Files.readAllBytes(p),StandardCharsets.UTF_8);}

 @Test public void coldStartShowsExistingWorldBeforeAnyReconciliation()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int create=main.indexOf("protected void onCreate(Bundle b)");
  int show=main.indexOf("showWorldPreview(r,preview)",create);
  int first=main.indexOf("public void onFirstWorldFrame()",show);
  assertTrue(create>=0&&show>create&&first>show);
  String coldPath=main.substring(create,show);
  assertTrue(coldPath.contains("WorldState preview=r.loadPreviewOrCreate()"));
  assertFalse("opening the app must not reconcile the world before first render",coldPath.contains("WorldContinuityEngine.advanceForPlayerOpen"));
  assertTrue(main.contains("new GameView(this,state,this,true)"));
  assertFalse(main.contains("new GameView(this,state,this,false)"));
  assertTrue(main.contains("STARTUP_FIRST_WORLD_FRAME"));
  assertTrue(main.substring(first).contains("refreshPersistentWorldAfterResume(System.currentTimeMillis())"));
 }

 @Test public void playerInteractionIsNeverBlockedByCatchupMessage()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  assertTrue(main.contains("private boolean worldReadyForInteraction(){return state!=null&&gameView!=null;}"));
  assertFalse(main.contains("Thế giới đang bắt kịp thời gian"));
  assertFalse(main.contains("gameView.setSimulationReady(false)"));
 }

 @Test public void appExitDoesNotForcePlayerCatToSleep()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int stop=main.indexOf("protected void onStop()");
  int destroy=main.indexOf("protected void onDestroy()",stop);
  assertTrue(stop>=0&&destroy>stop);
  String body=main.substring(stop,destroy);
  assertFalse(body.contains("CatOfflineEngine.beginSleep"));
  assertTrue(body.contains("WorldHeartbeatScheduler.requestSoon(this)"));

  String offline=read(appRoot().resolve("src/main/java/com/aicharacter/v3/OfflineLifeEngine.java"));
  assertFalse(offline.contains("s.catState.awake&&away>=2"));
 }

 @Test public void backgroundHeartbeatKeepsWorldAliveWhenAndroidAllows()throws Exception{
  String app=read(appRoot().resolve("src/main/java/com/aicharacter/v3/VnfApplication.java"));
  String scheduler=read(appRoot().resolve("src/main/java/com/aicharacter/v3/WorldHeartbeatScheduler.java"));
  String worker=read(appRoot().resolve("src/main/java/com/aicharacter/v3/WorldHeartbeatWorker.java"));
  assertTrue(app.contains("WorldHeartbeatScheduler.ensureScheduled(this)"));
  assertTrue(scheduler.contains("15,TimeUnit.MINUTES"));
  assertTrue(scheduler.contains("setInitialDelay(1,TimeUnit.MINUTES)"));
  assertTrue(worker.contains("WorldContinuityEngine.advanceBackground"));
  assertTrue(worker.contains("WorldRuntimePresence.isForeground()"));
 }

 @Test public void deepSleepFallbackIsStrictlyBounded()throws Exception{
  String offline=read(appRoot().resolve("src/main/java/com/aicharacter/v3/OfflineLifeEngine.java"));
  assertTrue(offline.contains("MAX_RECONSTRUCTION_STEPS=16"));
  assertTrue(offline.contains("minSliceMs=Math.max(60000L"));
  assertTrue(offline.contains("Math.max(requestedStep,minSliceMs)"));
  assertTrue(offline.contains("boundedSteps="));
 }

 @Test public void backgroundSimulationDoesNotPretendThePlayerOpenedTheGame()throws Exception{
  String offline=read(appRoot().resolve("src/main/java/com/aicharacter/v3/OfflineLifeEngine.java"));
  assertTrue(offline.contains("reconstructBackground"));
  assertTrue(offline.contains("if(playerPresent)s.lastOpenedAt=now"));
  assertTrue(offline.contains("if(playerPresent)s.reunionContext"));
 }

 @Test public void resumeRefreshNeverFreezesVisibleWorld()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int resume=main.indexOf("protected void onResume()");
  int pause=main.indexOf("protected void onPause()",resume);
  assertTrue(resume>=0&&pause>resume);
  String body=main.substring(resume,pause);
  assertTrue(body.contains("refreshPersistentWorldAfterResume(resumedAt)"));
  assertFalse(body.contains("setSimulationReady(false)"));
  assertTrue(main.contains("\"VNF-World-Refresh\""));
 }

 @Test public void heavyweightServicesStillWaitForFirstRenderedFrame()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int preview=main.indexOf("private void showWorldPreview");
  int first=main.indexOf("public void onFirstWorldFrame()",preview);
  int voice=main.indexOf("private void initializeDeferredVoice()",first);
  assertTrue(preview>=0&&first>preview&&voice>first);
  String previewBody=main.substring(preview,first);
  assertFalse(previewBody.contains("new VoiceController"));
  assertFalse(previewBody.contains("new ProceduralAudioEngine"));
  assertFalse(previewBody.contains("maybeCheckAppUpdate(true)"));
  String firstBody=main.substring(first,voice);
  assertTrue(firstBody.contains("initializeDeferredAudio"));
  assertTrue(firstBody.contains("initializeDeferredVoice"));
  assertTrue(firstBody.contains("maybeCheckAppUpdate(true)"));
  assertTrue(firstBody.contains("maybeHaruRequestCoreRuntimePermissions"));
 }

 @Test public void startupScreenDoesNotClaimToCreateOrWakeTheWorld()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  assertTrue(main.contains("t.setText(\"VNF\")"));
  assertFalse(main.contains("Đang đánh thức thế giới"));
 }

 @Test public void freshInstallDoesNotCreateAndValidateDiskSaveBeforeFirstFrame()throws Exception{
  String repo=read(appRoot().resolve("src/main/java/com/aicharacter/v3/WorldRepository.java"));
  int preview=repo.indexOf("public synchronized WorldState loadPreviewOrCreate()");
  int full=repo.indexOf("public synchronized WorldState loadOrCreate()",preview);
  assertTrue(preview>=0&&full>preview);
  String body=repo.substring(preview,full);
  assertTrue(body.contains("WorldState.fresh()"));
  assertFalse("fresh preview must not fall into full load/save recovery before rendering",body.contains("return loadOrCreate()"));
  assertFalse("fresh preview must not synchronously save before rendering",body.contains("save(state)"));
  assertTrue(repo.contains("commitPendingInitialSeed()"));

  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int create=main.indexOf("protected void onCreate(Bundle b)");
  int show=main.indexOf("showWorldPreview(r,preview)",create);
  assertTrue(create>=0&&show>create);
  assertFalse(main.substring(create,show).contains("commitPendingInitialSeed"));
  assertTrue(main.contains("r.commitPendingInitialSeed();"));
 }

 @Test public void visibleRendererNeverConsumesLongHistoricalBacklog()throws Exception{
  String view=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  assertTrue(view.contains("MAX_VISIBLE_CAUSAL_LAG_MS=2500L"));
  assertTrue(view.contains("historicalReconcilePending"));
  assertTrue(view.contains("if(historicalReconcilePending){activeBacklogMs=0;syncRealClock(wallNow);anim+=frameDt;return;}"));
  assertTrue(view.contains("MAX_ACTIVE_CATCHUP_STEPS=8"));
 }

 @Test public void persistentRefreshRetriesConflictsInsteadOfDroppingTimeline()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  assertTrue(main.contains("worldRefreshRetryCount<3"));
  assertTrue(main.contains("refreshPersistentWorldAfterResume(System.currentTimeMillis()),120L"));
  assertTrue(main.contains("RESUME_WORLD_REFRESH conflict=true"));
  assertTrue(main.contains("if(!isDebugVisualCapture())gameView.postDelayed(()->refreshPersistentWorldAfterResume(System.currentTimeMillis()),80L)"));
 }

 @Test public void v105UsesNextUpdaterVersion()throws Exception{
  String gradle=read(appRoot().resolve("build.gradle"));
  assertTrue(gradle.contains("versionCode 122"));
  assertTrue(gradle.contains("versionName '1.0.5'"));
  assertTrue(gradle.contains("androidx.work:work-runtime:2.10.0"));
 }
}
