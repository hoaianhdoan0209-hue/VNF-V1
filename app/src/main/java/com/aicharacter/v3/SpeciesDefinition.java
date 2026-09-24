package com.aicharacter.v3;

import java.util.*;

/** Immutable authored identity of one current base species. Variants/hybrids/mutations are not SpeciesDefinition entries. */
public final class SpeciesDefinition {
 public final String key,systemName,lineageId,lifeForm,bodyPlan,locomotion,metabolism,sensorySignature,trophicNiche,reproduction,sociality,cognitionPotential,communication,preferredAreaTags,resourceTags,attractCreatureTags,avoidCreatureTags,stimulateFloraTags,traitFingerprint;
 public final double resourceAffinity,migrationDrive,groupTolerance,stimulusStrength;
 public final boolean localPresenceEligible;

 public SpeciesDefinition(String key,String systemName,String lineageId,String lifeForm,String bodyPlan,String locomotion,String metabolism,String sensorySignature,String trophicNiche,String reproduction,String sociality,String cognitionPotential,String communication,String preferredAreaTags,String resourceTags,String attractCreatureTags,String avoidCreatureTags,String stimulateFloraTags,double resourceAffinity,double migrationDrive,double groupTolerance,double stimulusStrength,boolean localPresenceEligible){
  this.key=safe(key);this.systemName=safe(systemName);this.lineageId=safe(lineageId);this.lifeForm=safe(lifeForm);this.bodyPlan=safe(bodyPlan);this.locomotion=safe(locomotion);this.metabolism=safe(metabolism);this.sensorySignature=safe(sensorySignature);this.trophicNiche=safe(trophicNiche);this.reproduction=safe(reproduction);this.sociality=safe(sociality);this.cognitionPotential=safe(cognitionPotential);this.communication=safe(communication);this.preferredAreaTags=safe(preferredAreaTags);this.resourceTags=safe(resourceTags);this.attractCreatureTags=safe(attractCreatureTags);this.avoidCreatureTags=safe(avoidCreatureTags);this.stimulateFloraTags=safe(stimulateFloraTags);this.resourceAffinity=unit(resourceAffinity);this.migrationDrive=unit(migrationDrive);this.groupTolerance=unit(groupTolerance);this.stimulusStrength=unit(stimulusStrength);this.localPresenceEligible=localPresenceEligible;
  this.traitFingerprint=String.join("|",this.lifeForm,this.bodyPlan,this.locomotion,this.metabolism,this.sensorySignature,this.trophicNiche,this.reproduction,this.sociality,this.cognitionPotential,this.communication,this.preferredAreaTags,fmt(this.resourceAffinity),fmt(this.migrationDrive),fmt(this.groupTolerance),fmt(this.stimulusStrength));
 }
 public SpeciesEcologyProfile ecologyProfile(){return new SpeciesEcologyProfile(key,preferredAreaTags,resourceTags,attractCreatureTags,avoidCreatureTags,stimulateFloraTags,resourceAffinity,migrationDrive,groupTolerance,stimulusStrength);}
 private static String safe(String s){return s==null?"":s.trim();}
 private static double unit(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):0;}
 private static String fmt(double v){return String.format(Locale.US,"%.4f",v);}
}
