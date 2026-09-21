package com.aicharacter.v3;
import org.json.JSONObject;import java.nio.charset.StandardCharsets;import java.security.*;import java.security.spec.X509EncodedKeySpec;import java.util.Base64;
/** Verifies a bounded God world-event envelope. It cannot execute code or mutate girl cognition. */
public final class SignedGodWorldEvent{
 private SignedGodWorldEvent(){}
 public static GodWorldEventProposal verify(String payloadB64,String sigB64,long receivedAt)throws Exception{
  if(payloadB64==null||sigB64==null||payloadB64.isEmpty()||sigB64.isEmpty())throw new SecurityException("Missing signed God event");
  byte[] payload=Base64.getDecoder().decode(payloadB64),sig=Base64.getDecoder().decode(sigB64);
  if(payload.length>4096)throw new SecurityException("God event payload too large");
  byte[] kb=Base64.getDecoder().decode(VnfOnlineConfig.CONTENT_PUBLIC_KEY.replaceAll("\\s+",""));
  PublicKey key=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(kb));
  Signature v=Signature.getInstance("SHA256withRSA");v.initVerify(key);v.update(payload);
  if(!v.verify(sig))throw new SecurityException("God event signature invalid");
  GodWorldEventProposal p=GodWorldEventProposal.fromSignedPayload(new JSONObject(new String(payload,StandardCharsets.UTF_8)),receivedAt);
  if(p==null)throw new SecurityException("Unsupported God event payload");
  return p;
 }
}