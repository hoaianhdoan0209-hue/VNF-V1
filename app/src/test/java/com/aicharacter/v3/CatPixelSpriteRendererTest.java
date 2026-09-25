package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class CatPixelSpriteRendererTest {
 @Test public void movingCatExposesAllEightGaitFramesFromRealRigPhase(){
  WorldState s=WorldState.fresh();s.catRig=new CatRigState();
  CatAnimationController.Visual v=new CatAnimationController.Visual(CatAnimationController.State.WALK,true,.4,.1,.8,"test");
  boolean[] seen=new boolean[8];
  for(int i=0;i<8;i++){
   s.catRig.gaitPhase=i/8.0;
   int frame=CatPixelSpriteRenderer.frameIndex(s,v,0);
   assertTrue(frame>=0&&frame<8);seen[frame]=true;
  }
  for(int i=0;i<8;i++)assertTrue("missing gait frame "+i,seen[i]);
 }

 @Test public void idleAnimationAlsoCyclesAcrossEightPresentationFrames(){
  WorldState s=WorldState.fresh();s.catRig=new CatRigState();
  CatAnimationController.Visual v=new CatAnimationController.Visual(CatAnimationController.State.IDLE,true,.2,.1,.8,"test");
  boolean[] seen=new boolean[8];
  for(int i=0;i<8;i++)seen[CatPixelSpriteRenderer.frameIndex(s,v,i/2.5f)]=true;
  for(int i=0;i<8;i++)assertTrue("missing idle frame "+i,seen[i]);
 }
}
