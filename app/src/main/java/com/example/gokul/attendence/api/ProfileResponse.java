package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("empId")
    @Expose
    public Integer empId;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("username")
    @Expose
    public String username;
    @SerializedName("phone")
    @Expose
    public String phone;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("dob")
    @Expose
    public String dob;
    @SerializedName("gender")
    @Expose
    public String gender;
    @SerializedName("profilePic")
    @Expose
    public String profilePic;
}
