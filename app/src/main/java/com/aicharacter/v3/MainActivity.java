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
import android.widget.TextView;
import android.widget.ScrollView;
import android.graphics.Color;

public final class MainActivity extends Activity implements GameView.Host {
    private WorldRepository repository;
    private WorldState state;
    private GameView gameView;
    private VoiceController voice;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show a real frame immediately. World loading/offline reconstruction may be
        // expensive after a long absence and must never block Android's main thread.
        showStartupScreen();

        try {
            hideSystemUi();
        } catch (Throwable ignored) { }

        final android.content.Context appContext=getApplicationContext();
        new Thread(() -> {
            try {
                WorldRepository loadedRepository=new WorldRepository(appContext);
                WorldState loadedState=loadedRepository.loadOrCreate();
                long now=System.currentTimeMillis();

                LifeCycleEngine.apply(loadedState,now);
                StateInvariantChecker.repairOrReport(loadedState,now);
                if(!loadedState.catState.awake) CatOfflineEngine.followAttachment(loadedState);

                String reconstructed=OfflineLifeEngine.reconstruct(loadedState,now);

                StateInvariantChecker.repairOrReport(loadedState,now);
                GirlCatSearchEngine.advance(loadedState,now);
                CatOfflineEngine.followAttachment(loadedState);
                CatOfflineEngine.wakeForPlayer(loadedState,now);
                if(!reconstructed.isEmpty()) ReunionEngine.process(loadedState,now);
                loadedRepository.save(loadedState);

                runOnUiThread(() -> {
                    try {
                        repository=loadedRepository;
                        state=loadedState;
                        voice=new VoiceController(this,new VoiceController.Listener(){
                            public void onRecognized(String text){ respondToVoice(text); }
                            public void onStatus(String text){ Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show(); }
                        });
                        gameView=new GameView(this,state,this);
                        setContentView(gameView);
                        if(!reconstructed.isEmpty()) Toast.makeText(this,"Thế giới đã tiếp tục sống khi mèo ngủ.",Toast.LENGTH_LONG).show();
                        maybeAskNotificationPermission();
                    } catch (Throwable uiStartupError) {
                        showStartupFailure(uiStartupError);
                    }
                });
            } catch (Throwable startupError) {
                runOnUiThread(() -> showStartupFailure(startupError));
            }
        },"VNF-World-Startup").start();
    }

    private void showStartupScreen(){
        TextView textView=new TextView(this);
        textView.setText("VNF\n\nĐang đánh thức thế giới…");
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(Color.rgb(241,229,201));
        textView.setBackgroundColor(Color.rgb(18,31,33));
        textView.setTextSize(22);
        setContentView(textView);
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
        input.setHint("Nói với Thần…");
        input.setSingleLine(false); input.setMaxLines(4);
        new AlertDialog.Builder(this).setTitle("LIÊN HỆ VỚI THẦN")
            .setMessage(godInboxSummary())
            .setView(input).setPositiveButton("Gửi",(d,w)->{
                String raw=input.getText().toString().trim();
                if(raw.isEmpty()) return;

                // Privileged SYSTEM capabilities remain deterministic and local.
                // Online God may advise, but never becomes the authority that mutates world state.
                if(isLocalGodCapability(raw)){
                    String out=GodContactController.handle(state,raw);
                    repository.save(state);
                    new AlertDialog.Builder(this).setTitle("THẦN · SYSTEM").setMessage(out).setPositiveButton("Đóng",null).show();
                    return;
                }

                if(!VnfOnlineConfig.godConfigured()){
                    new AlertDialog.Builder(this).setTitle("THẦN · OFFLINE")
                        .setMessage("Thần online chưa được kết nối. Thế giới và cô gái vẫn tiếp tục sống hoàn toàn offline.\n\nCác capability cục bộ như đặt tên, trạng thái, bảo vệ và chẩn đoán vẫn dùng được.")
                        .setPositiveButton("Đóng",null).show();
                    return;
                }

                Toast.makeText(this,"Đang kết nối với Thần…",Toast.LENGTH_SHORT).show();
                GodOnlineClient.ask(state,raw,(reply,error)->runOnUiThread(()->{
                    if(error!=null){
                        new AlertDialog.Builder(this).setTitle("THẦN · MẤT KẾT NỐI")
                            .setMessage("Không thể liên hệ Thần online lúc này.\n\n"+error.getClass().getSimpleName()+"\n\nGame vẫn tiếp tục offline bình thường.")
                            .setPositiveButton("Đóng",null).show();
                    }else{
                        new AlertDialog.Builder(this).setTitle("THẦN").setMessage(reply).setPositiveButton("Đóng",null).show();
                    }
                }));
            }).setNegativeButton("Đóng",null).show();
    }

    private boolean isLocalGodCapability(String raw){
        String q=raw.toUpperCase(java.util.Locale.ROOT);
        if(!DivineNamingEngine.extractProposal(raw).isEmpty()) return true;
        return q.contains("TRẠNG THÁI")||q.contains("TRANG THAI")||q.contains("BẢO VỆ")||q.contains("BAO VE")||q.contains("ĐÁNH GIÁ")||q.contains("DANH GIA")||q.contains("CHẨN ĐOÁN")||q.contains("CHAN DOAN")||q.contains("TÊN")||q.contains("TEN");
    }

    @Override public void onConsole() {
        final EditText input=new EditText(this); input.setHint("TRẠNG THÁI / CHẨN ĐOÁN / ..."); input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("SYSTEM · Thần").setMessage("Console này chỉ thao tác vùng an toàn. Nó không phải AI online.").setView(input).setPositiveButton("Chạy",(d,w)->{
            String out=SystemConsole.execute(state,input.getText().toString()); repository.save(state);
            new AlertDialog.Builder(this).setTitle("SYSTEM").setMessage(out).setPositiveButton("Đóng",null).show();
        }).setNegativeButton("Đóng",null).show();
    }


    private String godInboxSummary(){StringBuilder b=new StringBuilder("VNF chạy offline-first. Thần dùng kênh online khi được cấu hình; mất mạng không làm thế giới dừng.\n\nTIN TỪ THẦN:\n");int start=Math.max(0,state.godInbox.size()-5);for(int i=start;i<state.godInbox.size();i++){GodMessage m=state.godInbox.get(i);b.append(m.read?"· ":"• ").append(m.text).append('\n');m.read=true;}if(state.godInbox.isEmpty())b.append("(chưa có tin quan trọng)");return b.toString();}

    private void showStartupFailure(Throwable error){
        StringBuilder msg=new StringBuilder();
        msg.append("VNF đã mở ở chế độ an toàn.\n\n");
        msg.append("Thế giới chưa được chạy để tránh làm hỏng save.\n");
        msg.append("Dữ liệu hiện tại không bị tự động xóa.\n\n");
        msg.append("Lỗi khởi động: ").append(error.getClass().getSimpleName());
        if(error.getMessage()!=null&&!error.getMessage().isEmpty()) msg.append("\n").append(error.getMessage());
        msg.append("\n\nCrash log được lưu trong thư mục nội bộ diagnostics của VNF.");
        TextView textView=new TextView(this);
        textView.setText(msg.toString());
        textView.setTextColor(Color.rgb(241,229,201));
        textView.setBackgroundColor(Color.rgb(18,31,33));
        textView.setTextSize(18);
        int pad=(int)(24*getResources().getDisplayMetrics().density);
        textView.setPadding(pad,pad,pad,pad);
        ScrollView scroll=new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(18,31,33));
        scroll.addView(textView);
        setContentView(scroll);
        Toast.makeText(this,"VNF: chế độ an toàn — gửi ảnh màn hình này cho Móng.",Toast.LENGTH_LONG).show();
    }

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
