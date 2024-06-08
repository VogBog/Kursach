package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursach.databinding.ActivityAddPostBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AddPostActivity extends AppCompatActivity {

    ActivityAddPostBinding binding;
    private String editPostID = "None";
    private ArrayList<String> players;
    public static final String EDIT_POST_ID = "EDIT_POST_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if(intent != null) {
            String id = intent.getStringExtra(EDIT_POST_ID);
            if(id != null && !id.isEmpty()) {
                editPostID = id;
                Post res = null;
                for(Post post : MainActivity.myPosts) {
                    if(post.id.equals(id)) {
                        res = post;
                        break;
                    }
                }
                if(res == null) {
                    editPostID = "None";
                    Toast.makeText(this, "Что-то пошло не так.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                setInfo(res);
            }
        }

        binding.backToProfileBtn.setOnClickListener(e -> finish());
        binding.uploadPostBtn.setOnClickListener(e -> uploadPost());
    }

    private void setInfo(Post post) {
        binding.nameInput.setText(post.postName);
        binding.descriptionInput.setText(post.postDescription);
        binding.inputPlayerCount.setText(String.valueOf(post.maxPlayers));
        players = post.initIds;
    }

    private void uploadPost() {
        String name = binding.nameInput.getText().toString();
        String description = binding.descriptionInput.getText().toString();
        String playerCountTxt = binding.inputPlayerCount.getText().toString();

        if(name.isEmpty() || description.isEmpty() || name.length() > 50) {
            return;
        }

        int maxPlayers = 10;
        if(!playerCountTxt.isEmpty()) {
            try {
                maxPlayers = Integer.parseInt(playerCountTxt);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        PostData data = new PostData();
        data.authorId = MainActivity.getAuth().getCurrentUser().getUid();
        data.postDescription = description;
        data.postName = name;
        data.maxPlayers = maxPlayers;
        data.players = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(Calendar.getInstance().getTime());
        data.id = MainActivity.getUser().id + "-" + timeStamp;
        if(editPostID != null && !editPostID.isEmpty() && !editPostID.equals("None")) {
            data.id = editPostID;
            data.players = players;
        }

        MainActivity.getPosts().child(data.id).setValue(data).addOnSuccessListener(e -> {
            MainActivity.getInstance().startOKAnimation();
            finish();
        });
    }
}