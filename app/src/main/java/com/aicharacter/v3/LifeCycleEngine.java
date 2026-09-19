package com.aicharacter.v3;

public final class LifeCycleEngine {
    private static final long YEAR_MS = 365L * 24L * 60L * 60L * 1000L;
    private LifeCycleEngine() {}
    public static void apply(WorldState s, long now) {
        long effectiveNow=Math.max(now,Math.max(s.lastSimulatedAt,s.lastOpenedAt));
        int livedYears = (int)Math.max(0, (effectiveNow - s.createdAt) / YEAR_MS);
        int targetAge = 15 + livedYears;
        if (targetAge > s.age) {
            for (int a = s.age + 1; a <= targetAge; a++) {
                s.age = a;
                s.brainGrowth = 5.0 + (a - 15) * 0.5;
                s.pendingBirthdayLearning = true;
                AgeDevelopmentEngine.onBirthday(s,a,now);
                s.memories.add(new MemoryEntry(now, "birthday", "She reached age " + a + " without losing the life that came before it.", 1.0));
            }
        }
    }
}
