package com.hmit.sportmirrorapp;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

        private static final String CHANNEL_ID = "1234";
        private static final String CHANNEL_NAME = "TEST";

        @Override
        public void onNewToken(String token) {
                Log.d("FCM Log", "Refreshed token: " + token);
        }

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
                if(remoteMessage.getNotification() != null){
                        Log.d("FCM Log", "알림 메시지 : " + remoteMessage.getNotification().getBody());
                        /*
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                        NotificationCompat.Builder builder = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                                        notificationManager.createNotificationChannel(channel);
                                }
                                builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
                        }else {
                                builder = new NotificationCompat.Builder(getApplicationContext());
                        }

                        String title = remoteMessage.getNotification().getTitle();
                        String body = remoteMessage.getNotification().getBody();

                        builder.setContentTitle(title)
                                .setContentText(body)
                                .setSmallIcon(R.drawable.ic_launcher_background);

                        Notification notification = builder.build();
                        notificationManager.notify(1, notification);
                        */
                        String messageBody = remoteMessage.getNotification().getBody();
                        String messageTitle = remoteMessage.getNotification().getTitle();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                        String channelId = "Channel ID";
                        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(this, channelId)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle(messageTitle)
                                        .setContentText(messageBody)
                                        .setAutoCancel(true)
                                        .setSound(defaultSoundUri)
                                        .setContentIntent(pendingIntent);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String channelName = "Channel Name";
                                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                                notificationManager.createNotificationChannel(channel);
                        }
                        notificationManager.notify(0, notificationBuilder.build());
                }

        }
}