package com.aicharacter.v3;

import android.content.Context;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;

/** Lightweight session presence for the God channel. No heartbeat and no dependency from the offline world. */
public final class GodSessionManager {
    public enum State { CONNECTING, ONLINE, DEGRADED, OFFLINE }
    public interface Listener { void onGodSessionState(State state); }
    private static volatile State state=State.OFFLINE;
    private static volatile long lastHealthyAt=0L;
    private static final CopyOnWriteArrayList<Listener> listeners=new CopyOnWriteArrayList<>();
    private GodSessionManager(){}

    public static State getState(){
        if(state==State.ONLINE && System.currentTimeMillis()-lastHealthyAt>5L*60L*1000L)return State.DEGRADED;
        return state;
    }
    public static void addListener(Listener l){if(l!=null&&!listeners.contains(l))listeners.add(l);}
    public static void removeListener(Listener l){listeners.remove(l);}
    public static void warmup(Context context){
        if(!VnfOnlineConfig.godConfigured()){set(State.OFFLINE);return;}
        State s=getState(); if(s==State.CONNECTING||s==State.ONLINE)return;
        set(State.CONNECTING);
        new Thread(()->{
            HttpURLConnection c=null;
            try{
                URL endpoint=new URL(VnfOnlineConfig.GOD_ENDPOINT);
                URL health=new URL(endpoint.getProtocol(),endpoint.getHost(),endpoint.getPort(),"/health");
                c=(HttpURLConnection)health.openConnection();c.setRequestMethod("GET");c.setConnectTimeout(6000);c.setReadTimeout(8000);c.setRequestProperty("Accept","application/json");
                int code=c.getResponseCode();String raw=read(code>=200&&code<300?c.getInputStream():c.getErrorStream());
                boolean ok=code>=200&&code<300 && new JSONObject(raw).optBoolean("ok",false);
                if(ok){lastHealthyAt=System.currentTimeMillis();set(State.ONLINE);}else set(State.OFFLINE);
            }catch(Throwable ignored){set(State.OFFLINE);}finally{if(c!=null)c.disconnect();}
        },"VNF-God-Warmup").start();
    }
    public static void noteRequestSuccess(){lastHealthyAt=System.currentTimeMillis();set(State.ONLINE);}
    public static void noteRequestFailure(){set(lastHealthyAt>0L?State.DEGRADED:State.OFFLINE);}
    private static void set(State s){state=s;for(Listener l:listeners)try{l.onGodSessionState(s);}catch(Throwable ignored){}}
    private static String read(InputStream in)throws Exception{if(in==null)return"";try(BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){StringBuilder b=new StringBuilder();String line;while((line=br.readLine())!=null)b.append(line);return b.toString();}}
}
