package com.aicharacter.v3;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** In-world God contact: the live world stays visible and the entity manifests inside it. */
public final class GodContactScene{
 public interface Host{void onSend(String text);void onClose();}
 private final Activity a;private final Host host;private final FrameLayout root;private final TextView status,conversation;private final ScrollView scroll;private final EditText input;private final Button send;private final DivineManifestationView manifestation;private boolean sending,closing;
 public GodContactScene(Activity a,String inbox,Host h){
  this.a=a;host=h;root=new FrameLayout(a);root.setClickable(true);root.setBackgroundColor(Color.argb(62,5,10,20));
  manifestation=new DivineManifestationView(a);root.addView(manifestation,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout c=new LinearLayout(a);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(20),dp(12),dp(20),dp(16));root.addView(c,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout top=new LinearLayout(a);top.setGravity(Gravity.CENTER_VERTICAL);TextView title=text("SỰ HIỆN DIỆN",16,Color.rgb(242,225,178));title.setLetterSpacing(.12f);top.addView(title,new LinearLayout.LayoutParams(0,-2,1));Button x=new Button(a);x.setText("×");x.setOnClickListener(v->{if(!sending&&!closing)dismiss(host::onClose);else Toast.makeText(a,"Không gian vẫn đang dao động…",Toast.LENGTH_SHORT).show();});top.addView(x,new LinearLayout.LayoutParams(dp(56),dp(46)));c.addView(top);
  status=text(label(GodSessionManager.getState()),12,Color.rgb(190,211,214));status.setGravity(Gravity.CENTER_HORIZONTAL);c.addView(status);
  Space presenceSpace=new Space(a);c.addView(presenceSpace,new LinearLayout.LayoutParams(-1,0,1));
  scroll=new ScrollView(a);conversation=text(inbox==null?"":inbox,14,Color.rgb(234,232,218));conversation.setPadding(dp(15),dp(11),dp(15),dp(11));GradientDrawable panel=new GradientDrawable();panel.setColor(Color.argb(145,10,18,31));panel.setCornerRadius(dp(10));conversation.setBackground(panel);scroll.addView(conversation);c.addView(scroll,new LinearLayout.LayoutParams(-1,dp(150)));
  LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);input=new EditText(a);input.setHint("Khẽ gọi…");input.setTextColor(Color.WHITE);input.setHintTextColor(Color.rgb(176,183,190));input.setMaxLines(3);row.addView(input,new LinearLayout.LayoutParams(0,-2,1));send=new Button(a);send.setText("GỬI");send.setOnClickListener(v->{String q=input.getText().toString().trim();if(!q.isEmpty()&&!sending&&!closing){busy(true);setPhase(DivineManifestationView.Phase.THINKING);conversation.append("\n\nMÈO · "+q+"\n\nTHẦN · …");bottom();host.onSend(q);}});row.addView(send,new LinearLayout.LayoutParams(dp(88),-2));c.addView(row);bottom();syncSession(GodSessionManager.getState());
 }
 public View view(){return root;}public boolean isSending(){return sending||closing;}
 public void setSessionState(GodSessionManager.State s){a.runOnUiThread(()->syncSession(s));}
 public void showThinking(){setPhase(DivineManifestationView.Phase.THINKING);}
 public void showObserving(){setPhase(DivineManifestationView.Phase.OBSERVING);}
 public void showWarning(){setPhase(DivineManifestationView.Phase.WARNING);}
 public void showApplying(){setPhase(DivineManifestationView.Phase.APPLYING);}
 public void showReply(String s){setPhase(DivineManifestationView.Phase.SPEAKING);finish(s,true);a.getWindow().getDecorView().postDelayed(()->{if(!sending&&!closing)setPhase(DivineManifestationView.Phase.PRESENT);},900);}
 public void showSystem(String s){showReply(s);}
 public void showError(String s){setPhase(DivineManifestationView.Phase.WARNING);finish("Sự hiện diện chợt mờ đi. "+s,true);}
 public void showProgress(String s){setPhase(DivineManifestationView.Phase.APPLYING);finish(s,false);}
 public void dismiss(Runnable after){if(closing)return;closing=true;busy(true);setPhase(DivineManifestationView.Phase.DISAPPEARING);root.animate().alpha(0f).setDuration(430).withEndAction(()->{if(after!=null)after.run();}).start();}
 private void setPhase(DivineManifestationView.Phase p){a.runOnUiThread(()->manifestation.setPhase(p));}
 private void syncSession(GodSessionManager.State s){status.setText(label(s));if(s==GodSessionManager.State.CONNECTING)setPhase(DivineManifestationView.Phase.CONNECTING);else if(s==GodSessionManager.State.DEGRADED||s==GodSessionManager.State.OFFLINE)setPhase(DivineManifestationView.Phase.DEGRADED);else if(!sending)setPhase(DivineManifestationView.Phase.PRESENT);}
 private void finish(String reply,boolean release){a.runOnUiThread(()->{String s=conversation.getText().toString();if(s.endsWith("THẦN · …"))s=s.substring(0,s.length()-1)+(reply==null?"…":reply);else s+="\n\nTHẦN · "+(reply==null?"…":reply);conversation.setText(s);if(release){input.setText("");busy(false);}bottom();});}
 private void bottom(){scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
 private void busy(boolean v){sending=v;input.setEnabled(!v);send.setEnabled(!v);}
 private String label(GodSessionManager.State s){switch(s){case CONNECTING:return"✦ ĐANG ĐẾN";case ONLINE:return"✦ HIỆN DIỆN";case DEGRADED:return"✦ ÁNH SÁNG CHẬP CHỜN";default:return"✦ DƯ ÂM";}}
 private TextView text(String s,float z,int color){TextView t=new TextView(a);t.setText(s);t.setTextSize(z);t.setTextColor(color);return t;}private int dp(int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
}
