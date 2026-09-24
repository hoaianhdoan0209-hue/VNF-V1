#!/usr/bin/env bash
set -euo pipefail

APK="app/build/outputs/apk/debug/app-debug.apk"
OUT="app/build/visual-runtime"
PKG="com.aicharacter.v3"
ACTIVITY="$PKG/.MainActivity"
APP_CAPTURE_DIR="files/visual-capture"

wait_for_pm() {
  adb wait-for-device
  for _ in $(seq 1 90); do
    if timeout 8s adb shell cmd package list packages >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Package Manager did not become healthy." >&2
  return 1
}

dump_runtime_debug() {
  echo "Window/activity state:" >&2
  adb shell dumpsys activity activities 2>/dev/null | grep -E 'mResumedActivity|topResumedActivity|com.aicharacter.v3' | tail -n 30 >&2 || true
  adb shell dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 12 >&2 || true
  echo "Recent VNF logcat:" >&2
  adb logcat -d -t 400 2>/dev/null | grep -E 'VNF|VISUAL_CAPTURE|AndroidRuntime|FATAL EXCEPTION|OutOfMemoryError' | tail -n 220 >&2 || true
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

pull_runtime_capture() {
  local name="$1" dest="$2"
  rm -f "$dest"
  local ready=0
  for _ in $(seq 1 180); do
    if adb logcat -d -s VNF:I VNF:E '*:S' 2>/dev/null | grep -Fq "VISUAL_CAPTURE_FILE name=$name ok=true"; then
      ready=1
      break
    fi
    if adb logcat -d -s VNF:E '*:S' 2>/dev/null | grep -Fq "VISUAL_CAPTURE_FILE failed name=$name"; then
      break
    fi
    sleep 1
  done
  if [[ "$ready" -eq 1 ]]; then
    for _ in $(seq 1 15); do
      if timeout 12s adb exec-out run-as "$PKG" cat "$APP_CAPTURE_DIR/$name.png" > "$dest" 2>/dev/null; then
        if [[ -s "$dest" ]] && valid_png "$dest"; then
          return 0
        fi
      fi
      rm -f "$dest"
      sleep 1
    done
  fi
  echo "Runtime renderer did not export a valid PNG for $name" >&2
  dump_runtime_debug
  return 1
}

install_apk
adb shell pm clear "$PKG" >/dev/null 2>&1 || true
rm -rf "$OUT"
mkdir -p "$OUT/biomes" "$OUT/haru-poses"

adb shell settings put secure immersive_mode_confirmations confirmed || true
adb shell settings put global hide_error_dialogs 1 || true
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
adb shell run-as "$PKG" rm -rf "$APP_CAPTURE_DIR" >/dev/null 2>&1 || true

APP_STARTED=0
capture() {
  local name="$1" biome="$2" pose="$3" target="$4"
  local file="$target/$name.png"

  adb logcat -c || true
  adb shell settings put secure immersive_mode_confirmations confirmed || true
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
  adb shell run-as "$PKG" rm -f "$APP_CAPTURE_DIR/$name.png" >/dev/null 2>&1 || true

  if [[ "$APP_STARTED" -eq 0 ]]; then
    timeout 45s adb shell am start -W -S -n "$ACTIVITY" \
      --es vnf_debug_biome "$biome" \
      --es vnf_debug_pose "$pose" \
      --es vnf_debug_capture_name "$name" >/dev/null
    APP_STARTED=1
  else
    timeout 30s adb shell am start -W --activity-single-top -n "$ACTIVITY" \
      --es vnf_debug_biome "$biome" \
      --es vnf_debug_pose "$pose" \
      --es vnf_debug_capture_name "$name" >/dev/null
  fi

  pull_runtime_capture "$name" "$file"
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
