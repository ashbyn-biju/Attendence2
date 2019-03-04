package com.example.gokul.attendence.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.ForgotPasswordResponse;
import com.example.gokul.attendence.api.ProfileResponse;
import com.example.gokul.attendence.api.ResetPasswordResponse;

import okhttp3.RequestBody;

public class ResetActivity extends AppCompatActivity {
    public static final String TAG = "ResetActivity";
    private EditText mEmailInput;
    private EditText mNewPassInput;
    private EditText mConfPassInput;
    private Button mSubmitButton;
    private FrameLayout mProgressBar;
    private Thread mNetworkThread;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        mPreferences = getSharedPreferences(AppDelegate.APP_NAME, MODE_PRIVATE);
        mEmailInput = findViewById(R.id.email_input);
        mNewPassInput = findViewById(R.id.new_password_input);
        mConfPassInput = findViewById(R.id.confirm_password_input);
        mProgressBar = findViewById(R.id.progress_bar);
        mSubmitButton = findViewById(R.id.submit_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSubmit(view);
            }
        });
        normalState();
    }

    void normalState() {
        mEmailInput.setEnabled(true);
        mNewPassInput.setEnabled(true);
        mConfPassInput.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
        mSubmitButton.setEnabled(true);
    }

    void busyState() {
        mEmailInput.setEnabled(false);
        mNewPassInput.setEnabled(false);
        mConfPassInput.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mSubmitButton.setEnabled(false);
    }

    public void resetSubmit(View view) {
//        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
//        startActivity(i);
        final String email = mEmailInput.getText().toString();
        final String newPass = mNewPassInput.getText().toString();
        final String confPass = mConfPassInput.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please provide your email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.isEmpty()) {
            Toast.makeText(this, "Please provide a new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (confPass.isEmpty()) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confPass)) {
            Toast.makeText(this, "Passwords must be the same", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mNetworkThread == null) {
            busyState();
            mNetworkThread = new Thread() {
                @Override
                public void run() {
                    try {
                        ForgotPasswordResponse r1 = AppDelegate.api.forgotPassword(email).execute().body();
                        if (r1.apiToken == null || r1.userToken == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ResetActivity.this, "There is no account associated with this email", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        ResetPasswordResponse r2 = AppDelegate.api.resetPassword(r1.apiToken, r1.userToken).execute().body();
                        mPreferences.edit()
                                .putString(AppDelegate.Preferences.ACCESS_TOKEN, r2.accessToken)
                                .apply();
                        ProfileResponse r3 = AppDelegate.api.profile().execute().body();
                        ProfileResponse r4 = AppDelegate.api.updateProfile(
                                RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, r3.name),
                                RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, r3.phone),
                                RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, r3.email),
                                RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, r3.gender),
                                RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, newPass),
                                RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, r3.dob),
                                null).execute().body();
                        Log.d(TAG, "Password updated for " + r4.email);
                        mPreferences.edit()
                                .remove(AppDelegate.Preferences.ACCESS_TOKEN)
                                .apply();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(ResetActivity.this, "Passwords updated successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(ResetActivity.this, "Failed to update passwords. Please provide correct email address.", Toast.LENGTH_LONG).show();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                normalState();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };
            mNetworkThread.start();
        }

    }

    public void remember(View view) {
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
        finish();
    }
}
