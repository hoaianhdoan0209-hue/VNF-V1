package com.aicharacter.v3;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

/**
 * Visual V1 repository acceptance gate.
 * Pure read-only test: it inspects authored assets + visual source contracts and never touches simulation/save state.
 */
public final class VisualV1DevTest {
    private static final String[] AREA_IDS={"home_shelter","garden_path","lakeside","quiet_grove"};
    private static final String[] BIOMES={"home","garden","lakeside","grove"};
    private static final String[] LAYERS={"sky","distant","mid","ground","foreground"};
    private static final String[] HARU={
        "girl_idle_right","girl_idle_left","girl_walk_right","girl_walk_left","girl_sit_right",
        "girl_crouch_right","girl_sleep_right","girl_think_right","girl_react_right",
        "girl_search_right","girl_search_left"
    };
    private VisualV1DevTest(){}

    public static void main(String[] args) throws Exception {
        Path root=Paths.get(args.length==0?".":args[0]).toAbsolutePath().normalize();
        List<String> ok=new ArrayList<>(),bad=new ArrayList<>();
        checkWorldAreas(root,ok,bad);
        checkLayeredBiomes(root,ok,bad);
        checkHaruAssets(root,ok,bad);
        checkRendererContracts(root,ok,bad);
        checkMobileHud(root,ok,bad);
        System.out.println("V1 VISUAL TEST\nPASS "+ok.size()+" / FAIL "+bad.size());
        for(String x:ok)System.out.println("✓ "+x);
        for(String x:bad)System.out.println("✗ "+x);
        if(!bad.isEmpty())throw new IllegalStateException("Visual V1 gate failed: "+bad.size());
    }

    private static void checkWorldAreas(Path root,List<String>ok,List<String>bad)throws Exception{
        String world=read(root.resolve("app/src/main/assets/world/world_v1.json"));
        for(String id:AREA_IDS){
            boolean present=Pattern.compile("\\\"id\\\"\\s*:\\s*\\\""+Pattern.quote(id)+"\\\"").matcher(world).find();
            check(present,"world definition contains launch area "+id,ok,bad);
        }
    }

