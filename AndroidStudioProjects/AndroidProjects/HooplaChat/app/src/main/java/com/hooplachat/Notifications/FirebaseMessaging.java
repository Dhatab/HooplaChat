package com.hooplachat.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hooplachat.MainActivity;
import com.hooplachat.Messages.MessageActivity;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class FirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        reference.child(user.getUid()).setValue(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //get Shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String  savedCurrentUser = sharedPreferences.getString("Current_USERID", "None");
        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && sent.equals(firebaseUser.getUid())){
            if (!savedCurrentUser.equals(user)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendOAndAboveNotification(remoteMessage);
                }
            } else {
                sendNormalNotification(remoteMessage);
            }
        }

    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String body = remoteMessage.getData().get("body");
        String title = remoteMessage.getData().get("title");
        String icon = remoteMessage.getData().get("icon");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i > 0){
            j = i;
        }
        notificationManager.notify(j,builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String body = remoteMessage.getData().get("body");
        String title = remoteMessage.getData().get("title");
        String icon = remoteMessage.getData().get("icon");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification oreoAndAboveNotification = new OreoAndAboveNotification(this);
        Notification.Builder builder = oreoAndAboveNotification.getONotifications(title, body, pendingIntent,defSoundUri, icon);

        int j = 0;
        if (i > 0){
            j = i;
        }
        oreoAndAboveNotification.getManager().notify(j, builder.build());
    }
}
