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
  SpeciesEvolutionCatalog.Species active=null,dormant=null;
  for(SpeciesEvolutionCatalog.Species x:SpeciesEvolutionCatalog.all()){
   if(active==null&&!x.lifeCycle.contains("dormant")&&!x.lifeCycle.contains("long-slow"))active=x;
   if(dormant==null&&(x.lifeCycle.contains("dormant")||x.lifeCycle.contains("long-slow")))dormant=x;
   if(active!=null&&dormant!=null)break;
  }
  assertNotNull(active);assertNotNull(dormant);
  WorldState s=WorldState.fresh();s.world=new WorldModel();s.livingWorld=new LivingWorldState();
  WorldArea a=new WorldArea("test","test","test",0,1000,846,true,"vegetation,water,open");s.world.areas.add(a);
  SpeciesPopulationState pa=s.livingWorld.population(active.key,"test");pa.relativeAbundance=.5;pa.carryingCapacity=.7;
  SpeciesPopulationState pd=s.livingWorld.population(dormant.key,"test");pd.relativeAbundance=.5;pd.carryingCapacity=.7;
  WildlifeManifestationEngine.sync(s,1_900_000_000_000L);
  double activeDrive=-1,dormantDrive=-1;
  for(WorldObject o:s.world.objects){
   CreatureLifeState life=s.livingWorld.creatures.get(o.id);if(life==null)continue;
   if(active.key.equals(o.dictionaryRef))activeDrive=life.locomotionDrive;
   if(dormant.key.equals(o.dictionaryRef))dormantDrive=life.locomotionDrive;
  }
  assertTrue(activeDrive>=0);assertTrue(dormantDrive>=0);
  assertTrue("dormant traits must remain slower than active representatives",activeDrive>dormantDrive);
 }
}
