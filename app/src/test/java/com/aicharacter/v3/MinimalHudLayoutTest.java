package com.aicharacter.v3;
import org.junit.Test;
import static org.junit.Assert.*;

public final class MinimalHudLayoutTest{
 private void verify(float w,float h,float d){
  MinimalHudLayout.Layout u=MinimalHudLayout.forScreen(w,h,d);
  MinimalHudLayout.Box[] b={u.status,u.god,u.chat,u.mic};
  float edge=Math.max(6f,d*4f);
  for(MinimalHudLayout.Box x:b)assertTrue("box outside screen",x.inside(w,h,edge));
  for(int i=0;i<b.length;i++)for(int j=i+1;j<b.length;j++)assertFalse("overlap "+i+"/"+j,b[i].overlaps(b[j]));
  assertTrue(u.god.width()/d>=36f);assertTrue(u.chat.width()/d>=36f);assertTrue(u.mic.width()/d>=36f);
  assertTrue("status too narrow for icon+temperature",u.status.width()/d>=96f);
  assertTrue("status too short",u.status.height()/d>=32f);
 }
 @Test public void landscape16x9(){verify(1280,720,2f);}
 @Test public void landscape18x9(){verify(1440,720,2f);}
 @Test public void landscape19_5x9(){verify(1560,720,2f);}
 @Test public void smallPhone(){verify(960,540,1.5f);}
 @Test public void compactPhone(){verify(854,480,1f);}
}
