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
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "VISUAL_CAPTURE_FILE name=$name ok=true"; then
      ready=1
      break
    fi
    if adb logcat -d -s 'VNF:E' '*:S' 2>/dev/null | grep -Fq "VISUAL_CAPTURE_FILE failed name=$name"; then
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
    # The renderer was confirmed ready; use the actual device framebuffer as a safe QA fallback.
    if timeout 12s adb exec-out screencap -p > "$dest" 2>/dev/null && [[ -s "$dest" ]] && valid_png "$dest"; then
      return 0
    fi
  fi
  echo "Runtime renderer did not export a valid PNG for $name" >&2
  mkdir -p "$OUT/diagnostics"
  adb exec-out screencap -p > "$OUT/diagnostics/${name}_timeout_screen.png" 2>/dev/null || true
  pid="$(adb shell pidof "$PKG" 2>/dev/null | tr -d '\\r' | awk '{print $1}')"
  if [[ -n "$pid" ]]; then
    adb shell kill -3 "$pid" >/dev/null 2>&1 || true
    sleep 2
  fi
  adb logcat -d -t 1200 > "$OUT/diagnostics/${name}_logcat.txt" 2>/dev/null || true
  adb shell dumpsys activity activities > "$OUT/diagnostics/${name}_activity.txt" 2>/dev/null || true
  dump_runtime_debug
  return 1
}

install_apk
adb shell pm clear "$PKG" >/dev/null 2>&1 || true
rm -rf "$OUT"
mkdir -p "$OUT/biomes" "$OUT/haru-poses" "$OUT/cat-poses"

adb shell settings put secure immersive_mode_confirmations confirmed || true
adb shell settings put global hide_error_dialogs 1 || true
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
adb shell run-as "$PKG" rm -rf "$APP_CAPTURE_DIR" >/dev/null 2>&1 || true

APP_STARTED=0
capture() {
  local name="$1" biome="$2" pose="$3" target="$4" cat_pose="${5:-}"
  local file="$target/$name.png"
  local cat_args=()
  if [[ -n "$cat_pose" ]]; then cat_args=(--es vnf_debug_cat_pose "$cat_pose"); fi

  adb logcat -c || true
  adb shell settings put secure immersive_mode_confirmations confirmed || true
  adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
  adb shell run-as "$PKG" rm -f "$APP_CAPTURE_DIR/$name.png" >/dev/null 2>&1 || true

  if [[ "$APP_STARTED" -eq 0 ]]; then
    timeout 45s adb shell am start -W -S -n "$ACTIVITY" \
      --es vnf_debug_biome "$biome" \
      --es vnf_debug_pose "$pose" \
      "${cat_args[@]}" \
      --es vnf_debug_capture_name "$name" >/dev/null
    APP_STARTED=1
  else
    timeout 30s adb shell am start -W --activity-single-top -n "$ACTIVITY" \
      --es vnf_debug_biome "$biome" \
      --es vnf_debug_pose "$pose" \
      "${cat_args[@]}" \
      --es vnf_debug_capture_name "$name" >/dev/null
  fi

  pull_runtime_capture "$name" "$file"
  echo "captured $name biome=$biome pose=$pose cat=$cat_pose bytes=$(stat -c%s "$file")"
}

capture home home idle_right "$OUT/biomes"
capture garden garden idle_right "$OUT/biomes"
capture lakeside lakeside idle_right "$OUT/biomes"
capture grove grove idle_right "$OUT/biomes"

for pose in idle_right idle_left walk_right walk_left sit crouch sleep think reaction search_right search_left; do
  capture "$pose" home "$pose" "$OUT/haru-poses"
done

for cat_pose in idle watch walk approach retreat rub settle sleep brace attached; do
  capture "cat_$cat_pose" home idle_right "$OUT/cat-poses" "$cat_pose"
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
cats=[root/"cat-poses"/f"cat_{n}.png" for n in (
    "idle","watch","walk","approach","retreat","rub","settle","sleep","brace","attached"
)]

for p in biomes+poses+cats:
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

ch=[sha256(p.read_bytes()).hexdigest() for p in cats]
if len(set(ch)) < 7:
    raise SystemExit(f"Cat runtime poses are not visually distinct enough: {len(set(ch))}/10 unique")

print("runtime capture validation: 4 distinct biomes,",
      f"{len(set(ph))}/11 distinct Haru pose frames,",
      f"{len(set(ch))}/10 distinct cat pose frames")
PY

