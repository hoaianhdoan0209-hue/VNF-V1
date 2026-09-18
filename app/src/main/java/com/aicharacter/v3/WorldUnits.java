package com.aicharacter.v3;
/** Single conversion boundary: simulation physics uses SI; map/rendering keeps world pixels. */
public final class WorldUnits{private WorldUnits(){}public static final double PIXELS_PER_METER=32.0;
 public static double pxToM(double px){return px/PIXELS_PER_METER;}public static double mToPx(double m){return m*PIXELS_PER_METER;}
 public static double pxPerSecToMps(double v){return pxToM(v);}public static double mpsToPxPerSec(double v){return mToPx(v);}
}