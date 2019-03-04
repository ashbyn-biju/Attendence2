package com.example.gokul.attendence.report;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.MonthlyAbsenteesResponse;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportItemViewHolder> {
    private List<MonthlyAbsenteesResponse> mDataList;
    private FragmentManager mFragmentManager;

    @Override
    public ReportItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        View view = li.inflate(R.layout.monthly_report_list_item, parent, false);
        return new ReportItemViewHolder(view, mFragmentManager);
    }

    @Override
    public void onBindViewHolder(ReportItemViewHolder holder, int position) {
        holder.setData(mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public void setDataList(List<MonthlyAbsenteesResponse> data) {
        this.mDataList = data;
        notifyDataSetChanged();
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.mFragmentManager = fragmentManager;
    }

    public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }
}
