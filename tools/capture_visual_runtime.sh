#!/usr/bin/env bash
set -euo pipefail

APK="app/build/outputs/apk/debug/app-debug.apk"
OUT="app/build/visual-runtime"
PKG="com.aicharacter.v3"
ACTIVITY="$PKG/.MainActivity"

adb install -r "$APK"
mkdir -p "$OUT/biomes" "$OUT/haru-poses"

# The emulator's first immersive-mode education overlay is not app content.
# Confirm immersive mode up front and hide host-SystemUI error dialogs so screenshots
# contain only the VNF runtime. These settings affect only the disposable CI emulator.
adb shell settings put secure immersive_mode_confirmations confirmed || true
adb shell settings put global hide_error_dialogs 1 || true
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
sleep 5

# Start from one clean VNF state, then reuse it. Repeated pm clear calls can make
# SystemUI unstable on the software-rendered API 35 emulator.
adb shell pm clear "$PKG" >/dev/null

wait_for_vnf_focus() {
  local focus=""
  for _ in $(seq 1 30); do
    focus="$(adb shell dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 8 || true)"
    if [[ "$focus" == *"$PKG"* ]]; then
      return 0
    fi
    sleep 1
  done
  echo "VNF never became the active runtime app. Window state:" >&2
  printf '%s\n' "$focus" >&2
  return 1
}

capture() {
  local name="$1" biome="$2" pose="$3" target="$4"
  adb shell am force-stop "$PKG" || true
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
  timeout 25s adb shell am start -W -S -n "$ACTIVITY" \
    --es vnf_debug_biome "$biome" \
    --es vnf_debug_pose "$pose" >/dev/null
  wait_for_vnf_focus
  sleep 5
  wait_for_vnf_focus
  adb exec-out screencap -p > "$target/$name.png"
  test -s "$target/$name.png"
  test "$(stat -c%s "$target/$name.png")" -gt 10000
}

capture home home idle_right "$OUT/biomes"
capture garden garden idle_right "$OUT/biomes"
capture lakeside lakeside idle_right "$OUT/biomes"
capture grove grove idle_right "$OUT/biomes"

for pose in idle_right idle_left walk_right walk_left sit crouch sleep think reaction search_right search_left; do
  capture "$pose" home "$pose" "$OUT/haru-poses"
done

python - <<'PY'
from pathlib import Path
from hashlib import sha256
from PIL import Image

root=Path("app/build/visual-runtime")
biomes=[root/"biomes"/f"{n}.png" for n in ("home","garden","lakeside","grove")]
poses=[root/"haru-poses"/f"{n}.png" for n in (
    "idle_right","idle_left","walk_right","walk_left","sit","crouch",
    "sleep","think","reaction","search_right","search_left"
)]
for p in biomes+poses:
    if not p.is_file() or p.stat().st_size <= 10000:
        raise SystemExit(f"invalid runtime capture: {p}")
    with Image.open(p) as im:
        if im.width < 1000 or im.height < 500:
            raise SystemExit(f"unexpected runtime dimensions: {p} {im.size}")

bh=[sha256(p.read_bytes()).hexdigest() for p in biomes]
if len(set(bh)) != 4:
    raise SystemExit("runtime biome captures are not four distinct frames")

ph=[sha256(p.read_bytes()).hexdigest() for p in poses]
if len(set(ph)) < 8:
    raise SystemExit(f"Haru runtime poses are not visually distinct enough: {len(set(ph))}/11 unique")

print("runtime capture validation: 4 distinct biomes,",
      f"{len(set(ph))}/11 distinct Haru pose frames")
PY

ls -lh "$OUT/biomes" "$OUT/haru-poses"
