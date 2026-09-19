package com.aicharacter.v3;

import org.json.JSONException;
import org.json.JSONObject;

public final class BodyState {
    public double health = 100.0;
    public double energy = 72.0;
    public double sleepiness = 22.0;
    public double pain = 0.0;
    public String injury = "";

    public void clamp() {
        health = clamp01x100(health); energy = clamp01x100(energy);
        sleepiness = clamp01x100(sleepiness); pain = clamp01x100(pain);
    }
    public boolean hasInjury(){return injury!=null&&!injury.trim().isEmpty();}
    public double injuryBurden(){if(!hasInjury())return 0;return Math.min(1.0,.18+pain/100.0*.72+(100-health)/100.0*.45);}
    public void healInjuryIfRecovered(){if(hasInjury()&&pain<4&&health>92)injury="";}
    private double clamp01x100(double v) { return Math.max(0, Math.min(100, v)); }
    public JSONObject toJson() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("health", health); j.put("energy", energy); j.put("sleepiness", sleepiness);
        j.put("pain", pain); j.put("injury", injury); return j;
    }
    public static BodyState fromJson(JSONObject j) {
        BodyState b = new BodyState();
        if (j == null) return b;
        b.health = j.optDouble("health", b.health); b.energy = j.optDouble("energy", b.energy);
        b.sleepiness = j.optDouble("sleepiness", b.sleepiness); b.pain = j.optDouble("pain", b.pain);
        b.injury = j.optString("injury", ""); b.clamp(); return b;
    }
}
