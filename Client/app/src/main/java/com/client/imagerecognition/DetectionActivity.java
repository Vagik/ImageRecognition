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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

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

    private ImageView detectionImageView;
    private String filePath;
    private Classifier detector;
    private ProgressDialog progressDialog;
    private Paint rectanglePaint;
    private Paint titlePaint;
    public static Map<String, Integer> productsColors = new HashMap<String, Integer>() {{
        put("chips", Color.YELLOW);
        put("juice", Color.RED);
        put("lemonade", Color.BLUE);
        put("milk", Color.WHITE);
        put("peas", Color.GREEN);
    }};

    public static Map<Integer, String> productsTitles = new HashMap<Integer, String>() {{
        put(1, "chips");
        put(2, "juice");
        put(3, "lemonade");
        put(4, "milk");
        put(5, "peas");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        filePath = getIntent().getStringExtra(DETECTION_IMAGE_FILE_PATH);
        detectionImageView = findViewById(R.id.detection_image_view);


        BitmapFactory.Options imageOptions = new BitmapFactory.Options();
        imageOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, imageOptions);
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
            detector = ObjectDetectionModel.create(getAssets(), MODEL_FILE_NAME, LABEL_FILE_NAME, TENSOR_INPUT_SIZE, false);
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

    private void startDetectionOnServer() {
        progressDialog.show();
        new SendImageTask(filePath, this).execute();
    }

    private void startLocalDetection() {
        progressDialog.show();

        AsyncTask.execute(() -> {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
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


            BitmapDrawable drawable = (BitmapDrawable) detectionImageView.getDrawable();
            Bitmap imageViewBitmap = drawable.getBitmap();

            float widthDiff = imageViewBitmap.getWidth() / 300.0f;
            float heightDiff = imageViewBitmap.getHeight() / 300.0f;

            Canvas canvas = new Canvas(imageViewBitmap);

            for (Classifier.Recognition result: results) {
                RectF rect = result.getLocation();

                rect.left = rect.left * widthDiff;
                rect.right = rect.right * widthDiff;
                rect.top = rect.top * heightDiff;
                rect.bottom = rect.bottom * heightDiff;

                Integer productColor = productsColors.get(result.getTitle());
                rectanglePaint.setColor(productColor);
                titlePaint.setColor(productColor);

                canvas.drawRect(rect, rectanglePaint);
                canvas.drawText(result.getTitle(), rect.left, rect.top - 10, titlePaint);
            }

            runOnUiThread(() ->{
                detectionImageView.setImageBitmap(imageViewBitmap);
                progressDialog.dismiss();
            });
        });
    }

    private void DrawDetectionResults(List<Classifier.Recognition> results, float widthDiff, float heightDiff) {
        BitmapDrawable drawable = (BitmapDrawable) detectionImageView.getDrawable();
        Bitmap imageViewBitmap = drawable.getBitmap();

        Canvas canvas = new Canvas(imageViewBitmap);

        for (Classifier.Recognition result: results) {
            RectF rect = result.getLocation();

            rect.left = rect.left * widthDiff;
            rect.right = rect.right * widthDiff;
            rect.top = rect.top * heightDiff;
            rect.bottom = rect.bottom * heightDiff;

            Integer productColor = productsColors.get(result.getTitle());
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

    @Override
    public void OnServerDetectionCompleted(String detectionResult) {
        Gson gson = new GsonBuilder().create();
        ArrayList<ServerDetectionResult> serverDetectionResults = gson.fromJson(detectionResult, ArrayList.class);
        List<Classifier.Recognition> results = new ArrayList<>();
        for (ServerDetectionResult res : serverDetectionResults) {
            String id = String.valueOf(res.class_id);
            String title = productsTitles.get(res.class_id);
            float score = res.score;
            RectF rect = new RectF(res.top_left_x, res.top_left_y, res.bottom_right_x, res.bottom_right_y);
            Classifier.Recognition item = new Classifier.Recognition(id, title, score, rect);
            results.add(item);
        }

        DrawDetectionResults(results, 1, 1);
    }
}
