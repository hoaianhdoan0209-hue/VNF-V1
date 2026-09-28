# VNF — Living World Android Project

VNF is a world-first Android game where the player experiences the world as a cat living beside an autonomous girl. Haru is not directly controllable; cognition, memory, beliefs, body state, relationship, world context and current intentions drive her behavior.

## Current state

- Current stable release: **VNF 1.0.5**
- Stable versionCode: **122**
- Stable source commit: `0448469c64e6399c5409989a17d52f97e8cdb5ec`
- Active development branch: **`team/visual-v1`**
- Current development gate: **VNF LIVING EXPERIENCE PASS**

Before changing design or continuing from another chat/session, read:

1. `VNF_CURRENT_TRUTH.md`
2. `VNF_PLAYER_EXPERIENCE.md`
3. current branch HEAD and current QA evidence

Historical V0.99 reports are retained only as development history and are not the current project status.

## Development focus

The repository already contains substantial autonomy, biology, ecology, God, persistence, audio, camera and pixel-rendering systems. The current priority is not adding more systems; it is making the existing systems connect into a convincing player-visible living experience.

Automated tests support this work, but the real-device checklist remains the perceptual acceptance gate.

## Build and QA

GitHub CI uses Java 17, Gradle 8.14.1, AGP 8.7.3 and Android API 35. CI builds only tracked source/assets from the commit under test; build/test automation is not allowed to generate replacement production art or push source changes.

Visual CI runs unit tests, builds the debug APK and captures runtime emulator evidence for the four launch biomes plus Haru/cat pose coverage.

## Release rule

A stable release must preserve package identity and existing world/save data, pass required automated gates, use the configured release signing identity, and have explicit release-ready version metadata.

Do not claim full player-experience PASS until the corresponding real-device checklist has been completed.
