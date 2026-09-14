package com.aicharacter.v3;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Network client used only by the God channel. Girl/world logic never depends on this class. */
public final class GodOnlineClient {
    public interface Callback { void onResult(String text, Throwable error); }
    private GodOnlineClient(){}

    public static void ask(WorldState state,String input,Callback callback){
        new Thread(() -> {
            HttpURLConnection c=null;
            try{
                if(!VnfOnlineConfig.godConfigured()) throw new IllegalStateException("God backend chưa được cấu hình");
                URL url=new URL(VnfOnlineConfig.GOD_ENDPOINT);
                c=(HttpURLConnection)url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(7000);
                c.setReadTimeout(18000);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type","application/json; charset=utf-8");
                c.setRequestProperty("Accept","application/json");
                c.setRequestProperty("X-VNF-Protocol","2");

                byte[] body=GodContextBuilder.build(state,input).toString().getBytes(StandardCharsets.UTF_8);
                c.setFixedLengthStreamingMode(body.length);
                try(OutputStream os=c.getOutputStream()){ os.write(body); }

                int code=c.getResponseCode();
                BufferedReader br=new BufferedReader(new InputStreamReader(
                        code>=200&&code<300?c.getInputStream():c.getErrorStream(),StandardCharsets.UTF_8));
                StringBuilder sb=new StringBuilder(); String line;
                while((line=br.readLine())!=null) sb.append(line);
                br.close();
                if(code<200||code>=300) throw new IllegalStateException("HTTP "+code+": "+safeError(sb.toString()));
                JSONObject json=new JSONObject(sb.toString());
                String reply=json.optString("reply","").trim();
                if(reply.isEmpty()) throw new IllegalStateException("God backend trả lời rỗng");
                if(reply.length()>3500) reply=reply.substring(0,3500).trim()+"…";
                callback.onResult(reply,null);
            }catch(Throwable t){ callback.onResult(null,t); }
            finally{ if(c!=null)c.disconnect(); }
        },"VNF-God-Online").start();
    }

    private static String safeError(String raw){
        if(raw==null)return "";
        raw=raw.replace('\n',' ').replace('\r',' ').trim();
        return raw.length()>240?raw.substring(0,240)+"…":raw;
    }
}
