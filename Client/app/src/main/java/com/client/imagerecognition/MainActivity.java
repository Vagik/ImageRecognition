package com.client.imagerecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private final static int CHECK_PERMISSIONS_REQUEST_CODE = 1000;
    public final static int TAKE_PHOTO_REQUEST_CODE = 1002;

    private GalleryView galleryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPermission()) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA}, CHECK_PERMISSIONS_REQUEST_CODE);
        }

        File dir = getExternalFilesDir(null);
        dir.mkdirs();
        File[] files = dir.listFiles((file, name) -> name.endsWith(".jpg"));

        List<String> filePaths = new ArrayList<>();
        for (File file : files) {
            filePaths.add(file.getPath());
        }

        galleryView = findViewById(R.id.gallery_view);
        galleryView.setImages(filePaths);


        findViewById(R.id.takePhotoButton).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TakeImageActivity.class);
            startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
        });
        findViewById(R.id.realTimeDetectionButton).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TakeImageActivity.class);
            intent.putExtra(TakeImageActivity.REAL_TIME_DETECTION_MODE, true);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_PERMISSIONS_REQUEST_CODE:
                break;
            case TAKE_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    String takeImages = extras.getString(TakeImageActivity.TAKEN_IMAGES_LIST);
                    if (takeImages != null) {
                        String[] images = takeImages.split(",");
                        for (String image : images) {
                            galleryView.addImage(image);
                        }
                    }
                }
                break;
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

}
