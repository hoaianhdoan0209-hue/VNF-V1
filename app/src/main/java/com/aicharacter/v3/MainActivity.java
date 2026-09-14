package com.aicharacter.v3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.Toast;

public final class MainActivity extends Activity implements GameView.Host {
    private WorldRepository repository;
    private WorldState state;
    private GameView gameView;
    private VoiceController voice;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUi();
        repository=new WorldRepository(this);
        state=repository.loadOrCreate();
        long now=System.currentTimeMillis();
        LifeCycleEngine.apply(state,now);
        StateInvariantChecker.repairOrReport(state,now);
        // The cat remains a persistent world entity while the player is away.
        if(!state.catState.awake) CatOfflineEngine.followAttachment(state);
        String reconstructed=OfflineLifeEngine.reconstruct(state,now);
        StateInvariantChecker.repairOrReport(state,now);
        GirlCatSearchEngine.advance(state,now);
        CatOfflineEngine.followAttachment(state);
        CatOfflineEngine.wakeForPlayer(state,now);
        if(!reconstructed.isEmpty()) ReunionEngine.process(state,now);
        repository.save(state);
        voice=new VoiceController(this,new VoiceController.Listener(){ public void onRecognized(String text){ respondToVoice(text); } public void onStatus(String text){ Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show(); }});
        gameView=new GameView(this,state,this);
        setContentView(gameView);
        if(!reconstructed.isEmpty()) Toast.makeText(this,"Thế giới đã tiếp tục sống khi mèo ngủ.",Toast.LENGTH_LONG).show();
        maybeAskNotificationPermission();
    }

    @Override protected void onResume(){
        super.onResume();
        if(state!=null&&!state.catState.awake){
            long now=System.currentTimeMillis();
            OfflineLifeEngine.reconstruct(state,now);
            GirlCatSearchEngine.advance(state,now);
            CatOfflineEngine.followAttachment(state);
            CatOfflineEngine.wakeForPlayer(state,now);
            ReunionEngine.process(state,now);
            repository.save(state);
        }
        if(gameView!=null)gameView.postInvalidate();
    }
    @Override protected void onPause(){ super.onPause(); if(state!=null){ long now=System.currentTimeMillis(); state.lastOpenedAt=now; state.lastSimulatedAt=now; state.catState.lastPlayerActiveAt=now; state.catState.x=state.catX; state.catState.awake=true; CatOfflineEngine.beginSleep(state,now); repository.save(state); ProactiveScheduler.scheduleApproximate(this,state); } }
    @Override protected void onDestroy(){ if(voice!=null)voice.destroy(); super.onDestroy(); }

    @Override public void onTalk() {
        final EditText input=new EditText(this); input.setHint("Nói với cô ấy…"); input.setSingleLine(false); input.setMaxLines(3);
        new AlertDialog.Builder(this).setTitle("Nói trong thế giới").setView(input).setPositiveButton("Nói",(d,w)->{
            HaruMind.Response r=HaruMind.respond(state,input.getText().toString()); repository.save(state);
            new AlertDialog.Builder(this).setTitle(DisplayNames.girl(state)).setMessage(r.speech).setPositiveButton("Ừ",null).show();
        }).setNegativeButton("Thôi",null).show();
    }


    @Override public void onVoiceTap(boolean doubleTap) {
        if(doubleTap) voice.toggle(); else voice.listen();
    }

    private void respondToVoice(String text){
        HaruMind.Response r=HaruMind.respond(state,text); repository.save(state); Toast.makeText(this,r.speech,Toast.LENGTH_LONG).show(); voice.speak(r.speech);
    }

    @Override public void onGod() {
        final EditText input=new EditText(this);
        input.setHint("Ví dụ: Tôi muốn gọi cô ấy là Mai");
        input.setSingleLine(false); input.setMaxLines(3);
        new AlertDialog.Builder(this).setTitle("LIÊN HỆ VỚI THẦN")
            .setMessage(godInboxSummary())
            .setView(input).setPositiveButton("Gửi",(d,w)->{
                String out=GodContactController.handle(state,input.getText().toString());
                repository.save(state);
                new AlertDialog.Builder(this).setTitle("THẦN").setMessage(out).setPositiveButton("Đóng",null).show();
            }).setNegativeButton("Đóng",null).show();
    }

    @Override public void onConsole() {
        final EditText input=new EditText(this); input.setHint("TRẠNG THÁI / CHẨN ĐOÁN / ..."); input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("SYSTEM · Thần").setMessage("Console này chỉ thao tác vùng an toàn. Nó không phải AI online.").setView(input).setPositiveButton("Chạy",(d,w)->{
            String out=SystemConsole.execute(state,input.getText().toString()); repository.save(state);
            new AlertDialog.Builder(this).setTitle("SYSTEM").setMessage(out).setPositiveButton("Đóng",null).show();
        }).setNegativeButton("Đóng",null).show();
    }


    private String godInboxSummary(){StringBuilder b=new StringBuilder("Kênh SYSTEM cục bộ. Không phải AI online.\n\nTIN TỪ THẦN:\n");int start=Math.max(0,state.godInbox.size()-5);for(int i=start;i<state.godInbox.size();i++){GodMessage m=state.godInbox.get(i);b.append(m.read?"· ":"• ").append(m.text).append('\n');m.read=true;}if(state.godInbox.isEmpty())b.append("(chưa có tin quan trọng)");return b.toString();}

    private void maybeAskNotificationPermission(){
        if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},401);
    }
    private void hideSystemUi(){
        if(android.os.Build.VERSION.SDK_INT>=30){
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController c=getWindow().getInsetsController(); if(c!=null)c.hide(WindowInsets.Type.systemBars());
        } else {
            getWindow().getDecorView().setSystemUiVisibility(5894);
        }
    }
}
