package com.client.imagerecognition;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

class MemoryCache {

    static void LoadImageThumbnail(ImageView imageView, String filePath, int width) {
        File file = new File(filePath);
        Picasso.get().load(file).resize(width, width).centerCrop().error(R.drawable.ic_launcher_foreground).into(imageView);
    }
}
