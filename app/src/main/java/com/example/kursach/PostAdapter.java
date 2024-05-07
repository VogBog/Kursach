package com.example.kursach;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kursach.callbacks.CallbackArg;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PostAdapter extends BaseAdapter {
    public final ArrayList<Post> posts = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private boolean isAuthor;
    private CallbackArg<Post> removePostCallback;
    private CallbackArg<Post> callPostCallback;
    private CallbackArg<User> userCallback;

    public PostAdapter(Context context, ArrayList<Post> posts, boolean isAuthor) {
        this.context = context;
        this.posts.addAll(posts);
        for(Post post : this.posts) {
            post.userAdapter = new UserAdapter(context, post.initIds);
        }
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isAuthor = isAuthor;
    }

    public void add(Post post) {
        posts.add(post);
        if(post.userAdapter == null) {
            post.userAdapter = new UserAdapter(context, post.initIds);
            if(userCallback != null) {
                post.userAdapter.setClickedEvent(userCallback);
            }
        }
        notifyDataSetChanged();
    }

    public void setUserAdapterCallback(CallbackArg<User> callback) {
        for(Post post : posts) {
            post.userAdapter.setClickedEvent(callback);
        }
        userCallback = callback;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int position) {
        return posts.get(position);
    }

    public Post getPost(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("Get view", "Start");
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.post, parent, false);
        }

        Post post = getPost(position);
        ((TextView)convertView.findViewById(R.id.authorName)).setText(post.author.name);
        ((TextView)convertView.findViewById(R.id.postName)).setText(post.postName);
        ((TextView)convertView.findViewById(R.id.postDescription)).setText(post.postDescription);
        ((TextView) convertView.findViewById(R.id.playersCount)).setText(post.getPlayersCount() + "/10");

        if(!isAuthor) {
            convertView.findViewById(R.id.removePostBtn).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.callBtn).setOnClickListener(e -> {
                if(callPostCallback != null) {
                    callPostCallback.callback(post);
                }
            });
        }
        else {
            convertView.findViewById(R.id.removePostBtn).setOnClickListener(e -> {
                if(removePostCallback != null) {
                    removePostCallback.callback(post);
                }
            });
            convertView.findViewById(R.id.callBtn).setVisibility(View.INVISIBLE);

            ListView list = convertView.findViewById(R.id.playersList);
            list.setAdapter(post.userAdapter);
        }

        return convertView;
    }

    public void setRemovePostCallback(CallbackArg<Post> callback) {
        removePostCallback = callback;
    }

    public void setCallPostCallback(CallbackArg<Post> callback) {
        callPostCallback = callback;
    }

    public void notificateAllUserAdapters() {
        for(Post post : posts) {
            if(post.userAdapter != null) {
                post.userAdapter.notifyDataSetChanged();
            }
        }
    }
}
