package com.example.gokul.attendence.login;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    String BASE_URL = "http://freemaart.com/demo1/attendance/api/web/v1/users/";

    @GET("login")
    Call<Login> getLogin();

    @POST("login")
    Call<Login> postLogin();

}
