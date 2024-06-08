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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
        postAdapter = new PostAdapter(binding.getRoot().getContext(), posts, MainActivity.getUser().isAdmin, false);
        postAdapter.setUserAdapterCallback(this::openUserProfile);
        if(MainActivity.getUser().isAdmin) {
            postAdapter.setRemovePostCallback(post -> {
                AreYouSureDialog dialog = new AreYouSureDialog();
                dialog.onAnswer = bool -> {
                    if(bool) {
                        MainActivity.getPosts().child(post.id).removeValue().addOnSuccessListener(e -> {
                            postAdapter.posts.remove(post);
                            postAdapter.notifyDataSetChanged();
                        });
                    }
                };
                dialog.show(getActivity().getSupportFragmentManager(), "custom");
            });
        }
        postAdapter.setCallPostCallback(post -> {
            MainActivity.getPosts().child(post.id).get().addOnSuccessListener(snapshot -> {
                PostData data = snapshot.getValue(PostData.class);
                data.players.add(MainActivity.getAuth().getCurrentUser().getUid());
                MainActivity.getPosts().child(post.id).setValue(data).addOnSuccessListener(d -> {
                    postAdapter.posts.remove(post);
                    postAdapter.notifyDataSetChanged();
                });
                MainActivity.getInstance().startOKAnimation(() -> openUserProfile(post.author));
                if(getActivity() instanceof MainActivity) {
                    NotificationSender sender = new NotificationSender((MainActivity) getActivity());
                    sender.sendNotification(post.author.id, new NotificationData(
                            "Новый игрок!",
                            "На вашу игру " + post.postName + " откликнулся " + MainActivity.getUser().name,
                            1
                    ));
                }
            });
        });

        if(posts.isEmpty()) {
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
                        if(datas.get(i).players.size() >= datas.get(i).maxPlayers) {
                            continue;
                        }
                        boolean isContinue = false;
                        if (datas.get(i).players != null) {
                            for (String id : datas.get(i).players) {
                                if(id == null) {
                                    continue;
                                }
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

        binding.mainList.setAdapter(postAdapter);

        binding.searchBtn.setOnClickListener(v -> searchPosts(binding.inputSearchKeys.getText().toString()));

        return view;
    }

    private void searchPosts(String filter) {
        postAdapter.posts.clear();
        if(filter.isEmpty()) {
            postAdapter.posts.addAll(MainActivity.wall);
        }
        else {
            for(Post post : MainActivity.wall) {
                if(post.author.name.toLowerCase().contains(filter.toLowerCase()) ||
                post.postName.toLowerCase().contains(filter.toLowerCase()) ||
                post.postDescription.toLowerCase().contains(filter.toLowerCase())) {
                    postAdapter.add(post);
                }
            }
        }
        postAdapter.notifyDataSetChanged();
    }

    private void openUserProfile(User user) {
        Intent intent = new Intent(getContext(), OtherPlayerProfileActivity.class);
        intent.putExtra(OtherPlayerProfileActivity.NAME, user.name);
        intent.putExtra(OtherPlayerProfileActivity.PHONE, user.phone);
        intent.putExtra(OtherPlayerProfileActivity.ID, user.id);
        intent.putExtra(OtherPlayerProfileActivity.DESCRIPTION, user.description);
        startActivity(intent);
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