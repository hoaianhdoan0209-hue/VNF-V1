package com.aicharacter.v3;

import org.json.JSONArray;import org.json.JSONException;import org.json.JSONObject;import java.util.ArrayList;import java.util.List;

public final class WorldArea {
    public String id, systemName, haruName, tags; public float left,right,groundY; public boolean weatherExposed; public final List<String> connections=new ArrayList<>();
    public WorldArea(String id,String systemName,String haruName,float left,float right,float groundY,boolean exposed,String tags){this.id=id;this.systemName=systemName;this.haruName=haruName;this.left=left;this.right=right;this.groundY=groundY;this.weatherExposed=exposed;this.tags=tags;}
    public boolean contains(float x){return x>=left&&x<=right;}
    public JSONObject toJson() throws JSONException{JSONObject j=new JSONObject();j.put("id",id);j.put("systemName",systemName);j.put("haruName",haruName);j.put("left",left);j.put("right",right);j.put("groundY",groundY);j.put("weatherExposed",weatherExposed);j.put("tags",tags);JSONArray a=new JSONArray();for(String c:connections)a.put(c);j.put("connections",a);return j;}
    public static WorldArea fromJson(JSONObject j){WorldArea a=new WorldArea(j.optString("id"),j.optString("systemName"),j.optString("haruName"),(float)j.optDouble("left"),(float)j.optDouble("right"),(float)j.optDouble("groundY",j.optDouble("ground",846)),j.optBoolean("weatherExposed",true),j.optString("tags"));JSONArray c=j.optJSONArray("connections");if(c!=null)for(int i=0;i<c.length();i++)a.connections.add(c.optString(i));return a;}
}
