package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SiteResponse {
    @SerializedName("id")
    @Expose
    public Long id;

    @SerializedName("name")
    @Expose
    public String name;
}
