package com.example.kursach;

public class Post {
    public User author;
    public String postName, postDescription;
    public int maxPlayers, totalPlayers, avatar;

    public Post() {
        author = new User();
        avatar = R.drawable.user;
        postName = "Post";
        postDescription = "Description";
    }

    public Post(User author, String postName, String postDescription, int maxPlayers,
                int totalPlayers, int avatar) {
        this.author = author;
        this.postName = postName;
        this.postDescription = postDescription;
        this.maxPlayers = maxPlayers;
        this.totalPlayers = totalPlayers;
        this.avatar = avatar;
    }
}
