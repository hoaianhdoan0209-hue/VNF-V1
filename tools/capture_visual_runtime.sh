#!/usr/bin/env bash
set -euo pipefail

APK="app/build/outputs/apk/debug/app-debug.apk"
OUT="app/build/visual-runtime"
PKG="com.aicharacter.v3"
ACTIVITY="$PKG/.MainActivity"

wait_for_pm() {
  adb wait-for-device
  for _ in $(seq 1 60); do
    if timeout 8s adb shell cmd package list packages "$PKG" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Package Manager did not become healthy." >&2
  return 1
}

wait_for_focus() {
  local state=""
  for _ in $(seq 1 40); do
    state="$(adb shell dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 10 || true)"
    if [[ "$state" == *"$PKG"* ]] && [[ "$state" != *"com.android.systemui"* ]] && [[ "$state" != *"com.android.permissioncontroller"* ]]; then
      return 0
    fi
    sleep 1
  done
  echo "VNF never became the focused app:" >&2
  printf '%s
' "$state" >&2
  return 1
}

dump_runtime_debug() {
  echo "Window state:" >&2
  adb shell dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 12 >&2 || true
  echo "Recent VNF logcat:" >&2
  adb logcat -d -t 260 2>/dev/null | grep -E "$PKG|AndroidRuntime|FATAL EXCEPTION|OutOfMemoryError" | tail -n 120 >&2 || true
}

wait_for_stable_focus() {
  for _ in $(seq 1 3); do
    wait_for_focus || { dump_runtime_debug; return 1; }
    sleep 1
  done
}

install_apk() {
  wait_for_pm
  for attempt in 1 2 3; do
    if timeout 60s adb install -r "$APK"; then
      return 0
    fi
    echo "APK install attempt $attempt failed." >&2
    sleep 5
    wait_for_pm
  done
  return 1
}

install_apk
rm -rf "$OUT"
mkdir -p "$OUT/biomes" "$OUT/haru-poses"

adb shell settings put secure immersive_mode_confirmations confirmed || true
adb shell settings put global hide_error_dialogs 1 || true
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true

capture() {
  local name="$1" biome="$2" pose="$3" target="$4"

  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true

  # Each runtime QA frame gets a pristine app state. This is required because
  # MainActivity persists Haru/world state during lifecycle transitions.
  timeout 30s adb shell pm clear "$PKG" >/dev/null
  wait_for_pm

  adb shell settings put secure immersive_mode_confirmations confirmed || true
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true

  timeout 30s adb shell am start -W -S -n "$ACTIVITY"     --es vnf_debug_biome "$biome"     --es vnf_debug_pose "$pose" >/dev/null

  # UIAutomator can destabilize the headless API-35 launcher/window stack.
  # The game is a custom Canvas, so stable foreground ownership is the correct
  # readiness signal; immersive-mode education is already disabled above.
  wait_for_stable_focus
  sleep 2
  wait_for_stable_focus

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
from PIL import Image, ImageChops, ImageStat

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

# Require meaningful pixel difference as well as different PNG bytes.
ims=[Image.open(p).convert("RGB") for p in biomes]
for i in range(len(ims)):
    for j in range(i+1,len(ims)):
        diff=ImageChops.difference(ims[i],ims[j])
        mean=sum(ImageStat.Stat(diff).mean)/3.0
        if mean < 1.0:
            raise SystemExit(f"runtime biome captures too visually similar: {biomes[i].name} vs {biomes[j].name} ({mean:.3f})")

ph=[sha256(p.read_bytes()).hexdigest() for p in poses]
if len(set(ph)) < 8:
    raise SystemExit(f"Haru runtime poses are not visually distinct enough: {len(set(ph))}/11 unique")

print("runtime capture validation: 4 distinct biomes,",
      f"{len(set(ph))}/11 distinct Haru pose frames")
PY

ls -lh "$OUT/biomes" "$OUT/haru-poses"
