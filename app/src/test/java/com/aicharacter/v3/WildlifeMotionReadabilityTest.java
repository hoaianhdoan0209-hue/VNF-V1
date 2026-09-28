package com.aicharacter.v3;

import org.junit.Test;
import static org.junit.Assert.*;

public final class WildlifeMotionReadabilityTest {
 @Test public void realVelocityMakesGroundGaitMoreReadable(){
  SpeciesEvolutionCatalog.Species sp=null;
  for(SpeciesEvolutionCatalog.Species x:SpeciesEvolutionCatalog.all()){
   if("six-beat scuttle".equals(x.locomotion)){sp=x;break;}
  }
  assertNotNull(sp);
  CreatureLifeState c=new CreatureLifeState();c.locomotionDrive=.45;c.velocityXMps=0;
  float idle=Math.abs(WildlifeTraitRenderer.motionOffset(sp,c,.73f));
  c.velocityXMps=.24;
  float moving=Math.abs(WildlifeTraitRenderer.motionOffset(sp,c,.73f));
  assertTrue("real movement should visibly amplify gait",moving>idle*1.35f);
 }

 @Test public void visibleRepresentativeMotionStillRespectsDormantTraits(){
  double activeDrive=WildlifeManifestationEngine.traitMotionFor("six-beat scuttle","seasonal-active");
  double dormantDrive=WildlifeManifestationEngine.traitMotionFor("six-beat scuttle","dormant-cycle");
  assertTrue("dormant lifecycle must reduce visible locomotion drive for the same locomotion family",activeDrive>dormantDrive);
  assertEquals(.12,activeDrive-dormantDrive,1e-9);
 }

 @Test public void activeWildlifeChangesWorldPositionWithinSeconds(){
  SpeciesEvolutionCatalog.Species sp=null;
  for(SpeciesEvolutionCatalog.Species x:SpeciesEvolutionCatalog.all()){
   if(!(x.lifeCycle.contains("dormant")||x.lifeCycle.contains("long-slow"))&&!"air-drift".equals(x.locomotion)){sp=x;break;}
  }
  assertNotNull(sp);
  WorldState s=WorldState.fresh();s.world=new WorldModel();s.livingWorld=new LivingWorldState();
  WorldArea a=new WorldArea("field","field","field",0,900,846,true,"vegetation,open");a.biomeId="field";s.world.areas.add(a);
  BiomeProfile b=new BiomeProfile();b.id="field";b.tags="vegetation,open";b.vegetation=b.tags;b.fauna=b.tags;b.baseMoisture=.45;b.baseTemperatureC=24;s.world.biomes.put("field",b);
  WorldObject o=new WorldObject("motion_probe","creature","field","","",180,846,36,28,"creature,dynamic_wildlife");o.dictionaryRef=sp.key;o.collision=false;s.world.objects.add(o);
  CreatureLifeState life=s.livingWorld.creature(o.id);life.areaId="field";life.x=180;life.body.vitalReserve=.82;life.body.strain=.08;life.curiosity=.55;life.lastUpdatedAt=0;
  s.haruX=500;s.catState.x=700;s.catState.areaId="field";s.environment.weather="CLEAR";s.environment.weatherIntensity=.1;
  float before=life.x;
  LivingWorldEngine.advance(s,.10,1_900_000_006_000L);
  assertTrue("species trait should seed visible locomotion drive",life.locomotionDrive>=.10);
  assertTrue("active wildlife should physically move within six seconds",Math.abs(life.x-before)>1f);
  assertTrue("motion must be real velocity, not render-only bob",Math.abs(life.velocityXMps)>.005);
 }


}
