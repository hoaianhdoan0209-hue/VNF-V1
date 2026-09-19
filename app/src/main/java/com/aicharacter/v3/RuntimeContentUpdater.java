package com.aicharacter.v3;

import android.content.Context;
import android.graphics.BitmapFactory;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Secure updater for data/assets in the private runtime content store. It cannot replace APK/DEX/Java. */
public final class RuntimeContentUpdater {
    public interface Callback{void onDone(Result r);}
    public static final class Result{public final boolean ok;public final String message;Result(boolean o,String m){ok=o;message=m;}}
    private static final long MAX_FILE=20L*1024*1024, MAX_TOTAL=100L*1024*1024;
    private RuntimeContentUpdater(){}

    public static void applySignedManifestAsync(Context c,String manifestUrl,Callback cb){new Thread(()->cb.onDone(applySignedManifest(c.getApplicationContext(),manifestUrl)),"VNF-WorldContent-Updater").start();}

    public static Result applySignedManifest(Context c,String manifestUrl){
        File stage=null;
        try{
            URL manifest=new URL(manifestUrl);
            enforceTrustedUrl(manifest);
            String envelope=readText(manifest,512*1024);
            RuntimePatchManifest m=RuntimePatchManifest.parseAndVerify(envelope,VnfOnlineConfig.CONTENT_PUBLIC_KEY);
            int appVersion=BuildConfig.VERSION_CODE;
            if(appVersion<m.minVersionCode||appVersion>m.maxVersionCode)throw new SecurityException("Patch is not compatible with this APK version");
            long total=0;Set<String> targets=new HashSet<>();
            for(RuntimePatchManifest.FileEntry e:m.files){total+=e.size;if(e.size>MAX_FILE||total>MAX_TOTAL)throw new SecurityException("Patch size limit exceeded");validateRelative(e.source);validateRelative(e.target);if(e.type.equals("png")){if(!e.logicalKey.matches("[A-Za-z0-9._-]{1,100}")||!e.target.equals("assets/"+e.logicalKey+".png"))throw new SecurityException("PNG target/logicalKey mismatch");if(!AssetManifest.runtimeOverrideAllowed(e.logicalKey))throw new SecurityException("Runtime override quarantined for frame asset: "+e.logicalKey);}if(e.type.equals("json")&&!e.target.startsWith("world/"))throw new SecurityException("World JSON must live under world/");if(!targets.add(e.target))throw new SecurityException("Duplicate patch target");}

            RuntimeContentStore.root(c).mkdirs();RuntimeContentStore.deleteTree(RuntimeContentStore.staging(c));
            stage=new File(RuntimeContentStore.staging(c),m.patchId);if(!stage.mkdirs())throw new IOException("Cannot create staging directory");
            URL base=new URL(manifest,".");
            for(RuntimePatchManifest.FileEntry e:m.files){URL u=new URL(base,e.source);enforceTrustedUrl(u);File out=new File(stage,e.target);ensureInside(stage,out);download(u,out,e.size);if(out.length()!=e.size)throw new SecurityException("Size mismatch: "+e.target);if(!sha256(out).equals(e.sha256))throw new SecurityException("SHA-256 mismatch: "+e.target);if(e.type.equals("png")){BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeFile(out.getAbsolutePath(),o);if(o.outWidth<=0||o.outHeight<=0||o.outWidth>8192||o.outHeight>8192)throw new SecurityException("Invalid PNG: "+e.target);}}
            try(FileOutputStream o=new FileOutputStream(new File(stage,"manifest.envelope.json"))){o.write(envelope.getBytes(StandardCharsets.UTF_8));}

            RuntimeContentStore.checkpoint(c,m.patchId);
            File cur=RuntimeContentStore.current(c),old=new File(RuntimeContentStore.root(c),"old_"+System.currentTimeMillis());
            if(cur.exists()&&!cur.renameTo(old))throw new IOException("Cannot checkpoint current content atomically");
            if(!stage.renameTo(cur)){if(old.exists())old.renameTo(cur);throw new IOException("Cannot activate staged content");}
            RuntimeContentStore.deleteTree(old);RuntimeContentStore.touchRevision(c);
            RuntimeContentEvaluator.Verdict verdict=RuntimeContentEvaluator.evaluate(c,m);
            if(!verdict.keep){
                boolean rolledBack=RuntimeContentStore.rollbackLatest(c);
                return new Result(false,"Patch "+m.patchId+" không qua đánh giá sau áp dụng ("+verdict.message+"). "+(rolledBack?"Đã tự rollback về checkpoint.":"Không thể rollback tự động."));
            }
            return new Result(true,"Đã áp dụng world-content patch "+m.patchId+" ("+m.files.size()+" file). Đánh giá sau áp dụng: KEEP. Checkpoint vẫn sẵn sàng để rollback.");
        }catch(Throwable t){if(stage!=null)RuntimeContentStore.deleteTree(stage);return new Result(false,"Không áp dụng patch: "+t.getClass().getSimpleName()+" — "+String.valueOf(t.getMessage()));}
    }

