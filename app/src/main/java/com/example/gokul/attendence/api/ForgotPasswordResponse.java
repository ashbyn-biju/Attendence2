package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ForgotPasswordResponse {
    @SerializedName("user_token")
    @Expose
    public String userToken;
    @SerializedName("api_token")
    @Expose
    public String apiToken;
}
