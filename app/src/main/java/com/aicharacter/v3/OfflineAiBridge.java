package com.aicharacter.v3;

public final class OfflineAiBridge implements AiBridge {
    @Override public boolean isAvailable(){ return false; }
    @Override public void requestGuidance(String a,String b,String c,Callback callback){ callback.onError("No production VNF backend is configured."); }
}
