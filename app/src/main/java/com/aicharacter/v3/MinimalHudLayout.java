package com.aicharacter.v3;

/** Pure geometry contract shared by HUD rendering, hit-testing and CI screen-size checks. */
public final class MinimalHudLayout{
 public static final int NONE=0,GOD=1,CHAT=2,MIC=3;
 private MinimalHudLayout(){}
 public static final class Box{
  public final float l,t,r,b;
  Box(float l,float t,float r,float b){this.l=l;this.t=t;this.r=r;this.b=b;}
  public float width(){return r-l;}public float height(){return b-t;}
  public boolean contains(float x,float y){return x>=l&&x<=r&&y>=t&&y<=b;}
  public boolean overlaps(Box o){return l<o.r&&r>o.l&&t<o.b&&b>o.t;}
  public boolean inside(float w,float h,float margin){return l>=margin&&t>=margin&&r<=w-margin&&b<=h-margin;}
 }
 public static final class Layout{
  public final Box status,god,chat,mic;public final float density,margin,button;
  Layout(Box status,Box god,Box chat,Box mic,float density,float margin,float button){this.status=status;this.god=god;this.chat=chat;this.mic=mic;this.density=density;this.margin=margin;this.button=button;}
  public int hit(float x,float y){if(god.contains(x,y))return GOD;if(chat.contains(x,y))return CHAT;if(mic.contains(x,y))return MIC;return NONE;}
 }
 public static Layout forScreen(float w,float h,float density){
  float d=Math.max(.75f,density),m=Math.max(10f*d,Math.min(w,h)*.018f),button=44f*d,gap=10f*d;
  float maxButton=Math.max(36f*d,(h-2*m)*.22f);button=Math.min(button,maxButton);
  float statusH=36f*d,statusW=106f*d;
  Box status=new Box(m,m,m+statusW,m+statusH);
  Box god=new Box(w-m-button,m,w-m,m+button);
  Box mic=new Box(w-m-button,h-m-button,w-m,h-m);
  Box chat=new Box(mic.l-gap-button,h-m-button,mic.l-gap,h-m);
  return new Layout(status,god,chat,mic,d,m,button);
 }
}
