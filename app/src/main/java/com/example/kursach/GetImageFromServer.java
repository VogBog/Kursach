package com.example.kursach;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.ArrayMap;

import com.example.kursach.callbacks.CallbackArg;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GetImageFromServer {
    private static final ArrayMap<String, Bitmap> map = new ArrayMap<>();

    public static void getAvatar(Context context, String userId, CallbackArg<Bitmap> onGetAvatar) {
        if(map.containsKey(userId)) {
            onGetAvatar.callback(map.get(userId));
        }

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(
                "images/" + userId + ".jpg");
        try {
            final File localFile = File.createTempFile("Images", "jpg");
            ref.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    map.put(userId, bitmap);
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