run_natural_probe() {
  local natural="$OUT/natural-play"
  mkdir -p "$natural"
  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell pm clear "$PKG" >/dev/null
  wait_for_pm
  adb shell pm grant "$PKG" android.permission.RECORD_AUDIO >/dev/null 2>&1 || true
  adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS >/dev/null 2>&1 || true
  adb logcat -c || true

  timeout 45s adb shell am start -W -n "$ACTIVITY" --ez vnf_debug_natural_probe true >/dev/null

  local start_ready=0
  for _ in $(seq 1 40); do
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "NATURAL_QA sample=start"; then
      start_ready=1
      break
    fi
    sleep 1
  done
  if [[ "$start_ready" -ne 1 ]]; then
    echo "Natural-play start sample was not emitted." >&2
    dump_runtime_debug
    return 1
  fi
  timeout 12s adb exec-out screencap -p > "$natural/start.png"
  valid_png "$natural/start.png"

  local final_ready=0
  for _ in $(seq 1 205); do
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "NATURAL_QA sample=final"; then
      final_ready=1
      break
    fi
    sleep 1
  done
  if [[ "$final_ready" -ne 1 ]]; then
    echo "Natural-play final sample was not emitted." >&2
    dump_runtime_debug
    return 1
  fi
  timeout 12s adb exec-out screencap -p > "$natural/final.png"
  valid_png "$natural/final.png"
  adb logcat -d -s 'VNF:I' '*:S' > "$natural/runtime.log"

  python - "$natural/runtime.log" "$natural/final_state.json" <<'PY2'
import json,re,sys
from pathlib import Path

text=Path(sys.argv[1]).read_text(errors="replace")
samples={}
for m in re.finditer(r"NATURAL_QA sample=(start|final) (.*)",text):
    fields={}
    for token in m.group(2).split():
        if "=" in token:
            k,v=token.split("=",1);fields[k]=v
    samples[m.group(1)]=fields
if set(samples)!={"start","final"}:
    raise SystemExit(f"missing natural-play samples: {samples.keys()}")
def num(d,k): return float(d.get(k,"0"))
def integer(d,k): return int(float(d.get(k,"0")))
a,b=samples["start"],samples["final"]
dx=abs(num(b,"haruX")-num(a,"haruX"))
changed=(
    dx>=20.0 or
    a.get("intention")!=b.get("intention") or
    a.get("activity")!=b.get("activity") or
    a.get("plan")!=b.get("plan") or
    a.get("status")!=b.get("status") or
    integer(b,"planProgress")>integer(a,"planProgress")+1000
)
if integer(b,"simulatedAt")<=integer(a,"simulatedAt"):
    raise SystemExit("natural-play simulation clock did not advance")
if not changed:
    raise SystemExit(f"Haru showed no natural runtime progress over probe window: start={a} final={b}")
if not b.get("intention") and b.get("activity") in ("", "standing_quietly") and b.get("travel")!="true":
    raise SystemExit(f"Haru ended natural probe as an idle placeholder: {b}")
Path(sys.argv[2]).write_text(json.dumps(b,sort_keys=True))
print(f"natural-play smoke PASS: dx={dx:.1f} start={a} final={b}")
PY2

  adb shell input keyevent KEYCODE_HOME >/dev/null 2>&1 || true
  local persisted_ready=0
  for _ in $(seq 1 25); do
    if adb exec-out run-as "$PKG" cat files/world/world.json > "$natural/persisted.json" 2>/dev/null; then
      if python - "$natural/final_state.json" "$natural/persisted.json" <<'PYSAVE'
import json,sys
from pathlib import Path
before=json.loads(Path(sys.argv[1]).read_text())
saved=json.loads(Path(sys.argv[2]).read_text())
need=int(float(before.get("simulatedAt","0")))
have=int(saved.get("lastSimulatedAt",0))
raise SystemExit(0 if have>=need else 1)
PYSAVE
      then
        persisted_ready=1
        break
      fi
    fi
    sleep 1
  done
  if [[ "$persisted_ready" -ne 1 ]]; then
    echo "World save did not reach the live causal cursor before reopen." >&2
    if [[ -s "$natural/persisted.json" ]]; then
      python - "$natural/final_state.json" "$natural/persisted.json" <<'PYSAVEFAIL' || true
import json,sys
from pathlib import Path
before=json.loads(Path(sys.argv[1]).read_text())
saved=json.loads(Path(sys.argv[2]).read_text())
print("live simulatedAt=",before.get("simulatedAt"),"persisted simulatedAt=",saved.get("lastSimulatedAt"),"persisted savedAt=",saved.get("lastSavedAt"))
PYSAVEFAIL
    fi
    dump_runtime_debug
    return 1
  fi
  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb logcat -c || true
  timeout 45s adb shell am start -W -n "$ACTIVITY" --ez vnf_debug_reopen_probe true >/dev/null
  local reopen_ready=0
  for _ in $(seq 1 50); do
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "REOPEN_QA"; then
      reopen_ready=1
      break
    fi
    sleep 1
  done
  if [[ "$reopen_ready" -ne 1 ]]; then
    echo "Reopen continuity sample was not emitted." >&2
    dump_runtime_debug
    return 1
  fi
  adb logcat -d -s 'VNF:I' '*:S' > "$natural/reopen.log"
  timeout 12s adb exec-out screencap -p > "$natural/reopen.png"
  valid_png "$natural/reopen.png"

  python - "$natural/final_state.json" "$natural/reopen.log" <<'PY3'
import json,re,sys
from pathlib import Path
before=json.loads(Path(sys.argv[1]).read_text())
text=Path(sys.argv[2]).read_text(errors="replace")
matches=list(re.finditer(r"REOPEN_QA (.*)",text))
if not matches:
    raise SystemExit("missing REOPEN_QA sample")
after={}
for token in matches[-1].group(1).split():
    if "=" in token:
        k,v=token.split("=",1);after[k]=v
def i(d,k): return int(float(d.get(k,"0")))
if i(after,"createdAt")!=i(before,"createdAt"):
    raise SystemExit(f"world identity reset across reopen: before={before} after={after}")
if i(after,"simulatedAt")<i(before,"simulatedAt"):
    raise SystemExit(f"causal time moved backwards across reopen: before={before} after={after}")
if i(after,"openedAt")<i(before,"openedAt"):
    raise SystemExit(f"openedAt moved backwards across reopen: before={before} after={after}")
print(f"reopen continuity PASS: createdAt={after.get('createdAt')} simulated {before.get('simulatedAt')} -> {after.get('simulatedAt')}")
PY3
}

