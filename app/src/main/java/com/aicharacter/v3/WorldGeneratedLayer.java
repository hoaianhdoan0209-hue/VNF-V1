package com.aicharacter.v3;
import android.content.Context;import android.graphics.*;import org.json.*;import java.io.*;import java.nio.charset.StandardCharsets;
/** Safe runtime-generated pixel-style world layer. Data is accepted only after SignedGodWorldRecipe verification. */
public final class WorldGeneratedLayer{
 private static long rev=-1;private static Scene cached;
 private WorldGeneratedLayer(){}
 public static void draw(Canvas c,Context ctx,Paint p){Scene s=load(ctx);if(s==null)return;p.setShader(null);p.setAntiAlias(false);
  p.setColor(s.mountain);Path m=new Path();m.moveTo(0,520);m.lineTo(250,360);m.lineTo(430,470);m.lineTo(720,300);m.lineTo(980,475);m.lineTo(1260,330);m.lineTo(1540,480);m.lineTo(1840,315);m.lineTo(2160,455);m.lineTo(2400,350);m.lineTo(2400,610);m.lineTo(0,610);m.close();c.drawPath(m,p);
  p.setColor(s.ground);for(int i=0;i<s.rockCount;i++){float x=180+i*(2040f/Math.max(1,s.rockCount));float y=790+(i%2)*28;c.drawRect(x,y,x+48,y+25,p);c.drawRect(x+10,y-12,x+38,y,p);}
  p.setColor(s.tree);for(int i=0;i<s.treeCount;i++){float x=120+i*(2160f/Math.max(1,s.treeCount-1));float base=720+(i%3)*24;c.drawRect(x-10,base-120,x+10,base,p);c.drawRect(x-48,base-115,x+48,base-72,p);c.drawRect(x-34,base-150,x+35,base-110,p);c.drawRect(x-20,base-178,x+22,base-145,p);}
 }
 private static Scene load(Context c){long r=RuntimeContentStore.revision(c);if(r==rev)return cached;synchronized(WorldGeneratedLayer.class){if(r==rev)return cached;cached=read(c);rev=r;return cached;}}
 private static Scene read(Context c){try{File f=new File(RuntimeContentStore.current(c),"world/visual_profile.json");if(!f.isFile())return null;byte[] b=new byte[(int)Math.min(f.length(),32768)];try(FileInputStream in=new FileInputStream(f)){int n=in.read(b);if(n<=0)return null;JSONObject root=new JSONObject(new String(b,0,n,StandardCharsets.UTF_8));if(!"world-scene-v2".equals(root.optString("type")))return null;JSONObject l=root.getJSONObject("lakeside");return new Scene(Color.parseColor(l.getString("mountain")),Color.parseColor(l.getString("tree")),Color.parseColor(l.getString("groundAccent")),l.optInt("treeCount",5),l.optInt("rockCount",3));}}catch(Throwable ignored){return null;}}
 private static final class Scene{final int mountain,tree,ground,treeCount,rockCount;Scene(int a,int b,int c,int d,int e){mountain=a;tree=b;ground=c;treeCount=d;rockCount=e;}}
}
