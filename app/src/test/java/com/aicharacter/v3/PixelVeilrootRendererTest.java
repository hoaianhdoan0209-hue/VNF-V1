package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class PixelVeilrootRendererTest {
 @Test public void authoredVeilrootUsesPixelRenderer(){
  WorldObject tree=new WorldObject("lake_tree_03","tree","quiet_grove","","",800,846,150,300,"vegetation,veilroot");
  assertTrue(PixelVeilrootRenderer.supports(tree));
 }
 @Test public void otherObjectsStayOnTheirOwnRenderer(){
  WorldObject herb=new WorldObject("herb_mistleaf_01","herb","garden_path","","",800,846,40,80,"herb");
  assertFalse(PixelVeilrootRenderer.supports(herb));
 }
}
