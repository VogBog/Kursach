package com.example.kursach;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kursach.callbacks.CallbackArg;
import com.example.kursach.callbacks.CallbackArg2;

import java.util.ArrayList;

public class PostAdapter extends BaseAdapter {
    public final ArrayList<Post> posts = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    private boolean isAuthor, canEdit;
    private CallbackArg<Post> removePostCallback;
    private CallbackArg<Post> callPostCallback;
    private CallbackArg<User> userCallback;
    private CallbackArg2<Post, User> removeUserCallback;
    private CallbackArg<Post> editPostCallback;

    public PostAdapter(Context context, ArrayList<Post> posts, boolean isAuthor, boolean canEdit) {
        this.context = context;
        this.posts.addAll(posts);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isAuthor = isAuthor;
        this.canEdit = canEdit;
    }

    public void add(Post post) {
        posts.add(post);
        notifyDataSetChanged();
    }

    public void setUserAdapterCallback(CallbackArg<User> callback) {
        userCallback = callback;
    }

    public void clear() {
        for(Post post : posts) {
            post.players.clear();
        }
        posts.clear();
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
        ViewManager manager = (ViewManager) convertView.findViewById(R.id.playersList);
        ViewGroup group = (ViewGroup) convertView.findViewById(R.id.playersList);
        while(group.getChildCount() > 0) {
            manager.removeView(group.getChildAt(0));
        }

        Post post = getPost(position);
        ((TextView)convertView.findViewById(R.id.authorName)).setText(post.author.name);
        ((TextView)convertView.findViewById(R.id.postName)).setText(post.postName);
        ((TextView)convertView.findViewById(R.id.postDescription)).setText(post.postDescription);
        ((TextView) convertView.findViewById(R.id.playersCount)).setText(
                post.getPlayersCount() + "/" + post.maxPlayers);
        post.layout = convertView.findViewById(R.id.playersList);

        if(isAuthor) {
            String uid = MainActivity.getUser().id;
            if(post.players != null && !post.players.isEmpty()) {
                for(User user : post.players) {
                    user.createView(inflater, post.layout, userCallback, removeUserCallback, post);
                }
            }
            for(String userId : post.initIds) {
                if(userId == null) {
                    continue;
                }
                if(userId.equals(uid)) {
                    continue;
                }
                MainActivity.getUsers().child(userId).get().addOnSuccessListener(snapshot -> {
                    User user = snapshot.getValue(User.class);
                    for(User player : post.players) {
                        if(player.id.equals(user.id)) {
                            return;
                        }
                    }
                    post.players.add(0, user);
                    user.createView(inflater, post.layout, userCallback, removeUserCallback, post);
                });
            }
        }

        final ImageView image = convertView.findViewById(R.id.avatarImg);

        if(!post.author.id.equals(MainActivity.getUser().id)) {
            image.setImageResource(R.drawable.user);
            GetImageFromServer.getAvatar(context, post.author.id, image::setImageBitmap);
        }
        else {
            if(MainActivity.userAvatar != null)
                image.setImageBitmap(MainActivity.userAvatar);
            else
                image.setImageResource(GetImageFromServer.getDefaultAvatarId());
        }

        if(!isAuthor) {
            convertView.findViewById(R.id.removePostBtn).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.callBtn).setOnClickListener(e -> {
                if(callPostCallback != null) {
                    callPostCallback.callback(post);
                }
            });
            if(userCallback != null) {
                convertView.findViewById(R.id.avatarBtn).setOnClickListener(v ->
                        userCallback.callback(post.author));
            }
        }
        else {
            if(callPostCallback == null) {
                convertView.findViewById(R.id.callBtn).setVisibility(View.INVISIBLE);
            }

            convertView.findViewById(R.id.removePostBtn).setOnClickListener(e -> {
                if(removePostCallback != null) {
                    removePostCallback.callback(post);
                }
            });
            if(MainActivity.getUser().isAdmin) {
                if(userCallback != null) {
                    convertView.findViewById(R.id.avatarBtn).setOnClickListener(v ->
                            userCallback.callback(post.author));
                }
                if(!post.author.id.equals(MainActivity.getUser().id)) {
                    convertView.findViewById(R.id.callBtn).setOnClickListener(e -> {
                        if(callPostCallback != null) {
                            callPostCallback.callback(post);
                        }
                    });
                }
                else {
                    convertView.findViewById(R.id.callBtn).setVisibility(View.INVISIBLE);
                }
            }
            else {
                convertView.findViewById(R.id.callBtn).setVisibility(View.INVISIBLE);
            }
        }

        if(canEdit) {
            Button btn = convertView.findViewById(R.id.callBtn);
            btn.setVisibility(View.VISIBLE);
            btn.setText("Редактировать");
            btn.setOnClickListener(v -> {
                if(editPostCallback != null) {
                    editPostCallback.callback(post);
                }
            });
        }

        return convertView;
    }

    public void setRemovePostCallback(CallbackArg<Post> callback) {
        removePostCallback = callback;
    }

    public void setCallPostCallback(CallbackArg<Post> callback) {
        callPostCallback = callback;
    }

    public void setRemoveUserCallback(CallbackArg2<Post, User> callback) {
        removeUserCallback = callback;
    }

    public void setEditPostCallback(CallbackArg<Post> callback) {
        editPostCallback = callback;
    }
}
