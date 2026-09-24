# VNF 1.0.3 — Real Experience QA Audit

Source checklist: VNF_Bang_Test_Trai_Nghiem_Thuc_Te.xlsx (30 current-experience tests).

## Gate meaning
- AUTOMATED: regression/contract coverage exists and must stay green.
- DEVICE E2E: automated coverage protects wiring/logic, but the final perceptual result must still be verified on a real phone.
- FIXED 1.0.3: a concrete regression found during the full audit was repaired in this release.

| # | Area | Audit coverage | 1.0.3 notes |
|---|---|---|---|
| 1 | Startup | AUTOMATED + DEVICE E2E | First world frame is shown before heavy catch-up/services. |
| 2 | Offline continuity | AUTOMATED + DEVICE E2E | Spatial recovery prevents invalid Haru/cat coordinates; offline time remains causal. |
| 3 | Permissions | AUTOMATED + DEVICE E2E | FIXED 1.0.3: Haru asks after first rendered world frame, not after catch-up. |
| 4 | Haru autonomy | AUTOMATED + DEVICE E2E | Plan watchdog restarts stalled physical execution; healthy Haru chooses/evaluates activity without player input. |
| 5 | Haru speaks first | AUTOMATED + DEVICE E2E | Active-only proactive cue generation from real causes. |
| 6 | Proactive bubble | AUTOMATED + DEVICE E2E | Renderer keeps HARU · TỰ NÓI bubble wired and timed. |
| 7 | Proactive TTS | AUTOMATED + DEVICE E2E | FIXED 1.0.3: Vietnamese TTS now falls back safely when locale data is unavailable. |
| 8 | No proactive spam | AUTOMATED | Cause ID + cooldown prevents replay/spam. |
| 9 | Chat / voice | AUTOMATED + DEVICE E2E | First mic tap can enable voice; normal offline dialogue path remains local. |
| 10 | No direct Haru control | AUTOMATED | FIXED 1.0.3: direct body commands rejected; invitations no longer write movement/intention directly. |
| 11 | Haru visible emotion/body | AUTOMATED + DEVICE E2E | Pose/expression/motion-style pipeline remains state driven. |
| 12 | Cat social autonomy | AUTOMATED + DEVICE E2E | WATCH / APPROACH / RETREAT / SETTLE covered. |
| 13 | Cat body animation | AUTOMATED + DEVICE E2E | Social/gait/brace/sleep/carry states remain distinct. |
| 14 | Cat sleep/carry/brace | AUTOMATED + DEVICE E2E | Persistent cat state + animation contracts covered. |
| 15 | Cat rub + Haru look-down | AUTOMATED + DEVICE E2E | MicroInteractionDirector coverage. |
| 16 | Interaction cancellation | AUTOMATED + DEVICE E2E | Distance/travel/pain cancel presentation. |
| 17 | Haru footsteps by surface | AUTOMATED + DEVICE E2E | Authored surface acoustic profiles distinct. |
| 18 | Cat footsteps distinct | AUTOMATED + DEVICE E2E | Cat/Haru audio paths remain separate. |
| 19 | Rain shelter difference | AUTOMATED + DEVICE E2E | Weather exposure uses listener/player-cat context. |
| 20 | Area ambience | AUTOMATED + DEVICE E2E | FIXED before 1.0.3 gate: ambience listener follows player cat, not Haru. |
| 21 | Haru breathing audio | AUTOMATED + DEVICE E2E | Respiration-driven audio scene wiring retained. |
| 22 | Cat purr | AUTOMATED + DEVICE E2E | Comfort/proximity purr coverage. |
| 23 | Emotion/social camera | AUTOMATED + DEVICE E2E | EmotionCameraDirector + camera reset coverage. |
| 24 | Reunion/warmth moment | AUTOMATED + DEVICE E2E | Moment Director camera/light/sound contract. |
| 25 | Retreat wide framing | AUTOMATED + DEVICE E2E | Moment/camera composition coverage. |
| 26 | Insight beat | AUTOMATED + DEVICE E2E | Causal-learning moment event coverage. |
| 27 | God manifestation | AUTOMATED + DEVICE E2E | Dedicated manifestation/presence visual pipeline retained. |
| 28 | Divine presence without Haru control | AUTOMATED + DEVICE E2E | Divine presentation is not persisted into Haru mind/world-control state. |
| 29 | God status/diagnosis/knowledge | AUTOMATED + DEVICE E2E | Character-God capability contracts remain covered. |
| 30 | Proactive speech persistence | AUTOMATED + DEVICE E2E | Delivered source/cooldown persists across save round-trip; deferred startup cue is preserved. |

## Concrete regressions repaired in this audit
1. Persisted invalid/gap actor coordinates could make a character appear to disappear after reopen.
2. ACTIVE Haru plan could lose travel and appear idle/stalled; watchdog recovery now restores causal physical execution.
3. Player walk invitation could write Haru intention/activity directly; it is now a suggestion only.
4. Direct body commands without explicit “hãy/phải/ngay” could escape the control boundary; parser coverage is broader.
5. Permission request could wait for offline catch-up; it is now scheduled from first real world frame.
6. Proactive TTS could mark itself ready even if vi-VN is unsupported; locale fallback is validated.
7. Deferred proactive speech, player-relative ambience, camera reset and first-tap voice are covered by RealExperienceRegressionTest from the existing 1.0.3 audit work.

No automated test is treated as proof of subjective phone experience. Rows marked DEVICE E2E still require the real-play checklist.
