package com.client.imagerecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TakeImageActivity extends AppCompatActivity implements IStartCameraCallback, Camera.PictureCallback, Camera.AutoFocusCallback {

    public final static String TAKEN_IMAGES_LIST = "com.client.imagerecognition.TAKEN_IMAGES_LIST";

    private SurfaceView surfaceView;
    private Camera camera;
    private boolean isImageTakingProcessStarted = false;
    private ProgressDialog progressDialog;
    private List<String> takenImages = new ArrayList<>();
    private boolean isSurfaceClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_image);

        surfaceView = findViewById(R.id.takeImageSurfaceView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CameraStartTask(this).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void OnCameraInitialized(Camera cameraInstance) {
        camera = cameraInstance;
        StartPreview();
    }

    private void StartPreview() {
        camera.setDisplayOrientation(90);

        Camera.Parameters params = camera.getParameters();
        params.setJpegQuality(100);

        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        params.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);

        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        params.setPictureSize(pictureSizes.get(0).width, pictureSizes.get(0).height);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);
        try {
            camera.setPreviewDisplay(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        surfaceView.setVisibility(View.VISIBLE);
        surfaceView.setOnClickListener(new OnSurfaceClickListener(this, camera));
        camera.startPreview();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(TAKEN_IMAGES_LIST, TextUtils.join(",", takenImages));
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    public void onPictureTaken(byte[] imageArray, Camera camera) {
        Thread saveImageThread = new Thread(() -> {
            SaveImage(imageArray);
            progressDialog.dismiss();
            StartPreview();
        });

        saveImageThread.start();
    }

    private void SaveImage(byte[] imageArray) {
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        File storageDir = getExternalFilesDir(null);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File newImage = new File(storageDir, imageFileName);
        if (newImage.exists()) {
            newImage.delete();
        }
        String imagePath = newImage.getPath();
        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            fos.write(imageArray);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        isImageTakingProcessStarted = false;
        takenImages.add(imagePath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length, options);
        if (options.outWidth > options.outHeight) {
            Bitmap rotatedBitmap = rotateImage(bitmap, 90);
            newImage.delete();
            try {
                OutputStream os = new BufferedOutputStream(new FileOutputStream(newImage));
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    public void onAutoFocus(boolean isSuccess, Camera camera) {
        if (isSurfaceClicked) {
            isSurfaceClicked = false;
            progressDialog.show();
            camera.takePicture(null, null, null, this);
        }
    }

    class OnSurfaceClickListener implements View.OnClickListener {
        private TakeImageActivity parentActivity;
        private Camera camera;

        OnSurfaceClickListener(TakeImageActivity parentActivity, Camera camera) {
            this.parentActivity = parentActivity;
            this.camera = camera;
        }

        @Override
        public void onClick(View view) {
            if (!parentActivity.isImageTakingProcessStarted) {
                parentActivity.isSurfaceClicked = true;
                camera.autoFocus(parentActivity);
            }

        }
    }
}
