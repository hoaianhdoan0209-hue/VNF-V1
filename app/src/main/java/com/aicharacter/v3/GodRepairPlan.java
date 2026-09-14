package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Structured, non-executable repair intent returned by the God backend. */
public final class GodRepairPlan {
    public final String id, reason, scope;
    public final List<String> targets;
    public final boolean requiresPatch;

    private GodRepairPlan(String id,String reason,String scope,List<String> targets,boolean requiresPatch){
        this.id=id;this.reason=reason;this.scope=scope;this.targets=Collections.unmodifiableList(targets);this.requiresPatch=requiresPatch;
    }

    public static GodRepairPlan fromJson(JSONObject j){
        if(j==null)return null;
        String id=safe(j.optString("id",""),80);
        String reason=safe(j.optString("reason",""),280);
        String scope=safe(j.optString("scope","world-content"),80);
        boolean requires=j.optBoolean("requiresPatch",true);
        ArrayList<String> targets=new ArrayList<>();
        JSONArray a=j.optJSONArray("targets");
        if(a!=null)for(int i=0;i<a.length()&&targets.size()<16;i++){
            String x=safe(a.optString(i,""),100);
            if(!x.isEmpty()&&x.matches("[A-Za-z0-9._-]{1,100}"))targets.add(x);
        }
        if(id.isEmpty())id="repair";
        return new GodRepairPlan(id,reason,scope,targets,requires);
    }

    public String summary(){
        StringBuilder b=new StringBuilder();
        if(!reason.isEmpty())b.append(reason);
        if(!targets.isEmpty()){
            if(b.length()>0)b.append("\n");
            b.append("Mục tiêu: ");
            for(int i=0;i<targets.size();i++){if(i>0)b.append(", ");b.append(targets.get(i));}
        }
        return b.toString();
    }

    private static String safe(String s,int max){
        if(s==null)return"";s=s.replace('\n',' ').replace('\r',' ').trim();return s.length()>max?s.substring(0,max):s;
    }
}