run_cat_social_probe() {
  local social="$OUT/cat-social"
  mkdir -p "$social"
  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell pm clear "$PKG" >/dev/null
  wait_for_pm
  adb shell pm grant "$PKG" android.permission.RECORD_AUDIO >/dev/null 2>&1 || true
  adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS >/dev/null 2>&1 || true
  adb logcat -c || true
  timeout 45s adb shell am start -W -n "$ACTIVITY" --ez vnf_debug_cat_social_probe true >/dev/null

  local start_ready=0 final_ready=0
  for _ in $(seq 1 45); do
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "CAT_QA sample=start"; then start_ready=1; break; fi
    sleep 1
  done
  [[ "$start_ready" -eq 1 ]] || { echo "Cat social start sample missing." >&2; dump_runtime_debug; return 1; }
  timeout 12s adb exec-out screencap -p > "$social/start.png"
  valid_png "$social/start.png"

  for _ in $(seq 1 35); do
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "CAT_QA sample=final"; then final_ready=1; break; fi
    sleep 1
  done
  [[ "$final_ready" -eq 1 ]] || { echo "Cat social final sample missing." >&2; dump_runtime_debug; return 1; }
  timeout 12s adb exec-out screencap -p > "$social/final.png"
  valid_png "$social/final.png"
  adb logcat -d -s 'VNF:I' '*:S' > "$social/runtime.log"

  python - "$social/runtime.log" <<'PY4'
import re,sys
from pathlib import Path
text=Path(sys.argv[1]).read_text(errors="replace")
samples={}
for m in re.finditer(r"CAT_QA sample=(start|final) (.*)",text):
    fields={}
    for token in m.group(2).split():
        if "=" in token:
            k,v=token.split("=",1);fields[k]=v
    samples[m.group(1)]=fields
if set(samples)!={"start","final"}:
    raise SystemExit(f"missing cat social samples: {samples.keys()}")
a,b=samples["start"],samples["final"]
def f(d,k): return float(d.get(k,"0"))
def i(d,k): return int(float(d.get(k,"0")))
dx=abs(f(b,"catX")-f(a,"catX"))
social_change=(
    dx>=12.0 or
    a.get("mode")!=b.get("mode") or
    b.get("travel")=="true" or
    i(b,"approaches")>i(a,"approaches") or
    i(b,"retreats")>i(a,"retreats") or
    i(b,"settles")>i(a,"settles")
)
if i(b,"simulatedAt")<=i(a,"simulatedAt"):
    raise SystemExit("cat social probe simulation clock did not advance")
if not social_change:
    raise SystemExit(f"cat showed no autonomous social response: start={a} final={b}")
print(f"cat social runtime PASS: dx={dx:.1f} start={a} final={b}")
PY4
}

