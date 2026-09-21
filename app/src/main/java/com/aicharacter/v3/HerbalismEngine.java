package com.aicharacter.v3;
import java.util.*;
/** Haru learns fictional VNF herbs only from observation and real trial outcomes. No real-world medical guidance. */
public final class HerbalismEngine{
 private static final String[] METHODS={"DEW_FOLD","LUMEN_REST","ECHO_DRY"};
 private HerbalismEngine(){}
 public static boolean isHerb(WorldObject o){return o!=null&&"herb".equals(o.type)&&HerbalDictionary.get(o.dictionaryRef)!=null;}
 public static HerbDefinition definition(WorldObject o){return isHerb(o)?HerbalDictionary.get(o.dictionaryRef):null;}
 public static boolean observe(WorldState s,WorldObject o,long now){
  HerbDefinition d=definition(o);if(s==null||d==null||!HaruVisionEngine.canSee(s,o))return false;ensure(s);HerbalismState.Knowledge k=s.herbalism.know(d.id);k.observations=Math.min(20,k.observations+1);s.knowledge.put("herb:"+d.id,Math.min(4,Math.max(s.knowledge.getOrDefault("herb:"+d.id,0),k.observations>=4?2:1)));s.herbalism.lastUpdatedAt=now;
  CognitionEngine.process(s,new Experience("herb_observation","She noticed "+d.appearance+" but did not yet know whether it was useful.",.04,.34).atTime(now).at(o.areaId,o.id).tag("herb").tag("observe").tag("herb:"+d.id));return true;
 }
 public static boolean harvest(WorldState s,WorldObject o,long now){
  HerbDefinition d=definition(o);if(s==null||d==null)return false;ensure(s);HerbalismState.Knowledge k=s.herbalism.know(d.id);if(k.observations<=0)return false;if(s.herbalism.carriedCount>0&&!d.id.equals(s.herbalism.carriedHerbId))return false;if(s.herbalism.carriedCount>=3)return false;s.herbalism.carriedHerbId=d.id;s.herbalism.carriedCount++;s.herbalism.lastUpdatedAt=now;
  CognitionEngine.process(s,new Experience("herb_sample","She carefully collected a small sample of an unfamiliar plant for later study.",.02,.38).atTime(now).at(o.areaId,o.id).tag("herb").tag("sample").tag("herb:"+d.id));return true;
 }
 public static String chooseExperimentMethod(WorldState s){
  ensure(s);String id=s.herbalism.carriedHerbId;if(id.isEmpty())return"";HerbalismState.Knowledge k=s.herbalism.know(id);if(!k.discoveredPreparation.isEmpty())return k.discoveredPreparation;int seed=Math.abs(Objects.hash(id,k.trials));return METHODS[seed%METHODS.length];
 }
 public static boolean prepareAtHome(WorldState s,long now){
  ensure(s);if(s.herbalism.carriedCount<=0||s.herbalism.carriedHerbId.isEmpty())return false;HerbDefinition d=HerbalDictionary.get(s.herbalism.carriedHerbId);if(d==null)return false;String method=chooseExperimentMethod(s);HerbalismState.Knowledge k=s.herbalism.know(d.id);boolean correct=method.equals(d.preferredPreparation);s.herbalism.preparedHerbId=d.id;s.herbalism.preparedMethod=method;s.herbalism.preparedPotency=correct?1.0:.32;s.herbalism.preparedRisk=correct?d.rawRisk*.35:d.wrongPrepRisk;s.herbalism.carriedCount--;if(s.herbalism.carriedCount<=0)s.herbalism.carriedHerbId="";k.lastMethod=method;s.herbalism.lastUpdatedAt=now;
  CognitionEngine.process(s,new Experience("herb_preparation","She tried a small, cautious preparation method on the plant sample without knowing if it was the right one.",.01,.44).atTime(now).at("home_shelter","shelter_01").tag("herb").tag("experiment").tag("herb:"+d.id).tag("method:"+method));return true;
 }
 public static boolean tryPrepared(WorldState s,long now){
  ensure(s);if(s.herbalism.preparedHerbId.isEmpty())return false;HerbDefinition d=HerbalDictionary.get(s.herbalism.preparedHerbId);if(d==null)return false;HerbalismState.Knowledge k=s.herbalism.know(d.id);double potency=s.herbalism.preparedPotency,risk=s.herbalism.preparedRisk,need=bodyNeed(s);double scale=.20+.35*Math.min(1,need);double benefit=0;
  if(s.immune!=null&&s.immune.inflammation>0){double x=Math.min(s.immune.inflammation,d.inflammationRelief*potency*scale);s.immune.inflammation=Math.max(0,s.immune.inflammation-x);benefit+=x;}
  if(s.skin!=null){double x=Math.min(s.skin.surfaceDamage,d.skinRepair*potency*scale);s.skin.surfaceDamage=Math.max(0,s.skin.surfaceDamage-x);s.skin.irritation=Math.max(0,s.skin.irritation-x*.55);benefit+=x;}
  if(s.endocrine!=null&&s.endocrine.stressResponse>0){double x=Math.min(s.endocrine.stressResponse,d.calming*potency*scale);s.endocrine.stressResponse=Math.max(0,s.endocrine.stressResponse-x);benefit+=x;}
  double harm=risk*(.08+.12*(1-k.safetyConfidence));if(harm>0){if(s.skin!=null)s.skin.irritation=Math.min(1,s.skin.irritation+harm*.55);if(s.endocrine!=null)s.endocrine.stressResponse=Math.min(1,s.endocrine.stressResponse+harm*.35);s.body.energy=Math.max(0,s.body.energy-harm*7);s.body.clamp();}
  k.trials++;boolean positive=benefit>harm*.45;k.safetyConfidence=cl(k.safetyConfidence+(positive?.12:-.06));k.effectConfidence=cl(k.effectConfidence+(benefit>.025?.15:.025));k.lastObservedEffect=positive?(benefit>.06?"noticeable relief":"mild relief"):(harm>.035?"unpleasant reaction":"no clear effect");
  if(s.herbalism.preparedMethod.equals(d.preferredPreparation)&&positive&&k.trials>=2&&k.effectConfidence>=.28)k.discoveredPreparation=d.preferredPreparation;
  s.knowledge.put("herb:"+d.id,Math.min(4,1+(k.trials>=1?1:0)+(k.discoveredPreparation.isEmpty()?0:1)));String method=s.herbalism.preparedMethod;s.herbalism.preparedHerbId="";s.herbalism.preparedMethod="";s.herbalism.preparedPotency=0;s.herbalism.preparedRisk=0;s.herbalism.lastUpdatedAt=now;
  double valence=positive?Math.min(.18,.04+benefit):Math.max(-.20,-harm);CognitionEngine.process(s,new Experience("herb_trial",positive?"The small trial seemed to help a little.":"The small trial did not help clearly and may have felt unpleasant.",valence,.52).atTime(now).at("home_shelter","shelter_01").tag("herb").tag("experiment").tag("herb:"+d.id).tag("method:"+method).tag(positive?"helped":"uncertain"));return true;
 }
 public static double bodyNeed(WorldState s){if(s==null)return 0;double inf=s.immune==null?0:s.immune.inflammation,skin=s.skin==null?0:Math.max(s.skin.surfaceDamage,s.skin.irritation),stress=s.endocrine==null?0:s.endocrine.stressResponse,pain=s.body==null?0:s.body.pain/100.0;return cl(Math.max(Math.max(inf,skin),Math.max(stress*.72,pain*.55)));}
 public static WorldObject nearestHerb(WorldState s){if(s==null||s.world==null)return null;WorldArea a=s.world.areaAt(s.haruX);WorldObject best=null;double d=Double.MAX_VALUE;for(WorldObject o:s.world.objects){if(!isHerb(o)||a==null||!a.id.equals(o.areaId))continue;double x=Math.abs(o.x-s.haruX);if(x<d){d=x;best=o;}}return best;}
 public static String diagnostic(WorldState s){ensure(s);StringBuilder b=new StringBuilder("HERBAL TRACE\ncarried=").append(s.herbalism.carriedHerbId).append(" x").append(s.herbalism.carriedCount).append(" prepared=").append(s.herbalism.preparedHerbId).append("/").append(s.herbalism.preparedMethod).append('\n');for(HerbDefinition d:HerbalDictionary.all()){HerbalismState.Knowledge k=s.herbalism.knowledge.get(d.id);b.append(d.label).append(" | biome=").append(d.biomeId);if(k==null)b.append(" | Haru: unknown");else b.append(" | obs=").append(k.observations).append(" trials=").append(k.trials).append(" safety=").append(String.format(Locale.US,"%.2f",k.safetyConfidence)).append(" effect=").append(String.format(Locale.US,"%.2f",k.effectConfidence)).append(" learnedPrep=").append(k.discoveredPreparation.isEmpty()?"?":k.discoveredPreparation).append(" last=").append(k.lastObservedEffect);b.append('\n');}return b.toString();}
 private static void ensure(WorldState s){if(s.herbalism==null)s.herbalism=new HerbalismState();}
 private static double cl(double v){return Math.max(0,Math.min(1,v));}
}
