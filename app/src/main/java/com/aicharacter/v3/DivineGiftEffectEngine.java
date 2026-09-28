package com.aicharacter.v3;

/**
 * Explicit consumers for reversible divine gift effects.
 * This engine never writes cognition or world truth. It only activates a granted
 * contract when a compatible physical condition exists, then exposes bounded
 * modifiers to the owning physical subsystem.
 */
public final class DivineGiftEffectEngine {
 private static final String COLD_TOKEN="ThermalEngine.COLD_RESILIENCE";
 private static final String WARD_TOKEN="ThermalEngine.AREA_WARD";
 private DivineGiftEffectEngine(){}

 public static void advance(WorldState s,long now){
  if(s==null||s.divineOntology==null||s.divineOntology.gifts.isEmpty())return;
  String area=currentHaruArea(s);
  for(DivineGiftState g:s.divineOntology.gifts.values()){
   if(g==null)continue;g.clamp();
   if(!"GRANTED".equals(g.status)||expired(g,now))continue;
   if("COLD_RESILIENCE".equals(g.kind)&&appliesToEntity(g,"haru")&&haruNeedsColdProtection(s)){
    DivineGiftContract.activate(s,g.giftId,COLD_TOKEN,now);
   }else if("AREA_WARD".equals(g.kind)&&appliesToArea(g,area)&&weatherNeedsWard(s)){
    DivineGiftContract.activate(s,g.giftId,WARD_TOKEN,now);
   }
  }
 }

 public static double coldResilience(WorldState s,String bearerId,long now){
  if(s==null||s.divineOntology==null)return 0;
  double best=0;
  for(DivineGiftState g:s.divineOntology.gifts.values()){
   if(g==null)continue;g.clamp();
   if("ACTIVE".equals(g.status)&&"COLD_RESILIENCE".equals(g.kind)&&!expired(g,now)&&appliesToEntity(g,bearerId))best=Math.max(best,g.intensity);
  }
  return unit(best);
 }

 public static double areaWard(WorldState s,String areaId,long now){
  if(s==null||s.divineOntology==null)return 0;
  double best=0;
  for(DivineGiftState g:s.divineOntology.gifts.values()){
   if(g==null)continue;g.clamp();
   if("ACTIVE".equals(g.status)&&"AREA_WARD".equals(g.kind)&&!expired(g,now)&&appliesToArea(g,areaId))best=Math.max(best,g.intensity);
  }
  return unit(best);
 }

 private static boolean haruNeedsColdProtection(WorldState s){
  if(s.thermal==null)return false;
  return s.thermal.coldLoad>=.18||s.thermal.skinWetness>=.28||s.thermal.coreTemperatureC<36.75;
 }

 private static boolean weatherNeedsWard(WorldState s){
  if(s.environment==null)return false;
  return ("RAIN".equals(s.environment.weather)&&s.environment.weatherIntensity>=.30)||s.environment.wind>=.38;
 }

 private static boolean appliesToEntity(DivineGiftState g,String entityId){
  if(g==null||entityId==null||entityId.isEmpty())return false;
  if(!"ENTITY".equals(g.scopeType))return false;
  String target=g.scopeTarget==null||g.scopeTarget.isEmpty()?g.bearerId:g.scopeTarget;
  return entityId.equals(target)&&entityId.equals(g.bearerId);
 }

 private static boolean appliesToArea(DivineGiftState g,String areaId){
  if(g==null||areaId==null||areaId.isEmpty()||!"AREA".equals(g.scopeType))return false;
  return areaId.equals(g.scopeTarget);
 }

 private static boolean expired(DivineGiftState g,long now){return g.expiresAt>0&&now>=g.expiresAt;}
 private static String currentHaruArea(WorldState s){WorldArea a=s.world==null?null:s.world.areaAt(s.haruX);return a==null?"":a.id;}
 private static double unit(double v){if(!Double.isFinite(v))return 0;return Math.max(0,Math.min(1,v));}
}
