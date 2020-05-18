package com.client.imagerecognition;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class DetectionActivity extends AppCompatActivity {

    public final static String DETECTION_IMAGE_FILE_PATH = "com.client.imagerecognition.DETECTION_IMAGE_FILE_PATH";

    private ImageView detectionImageView;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        filePath = getIntent().getStringExtra(DETECTION_IMAGE_FILE_PATH);
        detectionImageView = findViewById(R.id.detection_image_view);

        Picasso.get().load(new File(filePath)).fit().into(detectionImageView);
    }
}
