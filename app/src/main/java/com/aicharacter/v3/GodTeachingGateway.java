package com.aicharacter.v3;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Divine teaching boundary.
 *
 * God can offer a lesson, but cannot install knowledge, memory, personality,
 * emotion, relationship, intention, skill or repertoire directly into Haru.
 * The durable offer is later evaluated by HaruTeachingOpportunityEngine.
 */
public final class GodTeachingGateway {
    private static final Set<String> SAFE_ACTIONS=new LinkedHashSet<>(Arrays.asList(
            "observe_carefully","read","sketch","tend_garden","prepare_simple_food","clean_space","care_for_creature"));

    private GodTeachingGateway(){}

    public static boolean looksLikeTeaching(String raw){
        String q=norm(raw);
        return q.startsWith("day ")||q.startsWith("day:")||q.contains("day co ay")||
                q.startsWith("teach ")||q.contains("cap nhat kien thuc")||q.contains("hoc hanh dong");
    }

    public static String teach(WorldState s,String raw,long now){
        if(s==null)return "Không có world state để tạo lời dạy.";
        String normalized=norm(raw);
        String payload=raw==null?"":raw.trim();
        int colon=payload.indexOf(':');
        if(colon>=0&&colon+1<payload.length())payload=payload.substring(colon+1).trim();
        if(payload.isEmpty())return "Hãy nói rõ điều muốn Thần dạy. Ví dụ: DẠY CÔ ẤY: kiến thức về mưa.";

        boolean action=normalized.contains("hanh dong")||normalized.contains("ky nang")||normalized.contains("skill");
        if(action)return offerAction(s,payload,now);
        return offerKnowledge(s,payload,now);
    }

    private static String offerKnowledge(WorldState s,String topic,long now){
        String key=key(topic);
        if(key.isEmpty())return "Nội dung dạy chưa đủ rõ.";
        publishOffer(s,"knowledge",key,now);
        return "Thần đã đưa ra một lời dạy về “"+topic.trim()+"”. Haru chưa bị thay đổi: cô ấy sẽ tự quyết định có lắng nghe hay không trong nhịp sống tiếp theo.";
    }

    private static String offerAction(WorldState s,String text,long now){
        String normalizedKey=key(text),chosen="";
        for(String action:SAFE_ACTIONS){
            if(normalizedKey.contains(action)||normalizedKey.contains(action.replace('_',' '))){chosen=action;break;}
        }
        if(chosen.isEmpty())
            return "Thần không thể cài hành động tùy ý vào Haru. Kỹ năng phải thuộc repertoire an toàn và chỉ tiến bộ sau trải nghiệm/thực hành thật.";
        publishOffer(s,"action",chosen,now);
        return "Thần đã giải thích “"+chosen+"” như một cơ hội học. Haru có thể nghe hoặc bỏ qua; skill và repertoire chưa thay đổi cho đến khi cô ấy tự thực hành và có kết quả thật.";
    }

    private static void publishOffer(WorldState s,String mode,String key,long now){
        long at=Math.max(now,Math.max(s.lastOpenedAt,s.lastSimulatedAt));
        String seed=mode+":"+key+":"+at;
        String id="god_teaching_offer_"+Long.toHexString(at)+"_"+Integer.toHexString(seed.hashCode());
        // History records only the real interaction: an offer was made. It does
        // not assert the lesson content as VNF world truth.
        WorldEventBus.publishId(s,at,id,HaruTeachingOpportunityEngine.OFFER_TYPE,
                "god_teaching","mode="+mode+" key="+key+" status=OFFERED");
    }

    private static String key(String x){
        String n=norm(x).replaceAll("[^a-z0-9]+","_").replaceAll("^_+|_+$","");
        return n.length()>48?n.substring(0,48):n;
    }

    private static String norm(String x){
        String n=Normalizer.normalize(x==null?"":x,Normalizer.Form.NFD)
                .replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);
        return n.replace('đ','d').trim();
    }
}
