package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class DialoguePainRegressionTest {
 @Test public void painDoesNotOverrideConcreteWeatherQuestion(){
  WorldState s=dialogueState();
  s.body.pain=64;
  s.environment.weather="CLEAR";
  String reply=LocalDialogueEngine.generic(s,"Thời tiết hôm nay thế nào?");
  assertTrue(reply.contains("Trời khá quang"));
  assertFalse(reply.contains("đau"));
 }

 @Test public void explicitHealthQuestionStillReportsPain(){
  WorldState s=dialogueState();
  s.body.pain=64;
  String reply=LocalDialogueEngine.health(s,"Cậu ổn không?");
  assertTrue(reply.toLowerCase().contains("đau"));
 }
 private static WorldState dialogueState(){
  WorldState s=WorldState.fresh();s.world=new WorldModel();s.world.areas.add(new WorldArea("test_area","Test","test place",0,100,0,true,"test"));s.haruX=10;return s;
 }
}
