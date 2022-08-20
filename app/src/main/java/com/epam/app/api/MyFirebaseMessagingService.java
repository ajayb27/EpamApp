package com.epam.app.api;

import android.util.Log;


import com.epam.app.activities.MainActivity;
import com.epam.app.application.MyApplication;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String debug = "trialPush";
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i(debug,"New Token : "+s);
        MyApplication.nKey=s;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(debug,"Message Received");
        String url = remoteMessage.getData().get("key1");
        Log.d("testData","data : "+url);
        ((MyApplication)getApplication()).triggerNotification(MainActivity.class,"FIRST",remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getBody(),true,14, url);

    }


}
