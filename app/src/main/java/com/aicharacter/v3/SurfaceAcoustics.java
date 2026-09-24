package com.aicharacter.v3;

/** Presentation-only acoustic material derived from authored area/biome semantics. */
public final class SurfaceAcoustics {
 public enum Surface{WOOD,ROOTMAT,WET_BANK,LEAF_FLOOR,SOFT_GROUND}
 public static final class Profile{
  public final Surface surface;public final double lowTone,highTone,noiseMix,damping;
  Profile(Surface s,double low,double high,double noise,double damping){surface=s;lowTone=low;highTone=high;noiseMix=cl(noise);this.damping=cl(damping);}
 }
 private SurfaceAcoustics(){}

 public static Surface surfaceAt(WorldState s,float x){
  WorldArea a=s==null||s.world==null?null:s.world.areaAt(x);if(a==null)return Surface.SOFT_GROUND;
  String tags=(a.tags==null?"":a.tags).toLowerCase(java.util.Locale.ROOT),biome=(a.biomeId==null?"":a.biomeId).toLowerCase(java.util.Locale.ROOT);
  if(tags.contains("interior")||tags.contains("home")||biome.contains("hearth"))return Surface.WOOD;
  if(tags.contains("wet_margin")||tags.contains("water")||biome.contains("lumenmere"))return Surface.WET_BANK;
  if(tags.contains("rootmat")||tags.contains("path")||biome.contains("silverfold"))return Surface.ROOTMAT;
  if(tags.contains("grove")||tags.contains("veilroot")||tags.contains("shade")||biome.contains("veilroot"))return Surface.LEAF_FLOOR;
  return Surface.SOFT_GROUND;
 }
 public static Profile profile(Surface s){
  if(s==Surface.WOOD)return new Profile(s,118,242,.16,.32);
  if(s==Surface.WET_BANK)return new Profile(s,72,132,.52,.58);
  if(s==Surface.ROOTMAT)return new Profile(s,82,174,.34,.52);
  if(s==Surface.LEAF_FLOOR)return new Profile(s,66,156,.62,.66);
  return new Profile(Surface.SOFT_GROUND,76,148,.42,.56);
 }
 private static double cl(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
}
