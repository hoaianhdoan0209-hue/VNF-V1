#!/usr/bin/env bash
set -euo pipefail

APK="app/build/outputs/apk/debug/app-debug.apk"
OUT="app/build/visual-runtime"
PKG="com.aicharacter.v3"
ACTIVITY="$PKG/.MainActivity"

wait_for_pm() {
  adb wait-for-device
  for _ in $(seq 1 60); do
    if timeout 8s adb shell cmd package list packages >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Package Manager did not become healthy." >&2
  return 1
}

wait_for_focus() {
  local state=""
  for _ in $(seq 1 45); do
    state="$(adb shell dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 10 || true)"
    if [[ "$state" == *"$PKG"* ]] && [[ "$state" != *"com.android.systemui"* ]] && [[ "$state" != *"com.android.permissioncontroller"* ]]; then
      return 0
    fi
    sleep 1
  done
  echo "VNF never became the focused app:" >&2
  printf '%s\n' "$state" >&2
  return 1
}

wait_for_render_ready() {
  local biome="$1"
  for _ in $(seq 1 60); do
    if adb logcat -d -s VNF:I '*:S' 2>/dev/null | grep -Fq "VISUAL_CAPTURE_READY biome=$biome"; then
      return 0
    fi
    sleep 1
  done
  echo "VNF never reported a successful rendered frame for biome=$biome" >&2
  dump_runtime_debug
  return 1
}

dump_runtime_debug() {
  echo "Window state:" >&2
  adb shell dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 12 >&2 || true
  echo "Surface state:" >&2
  adb shell dumpsys SurfaceFlinger --list 2>/dev/null | grep -E "$PKG|SurfaceView|BLAST" | tail -n 30 >&2 || true
  echo "Recent VNF logcat:" >&2
  adb logcat -d -t 320 2>/dev/null | grep -E "$PKG|VISUAL_CAPTURE_READY|AndroidRuntime|FATAL EXCEPTION|OutOfMemoryError" | tail -n 160 >&2 || true
}

install_apk() {
  wait_for_pm
  for attempt in 1 2 3; do
    if timeout 90s adb install -r "$APK"; then
      return 0
    fi
    echo "APK install attempt $attempt failed." >&2
    sleep 5
    wait_for_pm
  done
  return 1
}

valid_png() {
  local f="$1"
  python - "$f" <<'PY'
import sys
from PIL import Image
p=sys.argv[1]
try:
    with Image.open(p) as im:
        im.verify()
    with Image.open(p) as im:
        ok = im.width >= 1000 and im.height >= 500
except Exception:
    ok=False
raise SystemExit(0 if ok else 1)
PY
}

capture_png() {
  local path="$1"
  rm -f "$path"
  for attempt in 1 2 3 4; do
    if timeout 20s adb exec-out screencap -p > "$path" 2>/dev/null && [[ -s "$path" ]] && valid_png "$path"; then
      return 0
    fi
    echo "screencap attempt $attempt failed; retrying after renderer settle" >&2
    rm -f "$path"
    sleep 2
  done
  echo "Unable to capture a valid runtime PNG: $path" >&2
  dump_runtime_debug
  return 1
}

install_apk

# This emulator is new for every CI job. Clear app data once instead of before
# every screenshot; repeated pm clear was recycling surfaces and stressing
# gfxstream/ColorBuffer while also making every capture race the startup screen.
adb shell pm clear "$PKG" >/dev/null 2>&1 || true
rm -rf "$OUT"
mkdir -p "$OUT/biomes" "$OUT/haru-poses"

adb shell settings put secure immersive_mode_confirmations confirmed || true
adb shell settings put global hide_error_dialogs 1 || true
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true

APP_STARTED=0
capture() {
  local name="$1" biome="$2" pose="$3" target="$4"
  local file="$target/$name.png"

  adb logcat -c || true
  adb shell settings put secure immersive_mode_confirmations confirmed || true
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true

  # Keep one Activity/Surface alive for the entire visual pass. Repeated force-stop
  # was destroying/recreating emulator ColorBuffers and produced stale identical
  # screenshots under gfxstream. singleTop routes new debug parameters through
  # MainActivity.onNewIntent(), so only the scene changes while the Surface stays.
  if [[ "$APP_STARTED" -eq 0 ]]; then
    timeout 45s adb shell am start -W -S -n "$ACTIVITY" \
      --es vnf_debug_biome "$biome" \
      --es vnf_debug_pose "$pose" >/dev/null
    APP_STARTED=1
  else
    timeout 30s adb shell am start -W --activity-single-top -n "$ACTIVITY" \
      --es vnf_debug_biome "$biome" \
      --es vnf_debug_pose "$pose" >/dev/null
  fi

  wait_for_focus
  wait_for_render_ready "$biome"
  # Let two vsyncs pass after the renderer confirms the new scene.
  sleep 1
  wait_for_focus
  capture_png "$file"
  echo "captured $name biome=$biome pose=$pose bytes=$(stat -c%s "$file")"
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
print("biome hashes:", dict(zip([p.stem for p in biomes], [h[:12] for h in bh])))
if len(set(bh)) != 4:
    raise SystemExit("runtime biome captures are not four distinct frames")

ims=[Image.open(p).convert("RGB") for p in biomes]
for i in range(len(ims)):
    for j in range(i+1,len(ims)):
        diff=ImageChops.difference(ims[i],ims[j])
        mean=sum(ImageStat.Stat(diff).mean)/3.0
        print(f"biome diff {biomes[i].stem}/{biomes[j].stem}: {mean:.3f}")
        if mean < 1.0:
            raise SystemExit(f"runtime biome captures too visually similar: {biomes[i].name} vs {biomes[j].name} ({mean:.3f})")

ph=[sha256(p.read_bytes()).hexdigest() for p in poses]
if len(set(ph)) < 8:
    raise SystemExit(f"Haru runtime poses are not visually distinct enough: {len(set(ph))}/11 unique")

print("runtime capture validation: 4 distinct biomes,",
      f"{len(set(ph))}/11 distinct Haru pose frames")
PY

ls -lh "$OUT/biomes" "$OUT/haru-poses"
