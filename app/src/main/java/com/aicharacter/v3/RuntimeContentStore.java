package com.aicharacter.v3;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Private app-owned world-content store. Core APK and Haru cognition are outside this writable domain. */
public final class RuntimeContentStore {
    private RuntimeContentStore(){}
    public static File root(Context c){return new File(c.getFilesDir(),"vnf_world_content");}
    public static File current(Context c){return new File(root(c),"current");}
    public static File checkpoints(Context c){return new File(root(c),"checkpoints");}
    public static File staging(Context c){return new File(root(c),"staging");}
    public static File revisionFile(Context c){return new File(root(c),".revision");}
    public static long revision(Context c){return revisionFile(c).lastModified();}

    public static File overrideAsset(Context c,String logicalKey){
        if(logicalKey==null||!logicalKey.matches("[A-Za-z0-9._-]{1,100}"))return null;
        File png=new File(current(c),"assets/"+logicalKey+".png");
        return png.isFile()?png:null;
    }

    public static synchronized File checkpoint(Context c,String label)throws IOException{
        File cur=current(c); if(!cur.exists())return null;
        checkpoints(c).mkdirs();
        String safe=(label==null?"checkpoint":label).replaceAll("[^A-Za-z0-9._-]","_");
        File out=new File(checkpoints(c),System.currentTimeMillis()+"_"+safe);
        copyTree(cur,out); trimCheckpoints(c,3); return out;
    }

    public static synchronized boolean rollbackLatest(Context c)throws IOException{
        File cp=latestCheckpoint(c); if(cp==null)return false;
        File cur=current(c), backup=new File(root(c),"rollback_old_"+System.currentTimeMillis());
        root(c).mkdirs();
        if(cur.exists()&&!cur.renameTo(backup))throw new IOException("Cannot move current content for rollback");
        try{copyTree(cp,cur); deleteTree(backup); touchRevision(c); return true;}
        catch(IOException e){deleteTree(cur);if(backup.exists())backup.renameTo(cur);throw e;}
    }

    public static File latestCheckpoint(Context c){
        File[] xs=checkpoints(c).listFiles(File::isDirectory); if(xs==null||xs.length==0)return null;
        Arrays.sort(xs,(a,b)->Long.compare(b.lastModified(),a.lastModified())); return xs[0];
    }
    public static void touchRevision(Context c)throws IOException{root(c).mkdirs();try(FileOutputStream o=new FileOutputStream(revisionFile(c))){o.write(Long.toString(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));}}
    public static void deleteTree(File f){if(f==null||!f.exists())return;if(f.isDirectory()){File[] xs=f.listFiles();if(xs!=null)for(File x:xs)deleteTree(x);}f.delete();}
    public static void copyTree(File src,File dst)throws IOException{if(src.isDirectory()){if(!dst.exists()&&!dst.mkdirs())throw new IOException("Cannot mkdir "+dst);File[] xs=src.listFiles();if(xs!=null)for(File x:xs)copyTree(x,new File(dst,x.getName()));}else{File p=dst.getParentFile();if(p!=null)p.mkdirs();try(InputStream in=new FileInputStream(src);OutputStream out=new FileOutputStream(dst)){byte[] b=new byte[32768];int n;while((n=in.read(b))>0)out.write(b,0,n);}}}
    private static void trimCheckpoints(Context c,int keep){File[] xs=checkpoints(c).listFiles(File::isDirectory);if(xs==null)return;Arrays.sort(xs,(a,b)->Long.compare(b.lastModified(),a.lastModified()));for(int i=keep;i<xs.length;i++)deleteTree(xs[i]);}
}
