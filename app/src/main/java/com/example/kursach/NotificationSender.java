package com.example.kursach;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
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
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> ids = new ArrayList<>();
                        ArrayList<NotificationData> notifications = new ArrayList<>();
                        for(DataSnapshot data : snapshot.getChildren()) {
                            NotificationData notification = data.getValue(NotificationData.class);
                            ids.add(data.getKey());
                            notifications.add(notification);
                        }
                        if(!notifications.isEmpty()) {
                            showNotifications(notifications);
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

    public void showNotifications(List<NotificationData> datas) {
        String id = "KURSACH_NOTIFICATION_CHANNEL_ID";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(main.getApplicationContext(), id);
        String title = datas.get(0).title;
        String description = datas.get(0).description;
        if(datas.size() > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for(NotificationData data : datas) {
                inboxStyle.addLine(data.description);
            }
            builder.setStyle(inboxStyle);
            title = "Новые уведомления от ВТаверне";
            description = "";
        }
        builder
                .setSmallIcon(R.drawable.approve)
                .setContentTitle(title)
                .setContentText(description)
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
                        title, importance);
                channel.enableVibration(true);
                manager.createNotificationChannel(channel);
            }
        }

        manager.notify(0, builder.build());

        for(NotificationData data : datas) {
            if(data.notificationData == 3) {
                main.setUser(new User());
                main.finish();
                break;
            }
        }
    }

    public void showNotification(NotificationData data) {
        ArrayList<NotificationData> datas = new ArrayList<>();
        datas.add(data);
        showNotifications(datas);
    }
}
