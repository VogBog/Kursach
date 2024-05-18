package com.example.kursach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.kursach.callbacks.CallbackArg;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class GetImageFromServer {
    public static void getAvatar(Context context, String userId, CallbackArg<Bitmap> onGetAvatar) {
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(
                "images/" + userId + ".jpg");
        try {
            final File localFile = File.createTempFile("Images", "jpg");
            ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    onGetAvatar.callback(bitmap);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getDefaultAvatarId() {
        return R.drawable.user;
    }
}
