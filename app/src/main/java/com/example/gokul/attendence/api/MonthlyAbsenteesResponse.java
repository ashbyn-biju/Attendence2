package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MonthlyAbsenteesResponse {

    @SerializedName("label")
    @Expose
    public String label;
    @SerializedName("dates")
    @Expose
    public List<String> dates = null;

}