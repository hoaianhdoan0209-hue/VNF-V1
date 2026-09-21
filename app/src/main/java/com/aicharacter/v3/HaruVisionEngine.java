package com.aicharacter.v3;
import java.util.ArrayList;import java.util.Collections;import java.util.Comparator;import java.util.List;import java.util.Locale;
/** Bounded first-person perception for Haru. This is derived from local world conditions and never grants System omniscience. */
public final class HaruVisionEngine{
 public static final class Seen{
  public final String id,label,kind,direction;public final float distance;public final double clarity;public final boolean familiar;
  Seen(String i,String l,String k,String d,float dist,double c,boolean f){id=i;label=l;kind=k;direction=d;distance=dist;clarity=c;familiar=f;}
 }
 public static final class Snapshot{
  public final String areaId,areaName,weather,bodyHint;public final double visibility,attention;public final float range;public final boolean catVisible;public final float catDistance;public final List<Seen> seen;
  Snapshot(String ai,String an,String w,String b,double v,double at,float r,boolean cv,float cd,List<Seen>x){areaId=ai;areaName=an;weather=w;bodyHint=b;visibility=v;attention=at;range=r;catVisible=cv;catDistance=cd;seen=Collections.unmodifiableList(x);}
 }
 private HaruVisionEngine(){}
 public static Snapshot observe(WorldState s){
  if(s==null||s.world==null||s.environment==null)return new Snapshot("unknown","một nơi khó xác định","không rõ","không rõ",.15,.35,120,false,Float.NaN,new ArrayList<>());
  WorldArea area=s.world.areaAt(s.haruX);double visibility=WorldSemantics.visibility(s,area),attention=attention(s);float range=(float)(120+520*visibility*attention);String areaId=area==null?"unknown":area.id,areaName=area==null?"một nơi khó xác định":area.haruName;
  List<Seen> out=new ArrayList<>();
  if(area!=null)for(WorldObject o:s.world.objects){Seen x=seeObject(s,area,o,range,visibility,attention);if(x!=null)out.add(x);}
  out.sort(Comparator.comparingDouble(a->a.distance));
  boolean catVisible=false;float catDistance=Float.NaN;if(s.catState!=null){WorldArea ca=s.world.areaAt(s.catState.x);float d=Math.abs(s.catState.x-s.haruX);catVisible=ca!=null&&area!=null&&area.id.equals(ca.id)&&d<=range*.82f;if(catVisible)catDistance=d;}
  return new Snapshot(areaId,areaName,weatherText(s),bodyHint(s),visibility,attention,range,catVisible,catDistance,out);
 }
 public static boolean canSee(WorldState s,WorldObject o){if(s==null||s.world==null||o==null||!o.enabled)return false;WorldArea area=s.world.areaAt(s.haruX);if(area==null||!area.id.equals(o.areaId))return false;double visibility=WorldSemantics.visibility(s,area),attention=attention(s);float range=(float)(120+520*visibility*attention);return Math.abs(o.x-s.haruX)<=range;}
 public static String diagnostic(WorldState s){
  Snapshot v=observe(s);StringBuilder b=new StringBuilder("HARU VISION\n");b.append("place=").append(v.areaName).append(" visibility=").append(fmt(v.visibility)).append(" attention=").append(fmt(v.attention)).append(" range=").append((int)v.range).append("px\n");b.append("weather=").append(v.weather).append(" body=").append(v.bodyHint).append('\n');b.append("cat=").append(v.catVisible?"VISIBLE distance="+(int)v.catDistance+"px":"NOT VISIBLE").append('\n');for(Seen x:v.seen)b.append(x.label).append(" · ").append((int)x.distance).append("px · ").append(x.direction).append(" · clarity=").append(fmt(x.clarity)).append(x.familiar?" · familiar":"").append('\n');return b.toString();}
 private static Seen seeObject(WorldState s,WorldArea area,WorldObject o,float range,double visibility,double attention){if(o==null||!o.enabled||!area.id.equals(o.areaId))return null;float d=Math.abs(o.x-s.haruX);if(d>range)return null;double clarity=Math.max(.12,Math.min(1,visibility*attention*(1-d/Math.max(1f,range)*.55)));String label=o.haruDescription==null||o.haruDescription.trim().isEmpty()?"một vật ở gần":o.haruDescription;return new Seen(o.id,label,o.type,direction(o.x-s.haruX),d,clarity,familiar(s,o));}
 private static boolean familiar(WorldState s,WorldObject o){for(MemoryEntry m:s.memories)if(o.id.equals(m.location)||m.tags.contains(o.id)||m.participants.contains(o.id))return true;return false;}
 private static double attention(WorldState s){if(s.body==null)return .75;double energy=Math.max(0,Math.min(1,s.body.energy/100.0)),sleep=Math.max(0,Math.min(1,s.body.sleepiness/100.0)),pain=Math.max(0,Math.min(1,s.body.pain/100.0));double a=.42+energy*.48-sleep*.22-pain*.16;return Math.max(.32,Math.min(1,a));}
 private static String bodyHint(WorldState s){if(s.body==null)return"bình thường";if(s.body.pain>45)return"đau làm sự chú ý bị thu hẹp";if(s.body.sleepiness>75)return"buồn ngủ, khó tập trung";if(s.body.energy<28)return"mệt, chú ý kém";if(s.thermal!=null&&s.thermal.coldLoad>.55)return"lạnh";return"ổn định";}
 private static String weatherText(WorldState s){if("RAIN".equals(s.environment.weather))return"mưa "+(s.environment.weatherIntensity>.65?"mạnh":"nhẹ");if("CLOUDY".equals(s.environment.weather))return"nhiều mây";return"trời quang";}
 private static String direction(float dx){if(dx<-70)return"bên trái";if(dx>70)return"bên phải";return"ngay gần";}
 private static String fmt(double d){return String.format(Locale.US,"%.2f",d);}
}
