package com.aicharacter.v3;

import org.json.*;
import java.util.*;

/**
 * Autonomous worship ecology.
 * A child god chooses followers from real living populations; the player does not
 * preselect a species. Blessings are bounded ecological support. Offerings have a
 * real population cost and can occur only for some species with sufficient devotion.
 */
public final class DivineWorshipEngine {
 private static final int MAX_FOLLOWER_BONDS=4;
 private static final long FOLLOWER_SELECTION_COOLDOWN_MS=12L*60L*60L*1000L;
 private static final long OFFERING_COOLDOWN_MS=18L*60L*60L*1000L;
 private DivineWorshipEngine(){}

 public static void advance(WorldState s,double minutes,long now){
  if(s==null||s.livingWorld==null||!Double.isFinite(minutes)||minutes<=0)return;
  if(s.divineEcology==null)s.divineEcology=new DivineEcologyState();
  MinorGodState god=ensureChildGod(s,now);
  if(god==null)return;
  List<WorshipBondState> existing=s.divineEcology.bondsForGod(god.id);
  if(existing.size()<MAX_FOLLOWER_BONDS&&(god.lastSelectionAt<=0||now-god.lastSelectionAt>=FOLLOWER_SELECTION_COOLDOWN_MS)){
   chooseFollower(s,god,now);
   existing=s.divineEcology.bondsForGod(god.id);
  }
  double days=Math.max(0,minutes/1440.0);
  god.maturity=unit(god.maturity+days*(.0015+.0010*Math.min(1,existing.size()/4.0)));
  god.graceReserve=unit(god.graceReserve+days*.0012);
  for(WorshipBondState bond:existing)advanceBond(s,god,bond,minutes,days,now);
  s.divineEcology.lastAdvancedAt=Math.max(s.divineEcology.lastAdvancedAt,now);
 }

 private static MinorGodState ensureChildGod(WorldState s,long now){
  if(!s.divineEcology.minorGods.isEmpty())return s.divineEcology.minorGods.values().iterator().next();
  if(bestCandidate(s,"minor_god_01",Collections.emptySet())==null)return null;
  MinorGodState g=new MinorGodState();g.id="minor_god_01";g.title="Thần Con";g.awakenedAt=now;g.lastSelectionAt=0;
  s.divineEcology.minorGods.put(g.id,g);
  WorldEventBus.publishId(s,now,"divine_child_awakened_"+Long.toHexString(now),"DIVINE_CHILD_AWAKENED",g.id,"Một Thần Con đã thức tỉnh trong System Reality; chưa có loài nào được chọn sẵn.");
  return g;
 }

 private static void chooseFollower(WorldState s,MinorGodState god,long now){
  Set<String> already=new HashSet<>();for(WorshipBondState b:s.divineEcology.bondsForGod(god.id))already.add(LivingWorldState.populationKey(b.speciesKey,b.areaId));
  Candidate c=bestCandidate(s,god.id,already);god.lastSelectionAt=now;if(c==null)return;
  WorshipBondState b=s.divineEcology.bond(god.id,c.population.speciesKey,c.population.areaId);
  if(b.selectedAt<=0){b.selectedAt=now;b.devotion=unit(.07+c.affinity*.11);b.offeringAffinity=offeringPotential(c.population.speciesKey);god.followersChosen++;}
  WorldEventBus.publishId(s,now,"divine_follower_"+god.id+"_"+c.population.speciesKey+"_"+c.population.areaId,"DIVINE_FOLLOWER_CHOSEN",c.population.speciesKey,"Thần Con tự chọn một quần thể "+c.population.speciesKey+" làm Tín đồ tại "+c.population.areaId+"; lựa chọn dựa trên trạng thái sống hiện tại, không phải danh sách loài cố định.");
 }

