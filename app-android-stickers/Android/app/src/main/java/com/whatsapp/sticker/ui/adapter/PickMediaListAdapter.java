package com.whatsapp.sticker.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.whatsapp.sticker.R;
import com.whatsapp.sticker.ui.view.CropSquareTransformation;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PickMediaListAdapter extends RecyclerView.Adapter<PickMediaListAdapter.ImageViewHolder> {
    private final Context context;
    private final List<String> imagePaths;
    private final Set<Integer> selectedItems = new HashSet<>();

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    public PickMediaListAdapter(Context context, List<String> imagePaths, OnItemClickListener listener) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_ITEM;
    }

    public Set<String> getSelectedImagePaths() {
        Set<String> selectedPaths = new HashSet<>();
        for (Integer index : selectedItems) {
            if (index >= 0 && index < imagePaths.size()) {
                selectedPaths.add(imagePaths.get(index));
            }
        }
        return selectedPaths;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String path = imagePaths.get(position);
        Uri uri = Uri.fromFile(new File(path));

        RequestManager glide = Glide.with(holder.imageView.getContext());
        if (path.endsWith(".mp4") || path.endsWith(".webm") || path.endsWith(".3gp")) {
            glide.load(uri).thumbnail(0.1f).override(300, 300).centerCrop().into(holder.imageView);
        } else {
            glide.load(uri).override(300, 300).centerCrop().transform(new CropSquareTransformation(10f, 2, Color.GRAY)).into(holder.imageView);
        }

        holder.radioButton.setChecked(selectedItems.contains(position));
        holder.radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.add(position);
            } else {
                selectedItems.remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String imagePath);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        RadioButton radioButton;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            radioButton = itemView.findViewById(R.id.radio_button);
        }
    }
}
