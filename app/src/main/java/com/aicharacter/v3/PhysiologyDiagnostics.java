package com.aicharacter.v3;
import java.util.Locale;
/** SYSTEM-only physiology trace. Haru receives subjective body perceptions, never these engine values. */
public final class PhysiologyDiagnostics{
 private PhysiologyDiagnostics(){}
 public static String trace(WorldState s){
  if(s==null)return"HARU PHYSIOLOGY <no state>";
  DigestiveState d=s.digestive==null?new DigestiveState():s.digestive;HydrationState h=s.hydration==null?new HydrationState():s.hydration;
  CirculationState c=s.circulation==null?new CirculationState():s.circulation;MetabolismState m=s.metabolism==null?new MetabolismState():s.metabolism;
  return String.format(Locale.US,
   "HARU PHYSIOLOGY\nenergy=%.1f hunger=%.2f stomach=%.2f nutrientReserve=%.2f digestion=%.2f\nhydration=%.2f thirst=%.2f bladder=%.2f bladderUrgency=%.2f renalLoad=%.2f\nperfusion=%.2f oxygenDelivery=%.2f metabolicDemand=%.2f usableEnergy=%.2f",
   s.body==null?0:s.body.energy,DigestionHydrationEngine.hunger(s),d.stomachFood,d.nutrientReserve,d.digestionLoad,h.hydration,DigestionHydrationEngine.thirst(s),h.bladderFill,DigestionHydrationEngine.bladderUrgency(s),h.renalLoad,c.perfusion,c.oxygenDelivery,m.demand,m.availableEnergy);
 }
}
