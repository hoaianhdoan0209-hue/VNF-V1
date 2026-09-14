package com.aicharacter.v3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/** Fail-closed post-activation checks for runtime world content. Visual taste stays with God/player; this catches broken content. */
public final class RuntimeContentEvaluator {
    public static final class Verdict{
        public final boolean keep; public final String message;
        Verdict(boolean k,String m){keep=k;message=m;}
    }
    private RuntimeContentEvaluator(){}

    public static Verdict evaluate(Context c,RuntimePatchManifest manifest){
        try{
            File current=RuntimeContentStore.current(c);
            if(!current.isDirectory())return new Verdict(false,"runtime content directory missing");
            Set<String> seen=new HashSet<>();
            for(RuntimePatchManifest.FileEntry e:manifest.files){
                File f=new File(current,e.target);
                String root=current.getCanonicalPath()+File.separator;
                if(!f.getCanonicalPath().startsWith(root))return new Verdict(false,"unsafe activated target: "+e.target);
                if(!f.isFile()||f.length()!=e.size)return new Verdict(false,"activated file missing/size mismatch: "+e.target);
                if(!seen.add(e.target))return new Verdict(false,"duplicate activated target: "+e.target);
                if("png".equals(e.type)){
                    BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeFile(f.getAbsolutePath(),o);
                    if(o.outWidth<=0||o.outHeight<=0||o.outWidth>8192||o.outHeight>8192)return new Verdict(false,"invalid image bounds: "+e.logicalKey);
                    // Decode one representative pixel buffer to catch files that pass headers but cannot render.
                    BitmapFactory.Options d=new BitmapFactory.Options();d.inSampleSize=Math.max(1,Math.max(o.outWidth,o.outHeight)/512);
                    Bitmap b=BitmapFactory.decodeFile(f.getAbsolutePath(),d);
                    if(b==null)return new Verdict(false,"image decode failed: "+e.logicalKey);
                    if(b.getWidth()<2||b.getHeight()<2){b.recycle();return new Verdict(false,"image too small: "+e.logicalKey);}
                    b.recycle();
                }
            }
            return new Verdict(true,"post-apply integrity checks passed");
        }catch(Throwable t){return new Verdict(false,t.getClass().getSimpleName()+": "+String.valueOf(t.getMessage()));}
    }
}
