package com.example.dragapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.example.dragapp.DALAppWriteConnection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * تحميل صور Appwrite Storage مباشرة مع رؤوس المصادقة.
 * Glide لا يمرّر المفتاح بشكل موثوق — نستخدم HttpURLConnection.
 */
public final class AppwriteImageLoader {

    private static final String TAG = "AppwriteImageLoader";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final Map<ImageView, String> PENDING = new WeakHashMap<>();

    private AppwriteImageLoader() {}

    /**
     * تحويل أي رابط Appwrite Storage إلى /view الذي يقبل مفتاح API.
     * /preview يحتاج جلسة مستخدم (لا يقبل المفتاح ويُعيد 403).
     */
    public static String toDownloadUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (url.contains("/preview?")) return url.replace("/preview?", "/view?");
        if (url.contains("/preview&")) return url.replace("/preview&", "/view&");
        if (url.contains("/download?")) return url.replace("/download?", "/view?");
        if (url.contains("/download&")) return url.replace("/download&", "/view&");
        return url;
    }

    public static void load(Context context, ImageView imageView, String photoUrl, int placeholderRes) {
        if (imageView == null) return;

        if (photoUrl == null || photoUrl.isEmpty()) {
            PENDING.remove(imageView);
            imageView.setImageResource(placeholderRes);
            return;
        }

        final String downloadUrl = toDownloadUrl(photoUrl);
        PENDING.put(imageView, downloadUrl);
        imageView.setImageResource(placeholderRes);

        EXECUTOR.execute(() -> {
            Bitmap bitmap = downloadBitmap(downloadUrl);
            imageView.post(() -> {
                String pending = PENDING.get(imageView);
                if (pending == null || !pending.equals(downloadUrl)) return;
                if (bitmap != null) {
                    Log.d(TAG, "loaded image OK: " + downloadUrl);
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(placeholderRes);
                }
            });
        });
    }

    public static void clear(ImageView imageView) {
        if (imageView != null) {
            PENDING.remove(imageView);
        }
    }

    private static Bitmap downloadBitmap(String downloadUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Appwrite-Project", DALAppWriteConnection.PROJECT_ID);
            connection.setRequestProperty("X-Appwrite-Key", DALAppWriteConnection.API_KEY);
            connection.setRequestProperty("Accept", "image/*");
            connection.connect();

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP " + code + " for " + downloadUrl);
                return null;
            }

            try (InputStream input = connection.getInputStream()) {
                byte[] bytes = readAllBytes(input);
                if (bytes.length == 0) return null;
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } catch (Exception e) {
            Log.e(TAG, "download failed: " + downloadUrl, e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static byte[] readAllBytes(InputStream input) throws java.io.IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int n;
        while ((n = input.read(data)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }
}
