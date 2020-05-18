package com.client.imagerecognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class GalleryView extends FrameLayout {

    private RecyclerView gallery;
    private GalleryAdapter adapter;


    public GalleryView(@NonNull Context context) {
        super(context);
        Initialize(context);
    }

    public GalleryView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Initialize(context);
    }

    public GalleryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Initialize(context);
    }

    public GalleryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Initialize(context);
    }

    private void Initialize(Context context) {
        gallery = new RecyclerView(context);
        addView(gallery, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int ItemPerRowCount = 3;
        int InteritemSpacing = 5;
        int preferredItemSize = (screenWidth - (ItemPerRowCount - 1) * InteritemSpacing) / ItemPerRowCount;

        adapter = new GalleryAdapter(preferredItemSize);
        adapter.ImageItems = new ArrayList<>();

        gallery.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false);
        gallery.setLayoutManager(gridLayoutManager);
    }

    public void addImage(String filePath) {
        adapter.ImageItems.add(0, filePath);
        adapter.notifyItemInserted(0);
    }

    public void setImages(List<String> imagePaths) {
        adapter.ImageItems.clear();
        adapter.ImageItems.addAll(0, imagePaths);
        adapter.notifyDataSetChanged();
    }

    class OnImageClickListener implements OnClickListener {

        private String filePath;

        OnImageClickListener(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void onClick(View view) {
            // TODO: Start activity
            Intent intent = new Intent(getContext(), DetectionActivity.class);
            intent.putExtra(DetectionActivity.DETECTION_IMAGE_FILE_PATH, filePath);
            getContext().startActivity(intent);
        }
    }

    class GalleryAdapter extends RecyclerView.Adapter {
        List<String> ImageItems;
        private Integer thumbnailWidth;

        GalleryAdapter(Integer thumbnailWidth) {
            this.thumbnailWidth = thumbnailWidth;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.image_item_view, viewGroup, false);
            return new ImageViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            ImageViewHolder holder = (ImageViewHolder) viewHolder;
            MemoryCache.LoadImageThumbnail(holder.Image, ImageItems.get(position), thumbnailWidth);
            holder.setOnclickListener(new OnImageClickListener(ImageItems.get(position)));
            holder.Image.invalidate();
        }

        @Override
        public int getItemCount() {
            return ImageItems != null
                    ? ImageItems.size()
                    : 0;
        }

        private class ImageViewHolder extends RecyclerView.ViewHolder {
            SquareImageView Image;

            void setOnclickListener(OnImageClickListener clickListener) {
                Image.setOnClickListener(clickListener);
            }

            ImageViewHolder(@NonNull View itemView) {
                super(itemView);

                Image = itemView.findViewById(R.id.image_view);
            }
        }
    }
}
