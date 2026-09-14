package com.aicharacter.v3;

public final class VnfOnlineConfig {
    private VnfOnlineConfig(){}

    // Set this only after deploying the VNF God backend. Never put provider API keys in the APK.
    public static final String GOD_ENDPOINT = "https://CHANGE-ME.example.workers.dev/god";

    public static boolean godConfigured(){
        return GOD_ENDPOINT.startsWith("https://") && !GOD_ENDPOINT.contains("CHANGE-ME");
    }
}
