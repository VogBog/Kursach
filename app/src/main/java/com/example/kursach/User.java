package com.example.kursach;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.kursach.callbacks.CallbackArg;
import com.example.kursach.callbacks.CallbackArg2;

public class User implements Parcelable {
    public String id, email, pass, name, phone, description;
    public boolean isAdmin = false;

    public User() {
        this.name = "User";
    }

    public User(String id, String email, String pass, String name, String phone, String description) {
        this(id, email, pass, name, phone, description, false);
    }

    public User(String id, String email, String pass, String name, String phone, String description, boolean isAdmin) {
        this.id = id;
        this.email = email;
        this.pass = pass;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.isAdmin = isAdmin;
    }

    protected User(Parcel in) {
        id = in.readString();
        email = in.readString();
        pass = in.readString();
        name = in.readString();
        phone = in.readString();
        description = in.readString();
        String b = in.readString();
        isAdmin = b.equals("true");
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(pass);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(description);
        dest.writeString(isAdmin ? "true" : "false");
    }

    public View createView(LayoutInflater inflater, ViewGroup parent,
                           CallbackArg<User> userCallback, CallbackArg2<Post, User> removeUserCallback,
                           Post post) {
        inflater.inflate(R.layout.user_layout, parent);
        View userView = parent.getChildAt(parent.getChildCount() - 1);
        ((TextView) userView.findViewById(R.id.nickname)).setText(name);
        View removeUserBtn = userView.findViewById(R.id.removeUserBtn);
        if(removeUserCallback == null) {
            removeUserBtn.setVisibility(View.INVISIBLE);
        }
        else {
            removeUserBtn.setOnClickListener(l -> removeUserCallback.callback(post, this));
        }
        userView.findViewById(R.id.avatarBtn).setOnClickListener(e -> {
            if(userCallback != null) {
                userCallback.callback(this);
            }
        });
        ImageView avatar = userView.findViewById(R.id.avatarImg);
        avatar.setImageResource(R.drawable.user);
        GetImageFromServer.getAvatar(parent.getContext(), id, bitmap -> {
            if(avatar != null) {
                avatar.setImageBitmap(bitmap);
            }
        });
        return userView;
    }
}
