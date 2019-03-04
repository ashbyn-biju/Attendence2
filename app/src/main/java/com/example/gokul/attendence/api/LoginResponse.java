package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("empId")
    @Expose
    public Integer empId;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("phone")
    @Expose
    public String phone;
    @SerializedName("gender")
    @Expose
    public String gender;
    @SerializedName("access_token")
    @Expose
    public String accessToken;

    public boolean isValid() {
        return empId != null && name != null && email != null && phone != null && gender != null && accessToken != null;
    }
}