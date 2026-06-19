package com.example.ytsclient.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;

public class ImageCache {
    private static final long MAX_AGE_MS = 7L * 24L * 60L * 60L * 1000L; // 7 days
    private static final long ONE_DAY_MS = 24L * 60L * 60L * 1000L;     // 1 day

    private final File cacheDir;

    private static final String PREFS_NAME = "image_cache_prefs";
    private static final String KEY_LAST_CLEANUP = "last_cleanup_time";

    public ImageCache(Context context) {
        File root = context.getApplicationContext().getExternalFilesDir(null);
        if (root == null) {
            root = context.getApplicationContext().getFilesDir();
        }
        cacheDir = new File(root, "Android/image-cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        triggerDailyCleanupIfNeeded(context.getApplicationContext());
    }

    public File getImage(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        File file = new File(cacheDir, sha256(imageUrl) + extensionFor(imageUrl));
        if (file.exists()) {
            return file;
        }
        download(imageUrl, file);
        return file;
    }

    private static void download(String imageUrl, File destination) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("User-Agent", "YTSClientAndroid/1.0");
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Image request failed with HTTP " + status);
            }
            File temp = new File(destination.getParentFile(), destination.getName() + ".tmp");
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(temp)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
            }
            if (destination.exists() && !destination.delete()) {
                throw new IllegalStateException("Could not replace expired cached image");
            }
            if (!temp.renameTo(destination)) {
                throw new IllegalStateException("Could not store cached image");
            }
        } finally {
            connection.disconnect();
        }
    }

    private static String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(value.getBytes("UTF-8"));
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format(Locale.US, "%02x", b));
        }
        return builder.toString();
    }

    private static String extensionFor(String imageUrl) {
        String lower = imageUrl.toLowerCase(Locale.US);
        if (lower.contains(".png")) return ".png";
        if (lower.contains(".webp")) return ".webp";
        return ".jpg";
    }

    /**
     * Checks if a cleanup operation is due (has been 24 hours since the last one).
     * If yes, launches the cleanup task on a background thread.
     */
    private void triggerDailyCleanupIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCleanupTime = prefs.getLong(KEY_LAST_CLEANUP, 0L);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCleanupTime >= ONE_DAY_MS) {
            // Run on a background thread to avoid blocking the main UI thread during disk I/O
            new Thread(() -> {
                try {
                    performCacheCleanup();
                    // Save the timestamp on successful cleanup execution
                    prefs.edit().putLong(KEY_LAST_CLEANUP, System.currentTimeMillis()).apply();
                } catch (Exception e) {
                    // Log the error using your application's logging framework
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Iterates through cached files and deletes any file that is older than 7 days.
     */
    private void performCacheCleanup() {
        if (cacheDir == null || !cacheDir.exists() || !cacheDir.isDirectory()) {
            return;
        }

        File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        for (File file : files) {
            if (file.isFile()) {
                long fileAge = currentTime - file.lastModified();
                if (fileAge > MAX_AGE_MS) {
                    file.delete();
                }
            }
        }
    }
}
