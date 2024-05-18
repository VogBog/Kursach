package com.example.kursach;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kursach.callbacks.CallbackArg;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {
    public final ArrayList<User> users = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private CallbackArg<User> clicked;
    private ArrayList<String> initIds = new ArrayList<>();

    public UserAdapter(Context context, ArrayList<String> usersIds) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.initIds.addAll(usersIds);
        for(String str : usersIds) {
            users.add(new User());
        }
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public User getUser(int position) {
        return users.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if(convertView == null) {
            view = inflater.inflate(R.layout.user_layout, parent, false);
        }
        else {
            view = convertView;
        }

        MainActivity.getUsers().child(initIds.get(position)).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            users.set(position, user);

            ((TextView) view.findViewById(R.id.nickname)).setText(user.name);
            view.findViewById(R.id.avatarBtn).setOnClickListener(e -> {
                if(clicked != null) {
                    clicked.callback(user);
                }
            });
        });

        return view;
    }

    public void setClickedEvent(CallbackArg<User> callback) {
        clicked = callback;
    }
}
