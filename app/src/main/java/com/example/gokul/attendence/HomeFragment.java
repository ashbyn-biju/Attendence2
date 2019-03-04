package com.example.gokul.attendence;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokul.attendence.api.AttendanceCountResponse;
import com.example.gokul.attendence.api.MonthlyAbsenteesResponse;
import com.example.gokul.attendence.attendance.MarkAttendanceActivity;
import com.example.gokul.attendence.report.ReportDetailDialog;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class HomeFragment extends Fragment {
    private ImageView mLogoutButton;
    private TextView mCurrentDateTextView;
    private CircularProgressBar mProgressBar;
    private TextView mLeaveCountTextView;
    private Button mMarkAttendanceButton;

    private SimpleDateFormat mDisplayDateFormat;
    private SimpleDateFormat mApiDateFormat;
    private Handler mHandler;
    private MonthlyAbsenteesResponse mAbsenteesData;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mDisplayDateFormat = new SimpleDateFormat("dd / MM / yyyy", Locale.US);
        mApiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLogoutButton = view.findViewById(R.id.logout_button);
        mCurrentDateTextView = view.findViewById(R.id.current_date_text);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mLeaveCountTextView = view.findViewById(R.id.leave_count_text);
        mMarkAttendanceButton = view.findViewById(R.id.mark_attendance_button);

        mMarkAttendanceButton.setOnClickListener(v -> startActivity(new Intent(getContext(), MarkAttendanceActivity.class)));

        mLogoutButton.setOnClickListener(v -> startActivity(new Intent(getContext(), LogoutActivity.class)));

        mProgressBar.setOnClickListener(v -> {
            if (mAbsenteesData != null) {
                ReportDetailDialog dialog = new ReportDetailDialog();
                dialog.setData(mAbsenteesData);
                dialog.show(getFragmentManager(), "Dialog");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Calendar cal = Calendar.getInstance();

        mProgressBar.enableIndeterminateMode(true);
        mCurrentDateTextView.setText(mDisplayDateFormat.format(cal.getTime()));

//        AppDelegate.api.getAttendanceCount(
//                cal.get(Calendar.MONTH) + 1,
//                cal.get(Calendar.YEAR)
//        ).enqueue(new Callback<AttendanceCountResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<AttendanceCountResponse> call, @NonNull Response<AttendanceCountResponse> response) {
//                try {
//                    AttendanceCountResponse count = response.body();
//                    mProgressBar.enableIndeterminateMode(false);
//                    mProgressBar.setProgressMax(count.totalDays);
//                    mProgressBar.setProgressWithAnimation(count.absentDays);
//
//                    mLeaveCountTextView.setText(String.format(Locale.US, "%d", count.absentDays));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AttendanceCountResponse> call, @NonNull Throwable t) {
//                try {
//                    AppDelegate.safeToast(HomeFragment.this, "Network Error");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        new Thread() {
            @Override
            public void run() {
                try {
                    final AttendanceCountResponse count = AppDelegate.api.getAttendanceCount(
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)
                    ).execute().body();

                    if (count == null) throw new Exception("Failed to get attendanceCount");

                    mHandler.post(() -> {
                        mProgressBar.enableIndeterminateMode(false);
                        mProgressBar.setProgressMax(count.totalDays);
                        mProgressBar.setProgressWithAnimation(count.absentDays);

                        mLeaveCountTextView.setText(String.format(Locale.US, "%d", count.absentDays));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.post(() -> {
                        Toast.makeText(getContext(), "Failed to get leaves", Toast.LENGTH_SHORT).show();
                    });
                }

                try {
                    mAbsenteesData = AppDelegate.api.getMonthlyAbsentees(
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)
                    ).execute().body().get(0);
                } catch (Exception e) {
                    e.printStackTrace();
//                    mHandler.post(() -> {
//                        Toast.makeText(getContext(), "Failed to get monthly leaves", Toast.LENGTH_SHORT).show();
//                    });
                }
            }
        }.start();
    }
}
