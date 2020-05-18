package com.client.imagerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.widget.ImageView;

public class MemoryCache {

    public static void LoadImageThumbnail(ImageView imageView, String filePath, int width) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, width, width);
        imageView.setImageBitmap(thumbnail);
    }
}
