package com.aicharacter.v3;
import android.app.*;import android.content.*;import android.os.Build;
public final class ProactiveReceiver extends BroadcastReceiver{
 public static final String CHANNEL="vnf_world_events";
 @Override public void onReceive(Context context,Intent intent){
  WorldRepository repo=new WorldRepository(context);WorldState s=repo.loadOrCreate();long now=System.currentTimeMillis();
  try{
   // Alarm advances bounded persistent life; it never invents notification copy on its own.
   OfflineLifeEngine.reconstruct(s,now);CatOfflineEngine.followAttachment(s);
   if(GirlCatSearchEngine.shouldSearch(s,now))GirlCatSearchEngine.start(s,now,"absence/history made the cat worth looking for");
   if(s.catSearch.active)GirlCatSearchEngine.advance(s,now);CatOfflineEngine.followAttachment(s);
   GodMessage gm=GodAttentionEngine.nextDelivery(s);
   if(gm!=null&&GodAttentionEngine.grounded(s,gm)&&s.proactiveEnabled&&!ProactiveScheduler.isQuietHourAt(s,now)){
    NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(CHANNEL,"VNF — Tin từ Thần",NotificationManager.IMPORTANCE_DEFAULT));
    Intent open=new Intent(context,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(context,19,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,CHANNEL):new Notification.Builder(context);
    b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("VNF · THẦN").setContentText(gm.text).setAutoCancel(true).setContentIntent(pi);
    nm.notify(gm.messageId.hashCode(),b.build());gm.delivered=true;s.lastProactiveAt=now;s.lastGodTrace += " delivery=NOTIFIED quiet=false";
   }else if(gm!=null){s.lastGodTrace += " delivery=INBOX_ONLY grounded="+GodAttentionEngine.grounded(s,gm)+" quiet="+ProactiveScheduler.isQuietHourAt(s,now);}
   repo.save(s);
  }finally{ProactiveScheduler.scheduleApproximate(context,s);}
 }
}