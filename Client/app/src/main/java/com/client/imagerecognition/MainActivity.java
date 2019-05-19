package com.client.imagerecognition;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private final static int CHECK_PERMISSIONS_REQUEST_CODE = 1000;
    private final static int PICK_FILE_REQUEST_CODE = 1001;
    private final static int TAKE_PHOTO_REQUEST_CODE = 1002;

    private String selectedImagePath;
    private AlertDialog.Builder alertDialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPermission()) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA}, CHECK_PERMISSIONS_REQUEST_CODE);
        }

        alertDialog = new AlertDialog.Builder(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");

        findViewById(R.id.pickFileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FILE_REQUEST_CODE);
            }
        });

        findViewById(R.id.takePhotoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile;
                try {
                    photoFile = createImageFile();
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                "com.client.imagerecognition.fileprovider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        findViewById(R.id.recognizeImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImagePath != null) {
                    progressDialog.show();
                    new SendImageTask(selectedImagePath, progressDialog, alertDialog).execute();
                } else {
                    alertDialog
                            .setTitle("Error")
                            .setMessage("Please select image to recognize")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_PERMISSIONS_REQUEST_CODE:
                break;
            case PICK_FILE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    selectedImagePath = cursor.getString(columnIndex);
                    cursor.close();
                }
                break;
            case TAKE_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED) {
                    selectedImagePath = null;
                }
                break;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Image_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        selectedImagePath = image.getAbsolutePath();
        return image;
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

}
