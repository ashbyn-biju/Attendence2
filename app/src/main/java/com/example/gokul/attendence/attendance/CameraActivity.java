package com.example.gokul.attendence.attendance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 101;
    public static final int REQUEST_SUCCESS_POPUP = 212;
    private static final String TAG = "CameraActivity";

    private ImageView mPhotoView;
    private EditText mLocationText;
    private EditText mRemarksInput;
    private Location mLocation;
    private Bitmap mPhoto;
    private SimpleDateFormat mApiDateFormatter;
    private FrameLayout mProgressBar;
    private TextView mOkButton;
    private Spinner mSiteListSpinner;
    private List<SiteResponse> mSites;
    private boolean isUploading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_new);

        mPhotoView = this.findViewById(R.id.imageView1);
        mSiteListSpinner = findViewById(R.id.sites_list_spinner);
        mRemarksInput = findViewById(R.id.remarks_input);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }

        mApiDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        mLocationText = findViewById(R.id.tv_location);
        mProgressBar = findViewById(R.id.progress_bar);
        mOkButton = findViewById(R.id.ok_button);
        mSiteListSpinner.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.site_list_item,
                new String[]{"Select Site"}
        ));

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData(view);
            }
        });

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
                            CameraActivity.this,
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
                    Toast.makeText(CameraActivity.this, "Unable to get list of sites", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        normalState();
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

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d(TAG, "OnSaveInstanceState 2");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "OnSaveInstanceState 1");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        Log.d(TAG, "OnRestoreInstanceState 2");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "OnRestoreInstanceState 1");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_CAMERA_PERMISSION_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_LONG).show();
                    Intent cameraIntent = new
                            Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case REQUEST_LOCATION_PERMISSION_CODE: {
                if (grantResults.length == 2 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                }
                break;
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            mPhoto = (Bitmap) data.getExtras().get("data");
            mPhotoView.setImageBitmap(mPhoto);
            checkPermissionsAndCaptureLocation();
        }

        if (requestCode == REQUEST_SUCCESS_POPUP && resultCode == Activity.RESULT_OK) {
            finish();
        }
    }

    private void checkPermissionsAndCaptureLocation() {
        int r1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int r2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (r1 != PackageManager.PERMISSION_GRANTED || r2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION_CODE
            );
        } else {
            getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        LocationRequest request = LocationRequest.create()
                .setInterval(3000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setNumUpdates(1);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(request, new LocationCallback() {
                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        try {
                            Log.d(TAG, "Location available: " + locationAvailability.isLocationAvailable());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLocationResult(final LocationResult locationResult) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    mLocation = locationResult.getLocations().get(0);
                                    Log.d(TAG, String.format("Location Received: %f, %f", mLocation.getLatitude(), mLocation.getLongitude()));
                                    Geocoder geo = new Geocoder(CameraActivity.this);
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
                                        Log.d(TAG, "Address received: " + address);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    mLocationText.setText(address);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    mLocationText.setText("Unknown Location");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                AppDelegate.safeToast(CameraActivity.this, "Failed to get location");
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }.start();
                    }
                }, getMainLooper())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Location fetch completed: success = " + task.isSuccessful());
                    }
                });
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
                        startActivityForResult(new Intent(CameraActivity.this, SuccessPopupActivity.class), REQUEST_SUCCESS_POPUP);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    } else {
                        Toast.makeText(CameraActivity.this, "Unknown Error happened", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CameraActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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

