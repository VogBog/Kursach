package com.example.kursach;

import com.example.kursach.callbacks.CallbackArg;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.UUID;

public class Post {
    public String id;
    public User author;
    public String postName, postDescription;
    public int maxPlayers;
    public ArrayList<User> players;

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

        MainActivity.getUsers().child(data.authorId).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            author = user;
            getUserCallback.callback(this);
        });
    }
}