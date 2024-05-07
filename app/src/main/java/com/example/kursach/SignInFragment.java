package com.example.kursach;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.kursach.callbacks.Callback;
import com.example.kursach.callbacks.CallbackArg;
import com.example.kursach.databinding.FragmentSignInBinding;

public class SignInFragment extends Fragment {
    FragmentSignInBinding binding;

    private Callback wantToLogInCallback;
    private CallbackArg<FragmentSignInBinding> tryToRegisterCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.goToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wantToLogInCallback.callback();
            }
        });

        binding.RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameEdit.getText().toString();
                String email = binding.email.getText().toString();
                String pass = binding.password.getText().toString();
                String repeatPass = binding.repeatPassword.getText().toString();
                String phone = binding.phoneInput.getText().toString();

                if(name.isEmpty() || name.length() > 40 ||
                email.isEmpty() || email.length() > 60 ||
                pass.isEmpty() || pass.length() < 5 || pass.length() > 50 ||
                repeatPass.isEmpty() || repeatPass.length() < 5 || repeatPass.length() > 50 ||
                phone.isEmpty() || phone.length() > 13 || phone.length() < 10) {
                    showErrorMessage("Некорректные данные");
                    return;
                }

                if(!pass.equals(repeatPass)) {
                    showErrorMessage("Пароли не совпадают");
                    return;
                }

                tryToRegisterCallback.callback(binding);
            }
        });

        return view;
    }

    public void setWantToLogInCallback(Callback callback) {
        wantToLogInCallback = callback;
    }

    public void setTryToRegisterCallback(CallbackArg<FragmentSignInBinding> callback) {
        tryToRegisterCallback = callback;
    }

    public void showErrorMessage(String message) {
        Toast toast = Toast.makeText(getContext(),
                message,
                Toast.LENGTH_SHORT);
        toast.show();
    }
}