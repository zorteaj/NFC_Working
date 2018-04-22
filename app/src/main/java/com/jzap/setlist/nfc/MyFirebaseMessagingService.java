package com.jzap.setlist.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;

/**
 * Created by JZ_W541 on 3/29/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "JAZ_NFC";
    private static final String CHANNEL_ID = "JZ_NFC_CHANNEL";
    static int notificationId = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.

        Intent intent = new Intent(this, ContactActivity.class);
        intent.setAction("CONTACT_REQUEST");
        intent.putExtra("REQUESTOR", remoteMessage.getData().get("REQUESTOR"));
        // TODO: These are probably not the flags I want
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int dummyuniqueInt = new Random().nextInt(543254);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, dummyuniqueInt, intent, FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.greetlogo)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId++, mBuilder.build());
    }
}