package com.example.gokul.attendence.profile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.ProfileResponse;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileActivity extends AppCompatActivity {
    public static final String TAG = "UPActivity";
    private ProfileResponse mProfileData;
    private CircleImageView mProfilePicImageView;
    private TextView mUserNameTextView;
    private TextView mEmpIdTextView;
    private EditText mEmailInput;
    private EditText mPhoneInput;
    private TextView mDobInput;
    private RadioButton mMaleRadioButton;
    private RadioButton mFemaleRadioButton;
    private TextView mSaveButton;
    private FrameLayout mProgressBar;
    private RadioGroup mRadioGroup;
    private View mBackButton;

    private SimpleDateFormat mDobFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_new);
        mProfilePicImageView = findViewById(R.id.profile);
        mUserNameTextView = findViewById(R.id.user_name_text);
        mEmpIdTextView = findViewById(R.id.emp_id_text);
        mEmailInput = findViewById(R.id.email_input);
        mPhoneInput = findViewById(R.id.phone_input);
        mDobInput = findViewById(R.id.dob_input);
        mMaleRadioButton = findViewById(R.id.radioMale);
        mFemaleRadioButton = findViewById(R.id.radioFemale);
        mSaveButton = findViewById(R.id.save_button);
        mProgressBar = findViewById(R.id.progress_bar);
        mRadioGroup = findViewById(R.id.radioGroup);
        mBackButton = findViewById(R.id.imv_back_arrow_appbar);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        busyState();

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailInput.getText().toString();
                String phone = mPhoneInput.getText().toString();
                String dob = mDobInput.getText().toString();
                String gender = mMaleRadioButton.isChecked() ? "male" : "female";


                busyState();

                AppDelegate.api.updateProfileMultipart(
                        MultipartBody.Part.createFormData("name", mProfileData.name),
                        MultipartBody.Part.createFormData("phone", phone),
                        MultipartBody.Part.createFormData("email", email),
                        MultipartBody.Part.createFormData("gender", gender),
                        MultipartBody.Part.createFormData("password", ""),
                        MultipartBody.Part.createFormData("dob", dob)
                )
//                AppDelegate.api.updateProfile(
//                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, mProfileData.name),
//                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, phone),
//                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, email),
//                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, gender),
//                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, ""),
//                        RequestBody.create(AppDelegate.TEXT_PLAIN_TYPE, dob),
//                        null)
                        .enqueue(new Callback<ProfileResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                                try {
                                    normalState();
                                    ProfileResponse profile = response.body();
                                    Log.d(TAG, "Response: " + profile);
                                    Toast.makeText(UpdateProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                                try {
                                    normalState();
                                    Toast.makeText(UpdateProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                    t.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });

        mProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UpdateProfileActivity.this, ProfilePicUpdateActivity.class));
            }
        });

        mDobInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                try {
                    Date cur = mDobFormatter.parse(mDobInput.getText().toString());
                    cal.setTime(cur);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new DatePickerDialog(
                        UpdateProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                                Calendar now = Calendar.getInstance();
                                cal.set(Calendar.YEAR, y);
                                cal.set(Calendar.MONTH, m);
                                cal.set(Calendar.DAY_OF_MONTH, d);
                                if (cal.getTimeInMillis() > now.getTimeInMillis()) {
                                    Toast.makeText(UpdateProfileActivity.this, "Please provide a date in past", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                mDobInput.setText(mDobFormatter.format(cal.getTime()));
                            }
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppDelegate.api.profile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                try {
                    normalState();
                    mProfileData = response.body();
                    if (mProfileData != null) {
                        mUserNameTextView.setText(mProfileData.name);
                        mEmpIdTextView.setText(String.format("%d", mProfileData.empId));
                        mEmailInput.setText(mProfileData.email);
                        mPhoneInput.setText(mProfileData.phone);
                        mDobInput.setText(mProfileData.dob);
                        Log.d(TAG, "Gender: " + mProfileData.gender);
                        if ("male".equals(mProfileData.gender)) {
                            mMaleRadioButton.setChecked(true);
                        } else {
                            mFemaleRadioButton.setChecked(true);
                        }
                        Picasso.get()
                                .load(mProfileData.profilePic)
                                .noFade()
                                .placeholder(R.drawable.man)
                                .into(mProfilePicImageView);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                try {
                    normalState();
                    Log.e(TAG, "Network Error");
                    Toast.makeText(UpdateProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void normalState() {
        mEmailInput.setEnabled(true);
        mPhoneInput.setEnabled(true);
        mDobInput.setEnabled(true);
        mSaveButton.setEnabled(true);
        mRadioGroup.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void busyState() {
        mEmailInput.setEnabled(false);
        mPhoneInput.setEnabled(false);
        mDobInput.setEnabled(false);
        mSaveButton.setEnabled(false);
        mRadioGroup.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }
}
