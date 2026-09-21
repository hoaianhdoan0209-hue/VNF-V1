package com.aicharacter.v3;
import java.util.Locale;
/** SYSTEM-only physiology trace. Haru receives subjective body perceptions, never these engine values. */
public final class PhysiologyDiagnostics{
 private PhysiologyDiagnostics(){}
 public static String trace(WorldState s){
  if(s==null)return"HARU PHYSIOLOGY <no state>";
  DigestiveState d=s.digestive==null?new DigestiveState():s.digestive;HydrationState h=s.hydration==null?new HydrationState():s.hydration;
  CirculationState c=s.circulation==null?new CirculationState():s.circulation;MetabolismState m=s.metabolism==null?new MetabolismState():s.metabolism;
  EndocrineState e=s.endocrine==null?new EndocrineState():s.endocrine;SkinState k=s.skin==null?new SkinState():s.skin;ImmuneState im=s.immune==null?new ImmuneState():s.immune;return String.format(Locale.US,
   "HARU PHYSIOLOGY\nenergy=%.1f hunger=%.2f stomach=%.2f nutrientReserve=%.2f digestion=%.2f\nhydration=%.2f thirst=%.2f bladder=%.2f bladderUrgency=%.2f renalLoad=%.2f\nperfusion=%.2f oxygenDelivery=%.2f metabolicDemand=%.2f usableEnergy=%.2f\nendocrine stress=%.2f circadianSleep=%.2f metabolicSupport=%.2f recoverySignal=%.2f\nskin barrier=%.2f damage=%.2f irritation=%.2f moisture=%.2f thermalStrain=%.2f\nimmune readiness=%.2f inflammation=%.2f environmentalPressure=%.2f recoverySupport=%.2f",
   s.body==null?0:s.body.energy,DigestionHydrationEngine.hunger(s),d.stomachFood,d.nutrientReserve,d.digestionLoad,h.hydration,DigestionHydrationEngine.thirst(s),h.bladderFill,DigestionHydrationEngine.bladderUrgency(s),h.renalLoad,c.perfusion,c.oxygenDelivery,m.demand,m.availableEnergy,e.stressResponse,e.circadianSleepSignal,e.metabolicSupport,e.recoverySignal,k.barrierIntegrity,k.surfaceDamage,k.irritation,k.moistureExposure,k.thermoregulationStrain,im.readiness,im.inflammation,im.environmentalPressure,im.recoverySupport);
 }
}
