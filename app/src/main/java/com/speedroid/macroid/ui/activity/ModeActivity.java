package com.speedroid.macroid.ui.activity;

import static com.speedroid.macroid.Configs.DIALOG_TYPE_SELECT_MODE;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.speedroid.macroid.ui.fragment.dialog.DefaultDialogFragment;


public class ModeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DefaultDialogFragment defaultDialogFragment = new DefaultDialogFragment(DIALOG_TYPE_SELECT_MODE);
        defaultDialogFragment.show(getSupportFragmentManager(), Integer.toString(DIALOG_TYPE_SELECT_MODE));
    }
}
