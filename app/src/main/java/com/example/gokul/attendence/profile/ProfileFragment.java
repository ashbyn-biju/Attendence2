package com.example.gokul.attendence.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.LogoutActivity;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.ProfileResponse;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment {
    public static final String TAG = "ProfileFragment";
    private ProfileResponse mProfileData;
    private CircleImageView mProfilePicView;
    private TextView mUserNameTextView;
    private TextView mEmpIdTextView;
    private TextView mEmailTextView;
    private TextView mPhoneNumberTextView;
    private TextView mDobTextView;
    private TextView mGenderTextView;

    private View mUpdateProfileButton;
    private View mChangePasswordButton;
    private View mLogoutButton;
    private FrameLayout mProgressBar;

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_new, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProfilePicView = view.findViewById(R.id.profile);
        mUserNameTextView = view.findViewById(R.id.user_name_text);
        mEmpIdTextView = view.findViewById(R.id.emp_id_text);
        mEmailTextView = view.findViewById(R.id.email_text);
        mPhoneNumberTextView = view.findViewById(R.id.phone_number_text);
        mDobTextView = view.findViewById(R.id.dob_text);
        mGenderTextView = view.findViewById(R.id.gender_text);
        mProgressBar = view.findViewById(R.id.progress_bar);

        mUpdateProfileButton = view.findViewById(R.id.update_profile_btn);
        mChangePasswordButton = view.findViewById(R.id.change_password_btn);
        mLogoutButton = view.findViewById(R.id.logout_btn);

        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), UpdateProfileActivity.class));
            }
        });

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PasswordActivity.class));
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), LogoutActivity.class));
            }
        });

        mProfilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ProfilePicUpdateActivity.class));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        busyState();
        AppDelegate.api.profile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                try {
                    normalState();
                    mProfileData = response.body();
                    if (mProfileData != null) {
                        mUserNameTextView.setText(mProfileData.name);
                        mEmpIdTextView.setText(String.format("%d", mProfileData.empId));
                        mEmailTextView.setText(mProfileData.email);
                        mPhoneNumberTextView.setText(mProfileData.phone);
                        mDobTextView.setText(mProfileData.dob);
                        mGenderTextView.setText(mProfileData.gender);
                        Picasso.get()
                                .load(mProfileData.profilePic)
                                .noFade()
                                .placeholder(R.drawable.man)
                                .into(mProfilePicView);
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
                    Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void normalState() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void busyState() {
        mProgressBar.setVisibility(View.VISIBLE);
    }
}