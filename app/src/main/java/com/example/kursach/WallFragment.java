package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.kursach.databinding.FragmentWallBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class WallFragment extends Fragment {

    private FragmentWallBinding binding;
    private PostAdapter postAdapter;
    private String lastItemKey = "None";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWallBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        Query query = MainActivity.getPosts();
        ArrayList<Post> posts = MainActivity.wall;
        postAdapter = new PostAdapter(binding.getRoot().getContext(), posts, false);
        postAdapter.setUserAdapterCallback(user -> {
            Intent intent = new Intent(getContext(), OtherPlayerProfileActivity.class);
            intent.putExtra(OtherPlayerProfileActivity.NAME, user.name);
            intent.putExtra(OtherPlayerProfileActivity.PHONE, user.phone);
            intent.putExtra(OtherPlayerProfileActivity.ID, user.id);
            startActivity(intent);
        });
        if(posts.isEmpty()) {

            postAdapter.setCallPostCallback(post -> {
                MainActivity.getPosts().child(post.id).get().addOnSuccessListener(snapshot -> {
                    PostData data = snapshot.getValue(PostData.class);
                    data.players.add(MainActivity.getAuth().getCurrentUser().getUid());
                    MainActivity.getPosts().child(post.id).setValue(data).addOnSuccessListener(d -> {
                        postAdapter.posts.remove(post);
                        postAdapter.notifyDataSetChanged();
                    });
                });
            });
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<PostData> datas = new ArrayList<>();
                    for (DataSnapshot item : snapshot.getChildren()) {
                        PostData data = item.getValue(PostData.class);
                        datas.add(data);
                    }
                    for (int i = datas.size() - 1; i >= 0; i--) {
                        if (datas.get(i).authorId.equals(MainActivity.getAuth().getCurrentUser().getUid())) {
                            continue;
                        }
                        boolean isContinue = false;
                        if (datas.get(i).players != null) {
                            for (String id : datas.get(i).players) {
                                if (id.equals(MainActivity.getAuth().getCurrentUser().getUid())) {
                                    isContinue = true;
                                    break;
                                }
                            }
                        }
                        if (isContinue) {
                            continue;
                        }

                        Post post = new Post();
                        post.setData(datas.get(i), totalPost -> {
                            postAdapter.add(totalPost);
                            postAdapter.notifyDataSetChanged();
                            MainActivity.wall.clear();
                            MainActivity.wall.addAll(postAdapter.posts);
                        });
                        lastItemKey = datas.get(i).id;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ERROR", "Get posts canceled: " + error.getMessage());
                }
            });
        }

        //View footerBtn = getLayoutInflater().inflate(R.layout.update_wall_button, null);
        //footerBtn.findViewById(R.id.reloadWallButton).setOnClickListener(e -> updateWall());
        //binding.mainList.addFooterView(footerBtn);
        binding.mainList.setAdapter(postAdapter);

        return view;
    }

    private void updateWall() {
        if(lastItemKey.equals("None")) {
            return;
        }

        Query query = MainActivity.getPosts().startAfter(lastItemKey).limitToFirst(20);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot item : snapshot.getChildren()) {
                    Post post = new Post();
                    PostData data = item.getValue(PostData.class);
                    post.setData(data, totalPost -> {
                        postAdapter.add(totalPost);
                        postAdapter.notifyDataSetChanged();
                    });

                    lastItemKey = data.id;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ERROR", "Get posts canceled: " + error.getMessage());
            }
        });
    }
}