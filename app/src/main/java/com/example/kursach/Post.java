package com.example.kursach;

import android.widget.LinearLayout;

import com.example.kursach.callbacks.CallbackArg;

import java.util.ArrayList;
import java.util.Objects;

public class Post {
    public String id;
    public User author;
    public String postName, postDescription;
    public int maxPlayers;
    public ArrayList<User> players = new ArrayList<>();
    ArrayList<String> initIds = new ArrayList<>();
    private int playersCount = 0;
    LinearLayout layout;

    public Post() {
        author = new User();
        players = new ArrayList<>();
        postName = "Post";
        postDescription = "Description";
    }

    public Post(String id, User author, String postName, String postDescription, int maxPlayers,
                ArrayList<User> players) {
        this.author = author;
        this.postName = postName;
        this.postDescription = postDescription;
        this.maxPlayers = maxPlayers;
        this.players = players;
        this.id = id;
    }

    public void setData(PostData data, CallbackArg<Post> getUserCallback) {
        id = data.id;
        postName = data.postName;
        postDescription = data.postDescription;
        maxPlayers = data.maxPlayers;
        author = new User();

        players.clear();
        data.players.removeIf(Objects::isNull);
        if(data.players != null) {
            initIds = data.players;
            playersCount = data.players.size();
        }

        MainActivity.getUsers().child(data.authorId).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            author = user;
            getUserCallback.callback(this);
        });
    }

    public int getPlayersCount() {
        return playersCount;
    }
}
