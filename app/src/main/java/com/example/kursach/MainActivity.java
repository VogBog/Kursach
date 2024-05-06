package com.example.kursach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kursach.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    static FirebaseAuth auth;
    static FirebaseDatabase database;
    static DatabaseReference users;

    private User currentUser;
    private ActivityResultLauncher<Intent> startLogInForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if(o.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = o.getData();
                        if(intent != null) {
                            User user = intent.getParcelableExtra("User");
                            setUser(user);
                        }
                        else {
                            finish();
                        }
                    }
                    else {
                        finish();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");


        Intent intent = new Intent(binding.getRoot().getContext(), LogInActivity.class);
        startLogInForResult.launch(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    public User getUser() {
        return currentUser;
    }

    public void setUser(User user) {
        currentUser = user;
    }
}