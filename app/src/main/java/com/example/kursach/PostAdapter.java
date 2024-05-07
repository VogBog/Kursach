package com.example.kursach;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PostAdapter extends BaseAdapter {
    public final ArrayList<Post> posts = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts.addAll(posts);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.post, parent, false);
        }

        Post post = getPost(position);
        ((TextView)convertView.findViewById(R.id.authorName)).setText(post.author.name);
        ((TextView)convertView.findViewById(R.id.postName)).setText(post.postName);
        ((TextView)convertView.findViewById(R.id.postDescription)).setText(post.postDescription);

        return convertView;
    }
}
