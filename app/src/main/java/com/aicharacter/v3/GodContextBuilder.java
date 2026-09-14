package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;

public final class GodContextBuilder {
    private GodContextBuilder(){}

    public static JSONObject build(WorldState s, String playerText){
        JSONObject root=new JSONObject();
        try{
            root.put("playerText", playerText==null?"":playerText.trim());
            root.put("girlDisplayName", DisplayNames.girl(s));
            root.put("girlOfficiallyNamed", s.nameState!=null && s.nameState.isNamed());
            root.put("worldMinutes", s.worldMinutes);
            root.put("weather", s.environment==null?"UNKNOWN":s.environment.weather);
            root.put("girlActivity", s.haruActivity);
            root.put("girlMoodLabel", s.haruMood);
            root.put("currentIntention", s.currentIntention);
            root.put("bodyEnergy", s.body==null?50:s.body.energy);
            root.put("bodyPain", s.body==null?0:s.body.pain);
            root.put("emotion", s.emotion==null?"unknown":s.emotion.dominant());

            JSONObject rel=new JSONObject();
            if(s.relationship!=null){
                rel.put("affection",s.relationship.affection);
                rel.put("trust",s.relationship.trust);
                rel.put("attachment",s.relationship.attachment);
                rel.put("comfort",s.relationship.comfort);
                rel.put("gratitude",s.relationship.gratitude);
                rel.put("irritation",s.relationship.irritation);
                rel.put("hurt",s.relationship.hurt);
            }
            root.put("relationship",rel);

            JSONArray events=new JSONArray();
            int start=Math.max(0,s.worldHistory.size()-8);
            for(int i=start;i<s.worldHistory.size();i++){
                WorldHistoryEntry e=s.worldHistory.get(i);
                JSONObject j=new JSONObject();
                j.put("type",e.type);
                j.put("summary",e.summary);
                j.put("timestamp",e.time);
                events.put(j);
            }
            root.put("recentWorldEvents",events);

            JSONArray godInbox=new JSONArray();
            int gi=Math.max(0,s.godInbox.size()-5);
            for(int i=gi;i<s.godInbox.size();i++) godInbox.put(s.godInbox.get(i).text);
            root.put("recentGodMessages",godInbox);

            // System Reality may know precise state, but the backend is instructed not to reveal
            // internal coordinates/scores unless the user genuinely needs a technical diagnosis.
            root.put("systemRealityNote","You are God/SYSTEM for VNF. Observe and advise; never control the girl or rewrite her mind.");
        }catch(Exception ignored){}
        return root;
    }
}
