package com.aicharacter.v3;
import org.json.JSONArray;import org.json.JSONObject;import java.util.*;
public final class ThoughtState {public String subject,trigger,possibleIntention;public double uncertainty,emotionalWeight;public long createdAt;public final List<String> relatedMemories=new ArrayList<>();
 public ThoughtState(String s,String t,String i,double u,double w,long at){subject=s;trigger=t;possibleIntention=i;uncertainty=u;emotionalWeight=w;createdAt=at;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("subject",subject);j.put("trigger",trigger);j.put("possibleIntention",possibleIntention);j.put("uncertainty",uncertainty);j.put("emotionalWeight",emotionalWeight);j.put("createdAt",createdAt);j.put("relatedMemories",new JSONArray(relatedMemories));}catch(Exception ignored){}return j;}
 public static ThoughtState fromJson(JSONObject j){ThoughtState t=new ThoughtState(j.optString("subject"),j.optString("trigger"),j.optString("possibleIntention"),j.optDouble("uncertainty"),j.optDouble("emotionalWeight"),j.optLong("createdAt"));JSONArray a=j.optJSONArray("relatedMemories");if(a!=null)for(int i=0;i<a.length();i++)t.relatedMemories.add(a.optString(i));return t;}
}
