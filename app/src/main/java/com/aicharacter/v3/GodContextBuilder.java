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
                rel.put("affection",s.relationship.affection);rel.put("trust",s.relationship.trust);rel.put("attachment",s.relationship.attachment);rel.put("comfort",s.relationship.comfort);rel.put("gratitude",s.relationship.gratitude);rel.put("irritation",s.relationship.irritation);rel.put("hurt",s.relationship.hurt);
            }
            root.put("relationship",rel);

            JSONArray events=new JSONArray();int start=Math.max(0,s.worldHistory.size()-10);
            for(int i=start;i<s.worldHistory.size();i++){WorldHistoryEntry e=s.worldHistory.get(i);JSONObject j=new JSONObject();j.put("type",e.type);j.put("summary",e.summary);j.put("timestamp",e.time);events.put(j);}root.put("recentWorldEvents",events);
            JSONArray godInbox=new JSONArray();int gi=Math.max(0,s.godInbox.size()-6);for(int i=gi;i<s.godInbox.size();i++)godInbox.put(s.godInbox.get(i).text);root.put("recentGodMessages",godInbox);

            root.put("godIdentity","You are Thần (God/System) of this VNF world, not the girl and not the player.");
            root.put("godMemory",s.godMemory==null?new JSONArray():s.godMemory.recallFor(playerText));
            JSONArray library=new JSONArray();
            if(s.world!=null){
                int max=Math.min(12,s.world.knowledgeAnchors.size());
                for(int i=0;i<max;i++){
                    WorldKnowledgeAnchor a=s.world.knowledgeAnchors.get(i);
                    JSONObject x=new JSONObject();
                    x.put("id",a.id);x.put("domain",a.domain);x.put("realAnchor",a.realAnchor);
                    x.put("fantasyRule",a.fantasyRule);x.put("sourceFamily",a.sourceFamily);x.put("sourceRef",a.sourceRef);
                    x.put("retrievedAt",a.retrievedAt);x.put("confidence",a.confidence<0?JSONObject.NULL:a.confidence);x.put("limits",a.limits);
                    x.put("layer","REAL_WORLD_REFERENCE");x.put("worldTruth",false);
                    library.put(x);
                }
            }
            root.put("worldKnowledgeLibrary",library);

            // Technical snapshots are opt-in. Ordinary contact with Thần gets lived world reality,
            // not coordinates, runtime filenames, updater internals or developer diagnostics.
            boolean technical=GodCodeVision.isTechnicalCodeQuestion(playerText);
            root.put("technicalDiagnosticsRequested",technical);
            root.put("codeVision",GodCodeVision.build(appContext,playerText));
            if(technical){
                JSONObject contentUpdater=new JSONObject();contentUpdater.put("configured",VnfOnlineConfig.contentUpdaterConfigured());contentUpdater.put("runtimeRevision",RuntimeContentStore.revision(appContext));contentUpdater.put("hasCheckpoint",RuntimeContentStore.latestCheckpoint(appContext)!=null);contentUpdater.put("writableDomain","runtime world content only: graphics/assets + future world data; NOT APK/DEX/Java/save schema/girl mind");root.put("contentUpdater",contentUpdater);
                root.put("worldAccess",GodWorldAccess.snapshot(appContext,s));
            }

            root.put("godPolicy","You are Thần, the persistent VNF God/System contact. You know that you are Thần. Ground every answer in supplied System Reality and godMemory. You may observe, explain, warn, diagnose and suggest. Never directly control the girl, rewrite her mind, fabricate memories, backdate events, or claim an action happened when it did not. You may PROPOSE a new present-time bounded world condition only through worldEventProposal; a proposal is not history and must not describe a result as already happened. Supported proposal types are WEATHER_CLEAR, WEATHER_CLOUDY, WEATHER_RAIN. Use a stable unique id matching [A-Za-z0-9._-]{1,80}, requestedAt must be the present or future, and do not repeat an already-issued id. The girl is autonomous. Do not expose exact internal scores/coordinates unless the player explicitly asks for technical diagnostics. When codeVision.enabled=true you may inspect the supplied READ-ONLY build snapshot to explain architecture and trace bugs. Technical worldAccess/contentUpdater data exists only when technicalDiagnosticsRequested=true. A signed runtime world-content updater may replace bounded graphics/data in its private content store and can rollback checkpoints. Only report a world-content repair as applied after the updater confirms success. Never claim you edited/recompiled/deployed APK/core code. Use worldKnowledgeLibrary only as REAL-WORLD REFERENCE with provenance, never as proof that a fictional fact already exists in VNF. Broad real-world answers may be explained as reference, but must not be written into VNF history, Haru memory/knowledge/personality/emotion/relationship/intention merely because you answered them. Teaching creates only a present-time lesson opportunity; Haru decides whether to listen and learns through her own causal pipeline. Real concepts must be transformed through fantasyRule before proposing authored VNF content. Reply naturally and concisely in Vietnamese. If context is insufficient, say you do not know.");
        }catch(Exception ignored){}
        return root;
    }
}
