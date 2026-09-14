package com.aicharacter.v3;import java.util.*;import org.json.*;
public final class RouteRuntimeState{public final Set<String> blocked=new LinkedHashSet<>();public long topologyRevision=1;
 public boolean blocked(String id){return blocked.contains(id);}public void setBlocked(String id,boolean v){if(v)blocked.add(id);else blocked.remove(id);topologyRevision++;}
 public JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("blocked",new JSONArray(blocked));j.put("topologyRevision",topologyRevision);}catch(Exception ignored){}return j;}
 public static RouteRuntimeState fromJson(JSONObject j){RouteRuntimeState r=new RouteRuntimeState();if(j==null)return r;JSONArray a=j.optJSONArray("blocked");if(a!=null)for(int i=0;i<a.length();i++)r.blocked.add(a.optString(i));r.topologyRevision=j.optLong("topologyRevision",1);return r;}}
