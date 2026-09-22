#!/usr/bin/env bash
set -euo pipefail
APK="app/build/outputs/apk/debug/app-debug.apk"
OUT="app/build/visual-runtime"
adb install -r "$APK"
mkdir -p "$OUT/biomes" "$OUT/haru-poses"
capture() {
  local name="$1" biome="$2" pose="$3" target="$4"
  adb shell am force-stop com.aicharacter.v3 || true
  adb shell pm clear com.aicharacter.v3 >/dev/null
  adb shell am start -W -S -n com.aicharacter.v3/.MainActivity --es vnf_debug_biome "$biome" --es vnf_debug_pose "$pose" >/dev/null
  sleep 6
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
ls -lh "$OUT/biomes" "$OUT/haru-poses"
