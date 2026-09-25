package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelFloraRendererTest {
 @Test public void allPersistentFloraFamiliesUsePixelRenderer(){
  String[] ids={"hearth_bloom_01","silverfold_01","pulse_grass_01","lam_thread_01","shimmer_mat_01","echo_frond_01","threadbloom_01"};
  for(String id:ids){
   WorldObject o=new WorldObject(id,"flora","test","","",100,846,40,80,"vegetation,living_flora");
   assertTrue(id,PixelFloraRenderer.supports(o));
  }
  assertEquals(7,PixelFloraRenderer.familyCount());
 }
 @Test public void unrelatedObjectsStayOnTheirOwnRenderer(){
  WorldObject bench=new WorldObject("bench","bench","test","bench_wood_01","",100,846,100,60,"rest");
  assertFalse(PixelFloraRenderer.supports(bench));
 }
}
