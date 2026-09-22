package com.aicharacter.v3;

import android.content.Context;
import android.graphics.Color;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Presentation-only area palette. Optional runtime recipe may tune colors without touching world state. */
public final class WorldVisualProfile{
    public final String area;public final int skyTop,skyBottom,cloud,haze,accent,vignette;public final boolean clouds;
    private static volatile long cachedRevision=Long.MIN_VALUE;
    private static final Map<String,WorldVisualProfile> cache=new HashMap<>();
    private WorldVisualProfile(String area,int top,int bottom,int cloud,int haze,int accent,int vignette,boolean clouds){this.area=area;skyTop=top;skyBottom=bottom;this.cloud=cloud;this.haze=haze;this.accent=accent;this.vignette=vignette;this.clouds=clouds;}
    public static WorldVisualProfile lakeside(Context c){return forArea(c,"lakeside");}
    public static WorldVisualProfile forArea(Context c,String area){
        String key=area==null?"lakeside":area;long rev=RuntimeContentStore.revision(c);
        synchronized(WorldVisualProfile.class){if(cachedRevision!=rev){cache.clear();cachedRevision=rev;}WorldVisualProfile v=cache.get(key);if(v==null){v=load(c,key);cache.put(key,v);}return v;}
    }
    private static WorldVisualProfile load(Context c,String area){
        WorldVisualProfile d=defaults(area);int top=d.skyTop,bottom=d.skyBottom,cloud=d.cloud,haze=d.haze,accent=d.accent,vignette=d.vignette;boolean clouds=d.clouds;
        try{File f=new File(RuntimeContentStore.current(c),"world/visual_profile.json");if(f.isFile()){byte[] x=new byte[(int)Math.min(f.length(),65536)];try(InputStream in=new FileInputStream(f)){int n=in.read(x);JSONObject j=new JSONObject(new String(x,0,Math.max(0,n),StandardCharsets.UTF_8));JSONObject s=j.optJSONObject(area);if(s!=null){top=parse(s.optString("skyTop"),top);bottom=parse(s.optString("skyBottom"),bottom);cloud=parse(s.optString("cloud"),cloud);haze=parse(s.optString("haze"),haze);accent=parse(s.optString("accent"),accent);vignette=parse(s.optString("vignette"),vignette);clouds=s.optBoolean("clouds",clouds);}}}}catch(Throwable ignored){}
        return new WorldVisualProfile(area,top,bottom,cloud,haze,accent,vignette,clouds);
    }
    private static WorldVisualProfile defaults(String a){
        if("home".equals(a))return new WorldVisualProfile(a,Color.rgb(146,194,190),Color.rgb(219,204,158),Color.rgb(241,230,201),Color.rgb(206,195,157),Color.rgb(217,149,96),Color.rgb(35,52,47),true);
        if("garden".equals(a))return new WorldVisualProfile(a,Color.rgb(164,211,203),Color.rgb(220,228,181),Color.rgb(241,236,210),Color.rgb(190,214,170),Color.rgb(229,181,111),Color.rgb(38,61,46),true);
        if("grove".equals(a))return new WorldVisualProfile(a,Color.rgb(112,145,137),Color.rgb(77,108,99),Color.rgb(194,207,190),Color.rgb(117,139,122),Color.rgb(184,137,157),Color.rgb(27,42,38),false);
        return new WorldVisualProfile("lakeside",Color.rgb(153,207,220),Color.rgb(126,190,210),Color.rgb(235,238,213),Color.rgb(168,211,212),Color.rgb(190,218,195),Color.rgb(31,54,57),true);
    }
    private static int parse(String s,int fallback){if(s==null||s.isEmpty())return fallback;try{return Color.parseColor(s);}catch(Throwable t){return fallback;}}
}
