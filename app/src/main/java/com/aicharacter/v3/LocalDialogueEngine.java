package com.aicharacter.v3;

import java.util.Locale;

/**
 * Offline language realization for the girl.
 * This layer never calls a network service. It only turns existing cognition/state
 * into concise Vietnamese speech so WORLD FIRST remains intact.
 */
public final class LocalDialogueEngine {
    private LocalDialogueEngine(){}

    public static String generic(WorldState s,String raw){
        String q=raw==null?"":raw.trim().toLowerCase(Locale.ROOT);
        double remembered=CognitionEngine.recalledValence(s,"player");
        double closeness=relationshipWarmth(s);
        double tension=relationshipTension(s);

        if(s.body!=null && s.body.pain>20)
            return pick(s,raw,"Mình nghe cậu, nhưng chỗ đau đang làm mình khó tập trung.","Ừ… mình nghe. Cho mình ngồi yên một chút đã, đau quá.");
        if(s.body!=null && s.body.energy<22)
            return pick(s,raw,"Mình vẫn nghe đây. Chỉ là hôm nay đầu mình chậm hơn bình thường một chút.","Ừ. Mình hơi đuối, nên có thể trả lời chậm một chút nhé.");
        if(tension>32)
            return pick(s,raw,"Mình nghe. Nhưng chuyện trước đó vẫn còn làm mình khó chịu.","Ừ, mình nghe thấy. Mình chưa muốn giả vờ như mọi thứ đã bình thường đâu.");

        if(containsAny(q,"buồn","sad","khóc","không vui")){
            if(s.emotion!=null && (s.emotion.sadness>.35||s.emotion.loneliness>.35))
                return pick(s,raw,"Có một chút. Không hẳn vì một chuyện duy nhất.","Ừ… chắc là có. Mình vẫn chưa gọi tên nó rõ lắm.");
            return pick(s,raw,"Không đến mức buồn. Chỉ là mình đang nghĩ hơi nhiều thôi.","Mình ổn. Hơi lặng một chút thôi, không phải buồn hẳn.");
        }
        if(containsAny(q,"vui","happy","vui không")){
            if(s.emotion!=null && s.emotion.joy>.35)
                return pick(s,raw,"Có. Hôm nay mình thấy nhẹ người hơn.","Ừ, khá vui. Không có lý do gì lớn, chỉ là thấy dễ chịu.");
            return pick(s,raw,"Bình thường thôi. Nhưng cũng không tệ.","Chưa hẳn vui lắm. Mình thấy yên hơn là vui.");
        }
        if(containsAny(q,"đang làm gì","làm gì","doing")){
            String activity=(s.haruActivity==null||s.haruActivity.isEmpty())?"ngồi nhìn quanh":s.haruActivity;
            return "Mình đang "+naturalizeActivity(activity)+".";
        }
        if(containsAny(q,"mưa","trời","weather","thời tiết")){
            String w=s.environment==null?"CLEAR":s.environment.weather;
            if("RAIN".equals(w)) return pick(s,raw,"Mưa làm mọi thứ nhỏ tiếng đi nhỉ.","Trời mưa. Mình đang để ý xem có chỗ trú nào gần hơn không.");
            if("CLOUDY".equals(w)) return "Trời âm hơn lúc nãy. Mình thấy dễ chịu hơn nắng gắt.";
            return "Trời khá quang. Nhìn xa được nên mình cứ muốn đứng ngắm thêm một lúc.";
        }
        if(containsAny(q,"sợ","lo không","lo lắng","afraid")){
            if(s.emotion!=null && (s.emotion.fear>.28 || (s.personality!=null && s.personality.caution>.68)))
                return pick(s,raw,"Có hơi lo. Mình muốn nhìn kỹ tình hình trước đã.","Một chút. Mình chưa chắc chuyện này an toàn đến đâu.");
            return pick(s,raw,"Chưa đến mức sợ. Mình chỉ đang cẩn thận thôi.","Không hẳn. Mình muốn biết thêm trước khi quyết định.");
        }
        if(containsAny(q,"nhớ mình","nhớ cậu","có nhớ","miss me")){
            if(closeness>60 || remembered>.30)
                return pick(s,raw,"Có chứ. Lúc không thấy cậu, mình vẫn có lúc để ý xem cậu đang ở đâu.","Ừ… có. Nhưng mình không định nói câu đó trước đâu.");
            if(closeness>35) return pick(s,raw,"Có nghĩ tới một chút.","Thỉnh thoảng. Nhất là lúc chỗ này yên quá.");
            return pick(s,raw,"Mình có để ý khi cậu không ở đây, nhưng mình chưa biết có nên gọi đó là nhớ không.","Chắc là có một chút, nhưng mình chưa quen với chuyện đó.");
        }

        String intention=s.currentIntention==null?"":s.currentIntention;
        if("find_cat".equals(intention))
            return pick(s,raw,"Ừ, mình nghe. Nhưng lúc nãy mình còn đang tìm cậu đấy.","Nghe thấy rồi. Cậu làm mình mất công tìm quanh đây một lúc đấy nhé.");
        if("observe_lake".equals(intention))
            return pick(s,raw,"Ừ. Mình đang nhìn mặt hồ nên hơi lơ đãng một chút.","Mình nghe. Gió ngoài hồ làm đầu mình cứ chạy sang chuyện khác một chút thôi.");
        if("rest".equals(intention) || "seek_shelter".equals(intention))
            return pick(s,raw,"Mình nghe đây. Nhưng mình muốn nghỉ thêm một chút trước.","Ừ. Nói với mình được, chỉ là mình chưa muốn đứng dậy lúc này.");

        if(s.thoughts!=null && !s.thoughts.isEmpty()){
            ThoughtState t=s.thoughts.get(s.thoughts.size()-1);
            if(t!=null && t.uncertainty>.62)
                return pick(s,raw,"Mình chưa chắc mình hiểu đúng ý cậu.","Khoan, ý cậu là thế nào? Mình không muốn tự đoán rồi hiểu sai.");
        }

        if(s.emotion!=null && s.emotion.curiosity>.52)
            return pick(s,raw,"Hửm? Cái đó làm mình để ý đấy.","Khoan, nghe thú vị đấy. Cậu nói rõ hơn một chút được không?");
        if(remembered>.35 && closeness>45)
            return pick(s,raw,"Ừ, mình nghe đây.","Mình nghe. Cứ nói đi, mình đang để ý.","Hửm? Rồi sao nữa?");
        if(s.emotion!=null && s.emotion.calm>.6)
            return pick(s,raw,"Ừ, mình nghe đây.","Mình nghe. Cậu cứ nói tiếp đi.");
        if(s.personality!=null && s.personality.independence>.68)
            return pick(s,raw,"Mình nghe, nhưng mình sẽ tự nghĩ về chuyện đó nhé.","Ừ. Mình nghe ý cậu, còn mình quyết định thế nào thì để mình nghĩ đã.");
        return pick(s,raw,"Ừ.","Mình nghe thấy rồi.","Hửm? Nói tiếp đi.");
    }

