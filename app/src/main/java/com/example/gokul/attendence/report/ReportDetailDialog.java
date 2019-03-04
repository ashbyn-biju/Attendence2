package com.example.gokul.attendence.report;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.MonthlyAbsenteesResponse;

public class ReportDetailDialog extends DialogFragment {
    private MonthlyAbsenteesResponse mData;
    private TextView mHeaderText;
    private RecyclerView mListView;
    private ReportDetailAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.monthly_report_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHeaderText = view.findViewById(R.id.header_text);
        mListView = view.findViewById(R.id.date_list_view);
        mAdapter = new ReportDetailAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setLayoutManager(new GridLayoutManager(getContext(), 6));
        if (mData != null) {
            mHeaderText.setText(mData.label);
            mAdapter.setDates(mData.dates);
        }
    }

    public void setData(MonthlyAbsenteesResponse data) {
        this.mData = data;
    }
}
