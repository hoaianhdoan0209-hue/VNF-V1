package com.aicharacter.v3;

public final class VnfOnlineConfig {
    private VnfOnlineConfig(){}

    // Public backend URL only. Provider credentials must stay on the backend.
    // Supply at build time with Gradle property or environment variable VNF_GOD_ENDPOINT.
    public static final String GOD_ENDPOINT = BuildConfig.VNF_GOD_ENDPOINT == null ? "" : BuildConfig.VNF_GOD_ENDPOINT.trim();

    public static boolean godConfigured(){
        return GOD_ENDPOINT.startsWith("https://") && GOD_ENDPOINT.length()>12;
    }
}