 private static void advanceBond(WorldState s,MinorGodState god,WorshipBondState b,double minutes,double days,long now){
  SpeciesPopulationState p=s.livingWorld.populations.get(LivingWorldState.populationKey(b.speciesKey,b.areaId));if(p==null)return;p.clamp();
  double health=unit(.38*p.relativeAbundance+.26*p.recoveryPressure+.20*(1-p.resourcePressure)+.16*(1-p.weatherPressure));
  b.devotion=unit(b.devotion+days*(.018*health+.004*god.maturity-.012*p.mortalityPressure));
  double target=unit(.06+b.devotion*.48+god.maturity*.20+god.graceReserve*.18);
  b.blessingPower=follow(b.blessingPower,target,minutes,360);
  double blessingDays=Math.min(days,1.0);
  p.recoveryPressure=unit(p.recoveryPressure+b.blessingPower*.018*blessingDays);
  p.carryingCapacity=unit(p.carryingCapacity+b.blessingPower*.006*blessingDays);
  p.mortalityPressure=unit(p.mortalityPressure-b.blessingPower*.006*blessingDays);
  god.graceReserve=unit(god.graceReserve-b.blessingPower*.0025*blessingDays);
  b.lastBlessedAt=now;god.lastBlessingAt=now;p.clamp();
  applyVisibleBlessing(s,b,blessingDays);

  if(canOffer(b,p,now)){
   double cost=Math.min(.012,Math.max(.0025,p.relativeAbundance*.015));
   p.relativeAbundance=unit(p.relativeAbundance-cost);
   b.cumulativePopulationCost=Math.max(0,b.cumulativePopulationCost+cost);b.totalOfferings++;b.lastOfferingAt=now;
   god.totalOfferings++;god.lastOfferingAt=now;god.graceReserve=unit(god.graceReserve+cost*5.0+b.devotion*.006);b.blessingPower=unit(b.blessingPower+cost*2.5+b.devotion*.004);
   WorldEventBus.publishId(s,now,"divine_offering_"+god.id+"_"+b.speciesKey+"_"+Long.toHexString(now),"DIVINE_OFFERING_MADE",b.speciesKey,"Một phần quần thể Tín đồ "+b.speciesKey+" tự dâng hiến; population cost="+round(cost)+", quyền năng dự trữ Thần Con và blessing của chính Tín đồ tăng nhưng không miễn phí.");
   publishObservableOfferingEffect(s,b,cost,now);
  }
 }

 private static void applyVisibleBlessing(WorldState s,WorshipBondState b,double days){
  if(s==null||s.world==null||s.livingWorld==null||b==null||days<=0)return;
  double pulse=unit(b.blessingPower)*Math.min(1,days);
  if(pulse<=0)return;
  for(WorldObject o:s.world.objects){
   if(o==null||!o.enabled||!"creature".equals(o.type)||!b.areaId.equals(HaruVisionEngine.actualAreaId(s,o))||!b.speciesKey.equals(FantasyEcologyDictionary.keyForObject(o)))continue;
   CreatureLifeState life=s.livingWorld.creatures.get(o.id);if(life==null)continue;
   if(life.body==null)life.body=new OrganismBodyState();if(life.cycle==null)life.cycle=new OrganismCycleState();
   life.body.vitalReserve=unit(life.body.vitalReserve+pulse*.032);
   life.body.surfaceIntegrity=unit(life.body.surfaceIntegrity+pulse*.018);
   life.body.strain=unit(life.body.strain-pulse*.026);
   life.cycle.recoveryDrive=unit(life.cycle.recoveryDrive+pulse*.024);
  }
 }

 private static void publishObservableOfferingEffect(WorldState s,WorshipBondState b,double cost,long now){
  if(s==null||s.world==null||b==null)return;
  WorldArea haru=s.world.areaAt(s.haruX);if(haru==null||!b.areaId.equals(haru.id))return;
  WorldObject visible=null;
  for(WorldObject o:s.world.objects){
   if(o==null||!o.enabled||!"creature".equals(o.type))continue;
   if(!b.areaId.equals(HaruVisionEngine.actualAreaId(s,o))||!b.speciesKey.equals(FantasyEcologyDictionary.keyForObject(o)))continue;
   visible=o;break;
  }
  if(visible==null)return;
  WorldEventBus.publishId(s,now,"visible_ecology_pulse_"+visible.id+"_"+Long.toHexString(now),"VISIBLE_UNEXPLAINED_ECOLOGY_PULSE",visible.id,"area="+b.areaId+" species="+b.speciesKey+" recovery_shift="+round(b.blessingPower)+" population_shift="+round(cost));
 }

