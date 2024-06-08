package com.example.kursach;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.UUID;

public class NotificationSender {
    private final MainActivity main;

    public NotificationSender(MainActivity mainActivity) {
        main = mainActivity;
    }

    public void sendNotification(String handlerUserId, NotificationData data) {
        MainActivity.getNotifications()
                .child(handlerUserId)
                .child(String.valueOf(UUID.randomUUID()) + "_" + data.title)
                .setValue(data);
    }

    public void checkNotifications() {
        if(MainActivity.getUser() == null || MainActivity.getUser().isUserEmpty()) {
            return;
        }

        MainActivity.getNotifications()
                .child(MainActivity.getUser().id)
                .limitToFirst(5)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> ids = new ArrayList<>();
                        for(DataSnapshot data : snapshot.getChildren()) {
                            NotificationData notification = data.getValue(NotificationData.class);
                            ids.add(data.getKey());
                            showNotification(notification);
                        }

                        for(String id : ids) {
                            MainActivity.getNotifications().child(MainActivity.getUser().id)
                                    .child(id).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(main.getApplicationContext(),
                                "При попытке получить уведомления что-то пошло не так.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void showNotification(NotificationData data) {
        String id = "KURSACH_NOTIFICATION_CHANNEL_ID";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(main.getApplicationContext(), id);
        builder
                .setSmallIcon(R.drawable.approve)
                .setContentTitle(data.title)
                .setContentText(data.description)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(main.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(main.getApplicationContext(),
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) main.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    manager.getNotificationChannel(id);
            if(channel == null) {
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                channel = new NotificationChannel(id,
                        data.title, importance);
                channel.enableVibration(true);
                manager.createNotificationChannel(channel);
            }
        }

        manager.notify(0, builder.build());
     }
}
