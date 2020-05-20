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
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionActivity extends AppCompatActivity {

    public final static String DETECTION_IMAGE_FILE_PATH = "com.client.imagerecognition.DETECTION_IMAGE_FILE_PATH";

    private ImageView detectionImageView;
    private String filePath;
    private Classifier detector;
    private ProgressDialog progressDialog;
    public static Map<String, Integer> productsColors = new HashMap<String, Integer>() {{
        put("chips", Color.YELLOW);
        put("juice", Color.RED);
        put("lemonade", Color.BLUE);
        put("milk", Color.WHITE);
        put("peas", Color.GREEN);
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
        //Picasso.get().load(new File(filePath)).fit().centerCrop().memoryPolicy(MemoryPolicy.NO_CACHE).into(detectionImageView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        try {
            detector = ObjectDetectionModel.create(getAssets(), "model.tflite", "label_map.txt", 300, false);
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
                StartLocalDetection();
                return true;
            case R.id.action_server_detection:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void StartLocalDetection() {
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
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);

            Paint titlePaint = new Paint();
            titlePaint.setTextScaleX(2);
            for (Classifier.Recognition result: results) {
                RectF rect = result.getLocation();

                rect.left = rect.left * widthDiff;
                rect.right = rect.right * widthDiff;
                rect.top = rect.top * heightDiff;
                rect.bottom = rect.bottom * heightDiff;

                paint.setColor(productsColors.get(result.getTitle()));
                titlePaint.setColor(productsColors.get(result.getTitle()));

                canvas.drawRect(rect, paint);

                canvas.drawText(result.getTitle(), rect.left, rect.top - 10, titlePaint);
            }

            runOnUiThread(() ->{
                detectionImageView.setImageBitmap(imageViewBitmap);
                progressDialog.dismiss();
            });
        });

    }
}
