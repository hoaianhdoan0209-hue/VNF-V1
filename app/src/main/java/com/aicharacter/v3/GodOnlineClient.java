package com.aicharacter.v3;

import org.json.JSONObject;
import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Network client used only by the God channel. Girl/world offline cognition never depends on this class. */
public final class GodOnlineClient {
    public static final class Result{
        public final String reply,contentPatchManifestUrl,godRecipePayload,godRecipeSignature; public final boolean autoApplyContentPatch; public final GodRepairPlan repairPlan;
        Result(String r,String u,boolean a,GodRepairPlan p,String gp,String gs){reply=r;contentPatchManifestUrl=u;autoApplyContentPatch=a;repairPlan=p;godRecipePayload=gp;godRecipeSignature=gs;}
    }
    public interface Callback { void onResult(Result result, Throwable error); }
    private GodOnlineClient(){}

    public static void ask(Context appContext,WorldState state,String input,Callback callback){
        new Thread(() -> {
            Throwable last=null;
            for(int attempt=1;attempt<=2;attempt++){
                try{Result r=requestOnce(appContext,state,input);GodSessionManager.noteRequestSuccess();callback.onResult(r,null);return;}
                catch(Throwable t){last=t;if(attempt>=2||!retryable(t))break;try{Thread.sleep(1500L);}catch(InterruptedException interrupted){Thread.currentThread().interrupt();last=interrupted;break;}}
            }
            GodSessionManager.noteRequestFailure();callback.onResult(null,last==null?new IOException("Không nhận được phản hồi từ Thần"):last);
        },"VNF-God-Online").start();
    }

    private static Result requestOnce(Context appContext,WorldState state,String input) throws Exception{
        if(!VnfOnlineConfig.godConfigured())throw new IllegalStateException("God backend chưa được cấu hình");
        HttpURLConnection c=null;
        try{
            URL url=new URL(VnfOnlineConfig.GOD_ENDPOINT);c=(HttpURLConnection)url.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(8000);c.setReadTimeout(38000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");c.setRequestProperty("Accept","application/json");c.setRequestProperty("X-VNF-Protocol","3");byte[] body=GodContextBuilder.build(appContext,state,input).toString().getBytes(StandardCharsets.UTF_8);c.setFixedLengthStreamingMode(body.length);try(OutputStream os=c.getOutputStream()){os.write(body);}int code=c.getResponseCode();InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();String raw=readBody(stream);if(code<200||code>=300)throw new HttpFailure(code,"HTTP "+code+": "+safeError(raw));JSONObject json=new JSONObject(raw);String reply=json.optString("reply","").trim();if(reply.isEmpty())throw new IllegalStateException("God backend trả lời rỗng");if(reply.length()>3500)reply=reply.substring(0,3500).trim()+"…";JSONObject patch=json.optJSONObject("contentPatch");String manifestUrl=patch==null?"":patch.optString("manifestUrl","").trim();boolean autoApply=patch!=null&&patch.optBoolean("autoApply",false);GodRepairPlan repairPlan=GodRepairPlan.fromJson(json.optJSONObject("repairPlan"));JSONObject recipe=json.optJSONObject("signedWorldRecipe");String gp=recipe==null?"":recipe.optString("payloadBase64","");String gs=recipe==null?"":recipe.optString("signatureBase64","");GodWorldEventProposal worldEvent=GodWorldEventProposal.fromJson(json.optJSONObject("worldEventProposal"),System.currentTimeMillis());return new Result(reply,manifestUrl,autoApply,repairPlan,gp,gs,worldEvent);
        }finally{if(c!=null)c.disconnect();}
    }
    private static boolean retryable(Throwable t){if(t instanceof HttpFailure){int code=((HttpFailure)t).code;return code==408||code==425||code==429||code>=500;}return t instanceof SocketTimeoutException||t instanceof IOException;}
    private static String readBody(InputStream in)throws IOException{if(in==null)return"";try(BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);return sb.toString();}}
    private static String safeError(String raw){if(raw==null)return"";raw=raw.replace('\n',' ').replace('\r',' ').trim();return raw.length()>240?raw.substring(0,240)+"…":raw;}
    private static final class HttpFailure extends IOException{final int code;HttpFailure(int code,String message){super(message);this.code=code;}}
}
