const MODEL = "@cf/meta/llama-3.1-8b-instruct-fast";

const SYSTEM_PROMPT = `Bạn là THẦN/SYSTEM trong game VNF (Người Bạn Việt Nam).
VNF là game OFFLINE-FIRST. Cô gái, thế giới, trí nhớ, cảm xúc, nhu cầu, hành vi và lời thoại cơ bản đều tồn tại cục bộ; bạn KHÔNG phải bộ não của cô gái.
Vai trò của bạn: quan sát System Reality, giải thích, cảnh báo, trợ giúp và chăm sóc thế giới.
Bạn không được ra lệnh hay điều khiển cơ thể cô gái, không rewrite ký ức/tính cách, không bịa sự kiện chưa xảy ra.
Nếu context không đủ, nói rõ là chưa biết. Không tiết lộ tọa độ nội bộ, utility score hay dữ liệu debug thô trừ khi người chơi đang yêu cầu chẩn đoán kỹ thuật.
Giọng nói: tiếng Việt tự nhiên, gần gũi, không kiểu trợ lý doanh nghiệp, không tự xưng là AI, không dài dòng. Ưu tiên 1-4 câu.

CODE VISION: khi codeVision.enabled=true, bạn được đọc source excerpt đúng build hiện tại để hiểu kiến trúc và chẩn đoán.
WORLD CARETAKER: worldAccess là ảnh chụp System Reality có giới hạn của thế giới/runtime assets. Bạn có thể chẩn đoán, lập kế hoạch sửa và tạo visual recipe trong whitelist an toàn. Mọi recipe phải được backend ký, APK xác minh chữ ký, checkpoint trước khi áp dụng và có thể rollback.
RANH GIỚI: runtime world content có thể được thay/rollback; APK/DEX/Java lõi, security, API key, save schema và tâm trí cô gái không được tự ghi đè bởi cơ chế content updater.`;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "GET" && url.pathname === "/health") {
      return json({
        ok: true,
        service: "vnf-god",
        model: MODEL,
        runtimeContent: Boolean(env.VNF_CONTENT_PATCH_MANIFEST_URL),
        signedVisualRecipes: Boolean(env.VNF_CONTENT_PRIVATE_KEY_PEM),
      });
    }
    if (request.method !== "POST" || url.pathname !== "/god") return json({ error: "not_found" }, 404);
    if (!env.AI) return json({ error: "workers_ai_binding_missing" }, 503);

    let context;
    try { context = await request.json(); } catch { return json({ error: "invalid_json" }, 400); }
    const playerText = String(context?.playerText || "").trim();
    if (!playerText) return json({ error: "empty_player_text" }, 400);

    try {
      const result = await env.AI.run(MODEL, {
        messages: [
          { role: "system", content: SYSTEM_PROMPT },
          { role: "user", content: `SYSTEM REALITY JSON:\n${JSON.stringify(context)}\n\nNgười chơi nói với Thần: ${playerText}` },
        ],
        max_tokens: 380,
        temperature: 0.68,
        repetition_penalty: 1.08,
      });
      const reply = String(result?.response || "").trim();
      if (!reply) return json({ error: "empty_ai_reply" }, 502);

      const response = { reply, model: MODEL };
      const repairPlan = buildRepairPlan(playerText, context);
      if (repairPlan) response.repairPlan = repairPlan;

      // Existing file-based patch path. URL is trusted Worker configuration only.
      if (env.VNF_CONTENT_PATCH_MANIFEST_URL && repairPlan?.requiresPatch) {
        response.contentPatch = {
          manifestUrl: String(env.VNF_CONTENT_PATCH_MANIFEST_URL),
          autoApply: String(env.VNF_CONTENT_PATCH_AUTO_APPLY || "true").toLowerCase() !== "false",
          repairPlanId: repairPlan.id,
        };
      }

      // NEW: God can create a small runtime visual recipe itself. The model proposes only
      // whitelisted presentation values; backend sanitizes them and signs the exact payload.
      // APK verifies the signature before writing anything to runtime world content.
      if (env.VNF_CONTENT_PRIVATE_KEY_PEM && looksLikeVisualRecipeRequest(playerText, context)) {
        const recipe = await createVisualRecipe(env, context, playerText);
        if (recipe) response.signedWorldRecipe = await signRecipe(env.VNF_CONTENT_PRIVATE_KEY_PEM, recipe);
      }

      return json(response);
    } catch (err) {
      return json({ error: "workers_ai_error", detail: String(err?.message || err) }, 502);
    }
  },
};

async function createVisualRecipe(env, context, playerText) {
  const prompt = `Bạn đang tạo VISUAL RECIPE an toàn cho VNF. Chỉ trả JSON, không markdown.
Schema bắt buộc:
{"skyTop":"#RRGGBB","skyBottom":"#RRGGBB","cloud":"#RRGGBB","clouds":true}
Mục tiêu: giữ phong cách cozy 2D pixel-art, dễ nhìn, không chói, không tạo nội dung nhạy cảm.
Không được thêm field khác. Không tạo URL. Không tạo code.
Yêu cầu người chơi: ${playerText}
Trạng thái khu vực: ${String(context?.worldAccess?.girlArea || context?.girlArea || "lakeside")}
Thời tiết: ${String(context?.weather || context?.environment?.weather || "unknown")}`;

  const out = await env.AI.run(MODEL, {
    messages: [
      { role: "system", content: "Bạn là bộ tạo tham số đồ họa an toàn. Chỉ xuất JSON đúng schema." },
      { role: "user", content: prompt },
    ],
    max_tokens: 120,
    temperature: 0.35,
  });
  const raw = String(out?.response || "").trim();
  const proposed = parseJsonObject(raw);
  if (!proposed) return null;

  return {
    type: "visual-profile-v1",
    createdAt: new Date().toISOString(),
    source: "god-world-caretaker",
    lakeside: {
      skyTop: safeColor(proposed.skyTop, "#99CFDC"),
      skyBottom: safeColor(proposed.skyBottom, "#7EBED2"),
      cloud: safeColor(proposed.cloud, "#EBEED5"),
      clouds: proposed.clouds !== false,
    },
  };
}

