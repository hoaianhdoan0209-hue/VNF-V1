# VNF Foundation — V0.99C

VNF is a world-first Android foundation where the player experiences the world as a cat living beside an autonomous girl. The girl is not directly controllable; cognition, memory, beliefs, body state, relationship, world context and current intentions drive her behavior.

## Current milestone

This repository remains **V0.99C** until the foundation passes independent review. Do not rename it V1.0 until motion, visual integration, regressions, persistence and Android build gates are all proven.

## Player-facing target

See `VNF_PLAYER_EXPERIENCE.md` for the concise experience target.

## Build

CI uses Gradle 8.14.1, AGP 8.7.3, Android API 35 and Java 21 runtime with Java 17 source compatibility. A successful GitHub Actions run should produce `VNF-debug-apk`.

## Release rule

V0.99C PASS -> V1.0 FOUNDATION PLAYABLE -> first serious device playtest.
