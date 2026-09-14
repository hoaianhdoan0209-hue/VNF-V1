package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;

/** Builds bounded System-Reality context for the online God channel only. */
public final class GodContextBuilder {
    private GodContextBuilder(){}

    public static JSONObject build(WorldState s, String playerText){
        JSONObject root=new JSONObject();
        try{
            root.put("protocolVersion",2);
            root.put("playerText", playerText==null?"":playerText.trim());
            root.put("girlDisplayName", DisplayNames.girl(s));
            root.put("girlOfficiallyNamed", s.nameState!=null && s.nameState.isNamed());
            root.put("worldMinutes", s.worldMinutes);
            root.put("weather", s.environment==null?"UNKNOWN":s.environment.weather);
            root.put("dayPhase", s.environment==null?"UNKNOWN":s.environment.dayPhase(s.worldMinutes));
            WorldArea girlArea=s.world==null?null:s.world.areaAt(s.haruX);
            root.put("girlArea", girlArea==null?"unknown":girlArea.id);
            root.put("girlActivity", s.haruActivity);
            root.put("girlMoodLabel", s.haruMood);
            root.put("currentIntention", s.currentIntention);
            root.put("bodyEnergy", s.body==null?50:s.body.energy);
            root.put("bodyPain", s.body==null?0:s.body.pain);
            root.put("emotion", s.emotion==null?"unknown":s.emotion.dominant());
            root.put("catAwake", s.catState!=null && s.catState.awake);
            root.put("catAttachedToGirl", s.catState!=null && "girl".equals(s.catState.attachedToEntity));

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
            int start=Math.max(0,s.worldHistory.size()-10);
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
            int gi=Math.max(0,s.godInbox.size()-6);
            for(int i=gi;i<s.godInbox.size();i++) godInbox.put(s.godInbox.get(i).text);
            root.put("recentGodMessages",godInbox);

            root.put("godPolicy",
                    "You are the VNF God/System contact. Ground every answer in supplied System Reality. "+
                    "You may observe, explain, warn, diagnose and suggest. Never directly control the girl, "+
                    "rewrite her mind, invent world events, fabricate memories, or claim an action happened when it did not. "+
                    "The girl is autonomous. Do not expose exact internal scores/coordinates unless the player explicitly asks for technical diagnostics. "+
                    "Reply naturally and concisely in Vietnamese. If context is insufficient, say you do not know.");
        }catch(Exception ignored){}
        return root;
    }
}
