package com.aicharacter.v3;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public final class RealExperienceRegressionTest {
 private static final long T0=1_800_000_000_000L;

 @Test public void invalidPersistedHaruPositionCannotMakeCharacterDisappear(){
  WorldState s=state(T0);
  s.haruX=99999f;
  s.catState.attachedToEntity="girl";
  s.catState.carryKnownByGirl=true;
  s.girlTravel.active=true;
  s.planState=new PlanState();s.planState.planId="stale";s.planState.status="ACTIVE";s.planState.intentionId="observe_lake";s.planState.lastProgressAt=T0-60000;
  assertTrue(StateInvariantChecker.normalizeSpatialState(s,T0));
  assertNotNull("Haru must recover onto authored ground",s.world.areaAt(s.haruX));
  assertEquals(s.haruX,s.catState.x,.001f);
  assertEquals(s.haruX,s.catX,.001f);
  assertFalse(s.girlTravel.active);
  assertTrue("stale active plan must not survive spatial recovery",s.planState.terminal());
 }

 @Test public void stalledActivePlanRecoversLongBeforeThreeMinuteManualTest(){
  WorldState s=state(T0);s.haruX=315f;s.catState.x=s.catX=315f;
  PlanState p=new PlanState();p.planId="plan_stalled";p.status="ACTIVE";p.intentionId="observe_lake";p.destination="bench_lake_01";p.plannedAction="OBSERVE";p.createdAt=T0-30000;p.lastProgressAt=T0-16000;s.planState=p;s.currentIntention="observe_lake";
  s.girlTravel.active=false;
  String result=PlanIntegrityChecker.check(s,T0);
  assertEquals("REPLANNED",result);
  assertTrue("stalled plan should resume physical execution",s.girlTravel.active);
 }

 @Test public void healthyHaruCanChooseHerOwnNextActivityWithoutPlayerPrompt(){
  WorldState s=state(T0);s.haruX=315f;s.catState.x=s.catX=315f;s.catState.attachedToEntity="girl";s.catState.carryKnownByGirl=true;
  s.body.energy=96;s.body.sleepiness=4;s.body.pain=0;s.body.health=100;
  s.digestive.stomachFood=.82;s.digestive.nutrientReserve=.88;s.hydration.hydration=.94;s.hydration.bladderFill=.05;
  s.emotion.curiosity=.92;s.currentIntention="";s.haruActivity="standing quietly";s.planState=new PlanState();s.girlTravel=new TravelState();
  HaruAutonomyEngine.tickDecision(s,T0);
  boolean chose=!s.currentIntention.isEmpty()||!"IDLE".equals(s.planState.status)||!"standing quietly".equals(s.haruActivity);
  assertTrue("Haru must autonomously choose/evaluate an activity",chose);
 }

 @Test public void offlineCatchupNoLongerUsesTwelveSecondActiveDecisionSlices()throws Exception{
  String offline=read(appRoot().resolve("src/main/java/com/aicharacter/v3/OfflineLifeEngine.java"));
  assertTrue(offline.contains("OFFLINE_ACTIVE_STEP_MS=5L*60000L"));
  int method=offline.indexOf("private static long causalStepMs");
  int next=offline.indexOf("private static long planReconsiderationBoundaryMs",method);
  assertTrue(method>=0&&next>method);
  String causal=offline.substring(method,next);
  assertFalse("offline active catch-up must not inherit the 12-second active UI cadence",causal.contains("planReconsiderationBoundaryMs"));
 }

 @Test public void healthyHaruStillChoosesAndActsWhilePlayerIsAway(){
  WorldState s=state(T0);
  s.lastOpenedAt=T0;s.lastSimulatedAt=T0;s.lastSavedAt=T0;
  s.haruX=315f;s.catState.x=s.catX=340f;
  s.body.energy=96;s.body.sleepiness=4;s.body.pain=0;s.body.health=100;
  s.digestive.stomachFood=.82;s.digestive.nutrientReserve=.88;s.hydration.hydration=.94;s.hydration.bladderFill=.05;
  s.emotion.curiosity=.92;s.currentIntention="";s.haruActivity="standing quietly";s.planState=new PlanState();s.girlTravel=new TravelState();
  float before=s.haruX;
  WorldContinuityEngine.advanceBackground(s,T0+20L*60000L);
  boolean lived=!s.currentIntention.isEmpty()||!"IDLE".equals(s.planState.status)||Math.abs(s.haruX-before)>1f||!"standing quietly".equals(s.haruActivity);
  assertTrue("background world must include Haru autonomy, not only clocks/body",lived);
  assertEquals(T0+20L*60000L,s.lastSimulatedAt);
 }

 @Test public void backgroundWorldAdvanceDoesNotPretendPlayerReopenedGame(){
  WorldState s=state(T0);
  s.lastOpenedAt=T0;
  s.lastSimulatedAt=T0;
  s.catState.lastPlayerActiveAt=T0;
  s.catState.awake=true;
  OfflineLifeEngine.reconstructBackground(s,T0+20L*60000L);
  assertEquals(T0,s.lastOpenedAt);
  assertEquals(T0+20L*60000L,s.lastSimulatedAt);
  assertTrue("closing the app must not automatically put the player cat to sleep",s.catState.awake);
 }

 @Test public void longGapReconcileReportsABoundedStepCount(){
  WorldState s=state(T0);
  s.lastOpenedAt=T0;
  s.lastSimulatedAt=T0;
  String trace=OfflineLifeEngine.reconstructBackground(s,T0+7L*24L*60L*60000L);
  int marker=trace.lastIndexOf("boundedSteps=");
  assertTrue(marker>=0);
  String tail=trace.substring(marker+"boundedSteps=".length());
  int slash=tail.indexOf('/');
  int steps=Integer.parseInt(tail.substring(0,slash).trim());
  assertTrue("deep-sleep fallback must remain bounded",steps<=20);
 }

 @Test public void sameAreaObjectObservationCreatesVisiblePhysicalApproach(){
  WorldState s=state(T0);s.haruX=1450f;s.reedling.x=1730f;s.reedling.areaId="lakeside";
  PlanState p=new PlanState();p.planId="observe_creature";p.status="ACTIVE";p.intentionId="watch_reedling";p.destination="reedling_01";p.plannedAction="OBSERVE";p.createdAt=T0;p.lastProgressAt=T0;
  s.planState=p;s.currentIntention=p.intentionId;s.girlTravel=new TravelState();
  assertTrue(TravelEngine.start(s,s.girlTravel,"girl","lakeside",p.planId,T0));
  assertTrue("same-area observation must create local physical movement",s.girlTravel.active);
  assertEquals("LOCAL",s.girlTravel.travelMode);
  assertEquals("reedling_01",s.girlTravel.targetId);
  assertTrue(p.lastAction.contains("viewing position"));
 }

 @Test public void areaExplorationChoosesANewVisibleViewpointInsteadOfStandingStill(){
  WorldState s=state(T0);s.haruX=720f;
  PlanState p=new PlanState();p.planId="explore_area";p.status="ACTIVE";p.intentionId="explore_garden";p.destination="garden_path";p.plannedAction="OBSERVE";p.createdAt=T0;p.lastProgressAt=T0;
  s.planState=p;s.currentIntention=p.intentionId;s.girlTravel=new TravelState();
  float before=s.haruX;
  assertTrue(TravelEngine.start(s,s.girlTravel,"girl","garden_path",p.planId,T0));
  assertTrue("area observation must produce visible local travel",s.girlTravel.active);
  assertEquals("LOCAL",s.girlTravel.travelMode);
  assertTrue(Math.abs(s.girlTravel.segmentEndX-before)>56f);
  assertTrue(p.lastAction.contains("different viewpoint"));
 }

 @Test public void offlineTravelFastForwardStillArrivesAndResolvesPlan(){
  WorldState s=state(T0);s.haruX=315f;
  PlanState p=new PlanState();p.planId="offline_observe";p.status="ACTIVE";p.intentionId="observe_lake";p.destination="bench_lake_01";p.plannedAction="OBSERVE";p.createdAt=T0;p.lastProgressAt=T0;
  s.planState=p;s.currentIntention=p.intentionId;s.girlTravel=new TravelState();
  assertTrue(TravelEngine.start(s,s.girlTravel,"girl","lakeside",p.planId,T0));
  assertTrue(s.girlTravel.active);
  TravelEngine.advanceOfflineSeconds(s,s.girlTravel,3600,T0+3600000L);
  assertTrue("offline travel must physically progress",s.haruX>315f);
  assertTrue("arrival may start a local viewing approach or resolve the action",s.girlTravel.active||s.planState.terminal());
  if(s.girlTravel.active)TravelEngine.advanceOfflineSeconds(s,s.girlTravel,3600,T0+7200000L);
  assertTrue("plan must eventually reach an action outcome",s.planState.terminal());
 }

 @Test public void proactiveBubbleAndTtsCannotLoseCueDuringDeferredStartup()throws Exception{
  String main=read(appRoot().resolve("src/main/java/com/aicharacter/v3/MainActivity.java"));
  String view=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String voice=read(appRoot().resolve("src/main/java/com/aicharacter/v3/VoiceController.java"));
  assertTrue(main.contains("else deferredProactiveSpeech=cue.text"));
  assertTrue(main.contains("voice.speakProactive(pending)"));
  assertTrue(view.contains("· TỰ NÓI"));
  assertTrue(view.contains("now>=haruSpeechUntil"));
  assertTrue(voice.contains("pendingProactive"));
  assertTrue(voice.contains("QUEUE_ADD"));
 }

 @Test public void previewPathRepairsSpatialStateBeforeFirstFrame()throws Exception{
  String repo=read(appRoot().resolve("src/main/java/com/aicharacter/v3/WorldRepository.java"));
  int preview=repo.indexOf("loadPreviewOrCreate()");
  int full=repo.indexOf("public synchronized WorldState loadOrCreate()",preview);
  assertTrue(preview>=0&&full>preview);
  String body=repo.substring(preview,full);
  assertTrue(body.contains("attachDefinition(state)"));
  assertTrue(body.contains("StateInvariantChecker.normalizeSpatialState(state,now)"));
 }


 @Test public void directBodyCommandsAreRejectedButInvitationsRemainChoices(){
  WorldState s=state(T0);String before=s.currentIntention;
  HaruMind.Response forced=HaruMind.respond(s,"đi ra bờ hồ ngay");
  assertTrue(forced.controlAttempt);assertTrue(forced.speech.contains("không có quyền điều khiển"));
  assertEquals("direct command must not become Haru intention",before,s.currentIntention);
  IntentParser.Parsed invitation=IntentParser.parse("đi cùng mình nhé?");
  assertEquals(IntentParser.SpeechAct.INVITATION,invitation.act);assertFalse(invitation.isControlAttempt());
 }

 @Test public void walkInvitationCannotDirectlyOverwriteHaruLifePlan(){
  WorldState s=state(T0);s.currentIntention="observe_lake";s.haruActivity="watching the lake";
  s.relationship.trust=100;s.relationship.comfort=100;s.relationship.attachment=100;s.body.energy=95;s.body.pain=0;
  String beforePlan=s.planState.planId;
  HaruMind.Response r=HaruMind.respond(s,"đi dạo cùng mình nhé?");
  assertFalse(r.controlAttempt);
  assertEquals("observe_lake",s.currentIntention);
  assertEquals("watching the lake",s.haruActivity);
  assertEquals(beforePlan,s.planState.planId);
  assertFalse(s.girlTravel.active);
 }

 @Test public void proactiveTtsHasLocaleFallbackInsteadOfFalseReady()throws Exception{
  String voice=read(appRoot().resolve("src/main/java/com/aicharacter/v3/VoiceController.java"));
  assertTrue(voice.contains("TextToSpeech.LANG_MISSING_DATA"));
  assertTrue(voice.contains("TextToSpeech.LANG_NOT_SUPPORTED"));
  assertTrue(voice.contains("Locale.getDefault()"));
  assertTrue(voice.contains("ttsReady=lang!="));
 }

 @Test public void firstMicTapCanEnableVoiceWithoutHiddenDoubleTapRequirement()throws Exception{
  String voice=read(appRoot().resolve("src/main/java/com/aicharacter/v3/VoiceController.java"));
  int listen=voice.indexOf("public void listen()");
  int speak=voice.indexOf("public void speak(",listen);
  assertTrue(listen>=0&&speak>listen);
  String body=voice.substring(listen,speak);
  assertTrue(body.contains("enabled=true"));
  assertFalse(body.contains("Double tap nút mic để bật Voice"));
 }

 @Test public void cameraStateIsResetWhenWorldViewOrLiveStateIsRecreated()throws Exception{
  String view=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String camera=read(appRoot().resolve("src/main/java/com/aicharacter/v3/CatCameraDirector.java"));
  assertTrue(camera.contains("resetForNewView()"));
  assertTrue(view.contains("CatCameraDirector.resetForNewView();state=s"));
  assertTrue(view.contains("CatCameraDirector.resetForNewView();visualCameraReady=false"));
 }

 @Test public void ambienceUsesPlayerCatAsListener(){
  WorldState s=state(T0);s.environment.weather="RAIN";s.environment.weatherIntensity=.9;
  s.haruX=1450f;s.catState.x=s.catX=315f;
  AudioSceneFrame home=AudioSceneEmitter.derive(s,0);
  assertEquals("home_shelter",home.areaId);assertTrue(home.weatherExposure<.5);assertEquals(0.0,home.water,.0001);
  s.catState.x=s.catX=1450f;
  AudioSceneFrame lake=AudioSceneEmitter.derive(s,0);
  assertEquals("lakeside",lake.areaId);assertTrue(lake.weatherExposure>.5);assertTrue(lake.water>.5);
 }


 @Test public void spatialRecoveryAlsoEscapesGapsBetweenAuthoredAreas(){
  WorldState s=state(T0);s.world.area("garden_path").right=1000f;s.world.area("lakeside").left=1100f;s.haruX=1050f;
  assertNull("test precondition: x=1050 sits in authored gap",s.world.areaAt(s.haruX));
  assertTrue(StateInvariantChecker.normalizeSpatialState(s,T0));
  assertNotNull(s.world.areaAt(s.haruX));
 }

 @Test public void visualQaFreezesSimulationAndIllustratedHaruKeepsReadablePresence()throws Exception{
  String view=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
  String renderer=read(appRoot().resolve("src/main/java/com/aicharacter/v3/HaruVisualRenderer.java"));
  assertTrue(view.contains("BuildConfig.DEBUG&&!debugAreaKey.isEmpty()"));
  assertTrue(view.contains("historicalReconcilePending=false"));
  assertTrue(renderer.contains("ILLUSTRATED_SCALE=1.17f"));
  assertTrue(renderer.contains("c.scale(ILLUSTRATED_SCALE,ILLUSTRATED_SCALE,x,bodyGround)"));
 }

 @Test public void healthyFamiliarWorldCannotLeaveHaruStandingPastVisibleAgencyWatchdog(){
  WorldState s=state(T0);s.haruX=315f;s.catState.x=s.catX=340f;
  s.body.energy=96;s.body.sleepiness=4;s.body.pain=0;s.body.health=100;
  s.digestive.stomachFood=.82;s.digestive.nutrientReserve=.88;s.hydration.hydration=.94;s.hydration.bladderFill=.05;
  s.personality.curiosity=.78;s.emotion.curiosity=.72;s.currentIntention="";s.haruActivity="standing quietly";
  s.planState=new PlanState();s.girlTravel=new TravelState();s.lastOpenedAt=T0-60000L;s.intentionStartedAt=T0-60000L;
   LifeDecision selected=new LifeDecision(new Intention("linger",0,"curiosity","home_shelter","stay where she is",.10,T0+60000L)).reason("low_pressure_idle",1);
   LifeDecision adjusted=HaruVisibleAgencyEngine.adjustChoice(s,selected,T0);
   assertEquals("explore_world",adjusted.intention.id);
  OfflineLifeEngine.beginDecision(s,adjusted,T0);
  assertTrue("watchdog choice must become real physical travel",s.girlTravel.active);
  float before=s.haruX;
  TravelEngine.advanceSeconds(s,s.girlTravel,2.0,T0+2000L);
  assertTrue("Haru must visibly move instead of only changing state",Math.abs(s.haruX-before)>1f);
 }

 @Test public void visibleAgencyChoiceCanGroundProactiveSpeech(){
  WorldState s=state(T0);s.haruX=315f;s.body.energy=96;s.body.sleepiness=4;s.body.pain=0;s.body.health=100;
  s.digestive.stomachFood=.82;s.hydration.hydration=.94;s.hydration.bladderFill=.05;s.personality.curiosity=.88;
  s.planState=new PlanState();s.girlTravel=new TravelState();s.currentIntention="";s.intentionStartedAt=T0-60000L;s.lastOpenedAt=T0-60000L;
   LifeDecision idle=new LifeDecision(new Intention("linger",0,"curiosity","home_shelter","stay where she is",.10,T0+60000L)).reason("low_pressure_idle",1);
   LifeDecision d=HaruVisibleAgencyEngine.adjustChoice(s,idle,T0);
   OfflineLifeEngine.beginDecision(s,d,T0);
  assertEquals("explore_world",s.currentIntention);
  assertTrue(HaruProactiveSpeechEngine.advance(s,T0+1000L,LifeSimulationKernel.Mode.ACTIVE));
  HaruProactiveSpeechEngine.Cue cue=HaruProactiveSpeechEngine.consume(s,T0+1001L);
  assertNotNull(cue);assertTrue(cue.text.contains("sang chỗ khác")||cue.text.contains("thay đổi"));
 }

 @Test public void healthyHaruProducesVisibleAutonomousActivityWithinThreeMinutes(){
  WorldState s=state(T0);
  s.haruX=315f;s.catState.x=s.catX=315f;
  s.catState.attachedToEntity="";
  s.catState.carryKnownByGirl=false;
  s.relationship.trust=55;s.relationship.comfort=55;s.relationship.attachment=45;
  s.body.energy=96;s.body.sleepiness=4;s.body.pain=0;s.body.health=100;
  s.digestive.stomachFood=.82;s.digestive.nutrientReserve=.88;
  s.hydration.hydration=.94;s.hydration.bladderFill=.05;
  s.emotion.curiosity=.96;s.mood.arousal=.28;s.mood.pleasantness=.62;
  s.currentIntention="";s.haruActivity="standing quietly";
  s.planState=new PlanState();s.girlTravel=new TravelState();
  float startX=s.haruX,maxTravel=0f;int terminalTransitions=0;String lastStatus=s.planState.status;
  long now=T0;
  for(int i=0;i<180;i++){
   now+=1000L;
   LifeSimulationKernel.beginSlice(s,1.0,now,LifeSimulationKernel.Mode.ACTIVE);
   HaruAutonomyEngine.tickDecision(s,now);
   LifeSimulationKernel.endSlice(s,1.0,now,true,false,LifeSimulationKernel.Mode.ACTIVE);
   maxTravel=Math.max(maxTravel,Math.abs(s.haruX-startX));
   if(!lastStatus.equals(s.planState.status)&&s.planState.terminal())terminalTransitions++;
   lastStatus=s.planState.status;
  }
  boolean visible=maxTravel>=40f||terminalTransitions>0||s.girlTravel.active;
  assertTrue("three minutes of healthy autonomous play must produce visible physical or action progress",visible);
  assertFalse("Haru must not remain a permanently idle placeholder","standing quietly".equals(s.haruActivity)&&s.currentIntention.isEmpty());
 }

 private static WorldState state(long now){
  WorldState s=WorldState.fresh();s.createdAt=now-3600000;s.lastOpenedAt=now;s.lastSimulatedAt=now;s.lastSavedAt=now;s.world=world();
  s.haruX=315;s.catX=340;s.catState.x=340;s.catState.areaId="home_shelter";s.catState.awake=true;s.catState.attachedToEntity="";s.catState.carryKnownByGirl=false;
  s.environment.weather="CLEAR";s.environment.weatherIntensity=.1;s.worldMinutes=10*60;s.lastCatSeenAt=now;
  StateInvariantChecker.normalize(s,now);return s;
 }

 private static WorldModel world(){
  WorldModel w=new WorldModel();
  WorldArea home=area("home_shelter",60,590,"interior,shelter,home,dry,quiet,rest,sleep");home.weatherExposed=false;
  WorldArea garden=area("garden_path",590,1050,"path,vegetation,verge");
  WorldArea lake=area("lakeside",1050,1900,"lake,water,wet_margin,open,reflect");
  WorldArea grove=area("quiet_grove",1900,2320,"grove,quiet,vegetation,shade");
  connect(home,garden);connect(garden,lake);connect(lake,grove);w.areas.add(home);w.areas.add(garden);w.areas.add(lake);w.areas.add(grove);
  w.objects.add(object("shelter_01","home","home_shelter",315,"home,shelter,rest,dry,safe,sleep,food_source,water_source,toilet",true,105));
  w.objects.add(object("bench_lake_01","bench","lakeside",1430,"rest,reflect,landmark,sit",true,70));
  w.objects.add(object("lake_tree_03","tree","quiet_grove",2070,"vegetation,quiet,observe,living_flora",true,60));
  w.objects.add(object("reedling_01","creature","lakeside",1730,"creature,lake,vegetation",false,60));
  return w;
 }
 private static WorldArea area(String id,float left,float right,String tags){WorldArea a=new WorldArea(id,id,id,left,right,846,true,tags);return a;}
 private static void connect(WorldArea a,WorldArea b){a.connections.add(b.id);b.connections.add(a.id);}
 private static WorldObject object(String id,String type,String area,float x,String tags,boolean interactable,float radius){
  WorldObject o=new WorldObject(id,type,area,"",id,x,846,80,80,tags);o.enabled=true;o.interactable=interactable;o.interactionX=x;o.interactionRadius=radius;return o;
 }

 private static Path appRoot(){
  Path cwd=Paths.get("").toAbsolutePath().normalize();
  if(Files.isDirectory(cwd.resolve("src/main")))return cwd;
  Path app=cwd.resolve("app");if(Files.isDirectory(app.resolve("src/main")))return app;
  throw new AssertionError("Cannot locate app module from "+cwd);
 }
 private static String read(Path p)throws Exception{return new String(Files.readAllBytes(p),StandardCharsets.UTF_8);}
}
