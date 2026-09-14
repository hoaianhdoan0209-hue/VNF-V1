package com.aicharacter.v3;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CrashDiagnostics {
    private CrashDiagnostics() {}
    public static void install(Context context) {
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try {
                StringWriter sw = new StringWriter();
                error.printStackTrace(new PrintWriter(sw));
                String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
                File dir = new File(context.getFilesDir(), "diagnostics");
                if (!dir.exists()) dir.mkdirs();
                File out = new File(dir, "crash_" + stamp + ".log");
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(("Thread: " + thread.getName() + "\n" + sw).getBytes(StandardCharsets.UTF_8));
                }
            } catch (Throwable ignored) { }
            if (previous != null) previous.uncaughtException(thread, error);
            else System.exit(10);
        });
    }
}
