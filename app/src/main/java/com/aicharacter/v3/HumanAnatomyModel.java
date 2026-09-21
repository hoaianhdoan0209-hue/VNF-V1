package com.aicharacter.v3;
import java.util.*;
/** Semantic human anatomy for Haru. It maps body regions to organs/systems without duplicating physiology authority. */
public final class HumanAnatomyModel{
 public enum Region{HEAD,NECK,THORAX,ABDOMEN,LEFT_ARM,RIGHT_ARM,LEFT_LEG,RIGHT_LEG}
 public static final class RegionInfo{
  public final Region region;public final String label;public final List<String> structures,systems;
  RegionInfo(Region r,String l,String[] st,String[] sy){region=r;label=l;structures=Collections.unmodifiableList(Arrays.asList(st));systems=Collections.unmodifiableList(Arrays.asList(sy));}
 }
 private static final EnumMap<Region,RegionInfo> MAP=new EnumMap<>(Region.class);
 static{
  MAP.put(Region.HEAD,new RegionInfo(Region.HEAD,"đầu",new String[]{"hộp sọ","não","mắt","tai","mũi","miệng"},new String[]{"thần kinh","giác quan","vỏ bọc"}));
  MAP.put(Region.NECK,new RegionInfo(Region.NECK,"cổ",new String[]{"cột sống cổ","khí quản","thực quản","mạch máu lớn"},new String[]{"thần kinh","hô hấp","tiêu hóa","tuần hoàn","vận động"}));
  MAP.put(Region.THORAX,new RegionInfo(Region.THORAX,"ngực",new String[]{"tim","phổi","lồng ngực","cơ hoành"},new String[]{"tuần hoàn","hô hấp","vận động"}));
  MAP.put(Region.ABDOMEN,new RegionInfo(Region.ABDOMEN,"bụng",new String[]{"dạ dày","gan","tuyến tụy","ruột","thận","bàng quang"},new String[]{"tiêu hóa","bài tiết","nội tiết","miễn dịch"}));
  MAP.put(Region.LEFT_ARM,new RegionInfo(Region.LEFT_ARM,"tay trái",new String[]{"xương","khớp","cơ","da"},new String[]{"vận động","thần kinh","tuần hoàn","vỏ bọc"}));
  MAP.put(Region.RIGHT_ARM,new RegionInfo(Region.RIGHT_ARM,"tay phải",new String[]{"xương","khớp","cơ","da"},new String[]{"vận động","thần kinh","tuần hoàn","vỏ bọc"}));
  MAP.put(Region.LEFT_LEG,new RegionInfo(Region.LEFT_LEG,"chân trái",new String[]{"xương","khớp","cơ","da"},new String[]{"vận động","thần kinh","tuần hoàn","vỏ bọc"}));
  MAP.put(Region.RIGHT_LEG,new RegionInfo(Region.RIGHT_LEG,"chân phải",new String[]{"xương","khớp","cơ","da"},new String[]{"vận động","thần kinh","tuần hoàn","vỏ bọc"}));
 }
 private HumanAnatomyModel(){}
 public static RegionInfo info(Region r){return MAP.get(r);}
 public static Collection<RegionInfo> all(){return Collections.unmodifiableCollection(MAP.values());}
 public static String organizationLevels(){return"tế bào → mô → cơ quan → hệ cơ quan → cơ thể";}
 public static Region dominantPainRegion(WorldState s){
  if(s==null||s.localizedPain==null)return null;LocalizedPainState p=s.localizedPain;Region best=null;double v=0;
  double[] xs={p.head,p.neck,p.chest,p.abdomen,p.leftArm,p.rightArm,p.leftLeg,p.rightLeg};Region[] rs={Region.HEAD,Region.NECK,Region.THORAX,Region.ABDOMEN,Region.LEFT_ARM,Region.RIGHT_ARM,Region.LEFT_LEG,Region.RIGHT_LEG};
  for(int i=0;i<xs.length;i++)if(xs[i]>v){v=xs[i];best=rs[i];}
  return v>=.12?best:null;
 }
 public static double painLoad(WorldState s,Region r){
  if(s==null||s.localizedPain==null||r==null)return 0;LocalizedPainState p=s.localizedPain;
  switch(r){case HEAD:return p.head;case NECK:return p.neck;case THORAX:return p.chest;case ABDOMEN:return p.abdomen;case LEFT_ARM:return p.leftArm;case RIGHT_ARM:return p.rightArm;case LEFT_LEG:return p.leftLeg;case RIGHT_LEG:return p.rightLeg;default:return 0;}
 }
 public static String diagnostic(WorldState s){
  StringBuilder b=new StringBuilder("HARU ANATOMY\nLevels: ").append(organizationLevels()).append('\n');
  for(RegionInfo x:MAP.values()){double pain=painLoad(s,x.region);b.append(x.label).append(" | ").append(String.join(", ",x.structures)).append(" | systems=").append(String.join("/",x.systems));if(pain>.01)b.append(" | pain=").append(String.format(Locale.US,"%.2f",pain));b.append('\n');}
  Region d=dominantPainRegion(s);if(d!=null)b.append("dominantPain=").append(info(d).label);return b.toString();
 }
}
