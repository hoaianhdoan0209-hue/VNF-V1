# VNF 1.0.3 — Real Experience Regression Fixes

- versionCode 120
- versionName 1.0.3
- Preserve existing world/save; no uninstall or data reset required.

## Main fixes
- Recover invalid persisted Haru/cat positions into authored world space.
- Recover stalled ACTIVE Haru plans instead of standing indefinitely.
- Keep player invitations as suggestions; never directly write Haru movement/intention.
- Broaden direct body-command rejection.
- Ask Micro/Notification permissions after the first rendered world frame.
- Preserve proactive speech through deferred voice startup.
- Add TTS locale fallback when vi-VN data is unavailable.
- Keep ambience listener tied to the player cat.
- Reset camera state safely when live state/view is replaced.
- Keep bounded offline catch-up and fast startup behavior.

Full device-facing audit mapping: QA_REAL_EXPERIENCE_AUDIT_1.0.3.md
