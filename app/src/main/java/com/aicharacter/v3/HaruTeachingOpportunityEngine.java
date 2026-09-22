package com.aicharacter.v3;

import java.util.Locale;

/**
 * Converts a durable divine lesson offer into a Haru-owned learning opportunity.
 *
 * Contract:
 * - God may create only the external offer event.
 * - Haru decides whether to engage from her current body/personality state.
 * - Haru memory/knowledge is written only after that causal event exists.
 * - Skill teaching never installs a skill or action repertoire entry; practice must
 *   still happen through ordinary lived action/outcome learning.
 *
 * This engine is called from HaruAutonomyEngine.advanceMindBody(), which is used
 * by the shared LifeSimulationKernel for both ACTIVE and OFFLINE slices.
 */
public final class HaruTeachingOpportunityEngine {
    static final String OFFER_TYPE="DIVINE_TEACHING_OFFERED";
    static final String RESPONSE_TYPE="HARU_TEACHING_RESPONSE";
    private static final long MAX_OFFER_AGE_MS=24L*60L*60L*1000L;

    private HaruTeachingOpportunityEngine(){}

    public static boolean observe(WorldState s,long now){
        if(s==null||s.worldHistory==null||s.worldHistory.isEmpty())return false;
        WorldHistoryEntry offer=latestPendingOffer(s,now);
        if(offer==null)return false;

        // Urgent physiology has priority. Deferral is not consent or rejection.
        if(BodyRhythmEngine.isSleeping(s) || bodyOverloaded(s))return false;

        String mode=field(offer.summary,"mode");
        String key=field(offer.summary,"key");
        if(key.isEmpty()){
            respond(s,offer,now,false,"invalid_offer",null);
            return true;
        }

        double readiness=readiness(s);
        if(readiness<.38){
            respond(s,offer,now,false,"haru_declined",null);
            return true;
        }

        Experience e=new Experience(
                "divine_teaching_received",
                "She chose to listen to a lesson about "+humanize(key)+".",
                .04,
                "action".equals(mode)?.30:.36)
                .atTime(now)
                .id("lesson_"+offer.eventId)
                .at(HaruPerception.currentPlaceId(s),"")
                .with("god")
                .tag("learning")
                .tag("god_teaching")
                .tag("causal_event:"+offer.eventId)
                .tag(("action".equals(mode)?"skill_theory:":"topic:")+key);

        MemoryEntry memory=CognitionEngine.process(s,e);

        if("knowledge".equals(mode)){
            int old=s.knowledge.getOrDefault(key,0);
            int gain=(int)Math.max(1,Math.floor(AgeDevelopmentEngine.learningFactor(s)));
            int next=Math.min(AgeDevelopmentEngine.knowledgeCeiling(s),old+gain);
            s.knowledge.put(key,next);
        }
        // action mode deliberately does NOT mutate s.skills or s.learnedActions.
        respond(s,offer,now,true,"haru_chose_to_listen",memory);
        return true;
    }

    private static WorldHistoryEntry latestPendingOffer(WorldState s,long now){
        for(int i=s.worldHistory.size()-1;i>=0;i--){
            WorldHistoryEntry e=s.worldHistory.get(i);
            if(e==null)continue;
            if(now>=e.time&&now-e.time>MAX_OFFER_AGE_MS)break;
            if(!OFFER_TYPE.equals(e.type)||e.time>now)continue;
            if(!responded(s,e.eventId))return e;
        }
        return null;
    }

    private static boolean responded(WorldState s,String offerId){
        String responseId=responseId(offerId);
        for(int i=s.worldHistory.size()-1;i>=0;i--){
            WorldHistoryEntry e=s.worldHistory.get(i);
            if(e!=null&&responseId.equals(e.eventId))return true;
        }
        return false;
    }

    private static void respond(WorldState s,WorldHistoryEntry offer,long now,boolean accepted,String reason,MemoryEntry memory){
        String summary="accepted="+accepted+" reason="+reason+
                (memory==null?"":" memory="+memory.memoryId);
        WorldEventBus.publishId(s,now,responseId(offer.eventId),RESPONSE_TYPE,offer.eventId,summary);
    }

    private static String responseId(String offerId){
        String id=offerId==null?"unknown":offerId.replaceAll("[^A-Za-z0-9._-]","_");
        return "haru_teaching_response_"+id;
    }

    private static boolean bodyOverloaded(WorldState s){
        if(s.body==null)return false;
        return s.body.energy<24 || s.body.sleepiness>82 || s.body.pain>62 || s.body.health<52;
    }

    private static double readiness(WorldState s){
        double curiosity=s.personality==null?.5:s.personality.curiosity;
        double patience=s.personality==null?.5:s.personality.patience;
        double calm=s.emotion==null?.5:s.emotion.calm;
        double energy=s.body==null?.5:clamp(s.body.energy/100.0);
        double pain=s.body==null?0:clamp(s.body.pain/100.0);
        return clamp(.34*curiosity+.22*patience+.18*calm+.26*energy-.18*pain);
    }

    private static String field(String summary,String key){
        if(summary==null||key==null)return "";
        String token=key+"=";int i=summary.indexOf(token);if(i<0)return "";
        int start=i+token.length(),end=summary.indexOf(' ',start);
        return (end<0?summary.substring(start):summary.substring(start,end)).trim();
    }

    private static String humanize(String key){
        return key==null?"":key.replace('_',' ').toLowerCase(Locale.ROOT);
    }

    private static double clamp(double v){return Double.isFinite(v)?Math.max(0,Math.min(1,v)):.5;}
}
