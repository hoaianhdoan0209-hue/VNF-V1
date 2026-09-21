package com.aicharacter.v3;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.os.Build;
public final class ProactiveReceiver extends BroadcastReceiver{
 public static final String CHANNEL="vnf_world_events";
 @Override public void onReceive(Context context,Intent intent){
  WorldRepository repo=new WorldRepository(context);WorldState s=repo.loadOrCreate();long now=System.currentTimeMillis();
  try{
   WorldCaretaker.Result maintenance=DivineMaintenanceEngine.maintain(s,now);if(maintenance.kept>0)repo.save(s);
   OfflineLifeEngine.reconstruct(s,now);CatOfflineEngine.followAttachment(s);
   if(GirlCatSearchEngine.shouldSearch(s,now))GirlCatSearchEngine.start(s,now,"absence/history made the cat worth looking for");
   if(s.catSearch.active)GirlCatSearchEngine.advance(s,now);CatOfflineEngine.followAttachment(s);
   GodMessage gm=GodAttentionEngine.nextDelivery(s);
   boolean quiet=ProactiveScheduler.isQuietHourAt(s,now);
   boolean permission=Build.VERSION.SDK_INT<33||context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;
   if(gm!=null&&GodAttentionEngine.grounded(s,gm)&&s.proactiveEnabled&&!quiet&&permission){
    NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(CHANNEL,"VNF — Tin từ Thần",NotificationManager.IMPORTANCE_DEFAULT));
    Intent open=new Intent(context,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(context,19,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,CHANNEL):new Notification.Builder(context);
    b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("VNF · THẦN").setContentText(gm.text).setAutoCancel(true).setContentIntent(pi);
    nm.notify(gm.messageId.hashCode(),b.build());gm.delivered=true;s.lastProactiveAt=now;s.lastGodTrace+=" delivery=NOTIFIED quiet=false";
   }else if(gm!=null){s.lastGodTrace+=" delivery=INBOX_ONLY grounded="+GodAttentionEngine.grounded(s,gm)+" quiet="+quiet+" permission="+permission;}
   repo.save(s);
  }finally{ProactiveScheduler.scheduleApproximate(context,s);}
 }
}