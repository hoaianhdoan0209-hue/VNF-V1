package com.aicharacter.v3;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

/** Builds bounded System-Reality context for the online God channel only. */
public final class GodContextBuilder {
    private GodContextBuilder(){}

    public static JSONObject build(Context appContext, WorldState s, String playerText){
        JSONObject root=new JSONObject();
        try{
            root.put("protocolVersion",3);
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

            // God may inspect a bounded, read-only snapshot of the exact world/game code
            // compiled into this APK, but ONLY for technical/code questions.
            root.put("codeVision", GodCodeVision.build(appContext, playerText));

            JSONObject contentUpdater=new JSONObject();
            contentUpdater.put("configured",VnfOnlineConfig.contentUpdaterConfigured());
            contentUpdater.put("runtimeRevision",RuntimeContentStore.revision(appContext));
            contentUpdater.put("hasCheckpoint",RuntimeContentStore.latestCheckpoint(appContext)!=null);
            contentUpdater.put("writableDomain","runtime world content only: graphics/assets + future world data; NOT APK/DEX/Java/save schema/girl mind");
            root.put("contentUpdater",contentUpdater);

            root.put("godPolicy",
                    "You are the VNF God/System contact. Ground every answer in supplied System Reality. "+
                    "You may observe, explain, warn, diagnose and suggest. Never directly control the girl, "+
                    "rewrite her mind, invent world events, fabricate memories, or claim an action happened when it did not. "+
                    "The girl is autonomous. Do not expose exact internal scores/coordinates unless the player explicitly asks for technical diagnostics. "+
                    "When codeVision.enabled=true you may inspect the supplied READ-ONLY build snapshot to explain architecture and trace bugs. " +
                    "A signed runtime world-content updater may replace graphics/data in its private content store and can rollback checkpoints. " +
                    "Only report a world-content repair as applied after the updater confirms success. Never claim you edited/recompiled/deployed APK/core code. " +
                    "Reply naturally and concisely in Vietnamese. If context is insufficient, say you do not know.");
        }catch(Exception ignored){}
        return root;
    }
}
