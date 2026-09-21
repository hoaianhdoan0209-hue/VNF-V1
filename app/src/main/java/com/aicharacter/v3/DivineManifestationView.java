package com.aicharacter.v3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/** In-world, non-human God manifestation. Every visual mode is driven by real contact/session state. */
public final class DivineManifestationView extends View {
    public enum Phase { CONNECTING, PRESENT, THINKING, SPEAKING, WARNING, OBSERVING, APPLYING, DEGRADED, DISAPPEARING }
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Phase phase=Phase.CONNECTING;
    private long phaseAt=System.currentTimeMillis();

    public DivineManifestationView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public Phase phase(){return phase;}
    public void setPhase(Phase next){if(next==null)next=Phase.DEGRADED;if(phase!=next){phase=next;phaseAt=System.currentTimeMillis();}invalidate();}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);long now=System.currentTimeMillis();float t=(now-phaseAt)/1000f;float cx=getWidth()*.5f,cy=getHeight()*.47f;
        float pulse=(float)(.5+.5*Math.sin(now/420.0));float strength=strength();
        if(phase==Phase.DISAPPEARING)strength=Math.max(0f,1f-t/0.48f);
        int veil=(int)(18*strength);p.setColor(Color.argb(veil,28,38,64));c.drawRect(0,0,getWidth(),getHeight(),p);
        drawAtmosphere(c,cx,cy,t,pulse,strength);
        drawFragments(c,cx,cy,t,pulse,strength);
        drawSigil(c,cx,cy,pulse,strength);
        if(phase!=Phase.DISAPPEARING&&phase!=Phase.DEGRADED)postInvalidateDelayed(45);
        else if(phase==Phase.DISAPPEARING&&strength>0)postInvalidateDelayed(32);
    }
    private float strength(){switch(phase){case DEGRADED:return .38f;case CONNECTING:return .55f;case WARNING:return 1f;default:return .88f;}}
    private void drawAtmosphere(Canvas c,float cx,float cy,float t,float pulse,float strength){
        float d=getResources().getDisplayMetrics().density;
        p.setStyle(Paint.Style.FILL);
        int halo=(int)(34*strength);p.setColor(Color.argb(halo,223,226,191));
        c.drawOval(cx-86*d-pulse*8*d,cy-48*d,cx+86*d+pulse*8*d,cy+48*d,p);
        p.setColor(Color.argb((int)(22*strength),171,210,219));
        c.drawOval(cx-145*d,cy-23*d,cx+145*d,cy+23*d,p);
        int strands=phase==Phase.WARNING?11:7;
        for(int i=0;i<strands;i++){
            float off=(i-(strands-1)/2f)*18*d;
            float sway=(float)Math.sin(t*(.7+i*.05)+i*.8)*6*d;
            float top=cy-(92+i%3*18)*d,bottom=cy+(72+(i+1)%3*22)*d;
            p.setColor(Color.argb((int)((26+(i%3)*13)*strength),205,224,214));
            c.drawRect(cx+off+sway-1.1f*d,top,cx+off+sway+1.1f*d,bottom,p);
        }
    }
    private void drawSigil(Canvas c,float cx,float cy,float pulse,float strength){
        float unit=Math.max(4f,getResources().getDisplayMetrics().density*3f);float r=unit*(8.5f+pulse*1.2f);
        int a=(int)(220*strength);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit);p.setColor(Color.argb(a,244,224,164));
        c.drawLine(cx,cy-r,cx+r,cy,p);c.drawLine(cx+r,cy,cx,cy+r,p);c.drawLine(cx,cy+r,cx-r,cy,p);c.drawLine(cx-r,cy,cx,cy-r,p);
        float inner=r*.48f;p.setStrokeWidth(unit*.7f);p.setColor(Color.argb((int)(190*strength),196,220,224));c.drawRect(cx-inner,cy-inner,cx+inner,cy+inner,p);
        p.setStyle(Paint.Style.FILL);p.setColor(Color.argb((int)(245*strength),255,239,190));float core=unit*(2.2f+(phase==Phase.SPEAKING?pulse*1.4f:pulse*.5f));c.drawRect(cx-core,cy-core,cx+core,cy+core,p);
        if(phase==Phase.WARNING){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit*.7f);p.setColor(Color.argb((int)(220*strength),255,202,155));c.drawRect(cx-r*1.45f,cy-r*1.45f,cx+r*1.45f,cy+r*1.45f,p);}
        if(phase==Phase.APPLYING){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(unit*.55f);p.setColor(Color.argb((int)(210*strength),213,235,204));c.drawRect(cx-r*1.65f,cy-r*.25f,cx+r*1.65f,cy+r*.25f,p);}
    }
    private void drawFragments(Canvas c,float cx,float cy,float t,float pulse,float strength){
        float d=getResources().getDisplayMetrics().density;int count=phase==Phase.DEGRADED?5:9;float speed=phase==Phase.THINKING?1.35f:phase==Phase.SPEAKING?1.1f:.72f;
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<count;i++){double ang=i*2.399+(t*speed);float radius=(34+i*7)*d+(phase==Phase.CONNECTING?(1-pulse)*14*d:0);float x=cx+(float)Math.cos(ang)*radius;float y=cy+(float)Math.sin(ang)*radius*.58f;float sz=(2+(i%3))*d;p.setColor(Color.argb((int)((105+(i%3)*35)*strength),232,221,177));c.drawRect(x-sz,y-sz,x+sz,y+sz,p);}
        if(phase==Phase.DEGRADED&&((int)(t*5)%2==0)){p.setColor(Color.argb(70,220,225,230));c.drawRect(cx-44*d,cy+25*d,cx-12*d,cy+28*d,p);}
    }
}
