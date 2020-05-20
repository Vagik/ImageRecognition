package com.client.imagerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class Utilities {
    static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    static Bitmap convertYuvToJpeg(byte[] bytes, int previewFormat, int previewWidth, int previewHeight) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            YuvImage yuvImage = new YuvImage(bytes, previewFormat, previewWidth, previewHeight, null);
            yuvImage.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight), 90, out);
            byte[] imageBytes = out.toByteArray();
            out.flush();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
