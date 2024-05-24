package com.example.kursach;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.kursach.callbacks.CallbackArg;

public class AreYouSureDialog extends DialogFragment {
    public CallbackArg<Boolean> onAnswer;

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setTitle("Вы уверены?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(R.layout.dialog)
                .setNegativeButton("Нет", (d, w) -> callbackAnswer(false))
                .setPositiveButton("Да", (d, w) -> callbackAnswer(true))
                .create();
    }

    private void callbackAnswer(boolean value) {
        if(onAnswer != null) {
            onAnswer.callback(value);
        }
    }
}
