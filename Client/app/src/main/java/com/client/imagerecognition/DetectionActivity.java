package com.client.imagerecognition;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionActivity extends AppCompatActivity implements ISendImageCallback {

    public final static String DETECTION_IMAGE_FILE_PATH = "com.client.imagerecognition.DETECTION_IMAGE_FILE_PATH";
    public final static String MODEL_FILE_NAME = "model.tflite";
    public final static String LABEL_FILE_NAME = "label_map.txt";
    public final static Integer TENSOR_INPUT_SIZE = 300;
    public final static Boolean IS_QUANTIZED = false;

    private ImageView detectionImageView;
    private String filePath;
    private Classifier detector;
    private ProgressDialog progressDialog;
    private Paint rectanglePaint;
    private Paint titlePaint;
    private float imageWidth;
    private float imageHeight;
    private long serverStartTime;
    private long serverEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        filePath = getIntent().getStringExtra(DETECTION_IMAGE_FILE_PATH);
        detectionImageView = findViewById(R.id.detection_image_view);


        BitmapFactory.Options imageOptions = new BitmapFactory.Options();
        imageOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, imageOptions);
        imageWidth = imageOptions.outWidth;
        imageHeight = imageOptions.outHeight;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float screenWidth = metrics.widthPixels;
        float ratio = imageOptions.outWidth / screenWidth;

        int width = metrics.widthPixels;
        int height = (int)(imageOptions.outHeight / ratio);

        Picasso.get().load(new File(filePath)).resize(width, height).memoryPolicy(MemoryPolicy.NO_CACHE).into(detectionImageView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        rectanglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectanglePaint.setStyle(Paint.Style.STROKE);
        rectanglePaint.setStrokeWidth(8);
        titlePaint = new Paint();
        titlePaint.setTextScaleX(2);

        try {
            detector = ObjectDetectionModel.create(getAssets(), MODEL_FILE_NAME, LABEL_FILE_NAME, TENSOR_INPUT_SIZE, IS_QUANTIZED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Picasso.get().invalidate(filePath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detextion_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_local_detection:
                startLocalDetection();
                return true;
            case R.id.action_server_detection:
                startDetectionOnServer();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnServerDetectionCompleted(String detectionResult) {
        Gson gson = new GsonBuilder().create();
        ArrayList serverDetectionResults = gson.fromJson(detectionResult, ArrayList.class);
        List<Classifier.Recognition> results = new ArrayList<>();
        for (Object res : serverDetectionResults) {
            LinkedTreeMap<String, Double> treeMap = (LinkedTreeMap<String, Double>) res;

            String id = String.valueOf(convertDoubleToInt(treeMap.get("class_id")));
            String title = Utilities.getProductTitle(convertDoubleToInt(treeMap.get("class_id")));
            float score = convertDoubleToFloat(treeMap.get("score"));
            float topLeftX = convertDoubleToFloat(treeMap.get("top_left_x"));
            float topLeftY = convertDoubleToFloat(treeMap.get("top_left_y"));
            float bottomRightX = convertDoubleToFloat(treeMap.get("bottom_right_x"));
            float bottomRightY = convertDoubleToFloat(treeMap.get("bottom_right_y"));
            RectF rect = new RectF(topLeftX, topLeftY, bottomRightX, bottomRightY);
            Classifier.Recognition item = new Classifier.Recognition(id, title, score, rect);
            results.add(item);
        }

        serverEndTime = System.currentTimeMillis() - serverStartTime;
        BitmapDrawable drawable = (BitmapDrawable) detectionImageView.getDrawable();
        Bitmap imageViewBitmap = drawable.getBitmap();


        DrawDetectionResults(results, imageViewBitmap, imageViewBitmap.getWidth() / 300.0f, imageViewBitmap.getHeight() / 300.0f);

        runOnUiThread(() ->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Time: " + serverEndTime + " ms");
            builder.setNeutralButton("Ok", null);
            builder.show();
        });
    }

    private void startDetectionOnServer() {
        progressDialog.show();
        serverStartTime = System.currentTimeMillis();
        new SendImageTask(filePath, this).execute();
    }

    private void startLocalDetection() {
        long start = System.currentTimeMillis();

        progressDialog.show();

        AsyncTask.execute(() -> {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, TENSOR_INPUT_SIZE, TENSOR_INPUT_SIZE, false);
            List<Classifier.Recognition> results = new ArrayList<>();
            try{
                List<Classifier.Recognition> res = detector.recognizeImage(scaledBitmap);
                for (Classifier.Recognition recognition: res) {
                    if (recognition.getConfidence() >= 0.5f) {
                        results.add(recognition);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            long time = System.currentTimeMillis() - start;
            System.out.println("AAAAA Time: " + time);

            BitmapDrawable drawable = (BitmapDrawable) detectionImageView.getDrawable();
            Bitmap imageViewBitmap = drawable.getBitmap();

            float widthDiff = imageViewBitmap.getWidth() / 300.0f;
            float heightDiff = imageViewBitmap.getHeight() / 300.0f;

            DrawDetectionResults(results, imageViewBitmap, widthDiff, heightDiff);

            runOnUiThread(() ->{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Time: " + time + " ms");
                builder.setNeutralButton("Ok", null);
                builder.show();
            });
        });
    }

    private void DrawDetectionResults(List<Classifier.Recognition> results, Bitmap imageViewBitmap, float widthDiff, float heightDiff) {
        Canvas canvas = new Canvas(imageViewBitmap);

        for (Classifier.Recognition result: results) {
            RectF rect = result.getLocation();

            rect.left = rect.left * widthDiff;
            rect.right = rect.right * widthDiff;
            rect.top = rect.top * heightDiff;
            rect.bottom = rect.bottom * heightDiff;

            Integer productColor = Utilities.getProductColor(result.getTitle());
            rectanglePaint.setColor(productColor);
            titlePaint.setColor(productColor);

            canvas.drawRect(rect, rectanglePaint);
            canvas.drawText(result.getTitle(), rect.left, rect.top - 10, titlePaint);
        }

        runOnUiThread(() ->{
            detectionImageView.setImageBitmap(imageViewBitmap);
            progressDialog.dismiss();
        });
    }

    private int convertDoubleToInt (Double value) {
        return (int)(double)value;
    }
    private float convertDoubleToFloat (Double value) {
        return (float)(double)value;
    }
}
