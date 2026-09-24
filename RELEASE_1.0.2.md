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

## Release
- versionCode: 119
- versionName: 1.0.2
- Save/world hiện tại được giữ nguyên.