    public static Result rollback(Context c){try{return RuntimeContentStore.rollbackLatest(c)?new Result(true,"Đã rollback về checkpoint world-content gần nhất."):new Result(false,"Chưa có checkpoint world-content để rollback.");}catch(Throwable t){return new Result(false,"Rollback thất bại: "+t.getMessage());}}

    private static void enforceTrustedUrl(URL u)throws Exception{if(!"https".equalsIgnoreCase(u.getProtocol()))throw new SecurityException("HTTPS required");String origin=VnfOnlineConfig.CONTENT_ORIGIN;if(origin==null||origin.isEmpty())throw new SecurityException("VNF content origin is not configured");URL trusted=new URL(origin);if(!u.getHost().equalsIgnoreCase(trusted.getHost())||effectivePort(u)!=effectivePort(trusted))throw new SecurityException("Untrusted content host");}
    private static int effectivePort(URL u){int p=u.getPort();return p>=0?p:("https".equalsIgnoreCase(u.getProtocol())?443:80);}
    private static void validateRelative(String s){if(s.startsWith("/")||s.contains("..")||s.contains("\\")||s.contains(":"))throw new SecurityException("Unsafe path");}
    private static void ensureInside(File root,File f)throws Exception{String r=root.getCanonicalPath()+File.separator,x=f.getCanonicalPath();if(!x.startsWith(r))throw new SecurityException("Path traversal blocked");}
    private static String readText(URL u,int max)throws Exception{HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(7000);c.setReadTimeout(15000);c.setRequestProperty("Accept","application/json");int code=c.getResponseCode();if(code<200||code>=300)throw new IOException("HTTP "+code);try(InputStream in=c.getInputStream();ByteArrayOutputStream b=new ByteArrayOutputStream()){byte[] x=new byte[8192];int n,total=0;while((n=in.read(x))>0){total+=n;if(total>max)throw new IOException("Manifest too large");b.write(x,0,n);}return new String(b.toByteArray(),StandardCharsets.UTF_8);}finally{c.disconnect();}}
    private static void download(URL u,File out,long expected)throws Exception{File p=out.getParentFile();if(p!=null)p.mkdirs();HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(7000);c.setReadTimeout(20000);int code=c.getResponseCode();if(code<200||code>=300)throw new IOException("HTTP "+code);try(InputStream in=c.getInputStream();OutputStream o=new FileOutputStream(out)){byte[] b=new byte[32768];int n;long total=0;while((n=in.read(b))>0){total+=n;if(total>expected||total>MAX_FILE)throw new SecurityException("Download size exceeded");o.write(b,0,n);}}finally{c.disconnect();}}
    private static String sha256(File f)throws Exception{MessageDigest d=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(f)){byte[] b=new byte[32768];int n;while((n=in.read(b))>0)d.update(b,0,n);}StringBuilder s=new StringBuilder();for(byte x:d.digest())s.append(String.format(java.util.Locale.ROOT,"%02x",x&255));return s.toString();}
}
