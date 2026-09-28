# VNF — CURRENT TRUTH

Updated: 2026-09-28

This file is the project continuity lock. A new chat/session must read this file, `VNF_PLAYER_EXPERIENCE.md`, and the current branch HEAD before proposing or changing VNF.

## Source-of-truth order

1. Current Git repository state and executable code.
2. This file.
3. `VNF_PLAYER_EXPERIENCE.md` and current release/QA evidence.
4. Historical reports and chat summaries.

Historical notes must never override newer code or this file.

## Current release and development line

- Package: `com.aicharacter.v3`
- Current stable release: **VNF 1.0.5**
- Stable versionCode: **122**
- Stable source commit: `0448469c64e6399c5409989a17d52f97e8cdb5ec`
- Active development branch: **`team/visual-v1`**
- Do not create a new development branch unless the user explicitly changes this rule.
- Stable release branches/tags are evidence snapshots. Do not use a later-moving branch HEAD as proof of the exact released APK; use the release/tag target commit.

## Product invariants — do not reinterpret between chats

- WORLD FIRST: simulation state is truth. Presentation/dialogue may reveal it but may not invent history.
- The player experiences VNF as a cat living inside the world.
- Haru is autonomous and is not directly controllable.
- God/System may observe, explain, teach and make bounded proposals, but may not rewrite Haru's mind, memory, personality, relationship or intention.
- Active life and offline life are one causal timeline.
- No fixed NPC schedule used to fake life.
- No random animation used to fake causality.
- Camera is automatic cat-POV framing; no manual world-control camera.
- Visual language is coherent detailed 2D pixel art.
- Persistent world/save must survive normal app updates.
- Do not claim build/runtime/device PASS without matching evidence.

## Current development priority

Do **not** expand feature count merely because a new chat starts.

The active priority is **VNF LIVING EXPERIENCE PASS**: make existing systems visibly connect into a believable living experience.

Before broad new feature work, the following player-visible core must become convincing in natural play:

1. World opens quickly and resumes the existing causal world.
2. Haru visibly chooses and carries out activity without player prompting.
3. Haru movement/pose/action transitions read as one continuous behavior.
4. Haru can proactively speak from a real cause without spam.
5. Cat visibly behaves independently around Haru/world.
6. Camera lets the player read important character reactions naturally.
7. God feels present in the world rather than like a detached chatbot panel.
8. Save/reopen preserves the lived continuity instead of replaying/resetting it.

The existing 30-item real-device checklist remains the final perceptual gate. Automated tests are supporting evidence, not a replacement for the real-device result.

## Automation lock

CI/build automation may:
- run tests;
- compile APKs;
- capture emulator/runtime evidence;
- verify signatures, hashes, package/version and source cleanliness;
- publish an explicitly release-ready signed build.

CI/build automation must **not**:
- generate replacement production art as a side effect of building;
- commit or push source/assets;
- silently rewrite gameplay/design;
- make an unreviewed development branch into stable.

The APK tested/built must use the tracked source/assets from the commit being tested. A build must fail if its pipeline mutates tracked source.

## Existing systems that are not reasons to redesign VNF

The repository already contains substantial biology, ecology, cognition, autonomy, God, audio, camera, persistence and pixel rendering systems. Their existence does not prove the experience is good; however, a new chat must not rewrite them from scratch merely because it has a different design idea.

The 500-base-species evolutionary catalog, common ancestor model and divine ecology/follower/offering foundations already exist in code. Scale/depth work is lower priority than making a small representative slice visibly alive.

## Known evidence gap as of 2026-09-28

- Stable V1.0.5 has successful automated debug/unit/runtime visual CI evidence.
- The saved real-device checklist still records **0 PASS / 0 FAIL / 30 NOT TESTED**.
- Therefore do not describe the full player experience as device-verified yet.

## Change rule

Every meaningful development change should answer:

**What will the player directly see, hear, feel, or preserve better because of this change?**

If there is no clear answer and the change is not a correctness/safety/build fix, defer it until the Living Experience core is proven.
