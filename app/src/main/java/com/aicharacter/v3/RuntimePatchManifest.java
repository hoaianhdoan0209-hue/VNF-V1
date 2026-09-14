package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Signed manifest for runtime WORLD CONTENT only. Never grants APK/Java write access. */
public final class RuntimePatchManifest {
    public static final class FileEntry {
        public final String logicalKey, source, target, sha256, type;
        public final long size;
        FileEntry(JSONObject j) throws Exception {
            logicalKey=j.optString("logicalKey","").trim();
            source=j.getString("source").trim();
            target=j.getString("target").trim();
            sha256=j.getString("sha256").toLowerCase(java.util.Locale.ROOT).trim();
            type=j.optString("type","png").toLowerCase(java.util.Locale.ROOT).trim();
            size=j.getLong("size");
            if(source.isEmpty()||target.isEmpty()||sha256.length()!=64||size<0) throw new SecurityException("Invalid file entry");
            if(!(type.equals("png")||type.equals("json"))) throw new SecurityException("Unsupported runtime content type: "+type);
        }
    }

    public final String patchId;
    public final int manifestVersion, minVersionCode, maxVersionCode;
    public final long createdAt;
    public final List<FileEntry> files=new ArrayList<>();
    public final byte[] signedPayload;

    private RuntimePatchManifest(JSONObject payload, byte[] raw) throws Exception {
        signedPayload=raw;
        manifestVersion=payload.optInt("manifestVersion",1);
        patchId=payload.getString("patchId").trim();
        createdAt=payload.optLong("createdAt",0L);
        minVersionCode=payload.optInt("minVersionCode",1);
        maxVersionCode=payload.optInt("maxVersionCode",Integer.MAX_VALUE);
        if(manifestVersion!=1||!patchId.matches("[A-Za-z0-9._-]{1,80}")) throw new SecurityException("Invalid patch manifest identity");
        JSONArray a=payload.getJSONArray("files");
        if(a.length()==0||a.length()>128) throw new SecurityException("Invalid patch file count");
        for(int i=0;i<a.length();i++) files.add(new FileEntry(a.getJSONObject(i)));
    }

    /** Envelope = {payloadBase64, signatureBase64}. Signature = SHA256withRSA(payload bytes). */
    public static RuntimePatchManifest parseAndVerify(String envelopeJson, String publicKeyBase64) throws Exception {
        if(publicKeyBase64==null||publicKeyBase64.trim().isEmpty()) throw new SecurityException("VNF content public key is not configured");
        JSONObject env=new JSONObject(envelopeJson);
        byte[] payload=Base64.getDecoder().decode(env.getString("payloadBase64"));
        byte[] signature=Base64.getDecoder().decode(env.getString("signatureBase64"));
        byte[] keyBytes=Base64.getDecoder().decode(publicKeyBase64.replaceAll("\\s+",""));
        PublicKey key=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        Signature verifier=Signature.getInstance("SHA256withRSA");
        verifier.initVerify(key); verifier.update(payload);
        if(!verifier.verify(signature)) throw new SecurityException("Runtime patch signature is invalid");
        return new RuntimePatchManifest(new JSONObject(new String(payload,StandardCharsets.UTF_8)),payload);
    }
}
