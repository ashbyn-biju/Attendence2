package com.example.gokul.attendence.api;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface WebService {

    @POST("users/login")
    @Multipart
    Call<LoginResponse> login(
            @Part("username") RequestBody userName,
            @Part("password") RequestBody password
    );

    @GET("users/get-profile")
    Call<ProfileResponse> profile();

    @POST("users/update-profile")
    @Multipart
    Call<ProfileResponse> updateProfile(
            @Part("name") RequestBody name,
            @Part("phone") RequestBody phone,
            @Part("email") RequestBody email,
            @Part("gender") RequestBody gender,
            @Part("password") RequestBody password,
            @Part("dob") RequestBody dob,
            @Part("image") RequestBody image
    );

    @POST("users/update-profile")
    @Multipart
    Call<ProfileResponse> updateProfileMultipart(
            @Part MultipartBody.Part name,
            @Part MultipartBody.Part phone,
            @Part MultipartBody.Part email,
            @Part MultipartBody.Part gender,
            @Part MultipartBody.Part password,
            @Part MultipartBody.Part dob
    );

    @POST("users/update-profile")
    @Multipart
    Call<ProfileResponse> updateProfilePicture(
            @Part MultipartBody.Part image
    );

    @GET("attendance")
    Call<List<AttendanceItemResponse>> getAttendance(
            @Query("date1") String dateFrom,
            @Query("date2") String dateTo
    );

    @GET("reports/absent-dates-monthly")
    Call<List<MonthlyAbsenteesResponse>> getMonthlyAbsentees(
            @Query("month1") int month1,
            @Query("year1") int year1,
            @Query("month2") int month2,
            @Query("year2") int year2
    );

    @POST("attendance/mark-attendance")
    @Multipart
    Call<MarkAttendanceResponse> markAttendance(
            @Part MultipartBody.Part dateTime,
            @Part MultipartBody.Part lat,
            @Part MultipartBody.Part lng,
            @Part MultipartBody.Part remarks,
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part siteId,
            @Part MultipartBody.Part location);

    @GET("users/send-password-reset")
    Call<ForgotPasswordResponse> forgotPassword(
            @Query("email") String email
    );

    @GET("users/reset")
    Call<ResetPasswordResponse> resetPassword(
            @Query("apiToken") String apiToken,
            @Query("userToken") String userToken
    );

    @GET("job-sites")
    Call<List<SiteResponse>> getSites();

    @GET("attendance/get-attendance-count")
    Call<AttendanceCountResponse> getAttendanceCount(
            @Query("month") int month,
            @Query("year") int year
    );
}
