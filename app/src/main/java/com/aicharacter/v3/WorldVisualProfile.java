package com.aicharacter.v3;

import android.content.Context;
import android.graphics.Color;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Small signed runtime visual recipe. Cached by runtime-content revision to avoid disk I/O every frame. */
public final class WorldVisualProfile{
    public final int skyTop,skyBottom,cloud;public final boolean clouds;
    private static volatile long cachedRevision=Long.MIN_VALUE;
    private static volatile WorldVisualProfile cached;
    private WorldVisualProfile(int a,int b,int c,boolean d){skyTop=a;skyBottom=b;cloud=c;clouds=d;}

    public static WorldVisualProfile lakeside(Context c){
        long rev=RuntimeContentStore.revision(c);
        WorldVisualProfile local=cached;
        if(local!=null&&cachedRevision==rev)return local;
        synchronized(WorldVisualProfile.class){
            if(cached!=null&&cachedRevision==rev)return cached;
            WorldVisualProfile loaded=load(c);
            cached=loaded;cachedRevision=rev;return loaded;
        }
    }

    private static WorldVisualProfile load(Context c){
        int top=Color.rgb(153,207,220),bottom=Color.rgb(126,190,210),cloud=Color.rgb(235,238,213);boolean clouds=true;
        try{
            File f=new File(RuntimeContentStore.current(c),"world/visual_profile.json");
            if(f.isFile()){
                byte[] x=new byte[(int)Math.min(f.length(),65536)];
                try(InputStream in=new FileInputStream(f)){
                    int n=in.read(x);
                    JSONObject j=new JSONObject(new String(x,0,Math.max(0,n),StandardCharsets.UTF_8));
                    JSONObject s=j.optJSONObject("lakeside");
                    if(s!=null){
                        top=parse(s.optString("skyTop","#99CFDC"),top);
                        bottom=parse(s.optString("skyBottom","#7EBED2"),bottom);
                        cloud=parse(s.optString("cloud","#EBEED5"),cloud);
                        clouds=s.optBoolean("clouds",true);
                    }
                }
            }
        }catch(Throwable ignored){}
        return new WorldVisualProfile(top,bottom,cloud,clouds);
    }
    private static int parse(String s,int fallback){try{return Color.parseColor(s);}catch(Throwable t){return fallback;}}
}
