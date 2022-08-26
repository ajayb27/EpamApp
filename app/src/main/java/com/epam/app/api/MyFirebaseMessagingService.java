package com.epam.app.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;


import com.epam.app.activities.MainActivity;
import com.epam.app.application.MyApplication;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String debug = "trialPush";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i(debug, "New Token : " + s);
        MyApplication.nKey = s;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(debug, "Message Received");
//        String url = remoteMessage.getData().get("key1");
        String url1 = String.valueOf(remoteMessage.getNotification().getLink());
//        Log.d("testData", "data : " + url + ",\t notification : " + url1);
        Log.d("testImage","image url : "+remoteMessage.getNotification().getImageUrl());
        if (remoteMessage.getNotification().getImageUrl() == null) {
            ((MyApplication) getApplication()).triggerNotification(MainActivity.class, "FIRST", remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), "one more step:\n"+remoteMessage.getNotification().getBody(), true, 14, url1, null);
        }
        else {
//            RemoteMessage[] remoteMessages = new RemoteMessage[1];
//            remoteMessages[0] = remoteMessage;
//            SendNotification notification = new SendNotification(getApplicationContext());
//            notification.execute(remoteMessage);

            InputStream in;

            try {
                URL url = new URL(remoteMessage.getNotification().getImageUrl().toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap result = BitmapFactory.decodeStream(in);
                Log.d("testImage","bitmap : "+result);
                ((MyApplication) getApplication()).triggerNotification(MainActivity.class, "FIRST", remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), "one more step:\n"+remoteMessage.getNotification().getBody(), true, 14, url1,result);
            } catch (MalformedURLException e) {
                Log.d("testImage","exception : "+e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("testImage","exception : "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //    class SendNotification extends AsyncTask<RemoteMessage, Void, Bitmap> {
//
//        Context ctx;
//        RemoteMessage remoteMessage;
//
//        public SendNotification(Context context) {
//            super();
//            this.ctx = context;
//        }
//
//        @Override
//        protected Bitmap doInBackground(RemoteMessage... params) {
//
//            InputStream in;
//
//            try {
//                remoteMessage = params[0];
//                URL url = new URL(remoteMessage.getNotification().getImageUrl().toString());
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setDoInput(true);
//                connection.connect();
//                in = connection.getInputStream();
//                Bitmap myBitmap = BitmapFactory.decodeStream(in);
//                return myBitmap;
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap result) {
//
//            super.onPostExecute(result);
//            try {
//                Log.d("testImage", "Downloaded.."+result);
//                String url1 = String.valueOf(remoteMessage.getNotification().getLink());
//                ((MyApplication) getApplication()).triggerNotification(MainActivity.class, "FIRST", remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getBody(), true, 14, url1,result);
//            } catch (Exception e) {
//                Log.d("testImage", "Download failed.."+e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }
}
