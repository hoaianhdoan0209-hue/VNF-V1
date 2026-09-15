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

/** Contact presence for the cat; not a physical NPC and never a controller of the girl. */
public final class GodContactScene {
 public interface Host{void onSend(String text);void onClose();}
 private final Activity a;private final Host host;private final FrameLayout root;private final TextView status,conversation;private final EditText input;private final Button send;private boolean sending;
 public GodContactScene(Activity a,String inbox,Host h){this.a=a;host=h;root=new FrameLayout(a);root.setBackgroundColor(Color.rgb(10,16,28));LinearLayout c=new LinearLayout(a);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(22),dp(16),dp(22),dp(18));root.addView(c,new FrameLayout.LayoutParams(-1,-1));LinearLayout top=new LinearLayout(a);top.setGravity(Gravity.CENTER_VERTICAL);top.addView(text("LIÊN HỆ VỚI THẦN",20,Color.rgb(242,225,178)),new LinearLayout.LayoutParams(0,-2,1));Button x=new Button(a);x.setText("×");x.setOnClickListener(v->host.onClose());top.addView(x,new LinearLayout.LayoutParams(dp(58),dp(48)));c.addView(top);status=text("● "+label(GodSessionManager.getState()),13,Color.rgb(180,205,213));c.addView(status);TextView avatar=text("✦\nTHẦN\n✦",30,Color.rgb(246,222,157));avatar.setGravity(Gravity.CENTER);c.addView(avatar,new LinearLayout.LayoutParams(-1,dp(150)));ScrollView scroll=new ScrollView(a);conversation=text(inbox==null?"":inbox,15,Color.rgb(226,228,220));conversation.setPadding(dp(16),dp(14),dp(16),dp(14));GradientDrawable panel=new GradientDrawable();panel.setColor(Color.rgb(22,31,46));panel.setCornerRadius(dp(14));conversation.setBackground(panel);scroll.addView(conversation);c.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout row=new LinearLayout(a);input=new EditText(a);input.setHint("Nói với Thần…");input.setTextColor(Color.WHITE);input.setHintTextColor(Color.GRAY);input.setMaxLines(4);row.addView(input,new LinearLayout.LayoutParams(0,-2,1));send=new Button(a);send.setText("GỬI");send.setOnClickListener(v->{String q=input.getText().toString().trim();if(!q.isEmpty()&&!sending){busy(true);conversation.append("\n\nMÈO\n"+q+"\n\nTHẦN\n…");host.onSend(q);}});row.addView(send,new LinearLayout.LayoutParams(dp(90),-2));c.addView(row);}
 public View view(){return root;}public void setSessionState(GodSessionManager.State s){a.runOnUiThread(()->status.setText("● "+label(s)));}public void showReply(String s){finish(s);}public void showSystem(String s){finish(s);}public void showError(String s){finish("[Kết nối gián đoạn] "+s);}
 private void finish(String reply){a.runOnUiThread(()->{String s=conversation.getText().toString();if(s.endsWith("THẦN\n…"))s=s.substring(0,s.length()-1)+(reply==null?"…":reply);else s+="\n\nTHẦN\n"+(reply==null?"…":reply);conversation.setText(s);input.setText("");busy(false);});}private void busy(boolean v){sending=v;input.setEnabled(!v);send.setEnabled(!v);}private String label(GodSessionManager.State s){switch(s){case CONNECTING:return"ĐANG KẾT NỐI";case ONLINE:return"THẦN ONLINE";case DEGRADED:return"KẾT NỐI YẾU";default:return"THẦN OFFLINE";}}private TextView text(String s,float z,int color){TextView t=new TextView(a);t.setText(s);t.setTextSize(z);t.setTextColor(color);return t;}private int dp(int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
}
