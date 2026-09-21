package com.aicharacter.v3;
/** Hidden System-Reality ecology for an original VNF species. Haru can only infer these relations from observation. */
public final class SpeciesEcologyProfile{
 public final String key;public final String preferredAreaTags,resourceTags,attractCreatureTags,avoidCreatureTags,stimulateFloraTags;public final double resourceAffinity,migrationDrive,groupTolerance,stimulusStrength;
 public SpeciesEcologyProfile(String k,String area,String resources,String attract,String avoid,String flora,double resource,double migrate,double group,double stimulus){key=k;preferredAreaTags=area;resourceTags=resources;attractCreatureTags=attract;avoidCreatureTags=avoid;stimulateFloraTags=flora;resourceAffinity=resource;migrationDrive=migrate;groupTolerance=group;stimulusStrength=stimulus;}
}