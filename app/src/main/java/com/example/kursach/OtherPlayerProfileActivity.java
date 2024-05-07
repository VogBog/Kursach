package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kursach.databinding.ActivityOtherPlayerProfileBinding;

public class OtherPlayerProfileActivity extends AppCompatActivity {

    ActivityOtherPlayerProfileBinding binding;

    public static final String NAME = "NAME";
    public static final String PHONE = "PHONE";

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
            binding.phoneNumber.setText(phone);
            binding.playerName.setText(name);
        }
    }
}