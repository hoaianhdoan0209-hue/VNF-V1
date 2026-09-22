package com.aicharacter.v3;

/** Small pure-Java guard for renderer math that must stay finite and inside the authored 2400px stage. */
public final class VisualRegressionContract{
    private VisualRegressionContract(){}

    public static float layerShift(float cameraX,float parallax){
        float cam=Float.isFinite(cameraX)?Math.max(0f,cameraX):0f;
        float factor=Float.isFinite(parallax)?Math.max(0f,Math.min(1f,parallax)):0f;
        return cam*factor;
    }
}
