package com.example.gokul.attendence.report;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gokul.attendence.R;

import java.util.List;

public class ReportDetailAdapter extends RecyclerView.Adapter<ReportDateCellViewHolder> {
    private List<String> mDates;

    @Override
    public ReportDateCellViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        View view = li.inflate(R.layout.monthly_report_date_cell, parent, false);
        return new ReportDateCellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportDateCellViewHolder holder, int position) {
        holder.setText(mDates.get(position));
    }

    @Override
    public int getItemCount() {
        return mDates != null ? mDates.size() : 0;
    }

    public void setDates(List<String> dates) {
        this.mDates = dates;
        notifyDataSetChanged();
    }
}
