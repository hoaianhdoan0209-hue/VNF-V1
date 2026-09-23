package com.aicharacter.v3;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public final class VisualRegressionTest{
 private static Path appRoot(){
  Path cwd=Paths.get("").toAbsolutePath().normalize();
  if(Files.isDirectory(cwd.resolve("src/main")))return cwd;
  Path app=cwd.resolve("app");
  if(Files.isDirectory(app.resolve("src/main")))return app;
  throw new AssertionError("Cannot locate app module from "+cwd);
 }
 private static String read(Path p)throws Exception{return new String(Files.readAllBytes(p),StandardCharsets.UTF_8);}

 @Test public void foregroundParallaxCannotExposeBlankRightEdge(){
  float world=2400f;
  for(float viewport:new float[]{600f,900f,1200f,1800f,2399f}){
   float camera=world-viewport;
   float shift=VisualRegressionContract.layerShift(camera,1.06f,viewport);
   assertTrue(world-shift>=viewport);
  }
  assertEquals(0f,VisualRegressionContract.layerShift(Float.NaN,1.06f,900f),0f);
  assertEquals(0f,VisualRegressionContract.layerShift(400f,Float.POSITIVE_INFINITY,900f),0f);
 }

 @Test public void fourBiomesKeepFiveAuthoredLayersAndForeground()throws Exception{
  Path app=appRoot();
  String manifest=read(app.resolve("src/main/assets/visual/asset_manifest.json"));
  String renderer=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  for(String area:new String[]{"home","garden","lakeside","grove"}){
   for(String layer:new String[]{"sky","distant","mid","ground","foreground"}){
    String id=area+"_"+layer;
    assertTrue("missing "+id,Files.isRegularFile(app.resolve("src/main/res/drawable-nodpi/"+id+".png")));
    assertTrue("manifest lost "+id,manifest.contains("\"id\": \""+id+"\""));
   }
  }
  assertTrue(renderer.contains("drawForegroundLayer(c,area+\"_foreground\",cam)"));
  assertTrue(renderer.contains("VisualRegressionContract.layerShift(cam,parallax,viewportWorldW)"));
 }

 @Test public void haruUsesBiologyOutputWithoutAxisSquash()throws Exception{
  String renderer=read(appRoot().resolve("src/main/java/com/aicharacter/v3/HaruVisualRenderer.java"));
  assertTrue(renderer.contains("BiologyVisualOutput.from(s)"));
  assertTrue(renderer.contains("fw*sc"));
  assertTrue(renderer.contains("fh*sc"));
  assertFalse(renderer.contains("scaleX"));
  assertFalse(renderer.contains("scaleY"));
 }

 @Test public void closeCameraKeepsEmotionFaceAndMicroPostureInRenderer()throws Exception{
  String renderer=read(appRoot().resolve("src/main/java/com/aicharacter/v3/HaruVisualRenderer.java"));
  String gameView=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  assertTrue(renderer.contains("HaruFacialOverlayRenderer.draw"));
  assertTrue(renderer.contains("HaruExpressionEngine.derive"));
  assertTrue(renderer.contains("expression.headDropPx"));
  assertTrue(renderer.contains("expression.headTiltDeg"));
  assertTrue(gameView.contains("EmotionCameraDirector.Frame"));
  assertTrue(gameView.contains("EmotionalCameraProjection.worldScale"));
  assertTrue(gameView.contains("emotionShot="));
 }

 @Test public void legacyFullFrameLightRastersNeverOverlayWorld()throws Exception{
  String renderer=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  int from=renderer.indexOf("private void drawTimeTint");
  int to=renderer.indexOf("private void drawUi",from);
  assertTrue(from>=0&&to>from);
  String tint=renderer.substring(from,to);
  assertFalse(tint.contains("light_evening"));
  assertFalse(tint.contains("light_night"));
  assertFalse(tint.contains("home_warm_light"));
  assertTrue(tint.contains("new LinearGradient"));
 }
}
