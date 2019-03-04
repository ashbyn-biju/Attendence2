package com.example.gokul.attendence.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gokul.attendence.AppDelegate;
import com.example.gokul.attendence.R;
import com.example.gokul.attendence.api.ProfileResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProfilePicUpdateActivity extends AppCompatActivity {
    public enum State {
        INITIAL, AFTER_PICTURE_SELECTED, UPLOADING
    }

    public static final int REQUEST_PERMISSION_CAMERA = 100;
    public static final int REQUEST_PERMISSION_GALLERY = 101;
    public static final int REQUEST_CAMERA_PICTURE = 102;
    public static final int REQUEST_GALLERY_PICTURE = 103;
    public static final float MAX_PIC_SIZE_PX = 800f;

    private ImageView mProfilePictuerImage;
    private TextView mCameraButton;
    private TextView mGalleryButton;
    private TextView mTitleText;
    private FrameLayout mProgressBar;

    private State mState = State.INITIAL;
    private Bitmap mProfilePicture;
    String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic_update);
        mProfilePictuerImage = findViewById(R.id.profile_picture_image);
        mCameraButton = findViewById(R.id.camera_button);
        mGalleryButton = findViewById(R.id.gallery_button);
        mTitleText = findViewById(R.id.title_text);
        mProgressBar = findViewById(R.id.progress_bar);
        setState(State.INITIAL);

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mState) {
                    case INITIAL:
                        checkPermissionsAndOpenCamera();
                        break;
                    case AFTER_PICTURE_SELECTED:
                        finish();
                        break;
                }
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mState) {
                    case INITIAL:
                        checkPermissionsAndOpenGallery();
                        break;
                    case AFTER_PICTURE_SELECTED:
                        uploadProfilePicture();
                        break;
                }
            }
        });

    }

    private void setState(State state) {
        mState = state;
        switch (state) {
            case INITIAL:
                stateInitial();
                break;
            case AFTER_PICTURE_SELECTED:
                stateAfterPictureSelected();
                break;
            case UPLOADING:
                stateUploading();
                break;
        }
    }

    private void stateInitial() {
        mProfilePictuerImage.setImageResource(R.drawable.dp);
        mCameraButton.setText(R.string.ppu_initial_btn_camera);
        mGalleryButton.setText(R.string.ppu_initial_btn_gallery);
        mTitleText.setText(R.string.ppu_initial_title);
        mProgressBar.setVisibility(View.INVISIBLE);
        mCameraButton.setEnabled(true);
        mGalleryButton.setEnabled(true);
    }

    private void stateAfterPictureSelected() {
        if (mProfilePicture != null) {
            mProfilePictuerImage.setImageBitmap(mProfilePicture);
        } else {
            mProfilePictuerImage.setImageResource(R.drawable.dp);
        }
        mCameraButton.setText(R.string.ppu_after_picture_selected_btn_camera);
        mGalleryButton.setText(R.string.ppu_after_picture_selected_btn_gallery);
        mTitleText.setText(R.string.ppu_after_picture_selected_title);
        mProgressBar.setVisibility(View.INVISIBLE);
        mCameraButton.setEnabled(true);
        mGalleryButton.setEnabled(true);
    }

    private void stateUploading() {
        mCameraButton.setEnabled(false);
        mGalleryButton.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void checkPermissionsAndOpenCamera() {
        int p1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int p2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int p3 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (p1 != PackageManager.PERMISSION_GRANTED || p2 != PackageManager.PERMISSION_GRANTED || p3 != PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSION_CAMERA
            );
        } else {
            openCameraWithIntent();
        }
    }

    private void openCameraWithIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                Toast.makeText(this, "Unable to create file to store picture", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.gokul.attendence",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_CAMERA_PICTURE);
            }
        }
    }

    private void checkPermissionsAndOpenGallery() {
        int p1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int p2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (p1 != PackageManager.PERMISSION_GRANTED && p2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSION_GALLERY
            );
        } else {
            openGalleryWithIntent();
        }
    }

    private void openGalleryWithIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.ppu_initial_btn_gallery)),
                REQUEST_GALLERY_PICTURE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    openCameraWithIntent();
                } else {
                    Toast.makeText(this, "Please allow this app to take picture", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case REQUEST_PERMISSION_GALLERY: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryWithIntent();
                } else {
                    Toast.makeText(this, "Please allow this app to open camera roll", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    @WorkerThread
    private void setProfilePictureFromUri(Uri uri) {
        if (uri == null) return;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] mem = new byte[is.available()];
            is.read(mem, 0, mem.length);
            Bitmap origBitmap = BitmapFactory.decodeByteArray(mem, 0, mem.length);
            int w = origBitmap.getWidth();
            int h = origBitmap.getHeight();
            float scale = 1f;
            if (w > h) {
                scale = MAX_PIC_SIZE_PX / w;
            } else {
                scale = MAX_PIC_SIZE_PX / h;
            }
            if (scale > 1f) scale = 1f;

            mProfilePicture = Bitmap.createScaledBitmap(origBitmap, (int) (w * scale), (int) (h * scale), true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setState(State.AFTER_PICTURE_SELECTED);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProfilePicUpdateActivity.this, "Failed to read picture", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadProfilePicture() {
        setState(State.UPLOADING);
        new Thread() {
            @Override
            public void run() {
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    mProfilePicture.compress(Bitmap.CompressFormat.JPEG, 90, os);

                    ProfileResponse response = AppDelegate.api.updateProfilePicture(
                            MultipartBody.Part.createFormData(
                                    "image",
                                    "image.jpg",
                                    RequestBody.create(
                                            AppDelegate.IMAGE_JPEG_TYPE,
                                            os.toByteArray()
                                    )
                            )
                    ).execute().body();
                    Log.d("PPU", "Response: " + response.email);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(ProfilePicUpdateActivity.this, "Successfully updated profile picture", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(ProfilePicUpdateActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                setState(ProfilePicUpdateActivity.State.AFTER_PICTURE_SELECTED);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CAMERA_PICTURE: {
                if (resultCode == RESULT_OK && mCurrentPhotoPath != null) {
                    MediaScannerConnection.scanFile(
                            this,
                            new String[]{mCurrentPhotoPath},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, final Uri uri) {
                                    setProfilePictureFromUri(uri);
                                }
                            }
                    );
                } else {
                    Toast.makeText(this, "Camera shoot cancelled", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case REQUEST_GALLERY_PICTURE: {
                if (resultCode == RESULT_OK) {
                    setProfilePictureFromUri(data.getData());
                } else {
                    Toast.makeText(this, "Gallery pick cancelled", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            default: {
                super.onActivityResult(requestCode, resultCode, data);
                break;
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
