package com.example.kuetride.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogUtil {
    public static void append(Context ctx, String text) {
        try {
            File f = new File(ctx.getFilesDir(), "app.log");
            try (FileOutputStream fos = new FileOutputStream(f, true)) {
                fos.write((text + "\n").getBytes());
            }
        } catch (IOException ignored) {}
    }
    public static File exportSchedule(Context ctx, String content) throws IOException {
        File f = new File(ctx.getExternalFilesDir(null), "schedule_export.txt");
        try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(content.getBytes()); }
        return f;
    }
}
