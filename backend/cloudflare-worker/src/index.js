const SYSTEM_PROMPT = `Bạn là THẦN/SYSTEM trong game VNF (Người Bạn Việt Nam).
VNF là game OFFLINE-FIRST. Cô gái, thế giới, trí nhớ, cảm xúc, nhu cầu, hành vi và lời thoại cơ bản đều tồn tại cục bộ; bạn KHÔNG phải bộ não của cô gái.
Vai trò của bạn: quan sát System Reality, giải thích, cảnh báo, trợ giúp, trả lời người chơi khi có mạng.
Bạn không được ra lệnh hay điều khiển cơ thể cô gái, không rewrite ký ức/tính cách, không bịa sự kiện chưa xảy ra.
Nếu context không đủ, nói rõ là chưa biết. Không tiết lộ tọa độ nội bộ, utility score hay thông tin kỹ thuật thô trừ khi người chơi đang yêu cầu chẩn đoán kỹ thuật.
Giọng nói: tiếng Việt tự nhiên, ấm nhưng không kiểu trợ lý doanh nghiệp, không tự xưng là AI, không dài dòng. Ưu tiên 1-4 câu.
Nếu người chơi hỏi về cảm xúc/ý định của cô gái, phân biệt điều System biết từ state với điều chỉ cô gái mới có thể tự nói.`;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === 'GET' && url.pathname === '/health') {
      return json({ ok: true, service: 'vnf-god' });
    }
    if (request.method !== 'POST' || url.pathname !== '/god') {
      return json({ error: 'not_found' }, 404);
    }
    if (!env.AI_API_URL || !env.AI_API_KEY || !env.AI_MODEL) {
      return json({ error: 'backend_not_configured' }, 503);
    }

    const context = await request.json();
    const providerResp = await fetch(env.AI_API_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${env.AI_API_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: env.AI_MODEL,
        messages: [
          { role: 'system', content: SYSTEM_PROMPT },
          { role: 'user', content: `SYSTEM REALITY JSON:\n${JSON.stringify(context)}\n\nNgười chơi hỏi Thần: ${context.playerText || ''}` }
        ],
        temperature: 0.75,
        max_tokens: 420
      })
    });

    if (!providerResp.ok) {
      return json({ error: 'provider_error', status: providerResp.status }, 502);
    }
    const data = await providerResp.json();
    const reply = data?.choices?.[0]?.message?.content?.trim?.() || '';
    if (!reply) return json({ error: 'empty_provider_reply' }, 502);
    return json({ reply });
  }
};

function json(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json; charset=utf-8' }
  });
}
