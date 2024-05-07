package com.example.kursach;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.kursach.callbacks.CallbackArg;

import java.util.ArrayList;

public class PostAdapter extends BaseAdapter {
    public final ArrayList<Post> posts = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private boolean isAuthor;
    private CallbackArg<Post> removePostCallback;

    public PostAdapter(Context context, ArrayList<Post> posts, boolean isAuthor) {
        this.context = context;
        this.posts.addAll(posts);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isAuthor = isAuthor;
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
        Log.d("Get view", "Get view");

        if(!isAuthor) {
            convertView.findViewById(R.id.removePostBtn).setVisibility(View.INVISIBLE);
        }
        else {
            convertView.findViewById(R.id.removePostBtn).setOnClickListener(e -> {
                if(removePostCallback != null) {
                    removePostCallback.callback(post);
                }
            });
        }

        return convertView;
    }

    public void setRemovePostCallback(CallbackArg<Post> callback) {
        removePostCallback = callback;
    }
}
