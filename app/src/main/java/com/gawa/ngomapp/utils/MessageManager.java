package com.gawa.ngomapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * This class will contain all our code to handle notifications
 */
public class MessageManager {

    Context context;
    int music_icon;
    private static final String CHANNEL_ID = "com.gawa.ngomapp.NOTIFICATIONS";

    public MessageManager(Context context, int music_icon) {
        this.context = context;
        this.music_icon = music_icon;
    }

    public NotificationCompat.Builder setupNotification(String title, String text){
        //this method will be used to build a notification
        // with necessary details.

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(music_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder;
    }

    public void createNotificationChannel()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //This will only apply to Android 8 (API 26) and above.

            CharSequence name = "Ngomapp Notifications";
            String description = "Notifications from ngomapp";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);


            //Here we register the channel with the system.
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
    }
}
