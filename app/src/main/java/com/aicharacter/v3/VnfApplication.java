package com.aicharacter.v3;

import android.app.Application;

public final class VnfApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        CrashDiagnostics.install(this);
        WorldHeartbeatScheduler.ensureScheduled(this);
    }
}
