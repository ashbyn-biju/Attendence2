package com.example.gokul.attendence.report;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.gokul.attendence.R;

import java.util.Calendar;

public class ReportFilterDialog extends DialogFragment {
    private Button mStartDateButton;
    private Button mEndDateButton;
    private Button mOkayButton;
    private Button mCancelButton;

    private int startMonth;
    private int startYear;
    private int endMonth;
    private int endYear;

    private ReportFilterDialogListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.monthly_report_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStartDateButton = view.findViewById(R.id.start_date_button);
        mEndDateButton = view.findViewById(R.id.end_date_button);
        mOkayButton = view.findViewById(R.id.ok_button);
        mCancelButton = view.findViewById(R.id.cancel_button);

        mOkayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null && validate()) {
                    mListener.rangeDidSet(startYear, startMonth + 1, endYear, endMonth + 1);
                    dismiss();
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dpd = new DatePickerDialog(
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                startYear = year;
                                startMonth = month;
                                refresh();
                            }
                        },
                        startYear,
                        startMonth,
                        1
                );
                dpd.show();
            }
        });

        mEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dpd = new DatePickerDialog(
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                endYear = year;
                                endMonth = month;
                                refresh();
                            }
                        },
                        endYear,
                        endMonth,
                        1
                );
                dpd.show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (startYear == 0 || startMonth == 0 || endYear == 0 || endMonth == 0) {
            initializeValues();
        }
        refresh();
    }

    private void initializeValues() {
        Calendar cal = Calendar.getInstance();
        endMonth = cal.get(Calendar.MONTH);
        endYear = cal.get(Calendar.YEAR);
        //cal.add(Calendar.MONTH, -1);
        startMonth = 0;
        startYear = cal.get(Calendar.YEAR);
    }

    private void refresh() {
        mStartDateButton.setText(String.format("%4d/%02d", startYear, startMonth + 1));
        mEndDateButton.setText(String.format("%4d/%02d", endYear, endMonth + 1));
    }

    private boolean validate() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        start.set(startYear, startMonth, 1, 0, 0, 0);
        end.set(endYear, endMonth, 1, 0, 0, 0);
        boolean isValid = end.getTimeInMillis() > start.getTimeInMillis()
                && start.getTimeInMillis() < now.getTimeInMillis();
        if (!isValid) {
            Toast.makeText(getContext(), "Please provide a valid range", Toast.LENGTH_SHORT).show();
        }
        return isValid;
    }

    public void setListener(ReportFilterDialogListener mListener) {
        this.mListener = mListener;
    }

    public int[] getValues() {
        if (startYear == 0 || startMonth == 0 || endYear == 0 || endMonth == 0) {
            initializeValues();
        }
        return new int[]{startYear, startMonth + 1, endYear, endMonth + 1};
    }
}
