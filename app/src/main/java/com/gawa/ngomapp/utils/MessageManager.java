package com.gawa.ngomapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.gawa.ngomapp.MainActivity;

/**
 * This class will contain all our code to handle notifications
 */
public class MessageManager {

    Context context;
    int music_icon;
    public static final String CHANNEL_ID = "com.gawa.ngomapp.NOTIFICATIONS";
    private CharSequence bigText = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text.";

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
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder;
    }

    public NotificationCompat.Builder setUpNotifcationWithImage(String title, String text, Bitmap image){

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent
                        .getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(music_icon).setLargeIcon(image)
                .setContentTitle(title).setContentText(text)
                .setContentIntent(pendingIntent).setAutoCancel(true)
                .addAction(music_icon, "Pause", pendingIntent)
                .setStyle(new NotificationCompat.BigPictureStyle()
                .bigPicture(image).bigLargeIcon(null));

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
