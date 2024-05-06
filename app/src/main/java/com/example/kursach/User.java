package com.example.kursach;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User implements Parcelable {
    public String email, pass, name;

    public User() {}

    public User(String email, String pass, String name) {
        this.email = email;
        this.pass = pass;
        this.name = name;
    }

    protected User(Parcel in) {
        email = in.readString();
        pass = in.readString();
        name = in.readString();
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
        dest.writeString(email);
        dest.writeString(pass);
        dest.writeString(name);
    }
}
