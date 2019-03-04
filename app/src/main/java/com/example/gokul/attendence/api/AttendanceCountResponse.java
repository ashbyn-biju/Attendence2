package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AttendanceCountResponse {
    @SerializedName("totalDays")
    @Expose
    public Integer totalDays;

    @SerializedName("presentDays")
    @Expose
    public Integer presentDays;

    @SerializedName("absentDays")
    @Expose
    public Integer absentDays;
}
