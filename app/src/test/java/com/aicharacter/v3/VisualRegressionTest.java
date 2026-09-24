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

 @Test public void divineAndHaruPolishStayWiredToCurrentVisualPipeline()throws Exception{
  Path app=appRoot();
  String divine=read(app.resolve("src/main/java/com/aicharacter/v3/DivineManifestationView.java"));
  String scene=read(app.resolve("src/main/java/com/aicharacter/v3/GodContactScene.java"));
  String haru=read(app.resolve("src/main/java/com/aicharacter/v3/HaruVisualRenderer.java"));
  String game=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  assertTrue(divine.contains("drawAtmosphericField"));
  assertTrue(divine.contains("drawDepthVeil"));
  assertTrue(divine.contains("drawPresenceCore"));
  assertTrue(divine.contains("drawConstellationThreads"));
  assertTrue(divine.contains("drawLightColumn"));
  assertTrue(divine.contains("drawFloorEcho"));
  assertTrue(divine.contains("drawHaloLattice"));
  assertTrue(divine.contains("drawOrbitBands"));
  assertTrue(divine.contains("drawPhaseAccent"));
  assertTrue(scene.contains("THẦN // VNF"));
  assertTrue(scene.contains("KÊNH HIỆN DIỆN"));
  assertTrue(haru.contains("drawCloseSubjectLight"));
  assertTrue(haru.contains("drawLayeredContactShadow"));
  assertTrue(haru.contains("drawClosePortraitGrade"));
  assertTrue(haru.contains("PorterDuff.Mode.SRC_IN"));
  assertTrue(haru.contains("cameraZoom"));
  assertTrue(game.contains("anim,visualZoom"));
  assertTrue(game.contains("HudIconRenderer.drawGodButton"));
  assertTrue(read(app.resolve("src/main/java/com/aicharacter/v3/HudIconRenderer.java")).contains("GodSessionManager.State"));
 }

 @Test public void divinePresenceLightingCouplesWorldAndHaruWithoutEnteringWorldState()throws Exception{
  Path app=appRoot();
  String game=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String haru=read(app.resolve("src/main/java/com/aicharacter/v3/HaruVisualRenderer.java"));
  String activity=read(app.resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  String world=read(app.resolve("src/main/java/com/aicharacter/v3/WorldState.java"));
  assertTrue(game.contains("setDivineVisualPresence"));
  assertTrue(game.contains("drawDivineWorldAmbient"));
  assertTrue(game.contains("drawDivineNearField"));
  assertTrue(game.contains("drawDivineScreenGrade"));
  assertTrue(game.contains("visualDivinePresence"));
  assertTrue(haru.contains("drawDivineSubjectLight"));
  assertTrue(haru.contains("divinePresence"));
  assertTrue(haru.contains("relativeHumidity"));
  assertTrue(activity.contains("setDivineVisualPresence(true)"));
  assertTrue(activity.contains("setDivineVisualPresence(false)"));
  assertFalse(world.contains("divineVisualPresence"));
  assertFalse(world.contains("visualDivinePresence"));
 }

 @Test public void catAndHaruAnimationStayInTheVisibleWorldPipeline()throws Exception{
  Path app=appRoot();
  String game=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String cat=read(app.resolve("src/main/java/com/aicharacter/v3/CatVisualRenderer.java"));
  String catController=read(app.resolve("src/main/java/com/aicharacter/v3/CatAnimationController.java"));
  String haru=read(app.resolve("src/main/java/com/aicharacter/v3/HaruVisualRenderer.java"));
  assertTrue(game.contains("drawCat(c,cam)"));
  assertTrue(game.contains("drawAttachedCat"));
  assertTrue(game.contains("CatVisualRenderer.draw"));
  assertTrue(game.contains("HARU_POSE_BLEND_MS"));
  assertTrue(game.contains("saveLayerAlpha"));
  assertTrue(cat.contains("drawTail"));
  assertTrue(cat.contains("drawLegs"));
  assertTrue(cat.contains("catRig"));
  assertTrue(cat.contains("breathingDrive"));
  assertTrue(catController.contains("State.APPROACH"));
  assertTrue(catController.contains("State.RETREAT"));
  assertTrue(catController.contains("State.SETTLE"));
  assertTrue(haru.contains("HaruMotionStyleEngine.derive"));
 }

 @Test public void momentDirectorAndSoundContractStayPresentationOnly()throws Exception{
  Path app=appRoot();
  String game=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String moment=read(app.resolve("src/main/java/com/aicharacter/v3/MomentDirector.java"));
  String sound=read(app.resolve("src/main/java/com/aicharacter/v3/SoundEvent.java"));
  String world=read(app.resolve("src/main/java/com/aicharacter/v3/WorldState.java"));
  assertTrue(game.contains("MomentDirector"));
  assertTrue(game.contains("drawMomentGrade"));
  assertTrue(game.contains("pollSoundEvent"));
  assertTrue(game.contains("moment.minZoom"));
  assertTrue(moment.contains("RECENT_EVENT_MS"));
  assertTrue(moment.contains("lastAcceptedEventTime"));
  assertTrue(moment.contains("CAT_SOCIAL_SETTLE_NEAR"));
  assertTrue(moment.contains("CAUSAL_EXPERIMENT_RESOLVED"));
  assertTrue(sound.contains("semanticId"));
  assertTrue(sound.contains("Layer"));
  assertFalse(world.contains("pendingSoundEvent"));
  assertFalse(world.contains("MomentDirector"));
 }

 @Test public void proceduralAudioRendererStaysOutsideSimulationPersistence()throws Exception{
  Path app=appRoot();
  String game=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String main=read(app.resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  String audio=read(app.resolve("src/main/java/com/aicharacter/v3/ProceduralAudioEngine.java"));
  String emitter=read(app.resolve("src/main/java/com/aicharacter/v3/AudioSceneEmitter.java"));
  String world=read(app.resolve("src/main/java/com/aicharacter/v3/WorldState.java"));
  assertTrue(game.contains("onAudioScene"));
  assertTrue(game.contains("onSoundEvent"));
  assertTrue(game.contains("dispatchAudio"));
  assertTrue(main.contains("audio.resume()"));
  assertTrue(main.contains("audio.pause()"));
  assertTrue(main.contains("audio.destroy()"));
  assertTrue(audio.contains("AudioTrack"));
  assertTrue(audio.contains("social_reunion"));
  assertTrue(audio.contains("divine_presence_pulse"));
  assertTrue(emitter.contains("BiologyVisualOutput.from"));
  assertFalse(world.contains("ProceduralAudioEngine"));
  assertFalse(world.contains("AudioSceneFrame"));
  assertFalse(world.contains("SoundEvent"));
 }

 @Test public void audioSurfaceAndMicroInteractionStayPresentationOnly()throws Exception{
  Path app=appRoot();
  String emitter=read(app.resolve("src/main/java/com/aicharacter/v3/AudioSceneEmitter.java"));
  String audio=read(app.resolve("src/main/java/com/aicharacter/v3/ProceduralAudioEngine.java"));
  String cat=read(app.resolve("src/main/java/com/aicharacter/v3/CatAnimationController.java"));
  String haru=read(app.resolve("src/main/java/com/aicharacter/v3/GirlAnimationController.java"));
  String micro=read(app.resolve("src/main/java/com/aicharacter/v3/MicroInteractionDirector.java"));
  String world=read(app.resolve("src/main/java/com/aicharacter/v3/WorldState.java"));
  assertTrue(emitter.contains("WorldSemantics.exposure"));
  assertTrue(emitter.contains("SurfaceAcoustics.surfaceAt"));
  assertTrue(emitter.contains("catPurr"));
  assertTrue(audio.contains("weatherExposure"));
  assertTrue(audio.contains("surfaceProfile"));
  assertTrue(audio.contains("catPurr"));
  assertTrue(cat.contains("State.RUB"));
  assertTrue(haru.contains("MicroInteractionDirector"));
  assertTrue(micro.contains("CAT_SOCIAL_RESPONSE_COMPLETED"));
  assertTrue(micro.contains("CAT_SOCIAL_SETTLE_NEAR"));
  assertFalse(world.contains("MicroInteractionDirector"));
  assertFalse(world.contains("catPurr"));
  assertFalse(world.contains("weatherExposure"));
 }

 @Test public void HaruCanSpeakFirstAndPermissionsAreRequestedAtStartup()throws Exception{
  Path app=appRoot();
  String game=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String main=read(app.resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  String voice=read(app.resolve("src/main/java/com/aicharacter/v3/VoiceController.java"));
  String kernel=read(app.resolve("src/main/java/com/aicharacter/v3/LifeSimulationKernel.java"));
  String proactive=read(app.resolve("src/main/java/com/aicharacter/v3/HaruProactiveSpeechEngine.java"));
  String world=read(app.resolve("src/main/java/com/aicharacter/v3/WorldState.java"));
  assertTrue(kernel.contains("HaruProactiveSpeechEngine.advance"));
  assertTrue(proactive.contains("Mode.ACTIVE"));
  assertTrue(proactive.contains("CAT_SOCIAL_SETTLE_NEAR"));
  assertTrue(proactive.contains("intentionCandidate"));
  assertTrue(game.contains("HaruProactiveSpeechEngine.consume"));
  assertTrue(game.contains("drawProactiveSpeech"));
  assertTrue(game.contains("onHaruProactiveSpeech"));
  assertTrue(main.contains("voice.speakProactive"));
  assertTrue(voice.contains("speakProactive"));
  assertTrue(voice.contains("ttsReady"));
  assertTrue(main.contains("requestCoreRuntimePermissions"));
  assertTrue(main.contains("Manifest.permission.RECORD_AUDIO"));
  assertTrue(main.contains("Manifest.permission.POST_NOTIFICATIONS"));
  assertTrue(main.contains("postDelayed(this::requestCoreRuntimePermissions"));
  assertTrue(world.contains("HaruProactiveSpeechState haruSpeech"));
  assertTrue(world.contains("SAVE_VERSION = 53"));
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
