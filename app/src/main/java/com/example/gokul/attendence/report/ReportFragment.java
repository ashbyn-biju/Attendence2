package com.example.gokul.attendence.report;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.DashboardActivity;
import com.example.gokul.attendence.LogoutActivity;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.MonthlyAbsenteesResponse;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportFragment extends Fragment implements ReportFilterDialogListener {
    private ReportFilterDialog mDialog;
    private RecyclerView mReportListView;
    private FloatingActionButton mFab;
    private ImageView mLogoutButton;
    private FrameLayout mProgressBar;

    private ReportAdapter mAdapter;
    private View mBackButton;
    private ImageView mNoReportsImage;

    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDialog = new ReportFilterDialog();
        mDialog.setListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_new, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mReportListView = view.findViewById(R.id.recycler_view);
        mFab = view.findViewById(R.id.fab);
        mLogoutButton = view.findViewById(R.id.imv_logout_appbar);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mBackButton = view.findViewById(R.id.imv_back_arrow_appbar);
        mNoReportsImage = view.findViewById(R.id.no_reports_image);

        mAdapter = new ReportAdapter();
        mAdapter.setFragmentManager(getFragmentManager());
        mReportListView.setAdapter(mAdapter);
        mReportListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mFab.setOnClickListener(view1 -> mDialog.show(getFragmentManager(), "ReportFragment"));

        mLogoutButton.setOnClickListener(view12 -> {
            startActivity(new Intent(getActivity(), LogoutActivity.class));
            getActivity().finish();
        });

        mBackButton.setOnClickListener(view13 -> {
            try {
                DashboardActivity a = (DashboardActivity) getActivity();
                a.navigateToHomeFragment();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void rangeDidSet(int sY, int sM, int eY, int eM) {
        loadData(sY, sM, eY, eM);
    }

    @Override
    public void onStart() {
        super.onStart();
        int[] values = mDialog.getValues();
        loadData(values[0], values[1], values[2], values[3]);
    }

    private void loadData(int sY, int sM, int eY, int eM) {
        busyState();
        AppDelegate.api.getMonthlyAbsentees(sM, sY, eM, eY).enqueue(new Callback<List<MonthlyAbsenteesResponse>>() {
            @Override
            public void onResponse(@Nullable Call<List<MonthlyAbsenteesResponse>> call, @Nullable Response<List<MonthlyAbsenteesResponse>> response) {
                Log.d("Service Response", " "+ new Gson().toJson(response.body()));
                try {
                    normalState();
                    List<MonthlyAbsenteesResponse> body = response.body();
                    mAdapter.setDataList(body);
                    if (body.size() > 0) {
                        mNoReportsImage.setVisibility(View.INVISIBLE);
                    } else {
                        mNoReportsImage.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AppDelegate.safeToast(ReportFragment.this, "Failed to load reports");
                }
            }

            @Override
            public void onFailure(@Nullable Call<List<MonthlyAbsenteesResponse>> call, @Nullable Throwable t) {
                try {
                    normalState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                AppDelegate.safeToast(ReportFragment.this, "Failed to load reports");
            }
        });
    }

    private void normalState() {
        mFab.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void busyState() {
        mFab.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }
}