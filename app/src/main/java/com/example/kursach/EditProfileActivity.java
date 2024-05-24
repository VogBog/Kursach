package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kursach.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    ActivityEditProfileBinding binding;

    public static final String NAME = "NAME";
    public static final String PHONE = "PHONE";
    public static final String DESCRIPTION = "DESCRIPTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = MainActivity.getUser().name;
        String phone = MainActivity.getUser().phone;
        String description = MainActivity.getUser().description;

        if(name != null && phone != null & description != null) {
            binding.inputProfileName.setText(name);
            binding.inputProfileDescription.setText(description);
            binding.inputProfilePhone.setText(phone);
        }

        binding.backBtn.setOnClickListener(v -> {
            Intent finalIntent = new Intent();
            finalIntent.putExtra(NAME, binding.inputProfileName.getText().toString());
            finalIntent.putExtra(PHONE, binding.inputProfilePhone.getText().toString());
            finalIntent.putExtra(DESCRIPTION, binding.inputProfileDescription.getText().toString());
            setResult(RESULT_OK, finalIntent);
            finish();
        });
    }
}