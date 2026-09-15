package com.aicharacter.v3;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Persistent memory owned by the God/System channel. It is never copied into the girl's memories. */
public final class GodMemory {
    public static final int MAX_EXCHANGES=200;
    public static final class Exchange {
        public long time; public String player,god;
        public Exchange(long t,String p,String g){time=t;player=safe(p,1200);god=safe(g,2200);}
        JSONObject toJson(){JSONObject j=new JSONObject();try{j.put("time",time);j.put("player",player);j.put("god",god);}catch(Exception ignored){}return j;}
        static Exchange fromJson(JSONObject j){return new Exchange(j.optLong("time"),j.optString("player"),j.optString("god"));}
    }
    public final List<Exchange> exchanges=new ArrayList<>();

    public void remember(String player,String god,long now){
        if((player==null||player.trim().isEmpty())&&(god==null||god.trim().isEmpty()))return;
        exchanges.add(new Exchange(now,player,god));
        while(exchanges.size()>MAX_EXCHANGES)exchanges.remove(0);
    }

    /** Recent context plus older query-relevant memories, bounded before upload. */
    public JSONArray recallFor(String query){
        JSONArray out=new JSONArray(); if(exchanges.isEmpty())return out;
        Set<Integer> chosen=new LinkedHashSet<>();
        int recentStart=Math.max(0,exchanges.size()-8); for(int i=recentStart;i<exchanges.size();i++)chosen.add(i);
        Set<String> terms=terms(query);
        if(!terms.isEmpty()){
            List<int[]> scored=new ArrayList<>();
            for(int i=0;i<recentStart;i++){
                Exchange e=exchanges.get(i); int score=score(terms,e.player+" "+e.god);
                if(score>0)scored.add(new int[]{i,score});
            }
            scored.sort((a,b)->Integer.compare(b[1],a[1]));
            for(int k=0;k<Math.min(6,scored.size());k++)chosen.add(scored.get(k)[0]);
        }
        List<Integer> order=new ArrayList<>(chosen); order.sort(Comparator.naturalOrder());
        for(Integer i:order){Exchange e=exchanges.get(i);JSONObject j=new JSONObject();try{j.put("time",e.time);j.put("player",e.player);j.put("god",e.god);out.put(j);}catch(Exception ignored){}}
        return out;
    }
    public JSONObject toJson(){JSONObject j=new JSONObject();JSONArray a=new JSONArray();for(Exchange e:exchanges)a.put(e.toJson());try{j.put("exchanges",a);}catch(Exception ignored){}return j;}
    public static GodMemory fromJson(JSONObject j){GodMemory m=new GodMemory();if(j==null)return m;JSONArray a=j.optJSONArray("exchanges");if(a!=null)for(int i=0;i<a.length();i++){JSONObject e=a.optJSONObject(i);if(e!=null)m.exchanges.add(Exchange.fromJson(e));}while(m.exchanges.size()>MAX_EXCHANGES)m.exchanges.remove(0);return m;}
    private static int score(Set<String> q,String text){Set<String> t=terms(text);int s=0;for(String x:q)if(t.contains(x))s++;return s;}
    private static Set<String> terms(String s){Set<String> r=new LinkedHashSet<>();if(s==null)return r;for(String x:s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").split("\\s+"))if(x.length()>=3)r.add(x);return r;}
    private static String safe(String s,int n){s=s==null?"":s.trim();return s.length()>n?s.substring(0,n)+"…":s;}
}
