package com.example.kursach;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.kursach.callbacks.Callback;
import com.example.kursach.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private static FirebaseAuth auth;
    private static FirebaseDatabase database;
    private static DatabaseReference users;
    private static DatabaseReference posts;
    private static DatabaseReference notifications;
    private WorkManager workManager = WorkManager.getInstance(MainActivity.this);

    private static User currentUser;
    public static Bitmap userAvatar;
    private WallFragment wallFragment;
    private ProfileFragment profileFragment;
    private SettingsFragment settingsFragment;
    private MyGamesFragment myGamesFragment;
    public static final ArrayList<Post> myPosts = new ArrayList<>();
    public static final ArrayList<Post> wall = new ArrayList<>();
    public static final ArrayList<Post> subscribedWalls = new ArrayList<>();

    private static MainActivity instance;

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
            users = getDatabase().getReference("Users");
        }
        return users;
    }

    public static DatabaseReference getPosts() {
        if(posts == null) {
            posts = getDatabase().getReference("Posts");
        }
        return posts;
    }

    public static DatabaseReference getNotifications() {
        if(notifications == null) {
            notifications = getDatabase().getReference("Notifications");
        }
        return notifications;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");

        wallFragment = new WallFragment();
        profileFragment = new ProfileFragment();
        settingsFragment = new SettingsFragment();
        myGamesFragment = new MyGamesFragment();
        settingsFragment.setQuitPressedCallback(() -> {
            Intent intent = new Intent(binding.getRoot().getContext(), LogInActivity.class);
            intent.putExtra("IsClear", true);
            startLogInForResult.launch(intent);
        });

        binding.homeBtn.setOnClickListener(e -> openPage(0));
        binding.messagesBtn.setOnClickListener(e -> openPage(1));
        binding.userBtn.setOnClickListener(e -> openPage(2));
        binding.settingsBtn.setOnClickListener(e -> openPage(3));

        Intent intent = new Intent(binding.getRoot().getContext(), LogInActivity.class);
        startLogInForResult.launch(intent);

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificationWorkManager.class, 10, TimeUnit.SECONDS
        ).build();

        workManager.enqueue(workRequest);
    }

    public void openPage(int index) {
        Fragment[] fragments = new Fragment[] {wallFragment, myGamesFragment, profileFragment, settingsFragment};
        if(index >= 0 && index < fragments.length && fragments[index] != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(binding.mainFrame.getId(), fragments[index]);
            transaction.commit();
        }
    }

    public static User getUser() {
        if(currentUser == null)
            return new User();
        return currentUser;
    }

    public static void setUserStatic(User user, Context context) {
        wall.clear();
        myPosts.clear();
        subscribedWalls.clear();

        currentUser = user;
        userAvatar = null;
        GetImageFromServer.getAvatar(context, user.id, bitmap -> userAvatar = bitmap);

        NotificationSender sender = new NotificationSender(MainActivity.getInstance());
        sender.checkNotifications();
    }

    public void setUser(User user) {
        setUserStatic(user, this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(binding.mainFrame.getChildCount() == 0) {
            transaction.add(binding.mainFrame.getId(), wallFragment);
        }
        else {
            transaction.replace(binding.mainFrame.getId(), wallFragment);
        }
        transaction.commit();
    }

    public void startOKAnimation() {
        startOKAnimation(null);
    }

    public void startOKAnimation(Callback afterAnimation) {
        getLayoutInflater().inflate(R.layout.ok_anim, binding.animView);
        final View view = binding.animView.getChildAt(0);
        final Handler handler = new Handler(Looper.getMainLooper());
        final Context context = this;
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.ok_anim));
        handler.postDelayed(() -> {
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.ok_anim_2));
        }, 1400);
        handler.postDelayed(() -> {
            binding.animView.removeView(view);
            if(afterAnimation != null) {
                afterAnimation.callback();
            }
        }, 2000);
    }
}