package com.aicharacter.v3;

public final class VnfOnlineConfig {
    private VnfOnlineConfig(){}
    // Public URLs/verification key only. Provider credentials and PRIVATE signing key stay on backend/CI.
    public static final String GOD_ENDPOINT = clean(BuildConfig.VNF_GOD_ENDPOINT);
    public static final String CONTENT_ORIGIN = clean(BuildConfig.VNF_CONTENT_ORIGIN);
    public static final String CONTENT_PUBLIC_KEY = clean(BuildConfig.VNF_CONTENT_PUBLIC_KEY);
    private static String clean(String s){return s==null?"":s.trim();}
    public static boolean godConfigured(){return GOD_ENDPOINT.startsWith("https://")&&GOD_ENDPOINT.length()>12;}
    public static boolean contentUpdaterConfigured(){return CONTENT_ORIGIN.startsWith("https://")&&CONTENT_PUBLIC_KEY.length()>80;}
}
