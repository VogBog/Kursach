package com.example.kursach;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursach.databinding.ActivityAddPostBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {

    ActivityAddPostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backToProfileBtn.setOnClickListener(e -> finish());
        binding.uploadPostBtn.setOnClickListener(e -> uploadPost());
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

        MainActivity.getPosts().child(data.id).setValue(data).addOnSuccessListener(e -> {
            finish();
        });
    }
}