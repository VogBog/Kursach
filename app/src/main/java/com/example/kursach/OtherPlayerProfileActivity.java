package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursach.databinding.ActivityOtherPlayerProfileBinding;

public class OtherPlayerProfileActivity extends AppCompatActivity {

    ActivityOtherPlayerProfileBinding binding;

    public static final String NAME = "NAME";
    public static final String PHONE = "PHONE";
    public static final String ID = "ID";
    public static final String DESCRIPTION = "DESCRIPTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtherPlayerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(e -> {
            finish();
        });

        Intent intent = getIntent();
        if(intent != null) {
            String name = intent.getStringExtra(NAME);
            String phone = intent.getStringExtra(PHONE);
            String id = intent.getStringExtra(ID);
            String description = intent.getStringExtra(DESCRIPTION);
            binding.avatarImg.setImageResource(R.drawable.user);
            if(id != null) {
                GetImageFromServer.getAvatar(this, id, binding.avatarImg::setImageBitmap);
            }
            binding.phoneNumber.setText(phone);
            binding.playerName.setText(name);
            if(description != null) {
                binding.profileDescription.setText(description);
            }
        }
    }
}