    private static void checkLayeredBiomes(Path root,List<String>ok,List<String>bad)throws Exception{
        Path dir=root.resolve("app/src/main/res/drawable-nodpi");
        Set<String> hashes=new HashSet<>();
        for(String biome:BIOMES){
            for(String layer:LAYERS){
                Path f=dir.resolve(biome+"_"+layer+".png");
                if(!Files.isRegularFile(f)){bad.add("missing "+biome+" "+layer+" asset");continue;}
                Png p=Png.read(f);
                check(p.w>=800&&p.h>=360,biome+" "+layer+" is production-resolution pixel art",ok,bad);
                check(Files.size(f)>=150000,biome+" "+layer+" is not a tiny placeholder asset",ok,bad);
                double cov=p.coverage();
                double min="sky".equals(layer)?.95:"ground".equals(layer)?.18:.035;
                check(cov>=min,biome+" "+layer+" has authored visual occupancy ("+pct(cov)+")",ok,bad);
                hashes.add(hex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(f))));
            }
        }
        check(hashes.size()==BIOMES.length*LAYERS.length,"all 20 biome depth layers are materially distinct",ok,bad);
    }

    private static void checkHaruAssets(Path root,List<String>ok,List<String>bad)throws Exception{
        Path dir=root.resolve("app/src/main/res/drawable-nodpi");
        for(String name:HARU){
            Path f=dir.resolve(name+".png");
            if(!Files.isRegularFile(f)){bad.add("missing Haru sheet "+name);continue;}
            Png p=Png.read(f);
            check(p.w%12==0&&p.w/12>=144&&p.h>=216,name+" has 12 authored frames at >=144x216",ok,bad);
            check(Files.size(f)>=300000,name+" is a real raster sheet, not the old tiny placeholder",ok,bad);
            check(p.frameCoverage(0)>.035,name+" first frame contains a readable silhouette",ok,bad);
        }
        Png idle=Png.read(dir.resolve("girl_idle_right.png"));
        int fw=idle.w/12;
        int[] box=idle.bounds(0,fw);
        check(box[3]-box[1]>=idle.h*.75,"Haru idle silhouette spans full human height",ok,bad);
        check(box[2]-box[0]>=fw*.40&&box[2]-box[0]<=fw*.78,"Haru idle silhouette has stable human width",ok,bad);
        check(idle.count(0,fw,0,(int)(idle.h*.34))>80,"Haru has a visible head/neck region",ok,bad);
        check(idle.count(0,fw,(int)(idle.h*.30),(int)(idle.h*.70))>300,"Haru has visible shoulders/torso/waist/hips",ok,bad);
        check(idle.count(0,fw,(int)(idle.h*.68),idle.h)>260,"Haru has visible legs/feet region",ok,bad);
        check(idle.outerMidCount(0,fw)>40,"Haru arms separate from torso silhouette",ok,bad);
        check(idle.bottomColumnGroups(0,fw)>=2,"Haru has two distinct grounded foot silhouettes",ok,bad);
        check(idle.uniqueColors(0,fw,0,(int)(idle.h*.36))>=9,"Haru face/hair region has authored color detail",ok,bad);
        check(idle.uniqueColors(0,fw,(int)(idle.h*.30),(int)(idle.h*.72))>=10,"Haru clothing/body region has authored color detail",ok,bad);
    }

    private static void checkRendererContracts(Path root,List<String>ok,List<String>bad)throws Exception{
        String renderer=read(root.resolve("app/src/main/java/com/aicharacter/v3/HaruVisualRenderer.java"));
        String manifest=read(root.resolve("app/src/main/java/com/aicharacter/v3/AssetManifest.java"));
        String game=read(root.resolve("app/src/main/java/com/aicharacter/v3/GameView.java"));
        check(!renderer.contains("BlockBodyRenderer"),"primary Haru render path has no geometric fallback",ok,bad);
        check(renderer.contains("fw*sc")&&renderer.contains("fh*sc"),"Haru uses one uniform authored-body scale",ok,bad);
        check(!renderer.contains("scaleY")&&!renderer.contains("scaleX"),"biology/physics cannot squash Haru axes",ok,bad);
        check(manifest.contains("\"lakeside_sky\""),"Lakeside sky asset is registered",ok,bad);
        check(game.contains("drawLayer(c,area+\"_sky\"")&&game.contains("drawLayer(c,area+\"_distant\"")&&
              game.contains("drawLayer(c,area+\"_mid\"")&&game.contains("drawLayer(c,area+\"_ground\"")&&
              game.contains("drawForegroundLayer(c,area+\"_foreground\""),"renderer uses explicit sky/distant/mid/ground/foreground depth",ok,bad);
        check(game.contains("BackdropPolishRenderer.draw(")&&game.contains("BackdropPolishRenderer.drawGround("),"all biome scenes receive subtle depth/detail polish",ok,bad);
        check(game.contains("bg.setAlpha(198)")&&game.contains("cam*1.03f"),"foreground is visually restrained and does not dominate Haru",ok,bad);
        int tintStart=game.indexOf("private void drawTimeTint(Canvas c)");
        int tintEnd=tintStart<0?-1:game.indexOf(" private void drawUi",tintStart);
        String tint=tintStart>=0&&tintEnd>tintStart?game.substring(tintStart,tintEnd):"";
        check(!tint.contains("light_evening")&&!tint.contains("light_night")&&!tint.contains("home_warm_light"),
              "drawTimeTint forbids legacy full-frame light rasters",ok,bad);
        check(tint.contains("LinearGradient")&&tint.contains("drawRect(0,0,2400,1080"),
              "time-of-day compositing remains procedural gradient/tint",ok,bad);
    }

    private static void checkMobileHud(Path root,List<String>ok,List<String>bad)throws Exception{
        float[][] screens={{1280,720,2f},{1440,720,2f},{1560,720,2f},{960,540,1.5f},{854,480,1f}};
        String[] names={"16:9","18:9","19.5:9","small","small-16:9"};
        for(int i=0;i<screens.length;i++){
            float w=screens[i][0],h=screens[i][1],d=screens[i][2];MinimalHudLayout.Layout u=MinimalHudLayout.forScreen(w,h,d);
            MinimalHudLayout.Box[] boxes={u.status,u.god,u.chat,u.mic};
            boolean inside=true,overlap=false;for(MinimalHudLayout.Box b:boxes)inside&=b.inside(w,h,Math.max(6f,d*4f));
            for(int a=0;a<boxes.length;a++)for(int b=a+1;b<boxes.length;b++)overlap|=boxes[a].overlaps(boxes[b]);
            check(inside,names[i]+" HUD stays clear of screen edges",ok,bad);
            check(!overlap,names[i]+" HUD has no overlapping controls/text",ok,bad);
            check(u.god.width()/d>=36&&u.chat.width()/d>=36&&u.mic.width()/d>=36,names[i]+" icon hit targets remain usable",ok,bad);
        }
        String game=read(root.resolve("app/src/main/java/com/aicharacter/v3/GameView.java"));
        int start=game.indexOf("private void drawUi(Canvas c)"),end=start<0?-1:game.indexOf(" public String assetDiagnostic()",start);
        String ui=start>=0&&end>start?game.substring(start,end):"";
        check(!ui.contains("LIÊN HỆ THẦN")&&!ui.contains("\"NÓI\"")&&!ui.contains("\"MIC\""),"main HUD contains no long text buttons",ok,bad);
        check(ui.contains("HudIconRenderer.CHAT")&&ui.contains("HudIconRenderer.MIC")&&ui.contains("HudIconRenderer.GOD"),"chat/mic/God are icon controls",ok,bad);
        check(ui.contains("MinimalHudLayout.forScreen"),"render and hit-test share the same responsive layout",ok,bad);
        String icon=read(root.resolve("app/src/main/java/com/aicharacter/v3/HudIconRenderer.java"));
        check(icon.contains("pressed?176:104")&&icon.contains("c.translate(0,1.2f*d)"),"icon controls have a clear pressed state",ok,bad);
    }

    private static String read(Path p)throws IOException{return new String(Files.readAllBytes(p),StandardCharsets.UTF_8);}
    private static void check(boolean pass,String label,List<String>ok,List<String>bad){(pass?ok:bad).add(label);}
    private static String pct(double v){return String.format(Locale.ROOT,"%.1f%%",v*100);}
    private static String hex(byte[] a){StringBuilder b=new StringBuilder();for(byte x:a)b.append(String.format("%02x",x&255));return b.toString();}
    private static int paeth(int a,int b,int c){int p=a+b-c,pa=Math.abs(p-a),pb=Math.abs(p-b),pc=Math.abs(p-c);return pa<=pb&&pa<=pc?a:pb<=pc?b:c;}

    private static final class Png{
        final int w,h;final byte[] px,alpha;
        Png(int w,int h,byte[]px,byte[]alpha){this.w=w;this.h=h;this.px=px;this.alpha=alpha;}
        static Png read(Path path)throws Exception{
            byte[] b=Files.readAllBytes(path);
            if(b.length<24||b[0]!=(byte)137||b[1]!=80||b[2]!=78||b[3]!=71)throw new IOException("not PNG: "+path);
            int pos=8,w=0,h=0,depth=0,type=0;byte[] trns=new byte[0];ByteArrayOutputStream idat=new ByteArrayOutputStream();
            while(pos+12<=b.length){
                int len=i32(b,pos);pos+=4;String kind=new String(b,pos,4,StandardCharsets.US_ASCII);pos+=4;
                if(pos+len+4>b.length)throw new IOException("truncated PNG: "+path);
                if("IHDR".equals(kind)){w=i32(b,pos);h=i32(b,pos+4);depth=b[pos+8]&255;type=b[pos+9]&255;}
                else if("tRNS".equals(kind))trns=Arrays.copyOfRange(b,pos,pos+len);
                else if("IDAT".equals(kind))idat.write(b,pos,len);
                pos+=len+4;if("IEND".equals(kind))break;
            }
            if(w<=0||h<=0||depth!=8||type!=3)throw new IOException("Visual V1 expects indexed 8-bit PNG: "+path);
            byte[] raw;try(InflaterInputStream in=new InflaterInputStream(new ByteArrayInputStream(idat.toByteArray()));ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buf=new byte[8192];for(int n;(n=in.read(buf))!=-1;)out.write(buf,0,n);raw=out.toByteArray();}
            int stride=w,bpp=1,src=0;byte[] px=new byte[w*h],prev=new byte[stride],row=new byte[stride];
            for(int y=0;y<h;y++){
                if(src>=raw.length)throw new IOException("short PNG data: "+path);
                int filter=raw[src++]&255;
                for(int x=0;x<stride;x++){
                    int v=raw[src++]&255,a=x>=bpp?row[x-bpp]&255:0,up=prev[x]&255,ul=x>=bpp?prev[x-bpp]&255:0;
                    int z=switch(filter){case 0->v;case 1->v+a;case 2->v+up;case 3->v+((a+up)>>>1);case 4->v+paeth(a,up,ul);default->throw new IOException("bad PNG filter "+filter);};
                    row[x]=(byte)z;
                }
                System.arraycopy(row,0,px,y*w,w);byte[] tmp=prev;prev=row;row=tmp;
            }
            return new Png(w,h,px,trns);
        }
        boolean opaqueAt(int x,int y){int idx=px[y*w+x]&255;return idx>=alpha.length||(alpha[idx]&255)>8;}
        double coverage(){long n=0;for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(opaqueAt(x,y))n++;return n/(double)(w*h);}
        double frameCoverage(int frame){int fw=w/12,x0=frame*fw;return count(x0,x0+fw,0,h)/(double)(fw*h);}
        int count(int x0,int x1,int y0,int y1){int n=0;for(int y=Math.max(0,y0);y<Math.min(h,y1);y++)for(int x=Math.max(0,x0);x<Math.min(w,x1);x++)if(opaqueAt(x,y))n++;return n;}
        int[] bounds(int x0,int x1){int minX=x1,minY=h,maxX=x0,maxY=0;for(int y=0;y<h;y++)for(int x=x0;x<x1;x++)if(opaqueAt(x,y)){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}return new int[]{minX,minY,maxX+1,maxY+1};}
        int outerMidCount(int x0,int x1){int fw=x1-x0,l=x0+(int)(fw*.34),r=x0+(int)(fw*.66),y0=(int)(h*.32),y1=(int)(h*.68),n=0;for(int y=y0;y<y1;y++)for(int x=x0;x<x1;x++)if((x<l||x>=r)&&opaqueAt(x,y))n++;return n;}
        int uniqueColors(int x0,int x1,int y0,int y1){boolean[] seen=new boolean[256];int n=0;for(int y=Math.max(0,y0);y<Math.min(h,y1);y++)for(int x=Math.max(0,x0);x<Math.min(w,x1);x++){int q=px[y*w+x]&255;if(opaqueAt(x,y)&&!seen[q]){seen[q]=true;n++;}}return n;}
        int bottomColumnGroups(int x0,int x1){boolean on=false;int groups=0,y0=(int)(h*.87);for(int x=x0;x<x1;x++){boolean hit=false;for(int y=y0;y<h;y++)if(opaqueAt(x,y)){hit=true;break;}if(hit&&!on)groups++;on=hit;}return groups;}
        static int i32(byte[]b,int p){return((b[p]&255)<<24)|((b[p+1]&255)<<16)|((b[p+2]&255)<<8)|(b[p+3]&255);}
    }
}
