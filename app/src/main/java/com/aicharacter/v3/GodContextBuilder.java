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
            root.put("protocolVersion",4);
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
            root.put("catAwake", s.catState!=null && s.catState.awake);
            root.put("catAttachedToGirl", s.catState!=null && "girl".equals(s.catState.attachedToEntity));

            JSONArray events=new JSONArray();int start=Math.max(0,s.worldHistory.size()-10);
            for(int i=start;i<s.worldHistory.size();i++){WorldHistoryEntry e=s.worldHistory.get(i);JSONObject j=new JSONObject();j.put("type",e.type);j.put("summary",e.summary);j.put("timestamp",e.time);events.put(j);}root.put("recentWorldEvents",events);
            JSONArray godInbox=new JSONArray();int gi=Math.max(0,s.godInbox.size()-6);for(int i=gi;i<s.godInbox.size();i++)godInbox.put(s.godInbox.get(i).text);root.put("recentGodMessages",godInbox);

            root.put("godIdentity","You are Thần (God/System) of this VNF world, not the girl and not the player.");
            root.put("capabilities",GodCapabilityModel.toJson());
            root.put("worldObservation",GodObservationSnapshot.build(s));
            root.put("referenceKnowledgeResults",GodKnowledgeQueryEngine.toJson(GodKnowledgeQueryEngine.query(s,playerText,6)));
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
                    x.put("layer","REAL_REFERENCE");x.put("worldTruth",false);
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

            root.put("godPolicy","You are Thần, the persistent VNF God/System presence. Capabilities are explicitly bounded to OBSERVE, EXPLAIN, REFERENCE_KNOWLEDGE, TEACH, WARN, PROPOSE_WORLD_CONDITION, DIAGNOSE and SAFE_REPAIR. Observe the supplied qualitative worldObservation across environment/atmosphere, living world/population interface, recent causal history, Haru visible condition, current plan/outcome and abnormalities. Do not expose raw Haru internal scores in ordinary player-facing answers. Never control Haru, rewrite her mind, fabricate memory/history, set emotion/personality/relationship/intention, or claim a proposal already happened. referenceKnowledgeResults and worldKnowledgeLibrary distinguish REAL_REFERENCE from VNF_WORLD_TRUTH; preserve source/provenance/confidence and never convert an external reference into fictional truth automatically. TEACH creates a durable lesson opportunity only: Haru may LISTEN, DEFER, QUESTION, REJECT or PARTIALLY_UNDERSTAND, and learning requires her causal evidence pipeline. Skill requires practice/outcome. World interventions are bounded proposals with condition, desiredValue, GLOBAL/AREA scope, intensity, duration, provenance and rollback policy. WEATHER, WIND, HUMIDITY_MIST, TEMPERATURE, LIGHT and legacy ATMOSPHERE_PERTURBATION are contract types; new conditions are for World/Physics consumers and are not direct physics writes. SAFE_REPAIR is restricted to signed runtime world-content repair/rollback and never APK/core code or Haru cognition. Reply naturally and concisely in Vietnamese; if evidence is insufficient, say so.");
        }catch(Exception ignored){}
        return root;
    }
}
