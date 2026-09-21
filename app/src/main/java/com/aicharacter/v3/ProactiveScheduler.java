package com.aicharacter.v3;
import android.app.AlarmManager;import android.app.PendingIntent;import android.content.Context;import android.content.Intent;
public final class ProactiveScheduler{
 private ProactiveScheduler(){}
 public static void scheduleApproximate(Context c,WorldState s){
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  Intent i=new Intent(c,ProactiveReceiver.class);
  PendingIntent pi=PendingIntent.getBroadcast(c,191,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  if(!s.proactiveEnabled){am.cancel(pi);return;}
  long now=System.currentTimeMillis(),base=2L*3600000L;
  boolean quiet=isQuietHourAt(s,now);
  if(!quiet&&GodAttentionEngine.nextDelivery(s)!=null)base=15L*60000L;
  else if(!quiet&&(catWeatherRisk(s)||s.catSearch!=null&&s.catSearch.active))base=30L*60000L;
  long relation=(long)Math.max(0,(24-s.relationship.attachment)*5*60000L);
  long trigger=now+base+(base>=2L*3600000L?relation:0);
  am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pi);
 }
 private static boolean catWeatherRisk(WorldState s){
  if(s==null||s.environment==null||s.world==null||s.catState==null||!"RAIN".equals(s.environment.weather)||s.environment.weatherIntensity<.48||"girl".equals(s.catState.attachedToEntity))return false;
  WorldArea a=s.world.area(s.catState.areaId);if(a==null)a=s.world.areaAt(s.catState.x);
  return WorldSemantics.exposure(a)>=.55;
 }
 public static boolean isQuietHour(WorldState s){return QuietHoursPolicy.isQuiet(s,System.currentTimeMillis());}
 public static boolean isQuietHourAt(WorldState s,long time){return QuietHoursPolicy.isQuiet(s,time);}
}
