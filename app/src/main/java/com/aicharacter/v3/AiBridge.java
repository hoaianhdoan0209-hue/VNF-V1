package com.aicharacter.v3;

public interface AiBridge {
    boolean isAvailable();
    interface Callback { void onResult(String text); void onError(String reason); }
    void requestGuidance(String systemRealitySummary, String haruPerceptionSummary, String playerMessage, Callback callback);
}
