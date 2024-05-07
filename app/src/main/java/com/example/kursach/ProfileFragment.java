package com.example.kursach;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
import java.io.IOException;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;

    private PostAdapter postAdapter;
    private ActivityResultLauncher<PickVisualMediaRequest> getImageFromDevice;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

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

        ArrayList<Post> posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), posts, true);
        postAdapter.setRemovePostCallback(post -> {
            MainActivity.getPosts().child(post.id).removeValue().addOnSuccessListener(e -> {
                postAdapter.posts.remove(post);
                postAdapter.notifyDataSetChanged();
            });
        });

        Query query = MainActivity.getPosts().orderByChild("authorId")
                .equalTo(MainActivity.getAuth().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PostData> datas = new ArrayList<>();
                for(DataSnapshot item : snapshot.getChildren()) {
                    PostData data = item.getValue(PostData.class);
                    datas.add(data);
                }
                for(int i = datas.size() - 1; i >= 0; i--) {
                    Post post = new Post();
                    post.setData(datas.get(i), totalPost -> {
                        postAdapter.posts.add(totalPost);
                        postAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ERROR", "Posts canceled: " + error.getMessage());
            }
        });

        View header = getLayoutInflater().inflate(R.layout.profile_header, null);

        //if(getActivity() instanceof MainActivity) {
            //MainActivity activity = (MainActivity) getActivity();
            //((ImageView) header.findViewById(R.id.avatarImg)).setImageBitmap(activity.getDefaultAvatar());
            //activity.getAvatar(MainActivity.getAuth().getUid(), uri -> {
                //((ImageView) header.findViewById(R.id.avatarImg)).setImageURI(uri);
            //});
        //}
        header.findViewById(R.id.addPost).setOnClickListener(e -> addNewPost());

        ((TextView) header.findViewById(R.id.userName)).setText(MainActivity.getUser().name);
        header.findViewById(R.id.avatarBtn).setOnClickListener(e -> wantToChangeAvatar());
        binding.mainList.addHeaderView(header);
        binding.mainList.setAdapter(postAdapter);

        return view;
    }

    private void wantToChangeAvatar() {
        return;
        //getImageFromDevice.launch(new PickVisualMediaRequest.Builder()
                //.setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                //.build());
    }

    private void addNewPost() {
        Intent intent = new Intent(getContext(), AddPostActivity.class);
        startActivity(intent);
    }

    private void changeAvatar(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        Log.d("TEST", "Start");
        StorageReference storage = FirebaseStorage.getInstance().getReference()
                .child("images/"
                        + MainActivity.getAuth().getUid()
                        + ".jpg");
        UploadTask task = storage.putBytes(data);
        task.addOnSuccessListener(t -> {
            if(getActivity() instanceof MainActivity) {
                MainActivity.getUsers().child(MainActivity.getUser().id).setValue(
                        MainActivity.getUser()
                ).addOnSuccessListener(e -> ((MainActivity) getActivity()).openPage(2));
            }
        });
        Log.d("TEST", "Its okay");
    }
}