package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AttendanceItemResponse {
    @SerializedName("marked_at")
    @Expose
    public String markedAt;
    @SerializedName("lat")
    @Expose
    public Double lat;
    @SerializedName("lng")
    @Expose
    public Double lng;
    @SerializedName("attendance")
    @Expose
    public Integer attendance;
    @SerializedName("remarks")
    @Expose
    public String remarks;
    @SerializedName("image")
    @Expose
    public String image;

}