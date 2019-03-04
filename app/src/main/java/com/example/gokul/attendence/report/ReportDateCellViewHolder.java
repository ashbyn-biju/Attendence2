package com.example.gokul.attendence.report;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.gokul.attendence.R;

public class ReportDateCellViewHolder extends RecyclerView.ViewHolder {

    private final TextView mTextView;

    public ReportDateCellViewHolder(View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.cell_text);
    }

    public void setText(String text) {
        mTextView.setText(text);
    }
}
