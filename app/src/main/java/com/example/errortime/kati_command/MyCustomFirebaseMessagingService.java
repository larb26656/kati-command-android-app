package com.example.errortime.kati_command;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyCustomFirebaseMessagingService extends FirebaseMessagingService {
    Random random = new Random();
    int m = random.nextInt(9999 - 1000) + 1000;
    private NotificationChannel mChannel;
    private NotificationManager notifManager;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        Map<String, String> data = remoteMessage.getData();
        HashMap<String, String> message_map = null;
        if(data.get("Title").equals("<Message>")){
            message_map = covert_notification_message(data.get("Title"),data.get("Body"));
        }
        else if(data.get("Title").equals("<Pill>")) {
            message_map = covert_notification_message(data.get("Title"),data.get("Body_"+getString(R.string.lang_label)));
        }
        else if(data.get("Title").equals("<Behavior>")){
            message_map = covert_notification_message(data.get("Title"),data.get("Body_"+getString(R.string.lang_label)));
        }
        else{
            message_map = covert_notification_message("???","???");
        }
        String title = message_map.get("title");
        String body = message_map.get("body");
        sendNotification(title,body);
    }
    private HashMap<String, String> covert_notification_message(String title, String body) {
        HashMap<String, String> map = new HashMap<String, String>();
        String covert_title;
        if (title.equals("<Message>")) {
            covert_title = title.replace("<Message>", getString(R.string.message_title_label));
        } else if (title.equals("<Pill>")) {
            covert_title = title.replace("<Pill>", getString(R.string.pill_title_label));
        } else if (title.equals("<Behavior>")) {
            covert_title = title.replace("<Behavior>", getString(R.string.behavior_title_label));
        } else{
            covert_title = "???";
        }
        map.put("title", covert_title);
        map.put("body", body);
        return  map;
    }
    private void sendNotification(String title,String body){
        long[] v = {500,1000};
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService
                    (Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder;
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            if (mChannel == null) {
                mChannel = new NotificationChannel
                        ("0", title, importance);
                mChannel.setDescription(body);
                mChannel.enableVibration(true);
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, "0");

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 1251, intent, PendingIntent.FLAG_ONE_SHOT);
            builder
                    .setContentTitle(title) // title for notification
                    .setContentText(body) // message for notification
                    .setAutoCancel(true) // clear notification after click
                    .setSound(Uri.parse("android.resource://com.example.errortime.kati_read/"+R.raw.notification))
                    .setVibrate(new long[]{0, 1000, 1000, 1000, 1000})
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setSmallIcon(R.mipmap.ic_launcher);
            Notification notification = builder.build();
            notifManager.notify(m, notification);
        }
        else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(title) // title for notification
                    .setContentText(body) // message for notification
                    .setAutoCancel(true) // clear notification after click
                    .setSound(Uri.parse("android.resource://com.example.errortime.kati_read/"+R.raw.notification))
                    .setVibrate(new long[]{0, 1000, 1000, 1000, 1000})
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setSmallIcon(R.mipmap.ic_launcher);
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(pi);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(m, mBuilder.build());
        }
    }
}