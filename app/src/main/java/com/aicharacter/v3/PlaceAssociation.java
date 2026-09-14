package com.aicharacter.v3;import org.json.JSONObject;
public final class PlaceAssociation {public String id;public double warmth,unease,familiarity;public int experiences;public long lastVisit;
 public PlaceAssociation(String id){this.id=id;}public void learn(double valence,double significance,long now){experiences++;familiarity=Math.min(1,familiarity+.04);if(valence>=0)warmth=Math.min(1,warmth+valence*.06*significance);else unease=Math.min(1,unease-valence*.07*significance);lastVisit=now;}
 public double attraction(){return warmth+familiarity*.25-unease;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("warmth",warmth);j.put("unease",unease);j.put("familiarity",familiarity);j.put("experiences",experiences);j.put("lastVisit",lastVisit);}catch(Exception ignored){}return j;}
 public static PlaceAssociation fromJson(String id,JSONObject j){PlaceAssociation p=new PlaceAssociation(id);p.warmth=j.optDouble("warmth");p.unease=j.optDouble("unease");p.familiarity=j.optDouble("familiarity");p.experiences=j.optInt("experiences");p.lastVisit=j.optLong("lastVisit");return p;}
}
