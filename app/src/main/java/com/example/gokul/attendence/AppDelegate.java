package com.example.gokul.attendence;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.example.gokul.attendence.api.WebService;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppDelegate extends Application {
    public static final String APP_NAME = "app";
    //    public static final String API_ENDPOINT = "http://freemaart.com/demo1/attendance/";
    public static final String API_ENDPOINT = "https://api.wowhr.ml/v1/";
    public static OkHttpClient http;
    public static WebService api;

    public static final MediaType TEXT_PLAIN_TYPE = MediaType.parse("text/plain");
    public static final MediaType IMAGE_JPEG_TYPE = MediaType.parse("image/jpeg");

    public static class Preferences {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String EMP_ID = "emp_id";
        public static final String NAME = "name";
        public static final String GENDER = "gender";
        public static final String DOB = "dob";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final SharedPreferences pref = getSharedPreferences(APP_NAME, MODE_PRIVATE);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
            try {
                ProviderInstaller.installIfNeeded(getApplicationContext());
                SSLContext sslContext;
                sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, null, null);
                sslContext.createSSLEngine();
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
                    | NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }


        }

        http = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = pref.getString(Preferences.ACCESS_TOKEN, null);
                    Request.Builder builder = chain.request().newBuilder();
                    if (token != null) {
                        builder.addHeader("Authorization", String.format("Bearer %s", token));
                    }
                    return chain.proceed(builder.build());
                })
                .build();

        api = new Retrofit.Builder()
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(http)
                .build()
                .create(WebService.class);
    }

    public static void safeToast(final Activity activity, String message) {
        try {
            if (activity != null && !activity.isFinishing() && message != null) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void safeToast(final Activity activity, @StringRes int message) {
        try {
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void safeToast(final Fragment fragment, String message) {
        try {
            if (message != null && fragment != null && fragment.isInLayout() && fragment.isVisible()) {
                Toast.makeText(fragment.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void safeToast(final Fragment fragment, @StringRes int message) {
        try {
            if (message != 0 && fragment != null && fragment.isInLayout() && fragment.isVisible()) {
                Toast.makeText(fragment.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
