package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class HaruVisibleAgencyEngineTest {
 private static final long T0=1_900_000_000_000L;

 @Test public void healthyCuriousHaruTurnsPassiveChoiceIntoSelfChosenExploration(){
  WorldState s=state();
  LifeDecision passive=new LifeDecision(new Intention("quiet_pause",0,"solitude","home_shelter","stay quiet",.20,T0+3600000L));
  passive.intention.utility=1;
  LifeDecision adjusted=HaruVisibleAgencyEngine.adjustChoice(s,passive,T0);
  assertNotNull(adjusted);
  assertEquals("explore_world",adjusted.intention.id);
  assertNotEquals("home_shelter",adjusted.intention.targetId);
  assertTrue(adjusted.reasons.containsKey("memory_novelty"));
  OfflineLifeEngine.beginDecision(s,adjusted,T0);
  assertTrue("self-chosen exploration must create physical travel",s.girlTravel.active);
  assertEquals("explore_world",s.currentIntention);
 }

 @Test public void urgentBodyNeedsAreNeverOverriddenForVisualMotion(){
  WorldState s=state();
  s.hydration.hydration=.22;
  LifeDecision drink=new LifeDecision(new Intention("drink",0,"hydration","shelter_01","drink water",.90,T0+3600000L));
  assertSame(drink,HaruVisibleAgencyEngine.adjustChoice(s,drink,T0));
 }

 @Test public void alreadyMovingCuriosityChoiceIsPreserved(){
  WorldState s=state();
  LifeDecision lake=new LifeDecision(new Intention("observe_lake",0,"curiosity","lakeside","observe lake",.30,T0+3600000L));
  assertSame(lake,HaruVisibleAgencyEngine.adjustChoice(s,lake,T0));
 }

 private static WorldState state(){
  WorldState s=WorldState.fresh();
  s.world=new WorldModel();
  WorldArea home=area("home_shelter",60,590,"interior,shelter,home,dry,quiet,rest,sleep");
  WorldArea garden=area("garden_path",590,1050,"path,vegetation,verge");
  WorldArea lake=area("lakeside",1050,1900,"lake,water,wet_margin,open,reflect");
  WorldArea grove=area("quiet_grove",1900,2320,"grove,quiet,vegetation,shade");
  connect(home,garden);connect(garden,lake);connect(lake,grove);
  s.world.areas.add(home);s.world.areas.add(garden);s.world.areas.add(lake);s.world.areas.add(grove);
  s.world.objects.add(new WorldObject("shelter_01","home","home_shelter","","",315,846,80,80,"home,shelter,rest,dry,safe,sleep,food_source,water_source,toilet"));
  s.haruX=315;s.catState.x=s.catX=340;s.catState.areaId="home_shelter";
  s.body.energy=96;s.body.sleepiness=4;s.body.pain=0;s.body.health=100;
  s.digestive.stomachFood=.82;s.digestive.nutrientReserve=.88;
  s.hydration.hydration=.94;s.hydration.bladderFill=.05;
  s.personality.curiosity=.92;s.currentIntention="";s.planState=new PlanState();s.girlTravel=new TravelState();
  s.environment.weather="CLEAR";s.environment.weatherIntensity=.1;s.worldMinutes=600;
  StateInvariantChecker.normalize(s,T0);
  return s;
 }
 private static WorldArea area(String id,float left,float right,String tags){return new WorldArea(id,id,id,left,right,846,true,tags);}
 private static void connect(WorldArea a,WorldArea b){a.connections.add(b.id);b.connections.add(a.id);}
}