 private static boolean canOffer(WorshipBondState b,SpeciesPopulationState p,long now){
  return b!=null&&p!=null&&b.offeringAffinity>=.68&&b.devotion>=.45&&p.relativeAbundance>.20&&p.resourcePressure<.65&&p.mortalityPressure<.70&&(b.lastOfferingAt<=0||now-b.lastOfferingAt>=OFFERING_COOLDOWN_MS);
 }

 public static boolean eligibleForOffering(String speciesKey){return offeringPotential(speciesKey)>=.68;}
 public static double offeringPotential(String speciesKey){
  SpeciesEvolutionCatalog.Species s=SpeciesEvolutionCatalog.get(speciesKey);if(s==null)return 0;
  double stable=stable01("offering|"+s.traitFingerprint);
  double social=("nursery-colony".equals(s.sociality)||"trail-network".equals(s.sociality)||"seasonal-gathering".equals(s.sociality)||"call-and-response".equals(s.sociality))?.88:.32;
  double repro=("carried seed brood".equals(s.reproduction)||"root-nursery brood".equals(s.reproduction)||"hollow-shell nursery".equals(s.reproduction))?.78:.38;
  return unit(stable*.52+social*.30+repro*.18);
 }

 public static JSONObject godContext(WorldState s){
  JSONObject root=new JSONObject();try{
   root.put("visibility","GOD_SYSTEM_ONLY");root.put("playerSelectsFollowers",false);root.put("followersAreChosenByChildGod",true);root.put("offeringHasPopulationCost",true);
   JSONArray gods=new JSONArray();if(s!=null&&s.divineEcology!=null)for(MinorGodState g:s.divineEcology.minorGods.values()){
    JSONObject x=new JSONObject();x.put("id",g.id);x.put("title",g.title);x.put("maturity",band(g.maturity));x.put("graceReserve",band(g.graceReserve));x.put("followersChosen",g.followersChosen);x.put("totalOfferings",g.totalOfferings);
    JSONArray followers=new JSONArray();for(WorshipBondState b:s.divineEcology.bondsForGod(g.id)){JSONObject y=new JSONObject();y.put("speciesKey",b.speciesKey);y.put("areaId",b.areaId);y.put("devotion",band(b.devotion));y.put("blessingPower",band(b.blessingPower));y.put("canEverOffer",b.offeringAffinity>=.68);y.put("offerings",b.totalOfferings);followers.put(y);}x.put("followers",followers);gods.put(x);
   }root.put("childGods",gods);
  }catch(Exception ignored){}return root;
 }

 private static Candidate bestCandidate(WorldState s,String godId,Set<String> excluded){
  Candidate best=null;if(s==null||s.livingWorld==null)return null;
  for(SpeciesPopulationState p:s.livingWorld.populations.values()){
   if(p==null)continue;p.clamp();String key=LivingWorldState.populationKey(p.speciesKey,p.areaId);if(excluded.contains(key)||p.relativeAbundance<.025||p.carryingCapacity<.035||!SpeciesEvolutionCatalog.isBaseSpecies(p.speciesKey))continue;
   SpeciesEvolutionCatalog.Species sp=SpeciesEvolutionCatalog.get(p.speciesKey);double affinity=stable01(godId+"|"+sp.traitFingerprint);
   double score=p.relativeAbundance*.34+p.recoveryPressure*.22+(1-p.resourcePressure)*.18+(1-p.weatherPressure)*.10+affinity*.16;
   if(best==null||score>best.score||score==best.score&&key.compareTo(best.key)<0)best=new Candidate(key,p,score,affinity);
  }
  return best;
 }
 private static double stable01(String raw){int h=raw==null?0:raw.hashCode();long x=h&0x7fffffffL;return (x%10000L)/9999.0;}
 private static double follow(double v,double target,double minutes,double tau){v=unit(v);target=unit(target);double k=1-Math.exp(-Math.max(0,minutes)/Math.max(.05,tau));return unit(v+(target-v)*k);}
 private static double unit(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
 private static String band(double v){v=unit(v);return v<.2?"very_low":v<.4?"low":v<.65?"moderate":v<.85?"high":"very_high";}
 private static String round(double v){return String.format(Locale.US,"%.4f",v);}
 private static final class Candidate{final String key;final SpeciesPopulationState population;final double score,affinity;Candidate(String k,SpeciesPopulationState p,double s,double a){key=k;population=p;score=s;affinity=a;}}
}
