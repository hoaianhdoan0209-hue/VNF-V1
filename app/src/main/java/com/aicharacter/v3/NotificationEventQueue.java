package com.aicharacter.v3;
import java.util.*;import org.json.*;
public final class NotificationEventQueue{
 public static final class Event{public String id,type,text;public long time;public boolean delivered;Event(String i,String t,String x,long tm){id=i;type=t;text=x;time=tm;}JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("id",id);j.put("type",type);j.put("text",text);j.put("time",time);j.put("delivered",delivered);}catch(Exception ignored){}return j;}static Event fromJson(JSONObject j){Event e=new Event(j.optString("id"),j.optString("type"),j.optString("text"),j.optLong("time"));e.delivered=j.optBoolean("delivered");return e;}}
 private NotificationEventQueue(){}
 public static void emit(WorldState s,String type,String sourceId,String text,long now){String id=type+"_"+sourceId;for(Event e:s.notificationEvents)if(e.id.equals(id))return;s.notificationEvents.add(new Event(id,type,text,now));while(s.notificationEvents.size()>24)s.notificationEvents.remove(0);}
 public static Event nextEligible(WorldState s){for(Event e:s.notificationEvents)if(!e.delivered)return e;return null;}
 public static JSONArray toJson(java.util.List<Event> list){JSONArray a=new JSONArray();for(Event e:list)a.put(e.toJson());return a;}
 public static void load(JSONArray a,java.util.List<Event> out){if(a!=null)for(int i=0;i<a.length();i++)out.add(Event.fromJson(a.optJSONObject(i)));}
}