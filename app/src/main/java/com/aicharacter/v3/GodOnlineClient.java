package com.aicharacter.v3;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class GodOnlineClient {
    public interface Callback { void onResult(String text, Throwable error); }
    private GodOnlineClient(){}

    public static void ask(WorldState state,String input,Callback callback){
        new Thread(() -> {
            try{
                if(!VnfOnlineConfig.godConfigured()) throw new IllegalStateException("God backend chưa được cấu hình");
                URL url=new URL(VnfOnlineConfig.GOD_ENDPOINT);
                HttpURLConnection c=(HttpURLConnection)url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(8000);
                c.setReadTimeout(16000);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type","application/json; charset=utf-8");
                c.setRequestProperty("Accept","application/json");

                byte[] body=GodContextBuilder.build(state,input).toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream os=c.getOutputStream()){ os.write(body); }

                int code=c.getResponseCode();
                BufferedReader br=new BufferedReader(new InputStreamReader(
                        code>=200&&code<300?c.getInputStream():c.getErrorStream(),StandardCharsets.UTF_8));
                StringBuilder sb=new StringBuilder(); String line;
                while((line=br.readLine())!=null) sb.append(line);
                br.close(); c.disconnect();
                if(code<200||code>=300) throw new IllegalStateException("HTTP "+code+": "+sb);
                JSONObject json=new JSONObject(sb.toString());
                String reply=json.optString("reply","").trim();
                if(reply.isEmpty()) throw new IllegalStateException("God backend trả lời rỗng");
                callback.onResult(reply,null);
            }catch(Throwable t){ callback.onResult(null,t); }
        },"VNF-God-Online").start();
    }
}
