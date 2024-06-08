package com.example.kursach;

public class NotificationData {
    public String title;
    public String description;
    public int notificationData; //0 - default, 1 - new player, 2 - remove player, 3 - ban

    public NotificationData() {
        title = "";
        description = "";
        notificationData = 0;
    }

    public NotificationData(String title, String description, int notificationData) {
        this.title = title;
        this.description = description;
        this.notificationData = notificationData;
    }
}
