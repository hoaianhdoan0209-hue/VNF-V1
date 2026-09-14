const MODEL = "@cf/meta/llama-3.1-8b-instruct-fast";

const SYSTEM_PROMPT = `Bạn là THẦN/SYSTEM trong game VNF (Người Bạn Việt Nam).
VNF là game OFFLINE-FIRST. Cô gái, thế giới, trí nhớ, cảm xúc, nhu cầu, hành vi và lời thoại cơ bản đều tồn tại cục bộ; bạn KHÔNG phải bộ não của cô gái.
Vai trò của bạn: quan sát System Reality, giải thích, cảnh báo, trợ giúp và trả lời người chơi khi có mạng.
Bạn không được ra lệnh hay điều khiển cơ thể cô gái, không rewrite ký ức/tính cách, không bịa sự kiện chưa xảy ra.
Nếu context không đủ, nói rõ là chưa biết. Không tiết lộ tọa độ nội bộ, utility score hay dữ liệu debug thô trừ khi người chơi đang yêu cầu chẩn đoán kỹ thuật.
Giọng nói: tiếng Việt tự nhiên, gần gũi, không kiểu trợ lý doanh nghiệp, không tự xưng là AI, không dài dòng. Ưu tiên 1-4 câu.
Nếu người chơi hỏi về cảm xúc/ý định của cô gái, phân biệt điều SYSTEM biết từ state với điều chỉ cô ấy mới có thể tự nói.`;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (request.method === "GET" && url.pathname === "/health") {
      return json({ ok: true, service: "vnf-god", model: MODEL });
    }

    if (request.method !== "POST" || url.pathname !== "/god") {
      return json({ error: "not_found" }, 404);
    }

    if (!env.AI) {
      return json({ error: "workers_ai_binding_missing" }, 503);
    }

    let context;
    try {
      context = await request.json();
    } catch {
      return json({ error: "invalid_json" }, 400);
    }

    const playerText = String(context?.playerText || "").trim();
    if (!playerText) return json({ error: "empty_player_text" }, 400);

    const messages = [
      { role: "system", content: SYSTEM_PROMPT },
      {
        role: "user",
        content:
          `SYSTEM REALITY JSON:\n${JSON.stringify(context)}\n\n` +
          `Người chơi nói với Thần: ${playerText}`,
      },
    ];

    try {
      const result = await env.AI.run(MODEL, {
        messages,
        max_tokens: 360,
        temperature: 0.72,
        repetition_penalty: 1.08,
      });

      const reply = String(result?.response || "").trim();
      if (!reply) return json({ error: "empty_ai_reply" }, 502);
      return json({ reply, model: MODEL });
    } catch (err) {
      return json({ error: "workers_ai_error", detail: String(err?.message || err) }, 502);
    }
  },
};

function json(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "cache-control": "no-store",
    },
  });
}
