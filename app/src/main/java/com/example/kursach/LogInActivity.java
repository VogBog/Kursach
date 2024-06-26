package com.example.kursach;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.kursach.callbacks.Callback;
import com.example.kursach.databinding.ActivityLogInBinding;
import com.example.kursach.databinding.FragmentLogInBinding;
import com.example.kursach.databinding.FragmentSignInBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

public class LogInActivity extends AppCompatActivity {

    ActivityLogInBinding binding;

    private LogInFragment logInFragment;
    private SignInFragment signInFragment;
    public static final String EMAIL = "email";
    public static final String PASS = "pass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        logInFragment = new LogInFragment();
        logInFragment.setTryToLogInCallback(this::tryToLogIn);
        logInFragment.setWantToRegisterCallback(this::fromLogInToRegister);

        signInFragment = new SignInFragment();
        signInFragment.setWantToLogInCallback(this::fromRegisterToLogIn);
        signInFragment.setTryToRegisterCallback(this::tryToRegister);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Intent intent = getIntent();
        boolean isClear = intent.getBooleanExtra("IsClear", false);
        if(isClear) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(EMAIL, "None");
            editor.putString(PASS, "N");
            editor.commit();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(binding.main.getId(), logInFragment);
            transaction.commit();
        }
        else {
            String email = prefs.getString(EMAIL, "None");
            String pass = prefs.getString(PASS, "N");

            if(!email.equals("None") && !pass.equals("N")) {
                tryToLogInWithData(email, pass, () -> {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(binding.main.getId(), logInFragment);
                    transaction.commit();
                });
            }
            else {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(binding.main.getId(), logInFragment);
                transaction.commit();
            }
        }
    }

    private void fromLogInToRegister() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.main.getId(), signInFragment);
        transaction.commit();
    }

    private void fromRegisterToLogIn() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.main.getId(), logInFragment);
        transaction.commit();
    }

    private boolean tryToLogInWithData(String email, String password, Callback onFailed) {
        MainActivity.getAuth().signInWithEmailAndPassword(email, password).addOnFailureListener(
                e -> {
                    logInFragment.showErrorMessage("Не удалось найти пользователя");
                    if(onFailed != null) {
                        onFailed.callback();
                    }
                }
        ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                MainActivity.getUsers().child(
                        MainActivity.getAuth().getCurrentUser().getUid()).get().addOnSuccessListener(
                        new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                if(user == null) {
                                    logInFragment.showErrorMessage("Кажется, вас забанили))");
                                    if(onFailed != null) {
                                        onFailed.callback();
                                    }
                                    return;
                                }
                                logIn(user, password);
                            }
                        }
                );
            }
        }).addOnCanceledListener(() -> {
            Toast.makeText(getBaseContext(), "Что-то пошло не так.", Toast.LENGTH_SHORT).show();
            if(onFailed != null) {
                onFailed.callback();
            }
        });

        return true;
    }

    private boolean tryToLogIn(FragmentLogInBinding logInBinding) {
        String email = logInBinding.email.getText().toString();
        String password = logInBinding.password.getText().toString();

        return tryToLogInWithData(email, password, null);
    }

    private void tryToRegister(FragmentSignInBinding signInBinding) {
        String name = signInBinding.nameEdit.getText().toString();
        String email = signInBinding.email.getText().toString();
        String pass = signInBinding.password.getText().toString();
        String phone = signInBinding.phoneInput.getText().toString();

        MainActivity.getAuth().createUserWithEmailAndPassword(email, pass).addOnFailureListener(e ->
                signInFragment.showErrorMessage("Такой пользователь уже существует")).
                addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        User user = new User(id, email, name, phone, "");
                        MainActivity.getUsers().child(id)
                        .setValue(user).addOnSuccessListener(e -> logIn(user, pass)).addOnFailureListener(
                                e -> signInFragment.showErrorMessage("Что-то пошло не так")
                                );

                    }
                });
    }

    private void logIn(User user, String pass) {
        SharedPreferences.Editor prefs = getPreferences(MODE_PRIVATE).edit();
        prefs.putString(EMAIL, user.email);
        prefs.putString(PASS, pass);
        prefs.commit();

        Intent intent = new Intent();
        intent.putExtra("User", user);
        setResult(RESULT_OK, intent);
        finish();
    }
}