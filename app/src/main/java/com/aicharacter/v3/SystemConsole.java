package com.aicharacter.v3;
import java.util.*;
public final class SystemConsole {private SystemConsole(){}
 public static String execute(WorldState s,String command){String q=command==null?"":command.trim().toUpperCase(Locale.ROOT);
  if(q.equals("DEV LIFE TEST"))return LifeDevTest.runAll(s);
  if(q.equals("DEV QUALITY TEST"))return LifeQualityDevTest.runAll(s);
  if(q.equals("WHY HARU")||q.equals("WHY HARU V2")){LifeDecisionEngine.choose(s,System.currentTimeMillis());return s.lastDecisionTrace;}
  if(q.equals("MEMORY GRAPH"))return LivingCharacterDiagnostics.memoryGraph(s);
  if(q.equals("BELIEF DIAGNOSTIC V2"))return LivingCharacterDiagnostics.beliefs(s);
  if(q.equals("RELATIONSHIP DIAGNOSTIC"))return LivingCharacterDiagnostics.relationship(s);
  if(q.equals("BELIEF DIAGNOSTIC"))return LifeDiagnostics.beliefs(s);
  if(q.equals("DIARY DIAGNOSTIC"))return LifeDiagnostics.diary(s);
  if(q.equals("OFFLINE TRACE"))return s.lastOfflineTrace.isEmpty()?"No offline trace in this runtime.":s.lastOfflineTrace;
  if(q.equals("CAT SLEEP TRACE"))return CatDiagnostics.sleep(s);
  if(q.equals("FIND CAT TRACE"))return CatDiagnostics.find(s);
  if(q.equals("NAME STATE"))return CatDiagnostics.name(s);
  if(q.equals("DEV V06 TEST"))return V06DevTest.run(s);
  if(q.equals("DEV V07 TEST"))return V07DevTest.run(s);
  if(q.equals("PLAN TRACE"))return PlanDiagnostics.trace(s);
  if(q.equals("DEV V071 TEST"))return V071DevTest.run(s);
  if(q.equals("TRAVEL TRACE"))return TravelDiagnostics.travel(s);
  if(q.equals("CAT ATTACH TRACE"))return CatOfflineEngine.attachTrace(s);
  if(q.equals("DEV V081 TEST"))return V081DevTest.continuity(s);
  if(q.equals("PHYSICAL INTERACTION TRACE")){WorldObject o=s.world.object(s.planState.destination);if(o==null)o=s.world.firstTagged("reflect");return PhysicalInteraction.trace(s,o);}
  if(q.equals("HARU VISION"))return HaruVisionEngine.diagnostic(s);
  if(q.equals("ANATOMY TRACE")||q.equals("HARU ANATOMY"))return HumanAnatomyModel.diagnostic(s);
  if(q.equals("BIOLOGY TRACE")||q.equals("HARU PHYSIOLOGY"))return PhysiologyDiagnostics.trace(s);
  if(q.equals("ECOLOGY TRACE")||q.equals("BIOME TRACE"))return EcologyEngine.diagnostic(s);
  if(q.equals("PERCEPTION TRACE"))return TravelDiagnostics.perception(s);
  if(q.equals("ROUTE TRACE"))return TravelDiagnostics.route(s);
  if(q.equals("DEV V08 TEST"))return V08DevTest.run(s);
  if(q.equals("GOD TRACE"))return s.lastGodTrace.isEmpty()?"God has remained silent; no relevant grounded event in this runtime.":s.lastGodTrace;
  if(q.equals("CAMERA TRACE")){CatCameraDirector.direct(s);return VisualDiagnostics.camera(s);}
  if(q.equals("CAMERA TRACE V2"))return VisualDiagnostics.camera(s);
  if(q.equals("GRAPHICS QUALITY REPORT"))return GraphicsQualityReport.summarize(s);
  if(q.equals("DEV V091 TEST"))return V091DevTest.run(s);
  if(q.equals("DEV V092 TEST"))return V092DevTest.run(s);
  if(q.equals("ANIMATION TRACE"))return VisualDiagnostics.animation(s);
  if(q.startsWith("DEV TEST ")){try{int n=Integer.parseInt(q.substring(9).trim());WorldCaretaker.injectScenario(s,n);return"Injected caretaker test "+n+". Run ĐÁNH GIÁ THẾ GIỚI / SỬA THẾ GIỚI.";}catch(Exception e){return"Use DEV TEST 1..5";}}
  switch(q){case"TRẠNG THÁI":case"TRANG THAI":return WorldSnapshot.observe(s).summary()+"\nDefinition v"+s.world.definitionVersion+" · history="+s.worldHistory.size();case"ĐÁNH GIÁ THẾ GIỚI":case"DANH GIA THE GIOI":{List<WorldIssue>x=WorldCaretaker.evaluate(s);StringBuilder b=new StringBuilder("Prioritized issues: "+x.size()+"\n");for(WorldIssue i:x)b.append("• ").append(i).append(" · score=").append(i.priorityScore()).append('\n');return b.toString();}case"SỬA THẾ GIỚI":case"SUA THE GIOI":case"TỰ CHỮA":case"TU CHUA":return WorldCaretaker.repairPrioritized(s).message;case"HOÀN TÁC":case"HOAN TAC":return WorldCaretaker.rollback(s)?"Đã rollback checkpoint gần nhất.":"Không có checkpoint hợp lệ.";case"NHẬT KÝ THẦN":case"NHAT KY THAN":{StringBuilder b=new StringBuilder();for(int i=Math.max(0,s.caretakerLog.size()-10);i<s.caretakerLog.size();i++){CaretakerLogEntry e=s.caretakerLog.get(i);b.append(e.event).append(" ").append(e.result).append(": ").append(e.detail).append('\n');}return b.length()==0?"Caretaker chưa có log.":b.toString();}case"CHẨN ĐOÁN":case"CHAN DOAN":return"Save v"+WorldState.SAVE_VERSION+" / world definition v"+s.world.definitionVersion+"\nWeather source: EnvironmentState.weather\nAI backend: NOT CONFIGURED\nSystem Vision: VISION_BACKEND_NOT_CONFIGURED\nDeveloper reports: "+s.developerReports.size();case"BẢO VỆ CÔ GÁI":case"BAO VE CO GAI":case"BẢO VỆ HARU":case"BAO VE HARU":{List<WorldIssue>x=WorldEvaluator.evaluate(s);for(WorldIssue i:x)if("HARU_PROTECTION".equals(i.domain)){return WorldCaretaker.repairPrioritized(s).message;}return"No technical girl-protection fault detected. Emotional state was not modified.";}case"LUẬT CỦA THẦN":case"LUAT CUA THAN":return"SYSTEM may repair safe world/runtime data transactionally. Girl emotion, relationship, memory and intention are not caretaker writable domains.";case"OFFLINE TRACE V2":return s.lastOfflineTrace;case"CAT SLEEP TRACE V2":return PersistentLifeDiagnostics.sleep(s);case"GIRL SEARCH TRACE V2":return GirlCatSearchEngine.trace(s);case"GOD ATTENTION TRACE":return PersistentLifeDiagnostics.god(s);case"PERSISTENCE TRACE":return PersistentLifeDiagnostics.persistence(s);case"DEV V095 TEST":return V095DevTest.run(s);case"DEV V099A TEST":return V099ADevTest.run(s);default:return"Lệnh: TRẠNG THÁI, ĐÁNH GIÁ THẾ GIỚI, SỬA THẾ GIỚI, HOÀN TÁC, NHẬT KÝ THẦN, CHẨN ĐOÁN, BẢO VỆ HARU. Dev: DEV TEST 1..5, DEV LIFE TEST, DEV QUALITY TEST, WHY HARU, BELIEF DIAGNOSTIC, DIARY DIAGNOSTIC, OFFLINE TRACE, CAT SLEEP TRACE, FIND CAT TRACE, NAME STATE, DEV V06 TEST, DEV V07 TEST, DEV V071 TEST, PLAN TRACE, TRAVEL TRACE, PERCEPTION TRACE, ROUTE TRACE, DEV V08 TEST, GOD TRACE, CAMERA TRACE V2, ANIMATION TRACE, GRAPHICS QUALITY REPORT, DEV V092 TEST";}}
}
