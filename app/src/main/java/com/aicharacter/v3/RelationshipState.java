package com.aicharacter.v3;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public final class RelationshipState {
    public double affection, trust, attachment, comfort, gratitude, irritation, hurt;

    public static RelationshipState fresh(Random r) {
        RelationshipState s = new RelationshipState();
        s.affection = between(r); s.trust = between(r); s.attachment = between(r);
        s.comfort = between(r); s.gratitude = between(r); s.irritation = between(r); s.hurt = between(r);
        return s;
    }
    private static double between(Random r) { return 5 + r.nextInt(6); }
    public void clamp(){ affection=c(affection); trust=c(trust); attachment=c(attachment); comfort=c(comfort); gratitude=c(gratitude); irritation=c(irritation); hurt=c(hurt); }
    private static double c(double v){return Math.max(0,Math.min(101,v));}
    public JSONObject toJson() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("affection", affection); j.put("trust", trust); j.put("attachment", attachment);
        j.put("comfort", comfort); j.put("gratitude", gratitude); j.put("irritation", irritation); j.put("hurt", hurt); return j;
    }
    public static RelationshipState fromJson(JSONObject j) {
        RelationshipState s = new RelationshipState();
        if (j == null) return fresh(new Random(7));
        s.affection=j.optDouble("affection",7); s.trust=j.optDouble("trust",7); s.attachment=j.optDouble("attachment",7);
        s.comfort=j.optDouble("comfort",7); s.gratitude=j.optDouble("gratitude",7); s.irritation=j.optDouble("irritation",6); s.hurt=j.optDouble("hurt",5); return s;
    }
}
