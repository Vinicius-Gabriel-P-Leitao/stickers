/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.media.adapter;

import static com.vinicius.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MAX;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.MultiTransformation;
import com.vinicius.sticker.R;
import com.vinicius.sticker.view.feature.media.transformation.CropSquareTransformation;
import com.vinicius.sticker.view.feature.media.viewholder.MediaViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PickMediaListAdapter extends ListAdapter<Uri, MediaViewHolder> {
   public static final String PAYLOAD_SELECTION_CHANGED = "PAYLOAD_SELECTION_CHANGED";
   private final List<Integer> selectedItems = new ArrayList<>();
   private final Context context;

   public interface OnItemClickListener {
      void onItemClick(String imagePath);
   }

   public PickMediaListAdapter(Context context, OnItemClickListener itemClickListener) {
      super(new UriDiffCallback());
      this.context = context;
   }

   public Set<Uri> getSelectedMediaPaths() {
      Set<Uri> selectedPaths = new HashSet<>();
      for (Integer index : selectedItems) {
         if ( index >= 0 && index < getCurrentList().size() ) {
            selectedPaths.add(getCurrentList().get(index));
         }
      }
      return selectedPaths;
   }

   @NonNull
   @Override
   public MediaViewHolder onCreateViewHolder(
       @NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.container_thumbnail_media, parent, false);
      return new MediaViewHolder(view);
   }

   @Override
   public void onBindViewHolder(
       @NonNull MediaViewHolder holder, int position) {
      Uri uri = getItem(position);
      String fileName = new File(Objects.requireNonNull(uri.getPath())).getName();
      String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

         RequestManager glide = Glide.with(holder.imageView.getContext());

         MultiTransformation<Bitmap> commonTransform = new MultiTransformation<>(new CropSquareTransformation(10f, 5, R.color.catppuccin_overlay2));

         RequestBuilder<?> requestBuilder;
         if ( extension.endsWith(".mp4") || extension.endsWith(".webm") || extension.endsWith(".3gp") ) {
            requestBuilder = glide.asBitmap().frame(1_000_000).load(uri);
         } else if ( extension.endsWith(".gif") ) {
            requestBuilder = glide.asGif().load(uri);
         } else {
            requestBuilder = glide.load(uri);
         }
         requestBuilder.override(300, 300).centerCrop().transform(commonTransform).into(holder.imageView);

      holder.radioCheckBox.setChecked(selectedItems.contains(position));
      if ( selectedItems.contains(position) ) {
         int index = selectedItems.indexOf(position);
         if ( index >= 0 ) {
            int sequenceNumber = index + 1;
            // NOTE: espaço é para dar um "padding" no final do botão
            holder.radioCheckBox.setText(String.format("%s  ", sequenceNumber));
         } else {
            holder.radioCheckBox.setText("0  ");
         }
      } else {
         holder.radioCheckBox.setText("0  ");
      }

      holder.radioCheckBox.setOnClickListener(view -> {
         int adapterPosition = holder.getAbsoluteAdapterPosition();
         if ( adapterPosition == RecyclerView.NO_POSITION )
            return;

         if ( selectedItems.size() < STICKER_SIZE_MAX ) {
            if ( selectedItems.contains(adapterPosition) ) {
               selectedItems.remove((Integer) adapterPosition);
               holder.radioCheckBox.setChecked(false);
            } else {
               selectedItems.add(adapterPosition);
               holder.radioCheckBox.setChecked(true);
            }
         } else {
            Toast.makeText(view.getContext(), "Numero máximo de itens selecionados!", Toast.LENGTH_SHORT).show();
         }

         for (Integer pos : selectedItems) {
            notifyItemChanged(pos, PAYLOAD_SELECTION_CHANGED);
         }

         notifyItemChanged(adapterPosition, PAYLOAD_SELECTION_CHANGED);
      });
   }

   @Override
   public void onBindViewHolder(
       @NonNull MediaViewHolder holder, int position,
       @NonNull List<Object> payloads
   ) {
      if ( !payloads.isEmpty() ) {
         updateSelectionUI(holder, position);
      } else {
         super.onBindViewHolder(holder, position, payloads);
      }
   }

   @SuppressLint("DefaultLocale")
   private void updateSelectionUI(MediaViewHolder holder, int position) {
      holder.radioCheckBox.setChecked(selectedItems.contains(position));

      if ( selectedItems.contains(position) ) {
         int index = selectedItems.indexOf(position);
         if ( index >= 0 ) {
            int sequenceNumber = index + 1;
            // NOTE: espaços depois é necessário para um "padding"
            holder.radioCheckBox.setText(String.format("%d  ", sequenceNumber));
         } else {
            holder.radioCheckBox.setText("0  ");
         }
      } else {
         holder.radioCheckBox.setText("0  ");
      }
   }

   public static class UriDiffCallback extends DiffUtil.ItemCallback<Uri> {

      @Override
      public boolean areItemsTheSame(
          @NonNull Uri oldItem,
          @NonNull Uri newItem
      ) {
         return Objects.equals(oldItem, newItem);
      }

      @Override
      public boolean areContentsTheSame(
          @NonNull Uri oldItem,
          @NonNull Uri newItem
      ) {
         return Objects.equals(oldItem, newItem);
      }
   }
}