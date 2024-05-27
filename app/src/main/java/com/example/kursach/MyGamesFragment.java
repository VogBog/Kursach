package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kursach.databinding.FragmentMyGamesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyGamesFragment extends Fragment {
    FragmentMyGamesBinding binding;
    PostAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyGamesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ArrayList<Post> posts = MainActivity.subscribedWalls;
        Query query = MainActivity.getPosts();
        adapter = new PostAdapter(binding.getRoot().getContext(), posts, true);
        adapter.setRemovePostCallback(post -> {
            AreYouSureDialog dialog = new AreYouSureDialog();
            dialog.onAnswer = bool -> {
                if(bool) {
                    MainActivity.getPosts().child(post.id + "/players")
                            .get().addOnSuccessListener(data -> {
                                String uid = MainActivity.getUser().id;
                                int i = 0;
                                for(DataSnapshot d : data.getChildren()) {
                                    String val = d.getValue(String.class);
                                    if(val.equals(uid)) {
                                        MainActivity.getPosts().child(post.id + "/players/" + i)
                                                .removeValue()
                                                .addOnSuccessListener(e -> {
                                                    MainActivity.subscribedWalls.remove(post);
                                                    if(adapter.posts.contains(post)) {
                                                        adapter.posts.remove(post);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });
                                    }
                                    i++;
                                }
                            });
                }
            };
            dialog.show(getActivity().getSupportFragmentManager(), "custom");
        });
        adapter.setUserAdapterCallback(this::openUserProfile);
        binding.mainList.setAdapter(adapter);

        if(posts.isEmpty()) {
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<PostData> datas = new ArrayList<>();
                    for (DataSnapshot item : snapshot.getChildren()) {
                        PostData data = item.getValue(PostData.class);
                        datas.add(data);
                    }
                    String uid = MainActivity.getAuth().getCurrentUser().getUid();
                    for (int i = datas.size() - 1; i >= 0; i--) {
                        if (datas.get(i).authorId.equals(uid)) {
                            continue;
                        }
                        if(datas.get(i).players == null) {
                            continue;
                        }
                        if(!datas.get(i).players.contains(uid)) {
                            continue;
                        }

                        Post post = new Post();
                        post.setData(datas.get(i), totalPost -> {
                            adapter.add(totalPost);
                            adapter.notifyDataSetChanged();
                            MainActivity.subscribedWalls.clear();
                            MainActivity.subscribedWalls.addAll(adapter.posts);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ERROR", "Get posts canceled: " + error.getMessage());
                }
            });
        }

        return view;
    }

    private void openUserProfile(User user) {
        Intent intent = new Intent(getContext(), OtherPlayerProfileActivity.class);
        intent.putExtra(OtherPlayerProfileActivity.NAME, user.name);
        intent.putExtra(OtherPlayerProfileActivity.PHONE, user.phone);
        intent.putExtra(OtherPlayerProfileActivity.ID, user.id);
        intent.putExtra(OtherPlayerProfileActivity.DESCRIPTION, user.description);
        startActivity(intent);
    }
}