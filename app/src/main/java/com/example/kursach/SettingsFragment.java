package com.example.kursach;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kursach.callbacks.Callback;
import com.example.kursach.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private Callback quitPressed;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.quitBtn.setOnClickListener(e -> {
            if(quitPressed != null) {
                quitPressed.callback();
            }
        });

        return view;
    }

    public void setQuitPressedCallback(Callback callback) {
        quitPressed = callback;
    }

}