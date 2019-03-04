package com.example.gokul.attendence.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.LogoutActivity;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.LoginResponse;
import com.example.gokul.attendence.api.ProfileResponse;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordActivity extends AppCompatActivity {
    private CircleImageView mProfilePicView;
    private TextView mNameTextView;
    private TextView mEmpIdTextView;
    private EditText mOldPasswordInput;
    private EditText mNewPasswordInput;
    private EditText mConfirmPasswordInput;
    private TextView mOkayButton;
    private ImageView mLogoutButton;
    private SharedPreferences mPreferences;
    private ImageView mBackButton;
    private FrameLayout mProgressBar;

    private ProfileResponse mProfileData;
    private Thread mPasswordUpdateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_new);
        mPreferences = getSharedPreferences(AppDelegate.APP_NAME, MODE_PRIVATE);
        mProfilePicView = findViewById(R.id.profile);
        mNameTextView = findViewById(R.id.user_name_text);
        mEmpIdTextView = findViewById(R.id.emp_id_text);
        mOldPasswordInput = findViewById(R.id.old_password_input);
        mNewPasswordInput = findViewById(R.id.new_password_input);
        mConfirmPasswordInput = findViewById(R.id.confirm_password_input);
        mLogoutButton = findViewById(R.id.imv_logout_appbar);
        mOkayButton = findViewById(R.id.ok_button);
        mBackButton = findViewById(R.id.imv_back_arrow_appbar);
        mProgressBar = findViewById(R.id.progress_bar);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        busyState();
        AppDelegate.api.profile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                try {
                    normalState();
                    mProfileData = response.body();
                    if (mProfileData != null) {
                        mNameTextView.setText(mProfileData.name);
                        mEmpIdTextView.setText(String.format("%d", mProfileData.empId));
                        Picasso.get()
                                .load(mProfileData.profilePic)
                                .noFade()
                                .placeholder(R.drawable.man)
                                .into(mProfilePicView);
                    } else {
                        Toast.makeText(PasswordActivity.this, "Failed to get user profile. Please try again.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                try {
                    normalState();
                    t.printStackTrace();
                    Toast.makeText(PasswordActivity.this, "Failed to get user profile. Please try again.", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PasswordActivity.this, LogoutActivity.class));
            }
        });

        mOkayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String oldPass = mOldPasswordInput.getText().toString();
                final String newPass = mNewPasswordInput.getText().toString();
                final String cfmPass = mConfirmPasswordInput.getText().toString();
                if (!newPass.equals(cfmPass)) {
                    Toast.makeText(PasswordActivity.this, "New password and confirm password must be same", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPass.equals(oldPass)) {
                    Toast.makeText(PasswordActivity.this, "New password and old password must be different", Toast.LENGTH_SHORT).show();
                    return;
                }

                busyState();
                if (mPasswordUpdateThread == null) {
                    mPasswordUpdateThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                LoginResponse auth = AppDelegate.api.login(
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, mProfileData.username),
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, oldPass)
                                ).execute().body();

                                if (auth == null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Toast.makeText(PasswordActivity.this, "Your old password is incorrect", Toast.LENGTH_SHORT).show();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    return;
                                }
                                mPreferences.edit()
                                        .putString(AppDelegate.Preferences.ACCESS_TOKEN, auth.accessToken)
                                        .putInt(AppDelegate.Preferences.EMP_ID, auth.empId)
                                        .putString(AppDelegate.Preferences.NAME, auth.name)
                                        .putString(AppDelegate.Preferences.EMAIL, auth.email)
                                        .putString(AppDelegate.Preferences.PHONE, auth.phone)
                                        .putString(AppDelegate.Preferences.GENDER, auth.gender)
                                        .apply();

                                ProfileResponse resp = AppDelegate.api.updateProfile(
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, mProfileData.name),
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, mProfileData.phone),
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, mProfileData.email),
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, mProfileData.gender),
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, newPass),
                                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, mProfileData.dob),
                                        null).execute().body();

                                if (resp != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Toast.makeText(PasswordActivity.this, "Passwords updated successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Toast.makeText(PasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                            } catch (Exception e) {
                                                e.printStackTrace();
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
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            normalState();
                                            Toast.makeText(PasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    };
                    mPasswordUpdateThread.start();
                }
            }
        });
    }

    private void normalState() {
        mOldPasswordInput.setEnabled(true);
        mNewPasswordInput.setEnabled(true);
        mConfirmPasswordInput.setEnabled(true);
        mOkayButton.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void busyState() {
        mOldPasswordInput.setEnabled(false);
        mNewPasswordInput.setEnabled(false);
        mConfirmPasswordInput.setEnabled(false);
        mOkayButton.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }
}
