package com.aicharacter.v3;

import org.json.JSONArray;
import java.util.EnumSet;

/** Explicit bounded capability contract for Thần. Capabilities never grant authority over Haru's mind. */
public final class GodCapabilityModel {
 public enum Capability {
  OBSERVE, EXPLAIN, REFERENCE_KNOWLEDGE, TEACH, WARN,
  PROPOSE_WORLD_CONDITION, DIAGNOSE, SAFE_REPAIR
 }
 private static final EnumSet<Capability> ENABLED=EnumSet.allOf(Capability.class);
 private GodCapabilityModel(){}

 public static boolean supports(Capability capability){return capability!=null&&ENABLED.contains(capability);}
 public static JSONArray toJson(){JSONArray a=new JSONArray();for(Capability c:ENABLED)a.put(c.name());return a;}
}
