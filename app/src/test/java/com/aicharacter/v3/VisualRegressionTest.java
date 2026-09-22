package com.aicharacter.v3;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

public final class VisualRegressionTest{
    private static Path appRoot(){
        Path cwd=Paths.get("").toAbsolutePath().normalize();
        if(Files.isDirectory(cwd.resolve("src/main")))return cwd;
        Path app=cwd.resolve("app");
        if(Files.isDirectory(app.resolve("src/main")))return app;
        throw new AssertionError("Cannot locate app module from "+cwd);
    }

    private static String read(Path p)throws Exception{
        return new String(Files.readAllBytes(p),StandardCharsets.UTF_8);
    }

    @Test public void foregroundParallaxCannotExposeBlankRightEdge(){
        float world=2400f;
        for(float viewport:new float[]{600f,900f,1200f,1800f,2399f}){
            float camera=world-viewport;
            float shift=VisualRegressionContract.layerShift(camera,1.08f);
            assertTrue("foreground right edge must cover viewport",world-shift>=viewport);
        }
        assertEquals(0f,VisualRegressionContract.layerShift(Float.NaN,1.08f),0f);
        assertEquals(0f,VisualRegressionContract.layerShift(400f,Float.POSITIVE_INFINITY),0f);
    }

    @Test public void fourBiomesKeepAuthoredLayersReachableAndForegroundRendered()throws Exception{
        Path app=appRoot();
        String assetJson=read(app.resolve("src/main/assets/visual/asset_manifest.json"));
        String loader=read(app.resolve("src/main/java/com/aicharacter/v3/AssetManifest.java"));
        String renderer=read(app.resolve("src/main/java/com/aicharacter/v3/GameView.java"));
        String[] areas={"home","garden","lakeside","grove"};
        String[] authored={"sky","distant","mid","ground","foreground"};
        for(String area:areas){
            for(String layer:authored){
                String id=area+"_"+layer;
                assertTrue("missing authored asset "+id,Files.isRegularFile(app.resolve("src/main/res/drawable-nodpi/"+id+".png")));
                assertTrue("asset manifest json lost "+id,assetJson.contains("\"id\": \""+id+"\""));
            }
            for(String layer:new String[]{"distant","mid","ground","foreground"}){
                String id=area+"_"+layer;
                assertTrue("AssetManifest loader lost "+id,loader.contains("\""+id+"\""));
            }
        }
        assertTrue("generic foreground draw must include Lakeside",renderer.contains("drawLayer(c,area+\"_foreground\",cam,1.08f);"));
        assertFalse("Lakeside foreground must not be excluded",renderer.contains("if(!\"lakeside\".equals(area))drawLayer(c,area+\"_foreground\""));
        assertTrue("camera layer shift must use regression guard",renderer.contains("VisualRegressionContract.layerShift(cam,parallax)"));
        assertTrue("Lakeside keeps its explicit sky path",renderer.contains("if(\"lakeside\".equals(area)){WorldVisualProfile v=WorldVisualProfile.lakeside(getContext())"));
    }

    @Test public void haruSpriteCropAndDestinationPreserveFrameAspect()throws Exception{
        String renderer=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
        int from=renderer.indexOf("private void drawGirl");
        int to=renderer.indexOf("private void drawWeather",from);
        assertTrue(from>=0&&to>from);
        String girl=renderer.substring(from,to);
        assertTrue("frame width must be derived from sheet frame count",girl.contains("fw=b.getWidth()/v.frames"));
        assertTrue("crop must use exactly one frame",girl.contains("new Rect(frame*fw,0,(frame+1)*fw,fh)"));
        assertTrue("destination must scale width and height by the same factor",girl.contains("new RectF(left,top,left+fw*sc,top+fh*sc)"));
        assertTrue("gravity may translate body ground but not resize sprite",girl.contains("bodyGround=ground-lift"));
    }

    @Test public void legacyFullFrameLightingRasterIsNotCompositedOverWorld()throws Exception{
        String renderer=read(appRoot().resolve("src/main/java/com/aicharacter/v3/GameView.java"));
        int from=renderer.indexOf("private void drawTimeTint");
        int to=renderer.indexOf("private void drawUi",from);
        assertTrue(from>=0&&to>from);
        String tint=renderer.substring(from,to);
        assertFalse("legacy evening raster must not overlay the authored biome scene",tint.contains("light_evening"));
        assertFalse("legacy night raster must not overlay the authored biome scene",tint.contains("light_night"));
        assertFalse("legacy Home full-frame raster must not overlay the authored biome scene",tint.contains("home_warm_light"));
        assertTrue("time-of-day must remain a procedural tint, not a second background",tint.contains("new LinearGradient"));
    }

}
