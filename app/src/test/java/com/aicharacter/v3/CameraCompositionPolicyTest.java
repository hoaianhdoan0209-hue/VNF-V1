package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class CameraCompositionPolicyTest {
 @Test public void pixelPresentationStartsCloserThanLegacyWideShot(){
  assertTrue(CameraCompositionPolicy.BASE_PIXEL_ZOOM>1.10f);
  assertEquals(CameraCompositionPolicy.BASE_PIXEL_ZOOM,CameraCompositionPolicy.targetZoom(null,null),1e-6f);
 }
}
