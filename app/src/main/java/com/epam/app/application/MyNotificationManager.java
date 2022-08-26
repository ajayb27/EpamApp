package com.epam.app.application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.epam.app.R;


public class MyNotificationManager {

    private static MyNotificationManager myNotificationManager;
    private Context context;
    private NotificationManagerCompat notificationManagerCompat;

    private MyNotificationManager(Context context) {
        this.context = context;
        notificationManagerCompat = NotificationManagerCompat.from(context);
    }

    public static MyNotificationManager getInstance(Context context) {
        if(myNotificationManager==null)
            myNotificationManager = new MyNotificationManager(context);
        return myNotificationManager;
    }


    public void createNotification(String channelId,String channelName,String channelDescription) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            NotificationChannel notificationChannel = new NotificationChannel(channelId,channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.setShowBadge(true);
            notificationChannel.setSound(notificationSound,attributes);
            notificationManagerCompat.createNotificationChannel(notificationChannel);
        }
    }

    public void triggerNotification (Class targetNotificationActivity, String channelId, String title, String text, String bigText, boolean autoCancel, int notificationId, String url, Bitmap bitmap) {
        Intent intent = new Intent(context,targetNotificationActivity);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (url != null)
//            intent.putExtra("url",url);
            intent.setData(Uri.parse(url));
        Log.d("testUrl","testUrl manager : "+url);
        PendingIntent pendingIntent;
        if (url != null)
            pendingIntent = PendingIntent.getActivity(context,13,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        else
            pendingIntent = PendingIntent.getActivity(context,13,intent, 0);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int color = 0xff123456;
//        int color = getResources().getColor(R.color.my_notif_color);
//        int color = ContextCompat.getColor(context, R.color.my_notif_color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelId);
        builder.setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(autoCancel)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(notificationSound)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.mipmap.ic_logo_mvk_foreground);
            builder.setColor(color);
        } else {
            builder.setSmallIcon(R.mipmap.ic_logo_mvk_foreground);
        }

        Log.d("testImage","trigger notification : "+bitmap);
        notificationManagerCompat.notify(notificationId,builder.build());
    }
}
