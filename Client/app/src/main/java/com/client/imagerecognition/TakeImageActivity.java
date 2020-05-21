package com.client.imagerecognition;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TakeImageActivity extends AppCompatActivity implements IStartCameraCallback,
        Camera.PictureCallback, Camera.AutoFocusCallback, Camera.PreviewCallback {

    public final static String TAKEN_IMAGES_LIST = "com.client.imagerecognition.TAKEN_IMAGES_LIST";
    public final static String REAL_TIME_DETECTION_MODE = "com.client.imagerecognition.REAL_TIME_DETECTION_MODE";

    private SurfaceView surfaceView;
    private Camera camera;
    private boolean isImageTakingProcessStarted = false;
    private ProgressDialog progressDialog;
    private List<String> takenImages = new ArrayList<>();
    private boolean isSurfaceClicked = false;
    private boolean isRealTimeDetectionMode = false;
    private Classifier detector;
    private int previewHeight;
    private int previewWidth;
    private int surfaceViewHeight;
    private int surfaceViewWidth;
    private float surfaceWidthScale;
    private float surfaceHeightScale;
    private int previewFormat;
    private Bitmap frame;
    private boolean computingDetection = false;
    private Paint framePaint;
    private Paint titlePaint;
    private Bitmap scaledBitmap;
    private ImageView cameraImageView;
    private Bitmap imageViewBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_image);

        surfaceView = findViewById(R.id.takeImageSurfaceView);

        isRealTimeDetectionMode = getIntent().getBooleanExtra(REAL_TIME_DETECTION_MODE, false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        cameraImageView = findViewById(R.id.camera_image_view);

        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(8);
        titlePaint = new Paint();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CameraStartTask(this).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (!isRealTimeDetectionMode) {
            surfaceView.setOnClickListener(new OnSurfaceClickListener(this, camera));
        } else {
            camera.setPreviewCallback(this);
            try {
                detector = ObjectDetectionModel.create(getAssets(), "model.tflite", "label_map.txt", 300, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        previewFormat = camera.getParameters().getPreviewFormat();

        surfaceViewHeight = surfaceView.getHeight();
        surfaceViewWidth = surfaceView.getWidth();
        surfaceHeightScale = surfaceViewHeight / 300.0f;
        surfaceWidthScale = surfaceViewWidth / 300.0f;
        camera.startPreview();
    }

    @Override
    public void onBackPressed() {
        if (takenImages.size() > 0) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(TAKEN_IMAGES_LIST, TextUtils.join(",", takenImages));
            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }

        super.onBackPressed();
    }

    @Override
    public void onPictureTaken(byte[] imageArray, Camera camera) {
        new Thread(() -> {
            SaveImage(imageArray);
            progressDialog.dismiss();
            StartPreview();
        }).start();
    }

    private void SaveImage(byte[] imageArray) {
        String imageFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date()) + ".jpg";
        File storageDir = getExternalFilesDir(null);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File newImage = new File(storageDir, imageFileName);
        if (newImage.exists()) {
            newImage.delete();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length, options);
        if (options.outWidth > options.outHeight) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
            Bitmap rotatedBitmap = Utilities.rotateImage(bitmap, 90);
            try {
                OutputStream os = new BufferedOutputStream(new FileOutputStream(newImage));
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.recycle();
            rotatedBitmap.recycle();
        } else {
            try (FileOutputStream fos = new FileOutputStream(newImage.getPath())) {
                fos.write(imageArray);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        isImageTakingProcessStarted = false;
        takenImages.add(newImage.getPath());
    }



    @Override
    public void onAutoFocus(boolean isSuccess, Camera camera) {
        if (isSurfaceClicked) {
            isSurfaceClicked = false;
            progressDialog.show();
            camera.takePicture(null, null, null, this);
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (bytes == null || camera == null || computingDetection) {
            return;
        }

        computingDetection = true;
        AsyncTask.execute(() -> {
            try {
                frame = Utilities.convertYuvToJpeg(bytes, previewFormat, previewWidth, previewHeight);
                scaledBitmap = Utilities.rotateImage(Bitmap.createScaledBitmap(frame, 300, 300, false), 90);
                frame.recycle();
                List<Classifier.Recognition> results = new ArrayList<>();
                try {
                    List<Classifier.Recognition> res = detector.recognizeImage(scaledBitmap);
                    scaledBitmap.recycle();
                    for (Classifier.Recognition recognition : res) {
                        if (recognition.getConfidence() >= 0.4) {
                            results.add(recognition);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                imageViewBitmap = Bitmap.createBitmap(surfaceViewWidth, surfaceViewHeight, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(imageViewBitmap);

                for (Classifier.Recognition result : results) {
                    RectF rect = result.getLocation();

                    rect.left = rect.left * surfaceWidthScale;
                    rect.right = rect.right * surfaceWidthScale;
                    rect.top = rect.top * surfaceHeightScale;
                    rect.bottom = rect.bottom * surfaceHeightScale;

                    int productColor = Utilities.getProductColor(result.getTitle());
                    framePaint.setColor(productColor);
                    titlePaint.setColor(productColor);

                    canvas.drawRect(rect, framePaint);
                    canvas.drawText(result.getTitle(), rect.left, rect.top - 10, titlePaint);
                }

                runOnUiThread(() -> {
                    cameraImageView.setImageBitmap(imageViewBitmap);
                    computingDetection = false;
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
