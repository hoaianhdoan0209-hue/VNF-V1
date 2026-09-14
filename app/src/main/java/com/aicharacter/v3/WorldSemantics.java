package com.aicharacter.v3;
/** Derived authored semantics: values exist because authored tags/exposure say something real. */
public final class WorldSemantics {private WorldSemantics(){}
 public static double shelter(WorldArea a){if(a==null)return 0;double v=a.weatherExposed?.08:.72;if(a.tags.contains("shelter")||a.tags.contains("home"))v=Math.max(v,.95);return v;}
 public static double rest(WorldArea a){if(a==null)return 0;double v=.35;if(a.tags.contains("quiet"))v+=.25;if(a.tags.contains("home"))v+=.25;if(a.tags.contains("water"))v+=.12;return Math.min(1,v);}
 public static double exposure(WorldArea a){return a==null?1:(a.weatherExposed?1:.18);}
 public static double visibility(WorldState s,WorldArea a){double v=s.environment.ambientBrightness;if("RAIN".equals(s.environment.weather))v-=.28*s.environment.weatherIntensity;if(a!=null&&a.tags.contains("grove"))v-=.12;return Math.max(.15,Math.min(1,v));}
 public static double comfort(WorldState s,WorldArea a){double c=rest(a)-s.worldWetness*.25*exposure(a);if("RAIN".equals(s.environment.weather))c-=.35*s.environment.weatherIntensity*exposure(a);return c;}
}
