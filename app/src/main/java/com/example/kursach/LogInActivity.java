package com.example.kursach;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.kursach.callbacks.CallbackArg;
import com.example.kursach.databinding.ActivityLogInBinding;
import com.example.kursach.databinding.FragmentLogInBinding;
import com.example.kursach.databinding.FragmentSignInBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LogInActivity extends AppCompatActivity {

    ActivityLogInBinding binding;

    private LogInFragment logInFragment;
    private SignInFragment signInFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        logInFragment = new LogInFragment();
        logInFragment.setTryToLogInCallback(this::tryToLogIn);
        logInFragment.setWantToRegisterCallback(this::fromLogInToRegister);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(binding.main.getId(), logInFragment);
        transaction.commit();

        signInFragment = new SignInFragment();
        signInFragment.setWantToLogInCallback(this::fromRegisterToLogIn);
        signInFragment.setTryToRegisterCallback(this::tryToRegister);
    }

    private void fromLogInToRegister() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(logInFragment);
        transaction.add(binding.main.getId(), signInFragment);
        transaction.commit();
    }

    private void fromRegisterToLogIn() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(signInFragment);
        transaction.add(binding.main.getId(), logInFragment);
        transaction.commit();
    }

    private boolean tryToLogIn(FragmentLogInBinding logInBinding) {
        String email = logInBinding.email.getText().toString();
        String password = logInBinding.password.getText().toString();

        MainActivity.auth.signInWithEmailAndPassword(email, password).addOnFailureListener(
                e -> logInFragment.showErrorMessage("Такой пользователь не найден")
        ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                MainActivity.users.child(
                        MainActivity.auth.getCurrentUser().getUid()).get().addOnSuccessListener(
                        new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                logIn(user);
                            }
                        }
                );
            }
        });

        return true;
    }

    private void tryToRegister(FragmentSignInBinding signInBinding) {
        String name = signInBinding.nameEdit.getText().toString();
        String email = signInBinding.email.getText().toString();
        String pass = signInBinding.password.getText().toString();

        MainActivity.auth.createUserWithEmailAndPassword(email, pass).addOnFailureListener(e ->
                signInFragment.showErrorMessage("Такой пользователь уже существует")).
                addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        User user = new User(email, pass, name);
                        MainActivity.users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(user).addOnSuccessListener(e -> logIn(user)).addOnFailureListener(
                                e -> signInFragment.showErrorMessage("Что-то пошло не так")
                                );

                    }
                });
    }

    private void logIn(User user) {
        Intent intent = new Intent();
        intent.putExtra("User", user);
        setResult(RESULT_OK, intent);
        finish();
    }
}