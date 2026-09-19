package com.aicharacter.v3;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/** Developer-signed APK updater. Never modifies world state, saves, Java/DEX, or security policy itself. */
public final class AppUpdateManager {
    public interface Callback { void onChecked(Update update, Throwable error); }
    public static final class Update {
        public final long versionCode,size; public final String versionName,apkUrl,sha256,notes;
        Update(long c,String n,String u,String h,long s,String notes){versionCode=c;versionName=n;apkUrl=u;sha256=h;size=s;this.notes=notes;}
    }
    private static final long MAX_APK=150L*1024L*1024L;
    private static final int MAX_REDIRECTS=5;
    private AppUpdateManager(){}

    public static void checkAsync(Context context, Callback callback){
        Context app=context.getApplicationContext();
        new Thread(() -> { try{callback.onChecked(check(app),null);}catch(Throwable t){callback.onChecked(null,t);} },"VNF-App-Update-Check").start();
    }

    private static Update check(Context context)throws Exception{
        if(!VnfOnlineConfig.appUpdaterConfigured())return null;
        JSONObject j=new JSONObject(getHttps(VnfOnlineConfig.UPDATE_MANIFEST,256*1024));
        if(!j.optBoolean("ok",true)||!j.optBoolean("updateAvailable",true))return null;
        long vc=j.optLong("versionCode",-1), size=j.optLong("size",-1);
        String vn=j.optString("versionName","").trim(), url=j.optString("apkUrl","").trim();
        String hash=j.optString("sha256","").trim().toLowerCase(Locale.ROOT), notes=j.optString("notes","").trim();
        if(vc<=currentVersion(context))return null;
        if(size<=0||size>MAX_APK)throw new SecurityException("Kích thước APK cập nhật không hợp lệ");
        requireHttps(url,"APK update phải dùng HTTPS");
        if(!hash.matches("[0-9a-f]{64}"))throw new SecurityException("SHA-256 update không hợp lệ");
        return new Update(vc,vn,url,hash,size,notes.length()>1200?notes.substring(0,1200):notes);
    }

    public static File downloadAndVerify(Context context,Update u)throws Exception{
        File dir=new File(context.getCacheDir(),"vnf-updates"); if(!dir.exists()&&!dir.mkdirs())throw new IOException("Không tạo được thư mục update");
        File out=new File(dir,"vnf-update-"+u.versionCode+".apk"), tmp=new File(dir,out.getName()+".part");
        HttpURLConnection c=openHttpsFollowingRedirects(u.apkUrl,"application/vnd.android.package-archive");
        try{
            int code=c.getResponseCode(); if(code<200||code>=300)throw new IOException("Tải update thất bại HTTP "+code);
            long declared=c.getContentLengthLong(); if(declared>MAX_APK||declared>0&&declared!=u.size)throw new SecurityException("Kích thước APK không khớp manifest");
            MessageDigest md=MessageDigest.getInstance("SHA-256"); long total=0;
            try(InputStream in=c.getInputStream();OutputStream os=new FileOutputStream(tmp)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1){total+=n;if(total>MAX_APK||total>u.size)throw new SecurityException("APK vượt kích thước đã ký");md.update(b,0,n);os.write(b,0,n);}}
            if(total!=u.size)throw new SecurityException("Kích thước APK tải về không khớp");
            String got=hex(md.digest()); if(!got.equals(u.sha256))throw new SecurityException("SHA-256 APK không khớp");
            verifyPackageAndSigner(context,tmp,u.versionCode);
            if(out.exists()&&!out.delete())throw new IOException("Không thay được APK update cũ");
            if(!tmp.renameTo(out))throw new IOException("Không kích hoạt được APK update");
            return out;
        }finally{c.disconnect();if(tmp.exists()&&!tmp.equals(out))tmp.delete();}
    }

    public static void launchInstaller(Context context,File apk){
        Uri uri=FileProvider.getUriForFile(context,context.getPackageName()+".updatefiles",apk);
        Intent i=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    private static void verifyPackageAndSigner(Context context,File apk,long expectedVersion)throws Exception{
        PackageManager pm=context.getPackageManager();
        int flags=Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES;
        PackageInfo candidate=pm.getPackageArchiveInfo(apk.getAbsolutePath(),flags); PackageInfo installed=pm.getPackageInfo(context.getPackageName(),flags);
        if(candidate==null||!context.getPackageName().equals(candidate.packageName))throw new SecurityException("Sai package APK cập nhật");
        long candidateVersion=Build.VERSION.SDK_INT>=28?candidate.getLongVersionCode():candidate.versionCode;
        if(candidateVersion!=expectedVersion||candidateVersion<=currentVersion(context))throw new SecurityException("Sai version APK cập nhật");
        String a=signerDigest(installed),b=signerDigest(candidate); if(a.isEmpty()||!a.equals(b))throw new SecurityException("APK không cùng chữ ký với VNF đang cài");
    }

    private static String signerDigest(PackageInfo p)throws Exception{
        android.content.pm.Signature[] sig;
        if(Build.VERSION.SDK_INT>=28){if(p.signingInfo==null)return"";sig=p.signingInfo.hasMultipleSigners()?p.signingInfo.getApkContentsSigners():p.signingInfo.getSigningCertificateHistory();}
        else sig=p.signatures;
        if(sig==null||sig.length==0)return""; return hex(MessageDigest.getInstance("SHA-256").digest(sig[0].toByteArray()));
    }

    private static HttpURLConnection openHttpsFollowingRedirects(String initial,String accept)throws Exception{
        String current=initial;
        for(int hop=0;hop<=MAX_REDIRECTS;hop++){
            requireHttps(current,"Update redirect phải dùng HTTPS");
            HttpURLConnection c=(HttpURLConnection)new URL(current).openConnection();
            c.setConnectTimeout(10000);c.setReadTimeout(30000);c.setInstanceFollowRedirects(false);
            if(accept!=null&&!accept.isEmpty())c.setRequestProperty("Accept",accept);
            int code=c.getResponseCode();
            if(code==301||code==302||code==303||code==307||code==308){
                String location=c.getHeaderField("Location");
                if(location==null||location.trim().isEmpty()){c.disconnect();throw new IOException("Update redirect thiếu Location");}
                URL next=new URL(new URL(current),location.trim());
                String nextUrl=next.toString();
                c.disconnect();
                requireHttps(nextUrl,"Từ chối update redirect không phải HTTPS");
                current=nextUrl;
                continue;
            }
            return c;
        }
        throw new IOException("Update redirect quá nhiều lần");
    }

    private static void requireHttps(String url,String message)throws Exception{
        URL u=new URL(url);
        if(!"https".equalsIgnoreCase(u.getProtocol()))throw new SecurityException(message);
    }

    private static long currentVersion(Context c)throws Exception{PackageInfo p=c.getPackageManager().getPackageInfo(c.getPackageName(),0);return Build.VERSION.SDK_INT>=28?p.getLongVersionCode():p.versionCode;}
    private static String getHttps(String url,int max)throws Exception{
        HttpURLConnection c=openHttpsFollowingRedirects(url,"application/json");
        try{int code=c.getResponseCode();if(code<200||code>=300)throw new IOException("Update manifest HTTP "+code);try(InputStream in=c.getInputStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n,total=0;while((n=in.read(b))!=-1){total+=n;if(total>max)throw new IOException("Update manifest quá lớn");out.write(b,0,n);}return out.toString(StandardCharsets.UTF_8.name());}}finally{c.disconnect();}
    }
    private static String hex(byte[] x){StringBuilder s=new StringBuilder();for(byte b:x)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
}
