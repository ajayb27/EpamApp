package com.epam.app.application;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.epam.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyApplication extends Application {

    public static String nKey;
    String TAG = "csol_debug";

    private MyNotificationManager myNotificationManager;

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d("Ajayy","ok cooll");
        initializeFirebase();

        myNotificationManager = MyNotificationManager.getInstance(this);
        myNotificationManager.createNotification("FIRST", "NEW_DEALS", "Notifications on new deals");


        FirebaseMessaging.getInstance().subscribeToTopic("NEW_DEALS")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("TRIAL_NOTIFY")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                    }
                });

        FirebaseMessaging.getInstance().subscribeToTopic("ORDERS")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                    }
                });

    }

    public void initializeFirebase() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
//        Log.d("ajayy"," initialize firebase ");
//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                if (!task.isSuccessful()) {
//                    Log.i("firebasemer", "Task failed! " + task.getException());
//                    return;
//                }
//                Log.i("firebasemer", "Task successful! " + task.getResult().getToken());
//                nKey = task.getResult().getToken();
//
//            }
//        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
//                        String msg = getString(R.string.msg_token_fmt);
                        Log.d(TAG, token);
                        nKey = token;
                    }
                });

    }

    public void triggerNotification(Class targetNotificationActivity, String channelId, String title, String text, String bigText, boolean autoCancel, int notificationId, String url, Bitmap result) {
        myNotificationManager.triggerNotification(targetNotificationActivity, channelId, title, text, bigText, autoCancel, notificationId, url, result);
    }
}