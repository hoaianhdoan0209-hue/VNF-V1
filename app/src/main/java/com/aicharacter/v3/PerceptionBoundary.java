package com.aicharacter.v3;

import java.util.Locale;
/** One-way boundary from technical System Reality into Haru-facing concepts. */
public final class PerceptionBoundary {
    private PerceptionBoundary(){}
    public static String systemReality(WorldState s){return String.format(Locale.US,"catX=%.1f; haruX=%.1f; saveVersion=%d; worldDefinition=%d; weather=%s; light=%.2f",s.catX,s.haruX,WorldState.SAVE_VERSION,s.world.definitionVersion,s.environment.weather,s.environment.ambientBrightness);}
    public static PerceivedObject perceive(WorldState s,WorldObject o){
        if(!HaruVisionEngine.canSee(s,o))return new PerceivedObject("unknown","không nhìn thấy rõ vật đó","ngoài nhận thức hiện tại",false);
        boolean familiar=false;for(MemoryEntry m:s.memories)if(o.id.equals(m.location)||m.tags.contains(o.id)||m.participants.contains(o.id)){familiar=true;break;}
        String rel=o.x<s.haruX-70?"ở phía bên trái":o.x>s.haruX+70?"ở phía bên phải":"ở ngay gần đây";
        return new PerceivedObject(o.id,o.haruDescription,rel,familiar);
    }
    public static String haruPerception(WorldState s){int h=(int)(s.worldMinutes/60);String time=h<6?"đêm":h<11?"buổi sáng":h<17?"ban ngày":h<20?"chiều tối":"đêm";String weather="CLEAR".equals(s.environment.weather)?"trời quang":"CLOUDY".equals(s.environment.weather)?"trời nhiều mây":"trời đang mưa";WorldArea a=s.world.areaAt(s.haruX);return "Bây giờ là "+time+", "+weather+". Cô ấy đang ở "+(a==null?"một nơi khó xác định":a.haruName)+" và "+s.haruActivity+".";}
}
