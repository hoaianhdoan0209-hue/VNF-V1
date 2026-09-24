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
  assertTrue(main.contains("WorldState preview=r.loadPreviewOrCreate()"));\n  assertTrue(main.contains("new GameView(this,state,this,false)"));
  assertTrue(main.contains("gameView.replaceState(live,true)"));
  assertTrue(main.contains("STARTUP_WORLD_VISIBLE"));
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

 @Test public void hotfixUsesNewUpdaterVersion()throws Exception{
  String gradle=read(appRoot().resolve("build.gradle"));
  assertTrue(gradle.contains("versionCode 119"));
  assertTrue(gradle.contains("versionName '1.0.2'"));
 }
}
