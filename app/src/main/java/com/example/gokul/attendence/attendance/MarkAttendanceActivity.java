package com.example.gokul.attendence.attendance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.MarkAttendanceResponse;
import com.example.gokul.attendence.api.SiteResponse;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarkAttendanceActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;
    public static final int REQUEST_SUCCESS_POPUP = 212;
    public static final int REQUEST_CHECK_SETTINGS = 213;
    private static final String TAG = "CameraActivity";

    private ImageView mPhotoView;
    private TextView mLocationText;
    private EditText mRemarksInput;
    private Location mLocation;
    private Bitmap mPhoto;
    private SimpleDateFormat mApiDateFormatter;
    private FrameLayout mProgressBar;
    private TextView mOkButton;
    private Spinner mSiteListSpinner;
    private List<SiteResponse> mSites;
    private boolean isUploading = false;
    private boolean isSuccessfullyGotLocation = false;

    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_new);
        mPhotoView = this.findViewById(R.id.imageView1);
        mSiteListSpinner = findViewById(R.id.sites_list_spinner);
        mRemarksInput = findViewById(R.id.remarks_input);
        mLocationText = findViewById(R.id.tv_location);
        mProgressBar = findViewById(R.id.progress_bar);
        mOkButton = findViewById(R.id.ok_button);

        mApiDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        mSiteListSpinner.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.site_list_item,
                new String[]{"Select Site"}
        ));

        mOkButton.setOnClickListener(this::uploadData);

        AppDelegate.api.getSites().enqueue(new Callback<List<SiteResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<SiteResponse>> call, @NonNull Response<List<SiteResponse>> response) {
                try {
                    mSites = response.body();
                    List<String> items = new ArrayList<>();
                    items.add("Select Site");
                    for (SiteResponse site : mSites) {
                        items.add(site.name);
                    }
                    mSiteListSpinner.setAdapter(new ArrayAdapter<>(
                            MarkAttendanceActivity.this,
                            R.layout.site_list_item,
                            items
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SiteResponse>> call, @NonNull Throwable t) {
                try {
                    Toast.makeText(MarkAttendanceActivity.this, "Unable to get list of sites", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mLocationRequest = new LocationRequest()
                .setInterval(30 * 1000)
                .setFastestInterval(10 * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1);

        normalState();
        checkGooglePlayServices();
    }

    void normalState() {
        isUploading = false;
        mRemarksInput.setEnabled(true);
        mOkButton.setEnabled(true);
        mLocationText.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    void busyState() {
        isUploading = true;
        mRemarksInput.setEnabled(false);
        mOkButton.setEnabled(false);
        mLocationText.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void requestPermissions() {
        final String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        Long denials = Flowable.fromArray(permissions)
                .map(permission -> ContextCompat.checkSelfPermission(MarkAttendanceActivity.this, permission))
                .filter(result -> result != PackageManager.PERMISSION_GRANTED)
                .count()
                .blockingGet();

        if (denials > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    200
            );
        } else {
            openCamera();
            checkLocationSettingsAndGetLocation();
        }
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
                .addOnFailureListener(ex -> {
                    Toast.makeText(this, "Please update Google Play Services", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnSuccessListener(m -> requestPermissions());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200: {
                int denials = 0;
                for (int res : grantResults) {
                    if (res != PackageManager.PERMISSION_GRANTED) {
                        denials++;
                        break;
                    }
                }
                if (denials == 0) {
                    openCamera();
                    checkLocationSettingsAndGetLocation();
                } else {
                    Toast.makeText(this, "Permission denied. Please grant all permissions and try again", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new
                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void checkLocationSettingsAndGetLocation() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .setNeedBle(false)
                .setAlwaysShow(true)
                .build();

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        LocationSettingsResponse result = task.getResult();
                        if (result != null) {
                            LocationSettingsStates states = result.getLocationSettingsStates();
                            boolean isOkay =
                                    states.isLocationPresent() &&
                                    states.isLocationUsable();
                            if (isOkay) {
                                getGeoLocation();
                            } else {
                                onLocationSettingsError();
                            }
                        } else {
                            onLocationSettingsError();
                        }
                    } else {
                        if (task.getException() instanceof ResolvableApiException) {
                            ResolvableApiException ex = (ResolvableApiException) task.getException();
                            try {
                                ex.startResolutionForResult(MarkAttendanceActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                                onLocationSettingsError();
                            }
                        } else {
                            onLocationSettingsError();
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void getGeoLocation() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        if (!isSuccessfullyGotLocation) {
                            onReceiveLocation(location);
                            isSuccessfullyGotLocation = true;
                        }
                    } else {
                        onLocationFailed();
                    }
                })
                .addOnFailureListener(ex -> {
                    if (ex instanceof ResolvableApiException) {
                        ResolvableApiException ex1 = (ResolvableApiException) ex;
                        try {
                            ex1.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            onLocationFailed();
                        }
                    }
                });

        client.requestLocationUpdates(
                mLocationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location l1 = locationResult.getLastLocation();
                        Location l2 = null;
                        try {
                            l2 = locationResult.getLocations().get(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!isSuccessfullyGotLocation) {
                            if (l1 != null) {
                                onReceiveLocation(l1);
                                isSuccessfullyGotLocation = true;
                            } else if (l2 != null) {
                                onReceiveLocation(l2);
                                isSuccessfullyGotLocation = true;
                            } else {
                                onLocationFailed();

                            }
                        }
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        if (!isSuccessfullyGotLocation) {
                            if (!locationAvailability.isLocationAvailable()) {
                                onLocationFailed();
                            }
                        }
                    }
                },
                getMainLooper()
        )
                .addOnSuccessListener(m -> Toast.makeText(this, "Successfully got location", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(ex -> onLocationFailed());
    }

    private void onReceiveLocation(Location location) {
        Toast.makeText(this, "Successfully received location", Toast.LENGTH_SHORT).show();
        mLocation = location;
        tryDecodeGeoLocation();
    }

    private void tryDecodeGeoLocation() {
        try {
            Geocoder geo = new Geocoder(MarkAttendanceActivity.this);
            List<Address> addresses = geo.getFromLocation(
                    mLocation.getLatitude(),
                    mLocation.getLongitude(),
                    1
            );
            if (addresses.size() > 0) {
                Address addr = addresses.get(0);
                String[] parts = new String[]{
                        addr.getSubLocality(),
                        addr.getLocality(),
                        addr.getSubAdminArea(),
                        addr.getAdminArea(),
                        addr.getPostalCode(),
                        addr.getCountryCode()
                };

                StringBuilder addressBuilder = new StringBuilder();
                for (String part : parts) {
                    if (part != null && !part.isEmpty()) {
                        if (addressBuilder.length() != 0) {
                            addressBuilder.append(", ");
                        }
                        addressBuilder.append(part);
                    }
                }

                final String address = addressBuilder.toString();
                runOnUiThread(() -> onReceiveGeoLocationAddress(address));
            } else {
                runOnUiThread(this::onGeoDecodeFailed);
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(this::onGeoDecodeFailed);
        }
    }

    private void onReceiveGeoLocationAddress(String address) {
        try {
            mLocationText.setText(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onGeoDecodeFailed() {
        try {
            mLocationText.setText("Unknown Location");
            Toast.makeText(this, "Failed to get address", Toast.LENGTH_SHORT).show();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void onLocationFailed() {
        Toast.makeText(this, "Unable to receive location", Toast.LENGTH_SHORT).show();
    }

    private void onLocationSettingsError() {
        Toast.makeText(this, "Unable to apply location settings", Toast.LENGTH_SHORT).show();
    }

    public void uploadData(View view) {
        String dateTime = mApiDateFormatter.format(new Date(System.currentTimeMillis()));
        String lat = null;
        String lng = null;
        byte[] photo = null;

        if (mLocation != null) {
            lat = String.format(Locale.US, "%.5f", mLocation.getLatitude());
            lng = String.format(Locale.US, "%.5f", mLocation.getLongitude());

        } else {
            Toast.makeText(this, "Please wait, we are getting your location", Toast.LENGTH_SHORT).show();
            return;
        }
        String remarks = mRemarksInput.getText().toString();

        if (mPhoto != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mPhoto.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            photo = bos.toByteArray();
        } else {
            Toast.makeText(this, "Please take a photo of employee", Toast.LENGTH_SHORT).show();
            return;
        }

        int pos = mSiteListSpinner.getSelectedItemPosition();
        if (pos == 0) {
            Toast.makeText(this, "Please select a site", Toast.LENGTH_SHORT).show();
            return;
        }
        long siteId = mSites.get(pos - 1).id;

        String location = mLocationText.getText().toString();
        Log.d("Lat/lng saved :- ", lat + " - " + lng);
        busyState();
        AppDelegate.api.markAttendance(
                MultipartBody.Part.createFormData("datetime", dateTime),
                MultipartBody.Part.createFormData("lat", lat),
                MultipartBody.Part.createFormData("lng", lng),
                MultipartBody.Part.createFormData("remarks", remarks),
                MultipartBody.Part.createFormData("image", "image.jpg", RequestBody.create(AppDelegate.IMAGE_JPEG_TYPE, photo)),
                MultipartBody.Part.createFormData("site_id", String.format(Locale.US, "%d", siteId)),
                MultipartBody.Part.createFormData("location", location)
        ).enqueue(new Callback<MarkAttendanceResponse>() {
            @Override
            public void onResponse(@NonNull Call<MarkAttendanceResponse> call, @NonNull Response<MarkAttendanceResponse> response) {
                try {
                    normalState();
                    MarkAttendanceResponse body = response.body();
                    if (body != null && body.succes != null) {
                        startActivityForResult(new Intent(MarkAttendanceActivity.this, SuccessPopupActivity.class), REQUEST_SUCCESS_POPUP);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } else {
                        Toast.makeText(MarkAttendanceActivity.this, "Unknown Error happened", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MarkAttendanceResponse> call, @NonNull Throwable t) {
                try {
                    normalState();
                    t.printStackTrace();
                    Toast.makeText(MarkAttendanceActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (resultCode == RESULT_OK) {
                    mPhoto = (Bitmap) data.getExtras().get("data");
                    mPhotoView.setImageBitmap(mPhoto);
                } else {
                    finish();
                }
                break;
            }

            case REQUEST_SUCCESS_POPUP: {
                finish();
                break;
            }

            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    checkLocationSettingsAndGetLocation();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isUploading) {
            Toast.makeText(this, "Please wait until we mark your attendance", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }
}
