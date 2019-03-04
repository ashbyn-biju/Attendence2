package com.example.gokul.attendence.report;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.MonthlyAbsenteesResponse;

public class ReportItemViewHolder extends RecyclerView.ViewHolder {
    private TextView mMonthTextView;
    private TextView mYearTextView;
    private TextView mSummaryTextView;
    private FragmentManager mFragmentManager;

    private MonthlyAbsenteesResponse mData;

    public ReportItemViewHolder(View itemView, FragmentManager fragmentManager) {
        super(itemView);
        mFragmentManager = fragmentManager;
        mMonthTextView = itemView.findViewById(R.id.month_text);
        mYearTextView = itemView.findViewById(R.id.year_text);
        mSummaryTextView = itemView.findViewById(R.id.summary_text);
    }

    public void setData(MonthlyAbsenteesResponse data) {
        this.mData = data;
        try {
            String[] splits = mData.label.split(" ");
            mMonthTextView.setText(splits[0]);
            mYearTextView.setText(splits[1]);
            int size = mData.dates.size();
            if (size > 0) {
                if (size == 1) {
                    mSummaryTextView.setText(String.format("%d leave in total", size));
                } else {
                    mSummaryTextView.setText(String.format("%d leaves in total", size));
                }
            } else {
                mSummaryTextView.setText("No leaves in this month");
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReportDetailDialog dialog = new ReportDetailDialog();
                    dialog.show(mFragmentManager, "ReportDetailDialog");
                    dialog.setData(mData);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
