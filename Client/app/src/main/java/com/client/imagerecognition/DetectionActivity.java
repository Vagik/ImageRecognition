package com.client.imagerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DetectionActivity extends AppCompatActivity {

    public final static String DETECTION_IMAGE_FILE_PATH = "com.client.imagerecognition.DETECTION_IMAGE_FILE_PATH";

    private ImageView detectionImageView;
    private String filePath;
    private Classifier detector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        filePath = getIntent().getStringExtra(DETECTION_IMAGE_FILE_PATH);
        detectionImageView = findViewById(R.id.detection_image_view);

        Picasso.get().load(new File(filePath)).fit().into(detectionImageView);
        try {
            detector = ObjectDetectionModel.create(getAssets(), "model.tflite", "label_map.txt", 300, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
        try{
            List<Classifier.Recognition> results = detector.recognizeImage(scaledBitmap);
            int a = 5;
        } catch (Exception e){
            e.printStackTrace();
        }

        Bitmap bitmapd = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapd);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(50, 50, 10, paint);
        detectionImageView.setImageBitmap(bitmapd);
    }
}