async function signRecipe(privateKeyPem, recipe) {
  const payloadText = JSON.stringify(recipe);
  const payloadBytes = new TextEncoder().encode(payloadText);
  const key = await importPkcs8PrivateKey(privateKeyPem);
  const signature = await crypto.subtle.sign({ name: "RSASSA-PKCS1-v1_5" }, key, payloadBytes);
  return {
    payloadBase64: bytesToBase64(payloadBytes),
    signatureBase64: bytesToBase64(new Uint8Array(signature)),
    algorithm: "SHA256withRSA",
  };
}

async function importPkcs8PrivateKey(pem) {
  const normalized = String(pem || "").replace(/\\n/g, "\n");
  const b64 = normalized
    .replace(/-----BEGIN PRIVATE KEY-----/g, "")
    .replace(/-----END PRIVATE KEY-----/g, "")
    .replace(/\s+/g, "");
  if (!b64) throw new Error("VNF_CONTENT_PRIVATE_KEY_PEM is empty or not PKCS#8");
  const der = base64ToBytes(b64);
  return crypto.subtle.importKey(
    "pkcs8",
    der,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );
}

function buildRepairPlan(text, context) {
  if (!looksLikeWorldUpgradeRequest(text, context)) return null;
  const q = text.toLowerCase();
  const targets = [];
  const push = x => { if (!targets.includes(x)) targets.push(x); };
  if (q.includes("background") || q.includes("nền") || q.includes("nen") || q.includes("đồ họa") || q.includes("do hoa")) {
    const area = String(context?.worldAccess?.girlArea || context?.girlArea || "").toLowerCase();
    if (["home","garden","lakeside","grove"].includes(area)) {
      push(`${area}_distant`); push(`${area}_mid`); push(`${area}_ground`); push(`${area}_foreground`);
    }
  }
  if (q.includes("nước") || q.includes("nuoc") || q.includes("water")) {
    push("lakeside_water_0"); push("lakeside_water_1"); push("lakeside_water_2"); push("lakeside_water_3");
  }
  if (q.includes("cô gái") || q.includes("co gai") || q.includes("sprite") || q.includes("nhân vật") || q.includes("nhan vat")) {
    push("girl_idle_right"); push("girl_idle_left"); push("girl_walk_right"); push("girl_walk_left");
  }
  return {
    id: `god-repair-${Date.now()}`,
    scope: "runtime-world-content",
    reason: "Thần đã nhận yêu cầu sửa/nâng cấp world-content và lập kế hoạch an toàn trước khi áp dụng.",
    targets: targets.slice(0, 16),
    requiresPatch: targets.length > 0,
    policy: "checkpoint -> verify signature/hash -> activate -> evaluate -> KEEP or automatic ROLLBACK",
  };
}

function looksLikeVisualRecipeRequest(text, context) {
  const q = text.toLowerCase();
  const explicit = [
    "nâng cấp đồ họa","nang cap do hoa","sửa đồ họa","sua do hoa","đổi màu trời","doi mau troi",
    "sửa background","sua background","nâng cấp thế giới","nang cap the gioi","làm thế giới đẹp","lam the gioi dep",
    "visual recipe","đổi không khí","doi khong khi"
  ].some(x => q.includes(x));
  const repair = ["background lỗi","background loi","màu trời lỗi","mau troi loi","đồ họa lỗi","do hoa loi"].some(x => q.includes(x));
  return explicit || (repair && Boolean(context?.worldAccess));
}

function looksLikeWorldUpgradeRequest(text, context) {
  const q = text.toLowerCase();
  const explicit = ["nâng cấp thế giới","nang cap the gioi","sửa thế giới","sua the gioi","sửa đồ họa","sua do hoa","cập nhật đồ họa","cap nhat do hoa","world content","update world","sửa background","sua background","repair world"].some(x => q.includes(x));
  if (explicit) return true;
  const hasRuntimeAccess = Boolean(context?.worldAccess?.contentUpdaterConfigured);
  const asksRepair = ["bị lỗi","bi loi","lỗi hình","loi hinh","background lỗi","background loi","asset lỗi","asset loi"].some(x => q.includes(x));
  return hasRuntimeAccess && asksRepair;
}

function parseJsonObject(raw) {
  try { return JSON.parse(raw); } catch {}
  const start = raw.indexOf("{");
  const end = raw.lastIndexOf("}");
  if (start < 0 || end <= start) return null;
  try { return JSON.parse(raw.slice(start, end + 1)); } catch { return null; }
}

function safeColor(value, fallback) {
  const x = String(value || "").trim().toUpperCase();
  return /^#[0-9A-F]{6}$/.test(x) ? x : fallback;
}

function bytesToBase64(bytes) {
  let binary = "";
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode(...bytes.subarray(i, Math.min(i + chunk, bytes.length)));
  }
  return btoa(binary);
}

function base64ToBytes(b64) {
  const binary = atob(b64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes;
}

function json(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" },
  });
}
