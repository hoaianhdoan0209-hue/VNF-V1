package com.aicharacter.v3;
public final class V06DevTest{
 private V06DevTest(){}
 public static String run(WorldState s){
  StringBuilder b=new StringBuilder("V0.6 DEV TESTS (deterministic source-level scenarios)\n");
  float old=s.catX;boolean oldAwake=s.catState.awake;double oldSleep=s.catState.sleepiness;
  s.catState.awake=true;s.catState.sleepiness=75;s.catX=340;s.catState.x=340;CatSleepPlanner.Plan home=CatSleepPlanner.choose(s);b.append("A HOME: ").append("HOME".equals(home.mode)?"PASS":"CHECK "+home.mode).append('\n');
  s.catX=2250;s.catState.x=2250;s.haruX=2200;s.relationship.trust=Math.max(s.relationship.trust,55);s.relationship.comfort=Math.max(s.relationship.comfort,55);CatSleepPlanner.Plan far=CatSleepPlanner.choose(s);b.append("B FAR: selected ").append(far.mode).append(" (not coordinate random)\n");
  s.catState.awake=false;s.catState.attachedToEntity="";s.catState.sleepStartedAt=System.currentTimeMillis()-3*3600000L;s.catState.x=300;s.catX=300;s.haruX=1500;s.catSearch=new GirlCatSearchEngine.SearchState();GirlCatSearchEngine.start(s,System.currentTimeMillis(),"dev memory/habit search");b.append("C FIND planning: ").append(s.catSearch.candidates).append(" / no catX planner input\n");
  s.catSearch.active=true;s.catSearch.found=true;s.environment.weather="CLEAR";s.body.pain=0;int before=s.notificationEvents.size();GirlCatSearchEngine.decideWake(s,System.currentTimeMillis());b.append("D no urgency wake: ").append(s.notificationEvents.size()==before?"PASS non-verbal/wait":"context selected wake").append('\n');
  s.environment.weather="RAIN";GirlCatSearchEngine.decideWake(s,System.currentTimeMillis());b.append("E urgency wake: event queue=").append(s.notificationEvents.size()).append('\n');
  s.catX=old;s.catState.x=old;s.catState.awake=oldAwake;s.catState.sleepiness=oldSleep;return b.toString();
 }
}