package com.example.gokul.attendence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.gokul.attendence.login.LoginActivity;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        SharedPreferences pref = getSharedPreferences(AppDelegate.APP_NAME, MODE_PRIVATE);
        pref.edit()
                .remove(AppDelegate.Preferences.ACCESS_TOKEN)
                .remove(AppDelegate.Preferences.EMP_ID)
                .remove(AppDelegate.Preferences.NAME)
                .remove(AppDelegate.Preferences.EMAIL)
                .remove(AppDelegate.Preferences.PHONE)
                .remove(AppDelegate.Preferences.GENDER)
                .apply();

        startActivity(
                new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK
                        )
        );
        finish();
    }
}
