package com.aicharacter.v3;

import java.util.Locale;

public final class LocalDialogueEngine {
    private LocalDialogueEngine(){}

    public static String generic(WorldState s,String raw){
        String q=raw==null?"":raw.trim().toLowerCase(Locale.ROOT);
        if(s.body!=null && s.body.pain>16) return pick(raw,"Mình nghe cậu. Nhưng đầu mình cứ quay lại chuyện chỗ đau này.","Ừ… mình đang nghe. Cho mình ngồi yên một chút đã.");
        if(s.body!=null && s.body.energy<24) return pick(raw,"Mình nghe rồi. Hôm nay mình chậm hơn bình thường một chút.","Ừ. Mình hơi đuối, nhưng vẫn nghe cậu nói.");
        if(s.relationship!=null && s.relationship.hurt+s.relationship.irritation>30)
            return pick(raw,"Mình nghe. Nhưng chuyện trước đó vẫn còn làm mình khó chịu.","Ừ, mình nghe thấy. Mình chưa muốn giả vờ như mọi thứ bình thường ngay đâu.");

        if(containsAny(q,"buồn","sad","khóc")){
            if(s.emotion!=null && (s.emotion.sadness>.35||s.emotion.loneliness>.35))
                return pick(raw,"Có một chút. Không hẳn vì một chuyện duy nhất.","Ừ… chắc là có. Mình chưa gọi tên được nó rõ lắm.");
            return pick(raw,"Không đến mức buồn. Chỉ là mình đang nghĩ hơi nhiều thôi.","Mình ổn. Nếu có gì đổi, chắc cậu sẽ nhận ra trước khi mình chịu nói đấy.");
        }
        if(containsAny(q,"vui","happy","vui không")){
            if(s.emotion!=null && s.emotion.joy>.35) return pick(raw,"Có. Hôm nay mình thấy nhẹ người hơn.","Ừ, khá vui. Không có lý do gì lớn lắm, chỉ là thấy dễ chịu.");
            return pick(raw,"Bình thường thôi. Nhưng cũng không tệ.","Chưa hẳn vui lắm. Mình thấy yên hơn là vui.");
        }
        if(containsAny(q,"đang làm gì","làm gì","doing")){
            String activity=(s.haruActivity==null||s.haruActivity.isEmpty())?"ngồi nhìn quanh":s.haruActivity;
            return "Mình đang "+naturalizeActivity(activity)+".";
        }
        if(containsAny(q,"mưa","trời","weather")){
            String w=s.environment==null?"CLEAR":s.environment.weather;
            if("RAIN".equals(w)) return pick(raw,"Mưa làm mọi thứ nhỏ tiếng đi nhỉ.","Trời mưa. Mình đang nghĩ xem có nên ở gần chỗ trú hơn không.");
            if("CLOUDY".equals(w)) return "Trời âm hơn lúc nãy. Mình thấy dễ chịu hơn nắng gắt.";
            return "Trời đang khá quang. Mình nhìn được xa hơn bình thường.";
        }

        // Use current cognition instead of generic assistant-like filler.
        String intention=s.currentIntention==null?"":s.currentIntention;
        if("find_cat".equals(intention)) return pick(raw,"Ừ, mình nghe. Nhưng mình vẫn đang để ý xem cậu đã chạy đi đâu trước đó.","Nghe thấy rồi. Lúc nãy mình còn đang tìm cậu đấy.");
        if("observe_lake".equals(intention)) return pick(raw,"Ừ. Mình đang nhìn mặt hồ nên hơi lơ đãng một chút.","Mình nghe. Gió ngoài hồ hôm nay làm mình chú ý hơn câu chuyện một chút thôi.");
        if(s.emotion!=null && s.emotion.curiosity>.5) return pick(raw,"Ừ? Câu đó làm mình để ý đấy.","Khoan, cái đó nghe thú vị. Cậu nói rõ hơn một chút được không?");
        if(s.emotion!=null && s.emotion.calm>.6) return pick(raw,"Ừ, mình nghe đây.","Mình nghe. Cậu cứ nói tiếp đi.");
        return pick(raw,"Ừ.","Mình nghe thấy rồi.","Hửm? Nói tiếp đi.");
    }

    public static String walkAccepted(WorldState s,String raw){
        if(s.environment!=null && "RAIN".equals(s.environment.weather)) return pick(raw,"Đi cũng được. Nhưng mình muốn đi chỗ có mái trú gần gần thôi.","Ừ, nhưng đừng kéo mình ra xa lúc đang mưa nhé.");
        if(s.body!=null && s.body.energy<45) return pick(raw,"Được. Đi chậm thôi nhé, mình hơi mệt.","Ừ. Một đoạn ngắn thôi, chân mình không muốn đi nhanh đâu.");
        return pick(raw,"Ừ, đi một đoạn nhé. Mình muốn ghé gần mép hồ.","Được. Mình cũng đang muốn đổi chỗ một chút.");
    }

    public static String walkDeclined(WorldState s,String raw){
        if(s.body!=null && s.body.pain>12) return pick(raw,"Không đi đâu. Chân mình đang đau.","Để lúc khác nhé. Bây giờ mình không muốn cố đi khi đang đau.");
        if(s.relationship!=null && s.relationship.irritation+s.relationship.hurt>s.relationship.comfort)
            return pick(raw,"Không. Mình muốn ở một mình một lúc.","Lúc này thì không. Mình vẫn cần một chút khoảng riêng.");
        return pick(raw,"Không phải bây giờ. Mình muốn ở đây thêm một lát.","Để lát nữa xem sao. Giờ mình chưa muốn đi.");
    }

    private static boolean containsAny(String q,String...xs){ for(String x:xs)if(q.contains(x))return true; return false; }
    private static String pick(String seed,String...choices){
        int h=seed==null?0:seed.hashCode(); if(h==Integer.MIN_VALUE)h=0; return choices[Math.abs(h)%choices.length];
    }
    private static String naturalizeActivity(String a){
        String x=a.replace('_',' ').trim().toLowerCase(Locale.ROOT);
        if(x.contains("walking with the cat")) return "đi cùng cậu";
        if(x.contains("watching the lake")) return "nhìn mặt hồ";
        if(x.contains("walking")) return "đi quanh đây";
        if(x.contains("rest")) return "nghỉ một chút";
        if(x.contains("search")) return "tìm quanh đây";
        return x;
    }
}