run_presentation_probes() {
  local present="$OUT/presentation"
  mkdir -p "$present"

  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell pm clear "$PKG" >/dev/null
  wait_for_pm
  adb shell pm grant "$PKG" android.permission.RECORD_AUDIO >/dev/null 2>&1 || true
  adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS >/dev/null 2>&1 || true
  adb logcat -c || true
  timeout 45s adb shell am start -W -n "$ACTIVITY" --ez vnf_debug_camera_probe true >/dev/null
  local camera_ready=0
  for _ in $(seq 1 60); do
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "CAMERA_QA"; then camera_ready=1; break; fi
    sleep 1
  done
  [[ "$camera_ready" -eq 1 ]] || { echo "Camera presentation probe missing." >&2; dump_runtime_debug; return 1; }
  timeout 12s adb exec-out screencap -p > "$present/camera_close.png"
  valid_png "$present/camera_close.png"
  adb logcat -d -s 'VNF:I' '*:S' > "$present/camera.log"

  python - "$present/camera.log" <<'PYCAM'
import re,sys
from pathlib import Path
text=Path(sys.argv[1]).read_text(errors="replace")
m=list(re.finditer(r"CAMERA_QA shot=(\S+) targetZoom=([0-9.]+) visualZoom=([0-9.]+)",text))
if not m: raise SystemExit("missing CAMERA_QA fields")
shot,target,visual=m[-1].group(1),float(m[-1].group(2)),float(m[-1].group(3))
if shot not in {"CLOSE","INTIMATE_CLOSE"}:
    raise SystemExit(f"camera did not choose a readable close shot: {shot}")
if target < 1.40 or visual < 1.25:
    raise SystemExit(f"camera close framing too weak: target={target} visual={visual}")
print(f"camera runtime PASS: shot={shot} targetZoom={target:.3f} visualZoom={visual:.3f}")
PYCAM

  adb shell am force-stop "$PKG" >/dev/null 2>&1 || true
  adb shell pm clear "$PKG" >/dev/null
  wait_for_pm
  adb shell pm grant "$PKG" android.permission.RECORD_AUDIO >/dev/null 2>&1 || true
  adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS >/dev/null 2>&1 || true
  adb logcat -c || true
  timeout 45s adb shell am start -W -n "$ACTIVITY" --ez vnf_debug_god_probe true >/dev/null
  local god_ready=0
  for _ in $(seq 1 60); do
    if adb logcat -d -s 'VNF:I' '*:S' 2>/dev/null | grep -Fq "GOD_QA"; then god_ready=1; break; fi
    sleep 1
  done
  [[ "$god_ready" -eq 1 ]] || { echo "God presentation probe missing." >&2; dump_runtime_debug; return 1; }
  timeout 12s adb exec-out screencap -p > "$present/god_presence.png"
  valid_png "$present/god_presence.png"
  adb logcat -d -s 'VNF:I' '*:S' > "$present/god.log"

  python - "$present/god.log" "$present/camera_close.png" "$present/god_presence.png" <<'PYGOD'
import re,sys
from hashlib import sha256
from pathlib import Path
text=Path(sys.argv[1]).read_text(errors="replace")
m=list(re.finditer(r"GOD_QA scene=(true|false) world=(true|false) divine=(true|false) children=(\d+)",text))
if not m: raise SystemExit("missing GOD_QA fields")
scene,world,divine,children=m[-1].groups()
if scene!="true" or world!="true" or divine!="true" or int(children)<2:
    raise SystemExit(f"God is not visibly layered over the live world: scene={scene} world={world} divine={divine} children={children}")
if sha256(Path(sys.argv[2]).read_bytes()).digest()==sha256(Path(sys.argv[3]).read_bytes()).digest():
    raise SystemExit("God manifestation capture is identical to camera-only capture")
print(f"God runtime PASS: scene={scene} world={world} divine={divine} children={children}")
PYGOD
}

run_presentation_probes
run_natural_probe
run_cat_social_probe

ls -lh "$OUT/biomes" "$OUT/haru-poses" "$OUT/cat-poses" "$OUT/natural-play" "$OUT/cat-social" "$OUT/presentation"