    public static String walkAccepted(WorldState s,String raw){
        if(s.environment!=null && "RAIN".equals(s.environment.weather))
            return pick(s,raw,"Đi cũng được. Nhưng mình muốn đi chỗ có mái trú gần gần thôi.","Ừ, nhưng đừng đi xa lúc đang mưa nhé.");
        if(s.body!=null && s.body.energy<45)
            return pick(s,raw,"Được. Đi chậm thôi nhé, mình hơi mệt.","Ừ. Một đoạn ngắn thôi, chân mình không muốn đi nhanh đâu.");
        if(s.personality!=null && s.personality.curiosity>.65)
            return pick(s,raw,"Ừ, đi nhé. Mình muốn xem phía bên kia có gì khác hôm qua không.","Được. Mình cũng đang muốn đổi chỗ một chút.");
        return pick(s,raw,"Ừ, đi một đoạn nhé. Mình muốn ghé gần mép hồ.","Được. Mình cũng đang muốn đổi chỗ một chút.");
    }

    public static String walkDeclined(WorldState s,String raw){
        if(s.body!=null && s.body.pain>12)
            return pick(s,raw,"Không đi đâu. Chân mình đang đau.","Để lúc khác nhé. Bây giờ mình không muốn cố đi khi đang đau.");
        if(s.relationship!=null && s.relationship.irritation+s.relationship.hurt>s.relationship.comfort)
            return pick(s,raw,"Không. Mình muốn ở một mình một lúc.","Lúc này thì không. Mình vẫn cần một chút khoảng riêng.");
        if(s.personality!=null && s.personality.independence>.65)
            return pick(s,raw,"Không phải bây giờ. Mình đang muốn làm việc của mình trước.","Để lát nữa xem sao. Giờ mình chưa muốn đổi kế hoạch.");
        return pick(s,raw,"Không phải bây giờ. Mình muốn ở đây thêm một lát.","Để lát nữa xem sao. Giờ mình chưa muốn đi.");
    }

