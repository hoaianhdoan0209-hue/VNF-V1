package com.aicharacter.v3;
import java.util.Locale;
public final class GodContactController {
 private GodContactController(){}
 public static String handle(WorldState s,String input){
  String raw=input==null?"":input.trim();String q=raw.toUpperCase(Locale.ROOT);
  String proposed=DivineNamingEngine.extractProposal(raw);
  if(GodTeachingGateway.looksLikeTeaching(raw))return GodTeachingGateway.teach(s,raw,System.currentTimeMillis());
  if(!proposed.isEmpty())return DivineNamingEngine.grant(s,proposed,System.currentTimeMillis()).message;
  if(q.contains("TÊN")||q.contains("TEN"))return s.nameState.isNamed()?"Tên đã được ban là "+s.nameState.officialName+".":"Cô gái hiện chưa có tên chính thức. Bạn có thể đề nghị: “Tôi muốn gọi cô ấy là Mai.”";
  if(q.contains("TRẠNG THÁI")||q.contains("TRANG THAI"))return WorldSnapshot.observe(s).summary();
  if(q.contains("BẢO VỆ")||q.contains("BAO VE"))return SystemConsole.execute(s,"BẢO VỆ CÔ GÁI");
  if(q.contains("ĐÁNH GIÁ")||q.contains("DANH GIA"))return SystemConsole.execute(s,"ĐÁNH GIÁ THẾ GIỚI");
  if(q.contains("CHẨN ĐOÁN")||q.contains("CHAN DOAN"))return SystemConsole.execute(s,"CHẨN ĐOÁN");
  return "Kênh này dùng capability cục bộ của SYSTEM; AI online chưa được cấu hình. Bạn có thể hỏi trạng thái, đánh giá, chẩn đoán, bảo vệ cô gái hoặc đề nghị một tên.";
 }
}