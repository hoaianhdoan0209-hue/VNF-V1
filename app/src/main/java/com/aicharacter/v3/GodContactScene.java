package com.aicharacter.v3;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Authored contact layer: a presence/interface for the cat, never a physical NPC or controller of the girl. */
public final class GodContactScene {
    public interface Host { void onSend(String text); void onClose(); }
    private final Activity activity; private final Host host; private final FrameLayout root; private final TextView status,conversation,avatar; private final EditText input; private boolean sending;
    public GodContactScene(Activity a,View world,String inbox,Host h){activity=a;host=h;root=new FrameLayout(a);root.setBackgroundColor(Color.rgb(10,16,28));
        LinearLayout column=new LinearLayout(a);column.setOrientation(LinearLayout.VERTICAL);column.setPadding(dp(22),dp(16),dp(22),dp(18));root.addView(column,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(a);top.setGravity(Gravity.CENTER_VERTICAL);TextView title=text("LIÊN HỆ VỚI THẦN",20,Color.rgb(242,225,178));top.addView(title,new LinearLayout.LayoutParams(0,-2,1));Button close=new Button(a);close.setText("×");close.setOnClickListener(v->host.onClose());top.addView(close,new LinearLayout.LayoutParams(dp(58),dp(48)));column.addView(top);
        status=text("● "+statusLabel(GodSessionManager.getState()),13,Color.rgb(180,205,213));column.addView(status);
        avatar=text("✦\nTHẦN\n✦",30,Color.rgb(246,222,157));avatar.setGravity(Gravity.CENTER);avatar.setPadding(0,dp(16),0,dp(16));column.addView(avatar,new LinearLayout.LayoutParams(-1,dp(150)));
        ScrollView scroll=new ScrollView(a);conversation=text(inbox==null?"":inbox,15,Color.rgb(226,228,220));conversation.setPadding(dp(16),dp(14),dp(16),dp(14));GradientDrawable panel=new GradientDrawable();panel.setColor(Color.rgb(22,31,46));panel.setCornerRadius(dp(14));conversation.setBackground(panel);scroll.addView(conversation);column.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout sendRow=new LinearLayout(a);sendRow.setGravity(Gravity.BOTTOM);input=new EditText(a);input.setHint("Nói với Thần…");input.setTextColor(Color.WHITE);input.setHintTextColor(Color.rgb(145,155,168));input.setMaxLines(4);sendRow.addView(input,new LinearLayout.LayoutParams(0,-2,1));Button send=new Button(a);send.setText("GỬI");send.setOnClickListener(v->{String q=input.getText().toString().trim();if(!q.isEmpty()&&!sending){sending=true;input.setEnabled(false);send.setEnabled(false);append("\n\nMÈO\n"+q+"\n\nTHẦN\n…");host.onSend(q);}});sendRow.addView(send,new LinearLayout.LayoutParams(dp(90),-2));column.addView(sendRow);
    }
    public View view(){return root;}
    public void setSessionState(GodSessionManager.State s){activity.runOnUiThread(()->status.setText("● "+statusLabel(s)));}
    public void showReply(String reply){activity.runOnUiThread(()->{replaceWaiting(reply);sending=false;input.setText("");input.setEnabled(true);});}
    public void showError(String error){activity.runOnUiThread(()->{replaceWaiting("[Kết nối gián đoạn] "+error);sending=false;input.setEnabled(true);});}
    private void append(String s){conversation.append(s);}
    private void replaceWaiting(String reply){String s=conversation.getText().toString();if(s.endsWith("THẦN\n…"))s=s.substring(0,s.length()-1)+(reply==null?"…":reply);else s+="\n\nTHẦN\n"+(reply==null?"…":reply);conversation.setText(s);}
    private String statusLabel(GodSessionManager.State s){switch(s){case CONNECTING:return"ĐANG KẾT NỐI";case ONLINE:return"THẦN ONLINE";case DEGRADED:return"KẾT NỐI YẾU";default:return"THẦN OFFLINE";}}
    private TextView text(String s,float sp,int color){TextView t=new TextView(activity);t.setText(s);t.setTextSize(sp);t.setTextColor(color);return t;}
    private int dp(int n){return Math.round(n*activity.getResources().getDisplayMetrics().density);}
}