    public static String health(WorldState s,String raw){
        if(s.body.pain>20) return pick(s,raw,"Đau khá rõ. Mình không muốn cố tỏ ra ổn.","Mình đau. Chắc mình nên nghỉ và để ý xem có nặng hơn không.");
        if(s.body.pain>10) return pick(s,raw,"Mình vẫn đau một chút. Nó làm mình mất tập trung.","Có hơi đau, nhưng mình vẫn chịu được. Mình sẽ không cố quá.");
        if(s.body.energy<25) return pick(s,raw,"Mệt. Cơ thể đang bảo mình nên nghỉ.","Hơi kiệt sức. Mình muốn ngồi xuống hơn là đi đâu lúc này.");
        if(s.emotion!=null && s.emotion.sadness>.45) return pick(s,raw,"Cơ thể thì ổn. Tâm trạng thì chưa hẳn.","Mình không đau, chỉ hơi nặng lòng một chút.");
        return pick(s,raw,"Mình ổn. Hôm nay đầu óc khá tỉnh.","Ổn. Không có gì làm mình khó chịu rõ rệt lúc này.");
    }

    private static double relationshipWarmth(WorldState s){
        if(s.relationship==null)return 0;
        return s.relationship.affection*.28+s.relationship.trust*.30+s.relationship.comfort*.22+s.relationship.attachment*.12+s.relationship.gratitude*.08;
    }
    private static double relationshipTension(WorldState s){
        if(s.relationship==null)return 0;
        return s.relationship.irritation+s.relationship.hurt;
    }
    private static boolean containsAny(String q,String...xs){ for(String x:xs)if(q.contains(x))return true; return false; }
    private static String pick(WorldState s,String seed,String...choices){
        long minute=s==null?0:(long)s.worldMinutes;
        int h=seed==null?0:seed.hashCode();
        h=31*h+(s==null||s.currentIntention==null?0:s.currentIntention.hashCode());
        h=31*h+(int)(minute/8); // phrasing can evolve with the world, not pure random noise.
        if(h==Integer.MIN_VALUE)h=0;
        return choices[Math.abs(h)%choices.length];
    }
    private static String naturalizeActivity(String a){
        String x=a.replace('_',' ').trim().toLowerCase(Locale.ROOT);
        if(x.contains("walking with the cat")) return "đi cùng cậu";
        if(x.contains("watching the lake")) return "nhìn mặt hồ";
        if(x.contains("walking")) return "đi quanh đây";
        if(x.contains("rest")) return "nghỉ một chút";
        if(x.contains("search")) return "tìm quanh đây";
        if(x.contains("shelter")) return "ở gần chỗ trú";
        return x;
    }
}
