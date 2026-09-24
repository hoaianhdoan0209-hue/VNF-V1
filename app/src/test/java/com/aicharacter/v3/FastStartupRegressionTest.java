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

 @Test public void coldStartShowsWorldBeforeOfflineReconstruction()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int onCreate=main.indexOf("protected void onCreate(Bundle b)");
  int preview=main.indexOf("showWorldPreview(r,preview)",onCreate);
  int catchupMethod=main.indexOf("private void completeCausalStartup");
  assertTrue(onCreate>=0&&preview>onCreate&&catchupMethod>preview);
  String onCreateBody=main.substring(onCreate,catchupMethod);
  assertFalse("cold start must not run offline reconstruction before first world",onCreateBody.contains("OfflineLifeEngine.reconstruct"));
  assertTrue(main.substring(catchupMethod).contains("OfflineLifeEngine.reconstruct(live,now)"));
  assertTrue(main.contains("WorldState preview=r.loadPreviewOrCreate()"));
  assertTrue(main.contains("new GameView(this,state,this,false)"));
  assertTrue(main.contains("gameView.replaceState(live,true)"));
  assertTrue(main.contains("STARTUP_WORLD_ATTACHED"));
  assertTrue(main.contains("STARTUP_FIRST_WORLD_FRAME"));
  assertTrue(main.contains("STARTUP_CAUSAL_READY"));
 }

 @Test public void frozenPreviewCannotAdvanceOldTimeline()throws Exception{
  String view=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  assertTrue(view.contains("private volatile boolean simulationReady=true"));
  assertTrue(view.contains("if(!simulationReady){activeBacklogMs=0;anim+=frameDt;return;}"));
  assertTrue(view.contains("public void replaceState(WorldState next,boolean ready)"));
 }



 @Test public void previewLoaderDoesNotPerformFullRecoveryBeforeFirstFrame()throws Exception{
  String repo=read(appRoot().resolve("src/main/java/com/aicharacter/v3/WorldRepository.java"));
  int preview=repo.indexOf("loadPreviewOrCreate()");
  int full=repo.indexOf("loadOrCreate()",preview);
  assertTrue(preview>=0&&full>preview);
  String body=repo.substring(preview,full);
  assertTrue(body.contains("tryLoad(saveFile)"));
  assertTrue(body.contains("tryLoad(backupFile)"));
  assertTrue(body.contains("tryLoad(tmpFile)"));
  assertFalse(body.contains("promotePending"));
  assertFalse(body.contains("repairOrReport"));
 }

 @Test public void resumeDoesNotReconstructOnUiThread()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int resume=main.indexOf("protected void onResume()");
  int pause=main.indexOf("protected void onPause()",resume);
  assertTrue(resume>=0&&pause>resume);
  String body=main.substring(resume,pause);
  assertTrue(body.contains("startResumeCausalCatchup(resumedAt)"));
  assertFalse(body.contains("OfflineLifeEngine.reconstruct"));
  assertTrue(main.contains("\"VNF-Resume-Catchup\""));
  assertTrue(main.contains("gameView.setSimulationReady(false)"));
 }

 @Test public void heavyweightStartupWorkWaitsForFirstRenderedWorldFrame()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  String view=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  int preview=main.indexOf("private void showWorldPreview");
  int first=main.indexOf("public void onFirstWorldFrame()",preview);
  int catchup=main.indexOf("private void completeCausalStartup",first);
  assertTrue(preview>=0&&first>preview&&catchup>first);
  String previewBody=main.substring(preview,first);
  assertFalse(previewBody.contains("new VoiceController"));
  assertFalse(previewBody.contains("new ProceduralAudioEngine"));
  assertFalse(previewBody.contains("GodSessionManager.warmup"));
  assertFalse(previewBody.contains("maybeCheckAppUpdate(true)"));
  assertFalse(previewBody.contains("VNF-World-Catchup"));
  String firstBody=main.substring(first,catchup);
  assertTrue(firstBody.contains("STARTUP_FIRST_WORLD_FRAME"));
  assertTrue(firstBody.contains("postDelayed(()->new Thread"));
  assertTrue(firstBody.contains("initializeDeferredAudio"));
  assertTrue(firstBody.contains("initializeDeferredVoice"));
  assertTrue(firstBody.contains("maybeCheckAppUpdate(true)"));
  assertFalse(firstBody.contains("GodSessionManager.warmup"));
  assertTrue(firstBody.contains("VNF-World-Catchup"));
  assertTrue(main.contains("private void initializeDeferredVoice()"));
  assertTrue(main.contains("private void initializeDeferredAudio()"));
  assertTrue(view.contains("void onFirstWorldFrame()"));
  assertTrue(view.contains("firstWorldFrameReported"));
  assertTrue(view.contains("host.onFirstWorldFrame()"));
  assertTrue(view.contains("if(firstWorldFrameReported)dispatchAudio(wall)"));
 }
 @Test public void permissionPromptIsScheduledFromFirstWorldFrameNotCatchupCompletion()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  int first=main.indexOf("public void onFirstWorldFrame()");
  int catchup=main.indexOf("private void completeCausalStartup",first);
  int finish=main.indexOf("private void finishCausalStartup",catchup);
  int resume=main.indexOf("private void startResumeCausalCatchup",finish);
  assertTrue(first>=0&&catchup>first&&finish>catchup&&resume>finish);
  String firstBody=main.substring(first,catchup);
  assertTrue(firstBody.contains("postDelayed(this::maybeHaruRequestCoreRuntimePermissions,1200L)"));
  String deferred=main.substring(catchup,resume);
  assertFalse(deferred.contains("postDelayed(this::maybeHaruRequestCoreRuntimePermissions,1200L)"));
 }

 @Test public void proactiveTtsChecksLocaleSupportAndFallsBack()throws Exception{
  String voice=read(appRoot().resolve("src/main/java/com/aicharacter/v3/VoiceController.java"));
  assertTrue(voice.contains("TextToSpeech.LANG_MISSING_DATA"));
  assertTrue(voice.contains("TextToSpeech.LANG_NOT_SUPPORTED"));
  assertTrue(voice.contains("Locale.getDefault()"));
  assertTrue(voice.contains("ttsReady=lang!="));
 }

 @Test public void hotfixUsesNewUpdaterVersion()throws Exception{
  String gradle=read(appRoot().resolve("build.gradle"));
  assertTrue(gradle.contains("versionCode 119"));
  assertTrue(gradle.contains("versionName '1.0.2'"));
 }
}
