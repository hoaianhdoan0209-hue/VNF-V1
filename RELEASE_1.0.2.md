# VNF 1.0.2 — Fast Startup Hotfix

## Mục tiêu
Sửa lỗi test thực tế: mở VNF sau thời gian dài phải chờ lâu mới vào được world.

## Thay đổi
- Cold start chỉ đọc preview save tối thiểu và dựng world trước.
- Offline causal reconstruction chạy sau khi world đã render frame đầu tiên.
- Preview bị đóng băng simulation để không chạy lại timeline cũ trên UI thread.
- Resume catch-up cũng chuyển khỏi UI thread.
- Voice, audio và updater được giãn sau first frame; God chỉ warmup khi người chơi thực sự gọi.
- Frame world đầu tiên không derive audio scene.
- Thêm telemetry: STARTUP_WORLD_ATTACHED, STARTUP_FIRST_WORLD_FRAME, STARTUP_CAUSAL_READY.
- Thêm FastStartupRegressionTest để khóa thứ tự startup mới.

## Release identity
- versionCode: 119
- versionName: 1.0.2
- GitHub Release/tag source commit: `d9d8d5cf86af4bfb080fe30344157fe6206cab0a`
- Save/world hiện tại được giữ nguyên.

## Traceability note
The branch `release/v1.0.2` later advanced beyond the published release commit. Therefore the release/tag target commit above is the canonical source snapshot for the V1.0.2 APK; a later branch HEAD must not be substituted when auditing the released binary.
