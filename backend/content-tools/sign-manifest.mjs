// Signs a VNF runtime world-content manifest without putting the PRIVATE key in the APK/repository.
// Usage: VNF_CONTENT_PRIVATE_KEY_PEM="$(cat private.pem)" node sign-manifest.mjs payload.json manifest.json
import fs from "node:fs";
import crypto from "node:crypto";

const [, , input, output = "manifest.json"] = process.argv;
if (!input) throw new Error("Usage: node sign-manifest.mjs payload.json [manifest.json]");
const privateKey = process.env.VNF_CONTENT_PRIVATE_KEY_PEM;
if (!privateKey) throw new Error("VNF_CONTENT_PRIVATE_KEY_PEM is required");

const obj = JSON.parse(fs.readFileSync(input, "utf8"));
const payload = Buffer.from(JSON.stringify(obj), "utf8");
const signature = crypto.sign("RSA-SHA256", payload, privateKey);
const envelope = {
  payloadBase64: payload.toString("base64"),
  signatureBase64: signature.toString("base64"),
};
fs.writeFileSync(output, JSON.stringify(envelope));
console.log(`Signed ${obj.patchId || "patch"} -> ${output}`);
