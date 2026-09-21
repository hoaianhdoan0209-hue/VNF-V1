package com.aicharacter.v3;

import java.util.*;

/** Read-only V1 architecture gate. Uses a cloned save when a simulation step is needed. */
public final class V1FoundationDevTest{
 private V1FoundationDevTest(){}
 public static String run(WorldState original){
  List<String> ok=new ArrayList<>(),bad=new ArrayList<>();
  check(Math.abs(WholeBodyPhysicsEngine.GRAVITY-9.81)<.001,"gravity uses SI 9.81 m/s^2",ok,bad);
  check(WorldUnits.PIXELS_PER_METER>0,"world/render units have one SI conversion boundary",ok,bad);

  AtmosphereState a=new AtmosphereState();a.syncDerived();
  double gas=a.oxygenFraction+a.inertGasFraction+a.carbonDioxideFraction;
  check(Math.abs(gas-1.0)<.00001,"atmospheric dry-gas fractions normalize",ok,bad);
  check(a.airDensityKgM3>.7&&a.airDensityKgM3<1.5,"air density is physically plausible at launch conditions",ok,bad);

  if(original!=null&&original.world!=null){
   check(original.world.knowledgeAnchors.size()>=12,"World Library has broad provenance anchors",ok,bad);
   boolean provenance=true;
   for(WorldKnowledgeAnchor x:original.world.knowledgeAnchors)
    if(x.id.isEmpty()||x.domain.isEmpty()||x.realAnchor.isEmpty()||x.fantasyRule.isEmpty()||x.sourceFamily.isEmpty())provenance=false;
   check(provenance,"every knowledge anchor keeps real reference + fantasy transform + provenance",ok,bad);

   int creatures=0;boolean copiedRealSpecies=false;
   String[] forbidden={"dog","wolf","fox","rabbit","horse","cow","bear","bird","fish","deer","tiger","lion"};
   for(WorldObject o:original.world.objects)if(o.enabled&&"creature".equals(o.type)){
    creatures++;String id=o.id.toLowerCase(Locale.ROOT);
    for(String f:forbidden)if(id.contains(f))copiedRealSpecies=true;
   }
   check(creatures>=7,"launch world contains multiple authored fictional creatures",ok,bad);
   check(!copiedRealSpecies,"authored fauna are not direct real-species copies",ok,bad);

   try{
    WorldState s=WorldState.fromJson(original.toJson());s.world=original.world;
    long now=Math.max(System.currentTimeMillis(),s.lastSimulatedAt+60000L);s.lastSimulatedAt=now-60000L;
    AtmosphereEvolutionEngine.advance(s,now);
    check(s.atmosphere.pressureKPa>90&&s.atmosphere.pressureKPa<=101.5,"local elevation produces bounded atmospheric pressure",ok,bad);
    check(s.atmosphere.relativeHumidity>=0&&s.atmosphere.relativeHumidity<=1,"biome/weather humidity stays bounded",ok,bad);

    WorldState wake=WorldState.fromJson(original.toJson());wake.world=original.world;wake.haruActivity="idle";wake.body.energy=72;wake.body.sleepiness=22;
    BodyRhythmEngine.advanceMinutes(wake,60);
    check(wake.body.energy>70.8&&wake.body.energy<71.6,"quiet waking fatigue advances on real-hour timescale",ok,bad);
    check(wake.body.sleepiness>23&&wake.body.sleepiness<25,"quiet waking sleep pressure grows gradually",ok,bad);

    WorldState sleep=WorldState.fromJson(original.toJson());sleep.world=original.world;sleep.haruActivity="sleeping";sleep.body.energy=55;sleep.body.sleepiness=70;
    BodyRhythmEngine.advanceMinutes(sleep,60);
    check(sleep.body.energy>58&&sleep.body.energy<60,"sleep restores energy gradually over hours",ok,bad);
    check(sleep.body.sleepiness>63&&sleep.body.sleepiness<66,"sleep reduces sleep pressure over real hours",ok,bad);
   }catch(Throwable t){bad.add("cloned V1 simulation step: "+t.getClass().getSimpleName());}
  }else bad.add("world definition/library unavailable");

  StringBuilder b=new StringBuilder("V1 FOUNDATION TEST\nPASS ").append(ok.size()).append(" / FAIL ").append(bad.size()).append('\n');
  for(String x:ok)b.append("✓ ").append(x).append('\n');
  for(String x:bad)b.append("✗ ").append(x).append('\n');
  return b.toString();
 }
 private static void check(boolean pass,String label,List<String>ok,List<String>bad){(pass?ok:bad).add(label);}
}
