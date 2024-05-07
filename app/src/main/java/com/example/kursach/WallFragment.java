package com.example.kursach;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kursach.databinding.FragmentWallBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class WallFragment extends Fragment {

    private FragmentWallBinding binding;
    private Post[] TEST = new Post[] { new Post(), new Post(), new Post()};
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWallBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        postAdapter = new PostAdapter(getContext(), new ArrayList<>(Arrays.asList(TEST)));
        binding.mainList.setAdapter(postAdapter);

        return view;
    }
}