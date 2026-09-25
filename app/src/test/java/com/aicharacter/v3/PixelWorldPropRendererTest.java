package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelWorldPropRendererTest {
 @Test public void coreLandmarksUsePixelRenderer(){
  WorldObject shelter=new WorldObject("shelter_01","home","home_shelter","shelter_wood","",315,846,470,360,"home,shelter");
  WorldObject bench=new WorldObject("bench_lake_01","bench","lakeside","bench_wood_01","",1430,850,220,135,"rest");
  WorldObject other=new WorldObject("other","prop","garden","tree_autumn_02","",0,846,100,180,"tree");
  assertTrue(PixelWorldPropRenderer.supports(shelter));
  assertTrue(PixelWorldPropRenderer.supports(bench));
  assertFalse(PixelWorldPropRenderer.supports(other));
 }
}
