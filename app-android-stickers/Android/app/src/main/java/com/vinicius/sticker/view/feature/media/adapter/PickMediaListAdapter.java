/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */
package com.vinicius.sticker.view.feature.media.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.MultiTransformation;
import com.vinicius.sticker.R;
import com.vinicius.sticker.view.feature.media.util.CropSquareTransformation;
import com.vinicius.sticker.view.feature.media.viewholder.MediaViewHolder;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PickMediaListAdapter extends RecyclerView.Adapter<MediaViewHolder> {
   private final Context context;
   private final List<Uri> mediaUris;
   private final Set<Integer> selectedItems = new HashSet<>();

   public PickMediaListAdapter(Context context, List<Uri> mediaUris, OnItemClickListener itemClickListener) {
      this.context = context;
      this.mediaUris = mediaUris;
   }

   public Set<Uri> getSelectedMediaPaths() {
      Set<Uri> selectedPaths = new HashSet<>();
      for (Integer index : selectedItems) {
         if (index >= 0 && index < mediaUris.size()) {
            selectedPaths.add(mediaUris.get(index));
         }
      }
      return selectedPaths;
   }

   @NonNull
   @Override
   public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.container_thumbnail_media,
          parent,
          false);
      return new MediaViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
      Uri uri = mediaUris.get(position);
      String fileName = new File(Objects.requireNonNull(uri.getPath())).getName();
      String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

      RequestManager glide = Glide.with(holder.imageView.getContext());

      MultiTransformation<Bitmap> commonTransform = new MultiTransformation<>(
          new CropSquareTransformation(10f,
              5,
              R.color.catppuccin_overlay2)
      );

      RequestBuilder<?> requestBuilder;
      if (extension.endsWith(".mp4") || extension.endsWith(".webm") || extension.endsWith(".3gp")) {
         requestBuilder = glide.asBitmap().frame(1_000_000).load(uri);
      } else if (extension.endsWith(".gif")) {
         requestBuilder = glide.asGif().load(uri);
      } else {
         requestBuilder = glide.load(uri);
      }
      requestBuilder.override(300,
          300).centerCrop().transform(commonTransform).into(holder.imageView);

      holder.radioChip.setChecked(selectedItems.contains(position));
      holder.radioChip.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            if (selectedItems.contains(holder.getAbsoluteAdapterPosition())) {
               selectedItems.remove(holder.getAbsoluteAdapterPosition());
               holder.radioChip.setChecked(false);
            } else {
               selectedItems.add(holder.getAbsoluteAdapterPosition());
               holder.radioChip.setChecked(true);
            }
         }
      });
   }

   @Override
   public int getItemCount() {
      return mediaUris.size();
   }

   public interface OnItemClickListener {
      void onItemClick(String imagePath);
   }
}
