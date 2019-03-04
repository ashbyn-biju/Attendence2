package com.example.gokul.attendence.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResetPasswordResponse {
    @SerializedName("access_token")
    @Expose
    public String accessToken;
    @SerializedName("message")
    @Expose
    public String message;
}
