package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kursach.databinding.ActivityOtherPlayerProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
            final String id = intent.getStringExtra(ID);
            String description = intent.getStringExtra(DESCRIPTION);
            binding.avatarImg.setImageResource(R.drawable.user);
            if(id != null) {
                GetImageFromServer.getAvatar(this, id, binding.avatarImg::setImageBitmap);
                if(MainActivity.getUser().isAdmin) {
                    binding.removeProfileBtn.setVisibility(View.VISIBLE);
                    binding.removeProfileBtn.setOnClickListener(v -> {
                        AreYouSureDialog dialog = new AreYouSureDialog();
                        dialog.onAnswer = bool -> {
                            if(bool) {
                                removeUser(id);
                            }
                        };
                        dialog.show(getSupportFragmentManager(), "custom");
                    });
                }
            }
            binding.phoneNumber.setText(phone);
            binding.playerName.setText(name);
            if(description != null) {
                binding.profileDescription.setText(description);
            }
        }
    }

    public void removeUser(String userId) {
        MainActivity.getUsers().child(userId).get().addOnSuccessListener(data -> {
            User user = data.getValue(User.class);
            removeUser(user);
        });
    }

    public void removeUser(User user) {
        if(user.isAdmin) {
            Toast.makeText(this, "Вы не можете забанить администратора", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        MainActivity.getUsers().child(user.id).removeValue();
        MainActivity.getPosts().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> postsToRemove = new ArrayList<>();
                for(DataSnapshot data : snapshot.getChildren()) {
                    PostData postData = data.getValue(PostData.class);
                    if(postData.authorId.equals(user.id)) {
                        postsToRemove.add(postData.id);
                    }
                }
                for(String id : postsToRemove) {
                    MainActivity.getPosts().child(id).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getBaseContext(),
                        "Что-то пошло не так. Придётся удалить пользователя вручную.",
                        Toast.LENGTH_LONG).show();
            }
        });
        NotificationSender sender = new NotificationSender(MainActivity.getInstance());
        sender.sendNotification(user.id, new NotificationData(
                "Вас забанили",
                "Администратор " + MainActivity.getUser().name + " забанил Вас за нарушение правил приложения.",
                3
        ));
    }
}