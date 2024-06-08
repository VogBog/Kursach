package com.example.kursach;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorkManager extends Worker {
    private final NotificationSender sender;

    public NotificationWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sender = new NotificationSender(MainActivity.getInstance());
    }

    @NonNull
    @Override
    public Result doWork() {
        sender.checkNotifications();
        return Result.retry();
    }
}
