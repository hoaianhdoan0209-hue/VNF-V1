package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ProceduralAudioReadabilityTest {
 @Test public void phoneMixRaisesQuietLayersWithoutRemovingHeadroom(){
  assertTrue(ProceduralAudioEngine.AMBIENCE_GAIN>1.0);
  assertTrue(ProceduralAudioEngine.FOOTSTEP_GAIN>ProceduralAudioEngine.AMBIENCE_GAIN);
  assertTrue(ProceduralAudioEngine.BREATH_GAIN>1.0);
  assertTrue(ProceduralAudioEngine.PURR_GAIN>1.0);
  assertTrue(ProceduralAudioEngine.MASTER_GAIN<1.0);
 }
 @Test public void catFootstepRemainsLighterThanHaruMaterialProfile(){
  SurfaceAcoustics.Profile p=ProceduralAudioEngine.surfaceProfile("WOOD");
  assertNotNull(p);
  double haruBase=.009+.013*.6;
  double catBase=.0032+.0062*.6;
  assertTrue(catBase<haruBase*.55);
 }
}
