package com.example.kursach;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kursach.callbacks.Callback;
import com.example.kursach.callbacks.CallbackReturnedArg;
import com.example.kursach.databinding.FragmentLogInBinding;


public class LogInFragment extends Fragment {

    FragmentLogInBinding binding;

    private Callback wantToRegister;
    private CallbackReturnedArg<Boolean, FragmentLogInBinding> tryToLogInCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wantToRegister.callback();
            }
        });

        binding.logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.email.getText().toString();
                String pass = binding.password.getText().toString();
                if(email.isEmpty() || pass.isEmpty() || email.length() > 100 ||
                        pass.length() > 50 || pass.length() < 5) {
                    showErrorMessage("Неверные данные");
                    return;
                }

                if(!tryToLogInCallback.callback(binding)) {
                    showErrorMessage("Не получилось войти");
                }
            }
        });

        return view;
    }

    public void setWantToRegisterCallback(Callback callback) {
        this.wantToRegister = callback;
    }

    public void setTryToLogInCallback(CallbackReturnedArg<Boolean, FragmentLogInBinding> callback) {
        this.tryToLogInCallback = callback;
    }

    public void showErrorMessage(String message) {
        Toast toast = Toast.makeText(getContext(),
                message,
                Toast.LENGTH_SHORT);
        toast.show();
    }
}