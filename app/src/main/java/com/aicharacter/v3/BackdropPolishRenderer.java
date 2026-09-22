package com.aicharacter.v3;
import android.graphics.*;

/** Decorative depth/light texture only. Reads visual palette + environment; never mutates or invents world state. */
public final class BackdropPolishRenderer{
 private BackdropPolishRenderer(){}
 public static void draw(Canvas c,Paint p,WorldVisualProfile v,String area,float cam,float anim,EnvironmentState env){
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  float wind=env==null?0f:(float)Math.max(0,Math.min(1,env.wind));
  int accent=Color.argb(24,Color.red(v.accent),Color.green(v.accent),Color.blue(v.accent));
  int haze=Color.argb(18,Color.red(v.haze),Color.green(v.haze),Color.blue(v.haze));
  int seed="garden".equals(area)?37:"lakeside".equals(area)?71:"grove".equals(area)?109:13;
  if("grove".equals(area)){
   p.setShader(new LinearGradient(0,230,0,780,Color.argb(26,224,239,210),Color.TRANSPARENT,Shader.TileMode.CLAMP));
   for(int i=0;i<5;i++){float x=170+i*460-cam*.08f;c.drawRect(x,210,x+44,770,p);}p.setShader(null);
  }
  for(int i=0;i<30;i++){
   float x=((i*197+seed*23)%2580)-90-cam*.15f;
   float y=360+((i*83+seed*7)%330);
   float w=2+((i+seed)%4),h=1+((i*3+seed)%3);
   p.setColor((i&1)==0?accent:haze);c.drawRect(x,y,x+w,y+h,p);
  }
  if("lakeside".equals(area)){
   p.setColor(Color.argb(34,222,237,221));
   for(int i=0;i<18;i++){float x=((i*151+seed*9)%2470)-cam*.38f+(float)Math.sin(anim*.45+i)*4*wind;float y=654+(i%6)*19;c.drawRect(x,y,x+18+(i%4)*7,y+2,p);}
  }else if("garden".equals(area)){
   p.setColor(Color.argb(30,236,219,177));
   for(int i=0;i<16;i++){float x=((i*233+91)%2450)-cam*.24f;float y=545+(i%5)*31;c.drawRect(x,y,x+3,y+3,p);c.drawRect(x+4,y-2,x+6,y,p);}
  }else if("home".equals(area)){
   p.setColor(Color.argb(22,246,218,164));
   for(int i=0;i<14;i++){float x=((i*181+63)%2400)-cam*.20f;float y=500+(i%4)*37;c.drawRect(x,y,x+5,y+2,p);}
  }
 }
 public static void drawGround(Canvas c,Paint p,WorldVisualProfile v,String area,float cam){
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAntiAlias(false);
  int c1=Color.argb("grove".equals(area)?30:20,Color.red(v.accent),Color.green(v.accent),Color.blue(v.accent));
  int seed="garden".equals(area)?41:"lakeside".equals(area)?73:"grove".equals(area)?113:17;p.setColor(c1);
  for(int i=0;i<42;i++){float x=((i*149+seed*29)%2540)-70-cam*.92f;float y=820+((i*47+seed)%180);float w=2+(i%5);c.drawRect(x,y,x+w,y+2,p);}
 }
}
