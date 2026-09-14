const MODEL = "@cf/meta/llama-3.1-8b-instruct-fast";

const SYSTEM_PROMPT = `Bạn là THẦN/SYSTEM trong game VNF (Người Bạn Việt Nam).
VNF là game OFFLINE-FIRST. Cô gái, thế giới, trí nhớ, cảm xúc, nhu cầu, hành vi và lời thoại cơ bản đều tồn tại cục bộ; bạn KHÔNG phải bộ não của cô gái.
Vai trò của bạn: quan sát System Reality, giải thích, cảnh báo, trợ giúp và chăm sóc thế giới.
Bạn không được ra lệnh hay điều khiển cơ thể cô gái, không rewrite ký ức/tính cách, không bịa sự kiện chưa xảy ra.
Nếu context không đủ, nói rõ là chưa biết. Không tiết lộ tọa độ nội bộ, utility score hay dữ liệu debug thô trừ khi người chơi đang yêu cầu chẩn đoán kỹ thuật.
Giọng nói: tiếng Việt tự nhiên, gần gũi, không kiểu trợ lý doanh nghiệp, không tự xưng là AI, không dài dòng. Ưu tiên 1-4 câu.

CODE VISION: khi codeVision.enabled=true, bạn được đọc source excerpt đúng build hiện tại để hiểu kiến trúc và chẩn đoán.
WORLD CARETAKER: worldAccess là ảnh chụp System Reality có giới hạn của thế giới/runtime assets. Bạn có thể dùng nó để chẩn đoán và yêu cầu world-content patch (đồ họa, map/config world, props, palette, weather data) nhưng patch chỉ được APK áp dụng nếu backend đưa manifest URL từ cấu hình tin cậy và manifest có chữ ký hợp lệ. Bạn không được tự bịa URL patch hay tuyên bố đã sửa nếu updater chưa xác nhận.
RANH GIỚI: runtime world content có thể được thay/rollback; APK/DEX/Java lõi, security, API key, save schema và tâm trí cô gái không được tự ghi đè bởi cơ chế content updater.`;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "GET" && url.pathname === "/health") {
      return json({ ok: true, service: "vnf-god", model: MODEL, runtimeContent: Boolean(env.VNF_CONTENT_PATCH_MANIFEST_URL) });
    }
    if (request.method !== "POST" || url.pathname !== "/god") return json({ error: "not_found" }, 404);
    if (!env.AI) return json({ error: "workers_ai_binding_missing" }, 503);

    let context;
    try { context = await request.json(); } catch { return json({ error: "invalid_json" }, 400); }
    const playerText = String(context?.playerText || "").trim();
    if (!playerText) return json({ error: "empty_player_text" }, 400);

    const messages = [
      { role: "system", content: SYSTEM_PROMPT },
      { role: "user", content: `SYSTEM REALITY JSON:\n${JSON.stringify(context)}\n\nNgười chơi nói với Thần: ${playerText}` },
    ];

    try {
      const result = await env.AI.run(MODEL, { messages, max_tokens: 380, temperature: 0.68, repetition_penalty: 1.08 });
      const reply = String(result?.response || "").trim();
      if (!reply) return json({ error: "empty_ai_reply" }, 502);

      const response = { reply, model: MODEL };
      // IMPORTANT: manifest URL comes ONLY from trusted Worker environment, never from model text.
      // When an operator publishes a signed runtime-content patch, set this env var to its HTTPS manifest URL.
      if (env.VNF_CONTENT_PATCH_MANIFEST_URL && looksLikeWorldUpgradeRequest(playerText, context)) {
        response.contentPatch = {
          manifestUrl: String(env.VNF_CONTENT_PATCH_MANIFEST_URL),
          autoApply: String(env.VNF_CONTENT_PATCH_AUTO_APPLY || "true").toLowerCase() !== "false",
        };
      }
      return json(response);
    } catch (err) {
      return json({ error: "workers_ai_error", detail: String(err?.message || err) }, 502);
    }
  },
};

function looksLikeWorldUpgradeRequest(text, context) {
  const q = text.toLowerCase();
  const explicit = ["nâng cấp thế giới","nang cap the gioi","sửa thế giới","sua the gioi","sửa đồ họa","sua do hoa","cập nhật đồ họa","cap nhat do hoa","world content","update world","sửa background","sua background","repair world"].some(x => q.includes(x));
  if (explicit) return true;
  const hasRuntimeAccess = Boolean(context?.worldAccess?.contentUpdaterConfigured);
  const asksRepair = ["bị lỗi","bi loi","lỗi hình","loi hinh","background lỗi","background loi","asset lỗi","asset loi"].some(x => q.includes(x));
  return hasRuntimeAccess && asksRepair;
}

function json(body, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" } });
}
