package com.example.kursach;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kursach.databinding.FragmentProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;

    private PostAdapter postAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> getImageFromDevice;
    private ActivityResultLauncher<Intent> writeNewPost;
    private ActivityResultLauncher<Intent> editProfile;
    private ImageView avatarImg;
    private View profileHeader;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        writeNewPost = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), res -> {
                    updatePostsContent();
                }
        );

        getImageFromDevice = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if(uri == null) {
                        return;
                    }
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                getContext().getContentResolver(), uri);

                        changeAvatar(bitmap);

                    } catch (Exception e) {
                        Toast toast = Toast.makeText(
                                getContext(),
                                "Не получилось взять изображение",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        editProfile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), res -> {
                    if(res.getResultCode() == Activity.RESULT_OK) {
                        Intent data = res.getData();
                        String name = data.getStringExtra(EditProfileActivity.NAME);
                        String phone = data.getStringExtra(EditProfileActivity.PHONE);
                        String description = data.getStringExtra(EditProfileActivity.DESCRIPTION);
                        if(name != null && phone != null && description != null) {
                            if(name.isEmpty() || name.length() > 40 ||
                                    phone.isEmpty() || phone.length() > 13 || phone.length() < 10) {
                                return;
                            }
                            User user = MainActivity.getUser();
                            user.name = name;
                            user.description = description;
                            user.phone = phone;

                            MainActivity.getUsers().child(user.id).setValue(user)
                                    .addOnFailureListener(f -> {
                                        Toast.makeText(getContext(),
                                                "Что-то пошло не так!",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnSuccessListener(s -> {
                                        MainActivity.setUserStatic(user, getContext());
                                        updateProfileInfo();
                                    });
                        }
                    }
                }
        );

        ArrayList<Post> posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, true);
        postAdapter.setUserAdapterCallback(user -> {
            Intent intent = new Intent(getContext(), OtherPlayerProfileActivity.class);
            intent.putExtra(OtherPlayerProfileActivity.NAME, user.name);
            intent.putExtra(OtherPlayerProfileActivity.PHONE, user.phone);
            intent.putExtra(OtherPlayerProfileActivity.ID, user.id);
            intent.putExtra(OtherPlayerProfileActivity.DESCRIPTION, user.description);
            startActivity(intent);
        });
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
        postAdapter.setRemoveUserCallback((post, user) -> {
            MainActivity.getPosts().child(post.id + "/players")
                    .get().addOnSuccessListener(data -> {
                        String uid = user.id;
                        int i = 0;
                        for(DataSnapshot d : data.getChildren()) {
                            String val = d.getValue(String.class);
                            if(val.equals(uid)) {
                                Log.d("POST ID", post.id + "/players/" + i);
                                MainActivity.getPosts().child(post.id + "/players/" + i)
                                        .removeValue()
                                        .addOnSuccessListener(e -> {
                                            updatePostsContent();
                                        });
                            }
                            i++;
                        }
                    });
        });

        profileHeader = getLayoutInflater().inflate(R.layout.profile_header, null);
        updateProfileInfo();

        final ImageView image = profileHeader.findViewById(R.id.avatarImg);
        avatarImg = image;
        if(MainActivity.userAvatar == null)
            image.setImageResource(GetImageFromServer.getDefaultAvatarId());
        else
            image.setImageBitmap(MainActivity.userAvatar);
        binding.mainList.addHeaderView(profileHeader);
        binding.mainList.setAdapter(postAdapter);
        setAdapterFromMyUsers();

        return view;
    }

    private void updateProfileInfo() {
        profileHeader.findViewById(R.id.addPost).setOnClickListener(e -> addNewPost());

        ((TextView) profileHeader.findViewById(R.id.userName)).setText(MainActivity.getUser().name);
        ((TextView) profileHeader.findViewById(R.id.profileDescription)).setText(MainActivity.getUser().description);
        profileHeader.findViewById(R.id.avatarBtn).setOnClickListener(e -> wantToChangeAvatar());
        profileHeader.findViewById(R.id.profileSettings).setOnClickListener(e -> openProfileSettings());
    }

    private void openProfileSettings() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        editProfile.launch(intent);
    }

    private void updatePostsContent() {
        Query query = MainActivity.getPosts().orderByChild("authorId")
                .equalTo(MainActivity.getAuth().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updatePostsContent(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ERROR", "Posts canceled: " + error.getMessage());
            }
        });
    }

    private void setAdapterFromMyUsers() {
        ArrayList<Post> datas = MainActivity.myPosts;
        if(datas.isEmpty()) {
            updatePostsContent();
            return;
        }

        postAdapter.posts.clear();
        postAdapter.posts.addAll(datas);
    }

    private void updatePostsContent(@NonNull DataSnapshot snapshot) {
        ArrayList<PostData> datas = new ArrayList<>();
        for(DataSnapshot item : snapshot.getChildren()) {
            PostData data = item.getValue(PostData.class);
            datas.add(data);
        }
        if(datas.size() == 0)
            return;

        postAdapter.posts.clear();
        MainActivity.myPosts.clear();
        final String lastId = datas.get(0).id;
        for(int i = datas.size() - 1; i >= 0; i--) {
            Post post = new Post();
            post.setData(datas.get(i), totalPost -> {
                postAdapter.posts.add(totalPost);
                MainActivity.myPosts.add(totalPost);
                if(totalPost.id.equals(lastId)) {
                    postAdapter.notifyDataSetChanged();
                }
            });
        }
        postAdapter.notifyDataSetChanged();
    }

    private void wantToChangeAvatar() {
        getImageFromDevice.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void addNewPost() {
        Intent intent = new Intent(getContext(), AddPostActivity.class);
        writeNewPost.launch(intent);
    }

    private void changeAvatar(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        StorageReference storage = FirebaseStorage.getInstance().getReference()
                .child("images/"
                        + MainActivity.getAuth().getUid()
                        + ".jpg");
        UploadTask task = storage.putBytes(data);
        MainActivity.userAvatar = bitmap;
        task.addOnSuccessListener(t -> {
                MainActivity.getUsers().child(MainActivity.getUser().id).setValue(
                        MainActivity.getUser()
                ).addOnSuccessListener(e -> avatarImg.setImageBitmap(bitmap));
        });
    }

    @Override
    public void onDestroy() {
        postAdapter.clear();
        super.onDestroy();
    }
}