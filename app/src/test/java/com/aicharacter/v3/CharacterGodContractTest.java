package com.aicharacter.v3;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.*;

public final class CharacterGodContractTest {

    @Test public void godCannotDirectlyMutateHaruMind(){
        WorldState s=state();
        int memories=s.memories.size(),knowledge=s.knowledge.size(),skills=s.skills.size(),actions=s.learnedActions.size();
        double curiosity=s.personality.curiosity,calm=s.emotion.calm,trust=s.relationship.trust;
        String intention=s.currentIntention;

        GodTeachingGateway.teach(s,"DẠY CÔ ẤY: gravity",1000L);

        assertEquals(memories,s.memories.size());
        assertEquals(knowledge,s.knowledge.size());
        assertEquals(skills,s.skills.size());
        assertEquals(actions,s.learnedActions.size());
        assertEquals(curiosity,s.personality.curiosity,0.0);
        assertEquals(calm,s.emotion.calm,0.0);
        assertEquals(trust,s.relationship.trust,0.0);
        assertEquals(intention,s.currentIntention);
        assertEquals(1,s.worldHistory.size());
        assertEquals(HaruTeachingOpportunityEngine.OFFER_TYPE,s.worldHistory.get(0).type);
    }

    @Test public void haruMemoryRequiresCausalEvent(){
        WorldState s=state();
        assertFalse(HaruTeachingOpportunityEngine.observe(s,1100L));
        assertTrue(s.memories.isEmpty());

        GodTeachingGateway.teach(s,"DẠY CÔ ẤY: gravity",1200L);
        WorldHistoryEntry offer=s.worldHistory.get(s.worldHistory.size()-1);
        assertEquals(HaruTeachingOpportunityEngine.OFFER_TYPE,offer.type);

        assertTrue(HaruTeachingOpportunityEngine.observe(s,1300L));
        assertEquals(1,s.memories.size());
        MemoryEntry learned=s.memories.get(0);
        assertTrue(learned.hasTag("causal_event:"+offer.eventId));
        assertTrue(s.knowledge.getOrDefault("gravity",0)>0);

        boolean response=false;
        for(WorldHistoryEntry e:s.worldHistory)
            if(HaruTeachingOpportunityEngine.RESPONSE_TYPE.equals(e.type)&&offer.eventId.equals(e.entity))response=true;
        assertTrue(response);
    }

    @Test public void personalityChangesSlowly(){
        PersonalityState p=new PersonalityState();
        double before=p.curiosity;
        p.slowlyLearn("explore",1,1.0);
        double first=p.curiosity-before;
        assertTrue(first>0);
        assertTrue(first<.01);

        p.curiosity=.76;
        p.curiosityEvidence=100;
        p.normalize();
        double mature=p.curiosity;
        p.slowlyLearn("explore",-1,1.5);
        assertTrue(mature-p.curiosity>0);
        assertTrue(mature-p.curiosity<.003);
    }

    @Test public void knowledgeSeparationHolds(){
        WorldState s=state();
        WorldKnowledgeAnchor a=new WorldKnowledgeAnchor(
                "physics.gravity","physics","real reference","fictional transform",
                "open science","gravity","UNKNOWN_BUNDLED_REFERENCE",-1,
                "reference only", Arrays.asList("gravity"));
        s.world.knowledgeAnchors.add(a);

        assertTrue(a.hasCompleteProvenance());
        assertFalse(s.knowledge.containsKey("physics.gravity"));
        assertTrue(s.memories.isEmpty());

        GodTeachingGateway.teach(s,"DẠY CÔ ẤY: gravity",1400L);
        assertFalse(s.knowledge.containsKey("gravity"));
        assertTrue(s.memories.isEmpty());
    }

    @Test public void externalKnowledgeCannotBecomeWorldHistoryAutomatically(){
        WorldState s=state();
        s.world.knowledgeAnchors.add(new WorldKnowledgeAnchor(
                "biology.homeostasis","biology","reference principle","fictional body rule",
                "open biology","homeostasis","UNKNOWN_BUNDLED_REFERENCE",-1,
                "reference only", Collections.singletonList("body")));
        int before=s.worldHistory.size();

        String diagnostic=WorldKnowledgeAnchor.diagnostic(s.world);

        assertEquals(before,s.worldHistory.size());
        assertTrue(s.memories.isEmpty());
        assertTrue(s.knowledge.isEmpty());
        assertTrue(diagnostic.contains("REAL-WORLD REFERENCE"));
    }

    @Test public void teachingUsesSameActiveOfflineCausalPath(){
        WorldState active=state(),offline=state();
        GodTeachingGateway.teach(active,"DẠY CÔ ẤY: gravity",1600L);
        GodTeachingGateway.teach(offline,"DẠY CÔ ẤY: gravity",1600L);

        HaruAutonomyEngine.advanceMindBody(active,60.0,1700L);
        HaruAutonomyEngine.advanceMindBody(offline,60.0,1700L);

        assertEquals(active.knowledge.getOrDefault("gravity",0),offline.knowledge.getOrDefault("gravity",0));
        assertEquals(active.memories.size(),offline.memories.size());
        assertEquals(active.worldHistory.size(),offline.worldHistory.size());
        assertEquals(active.personality.curiosity,offline.personality.curiosity,0.0);
        assertEquals(active.currentIntention,offline.currentIntention);
    }

    @Test public void godCanAnswerBroadKnowledgeWithoutContaminatingSaveTruth(){
        WorldState s=state();
        int history=s.worldHistory.size(),memories=s.memories.size(),knowledge=s.knowledge.size();
        double trust=s.relationship.trust;

        s.godMemory.remember("What is gravity?","REAL-WORLD REFERENCE: unsupported bodies accelerate under gravity.",1500L);

        assertEquals(1,s.godMemory.exchanges.size());
        assertEquals(history,s.worldHistory.size());
        assertEquals(memories,s.memories.size());
        assertEquals(knowledge,s.knowledge.size());
        assertEquals(trust,s.relationship.trust,0.0);
    }

    private static WorldState state(){
        WorldState s=new WorldState();
        s.createdAt=100L;s.lastSavedAt=100L;s.lastOpenedAt=100L;s.lastSimulatedAt=100L;
        s.worldMinutes=8*60;s.haruX=10;s.catX=5;s.age=15;s.brainGrowth=5.0;
        s.haruMood="calm";s.haruActivity="idle";s.currentIntention="observe_lake";
        s.relationship=RelationshipState.fresh(new Random(7));
        s.body.energy=88;s.body.sleepiness=12;s.body.pain=0;s.body.health=100;
        s.emotion.calm=.82;
        s.personality.curiosity=.78;s.personality.patience=.72;
        s.world=new WorldModel();
        s.world.areas.add(new WorldArea("test_area","Test","test place",0,100,0,true,"test"));
        return s;
    }
}
