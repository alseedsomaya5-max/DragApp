package com.example.dragapp.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

public final class NotificationHelper {

    public static final String CHANNEL_ID = "dragapp_reminders";

    private NotificationHelper() {
    }

    /**
     * Creates the notification channel for Android O (API 26) and above.
     * No-op on older versions.
     */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelInternal(context);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static void createChannelInternal(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Medication and reminder notifications");
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
    }
}
