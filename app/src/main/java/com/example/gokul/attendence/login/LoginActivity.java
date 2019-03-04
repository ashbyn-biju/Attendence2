package com.example.gokul.attendence.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.DashboardActivity;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.LoginResponse;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private View mLoginRootLayout;
    private Button mLoginButton;
    private TextView mResetButton;
    private SharedPreferences mPreferences;
    private FrameLayout mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(AppDelegate.APP_NAME, MODE_PRIVATE);
        if (mPreferences.getString(AppDelegate.Preferences.ACCESS_TOKEN, null) != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        mLoginRootLayout = findViewById(R.id.login_root);
        mLoginButton = findViewById(R.id.btn_login);
        mResetButton = findViewById(R.id.btn_reset_password);
        mProgressBar = findViewById(R.id.progress_bar);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });
        normalState();
    }

    private void busyState() {
        mProgressBar.setVisibility(View.VISIBLE);
        etEmail.setEnabled(false);
        etPassword.setEnabled(false);
        mLoginButton.setEnabled(false);
        mResetButton.setEnabled(false);
    }

    private void normalState() {
        mProgressBar.setVisibility(View.INVISIBLE);
        etEmail.setEnabled(true);
        etPassword.setEnabled(true);
        mLoginButton.setEnabled(true);
        mResetButton.setEnabled(true);
    }

    void login() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty()) {
            Snackbar.make(mLoginRootLayout, R.string.login_err_email, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Snackbar.make(mLoginRootLayout, R.string.login_err_password, Snackbar.LENGTH_SHORT).show();
            return;
        }

        busyState();
        AppDelegate.api.login(
                RequestBody.create(MediaType.parse("text/plain"), email),
                RequestBody.create(MediaType.parse("text/plain"), password)
        ).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                try {
                    normalState();
                    LoginResponse auth = response.body();
                    if (auth != null && auth.isValid()) {
                        mPreferences.edit()
                                .putString(AppDelegate.Preferences.ACCESS_TOKEN, auth.accessToken)
                                .putInt(AppDelegate.Preferences.EMP_ID, auth.empId)
                                .putString(AppDelegate.Preferences.NAME, auth.name)
                                .putString(AppDelegate.Preferences.EMAIL, auth.email)
                                .putString(AppDelegate.Preferences.PHONE, auth.phone)
                                .putString(AppDelegate.Preferences.GENDER, auth.gender)
                                .apply();
                        Snackbar.make(mLoginRootLayout, "Login Success!, But I don't know what to do!", Snackbar.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Snackbar.make(mLoginRootLayout, R.string.login_err_auth, Snackbar.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                try {
                    normalState();
                    Snackbar.make(mLoginRootLayout, R.string.app_err_network, Snackbar.LENGTH_SHORT).show();
                    t.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void forgotPassword() {
        startActivity(new Intent(this, ResetActivity.class));
    }
}
