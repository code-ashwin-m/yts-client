package com.example.ytsclient.data;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;

public class ImageCache {
    private static final long MAX_AGE_MS = 7L * 24L * 60L * 60L * 1000L;

    private final File cacheDir;

    public ImageCache(Context context) {
        File root = context.getApplicationContext().getExternalFilesDir(null);
        if (root == null) {
            root = context.getApplicationContext().getFilesDir();
        }
        cacheDir = new File(root, "Android/image-cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public File getImage(String imageUrl) throws Exception {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        File file = new File(cacheDir, sha256(imageUrl) + extensionFor(imageUrl));
        if (file.exists() && System.currentTimeMillis() - file.lastModified() <= MAX_AGE_MS) {
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
}
