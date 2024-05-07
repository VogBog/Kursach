package com.example.kursach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.kursach.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private static FirebaseAuth auth;
    private static FirebaseDatabase database;
    private static DatabaseReference users;

    private User currentUser;
    private WallFragment wallFragment;

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

    public static FirebaseAuth getAuth() {
        if(auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public static FirebaseDatabase getDatabase() {
        if(database == null) {
            database = FirebaseDatabase.getInstance();
        }
        return database;
    }

    public static DatabaseReference getUsers() {
        if(users == null) {
            users = database.getReference("Users");
        }
        return users;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");

        wallFragment = new WallFragment();

        binding.homeBtn.setOnClickListener(e -> openPage(0));
        binding.messagesBtn.setOnClickListener(e -> openPage(1));
        binding.userBtn.setOnClickListener(e -> openPage(2));
        binding.settingsBtn.setOnClickListener(e -> openPage(3));

        Intent intent = new Intent(binding.getRoot().getContext(), LogInActivity.class);
        startLogInForResult.launch(intent);
    }

    public void openPage(int index) {
        Fragment[] fragments = new Fragment[] {wallFragment, null, null, null};
        if(index >= 0 && index < fragments.length && fragments[index] != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(binding.mainFrame.getId(), fragments[index]);
            transaction.commit();
        }
    }

    public User getUser() {
        return currentUser;
    }

    public void setUser(User user) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(binding.mainFrame.getChildCount() == 0) {
            transaction.add(binding.mainFrame.getId(), wallFragment);
        }
        else {
            transaction.replace(binding.mainFrame.getId(), wallFragment);
        }
        transaction.commit();
        currentUser = user;
    }
}