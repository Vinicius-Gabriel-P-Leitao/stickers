/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.adapter;

import static br.arch.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MAX;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.OpenableColumns;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import br.arch.sticker.R;
import br.arch.sticker.view.core.util.transformation.CropSquareTransformation;
import br.arch.sticker.view.feature.stickerpack.creation.viewholder.MediaViewHolder;

public class MediaPickerAdapter extends ListAdapter<Uri, MediaViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String imagePath);
    }

    public static final String PAYLOAD_SELECTION_CHANGED = "payload_selection_changed";

    private final List<Integer> selectedItems = new ArrayList<>();
    private final Context context;

    public MediaPickerAdapter(Context context, OnItemClickListener itemClickListener) {
        super(new UriDiffCallback());
        this.context = context;
    }

    public Set<Uri> getSelectedMediaPaths() {
        Set<Uri> selectedPaths = new HashSet<>();

        for (Integer index : selectedItems) {
            if (index >= 0 && index < getCurrentList().size()) {
                selectedPaths.add(getCurrentList().get(index));
            }
        }

        return selectedPaths;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.container_thumbnail_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        final Uri uri = getItem(position);
        String fileName = getFileNameFromUri(holder.itemView.getContext(), uri);

        if (fileName.isBlank()) {
            Toast.makeText(context, context.getString(R.string.error_message_file_not_found), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        RequestManager glide = Glide.with(holder.imageView.getContext());
        MultiTransformation<Bitmap> commonTransform = new MultiTransformation<>(new CropSquareTransformation(10f, 5, R.color.catppuccin_overlay2));
        RequestBuilder<?> requestBuilder = null;

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (extension.endsWith("mp4") || extension.endsWith("webm")) {
            requestBuilder = glide.asBitmap().frame(1_000_000).load(uri);
        }

        if (extension.endsWith("gif")) {
            requestBuilder = glide.asGif().load(uri);
        }

        if (extension.endsWith("jpeg") || extension.endsWith("jpg") || extension.endsWith("png")) {
            requestBuilder = glide.load(uri);
        }

        if (requestBuilder == null) {
            // TODO: Fazer carregar um placeholder
            Toast.makeText(context, context.getString(R.string.error_message_unsuported_media_type_in_gallery), Toast.LENGTH_LONG)
                    .show();
            return;
        }

        requestBuilder.override(300, 300).centerCrop().transform(commonTransform).into(holder.imageView);

        holder.radioCheckBox.setChecked(selectedItems.contains(position));
        if (selectedItems.contains(position)) {
            int index = selectedItems.indexOf(position);

            if (index >= 0) {
                int sequenceNumber = index + 1;
                holder.radioCheckBox.setText(String.format("%s  ", sequenceNumber));
            } else {
                holder.radioCheckBox.setText("");
            }
        } else {
            holder.radioCheckBox.setText("");
        }

        holder.radioCheckBox.setOnClickListener(view -> {
            int adapterPosition = holder.getAbsoluteAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            if (selectedItems.size() > STICKER_SIZE_MAX) {
                Toast.makeText(context, context.getString(R.string.error_message_max_media_selected), Toast.LENGTH_SHORT)
                        .show();
            }

            if (selectedItems.size() < STICKER_SIZE_MAX) {
                if (selectedItems.contains(adapterPosition)) {
                    selectedItems.remove((Integer) adapterPosition);
                    holder.radioCheckBox.setChecked(false);
                } else {
                    selectedItems.add(adapterPosition);
                    holder.radioCheckBox.setChecked(true);
                }
            }

            for (Integer pos : selectedItems) {
                notifyItemChanged(pos, PAYLOAD_SELECTION_CHANGED);
            }

            notifyItemChanged(adapterPosition, PAYLOAD_SELECTION_CHANGED);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            updateSelectionUI(holder, position);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    private static String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }

        if (uri.getPath() == null) {
            Toast.makeText(context, context.getString(R.string.error_message_could_not_extract_path_media, uri), Toast.LENGTH_SHORT)
                    .show();
            return "";
        }

        if (result == null) {
            result = new File(uri.getPath()).getName();
        }

        return result;
    }

    @SuppressLint("DefaultLocale")
    private void updateSelectionUI(MediaViewHolder holder, int position) {
        holder.radioCheckBox.setChecked(selectedItems.contains(position));

        if (selectedItems.contains(position)) {
            int index = selectedItems.indexOf(position);
            if (index >= 0) {
                int sequenceNumber = index + 1;
                // NOTE: espaços depois é necessário para um "padding"
                holder.radioCheckBox.setText(String.format("%d  ", sequenceNumber));
            } else {
                holder.radioCheckBox.setText("");
            }
        } else {
            holder.radioCheckBox.setText("");
        }
    }

    static class UriDiffCallback extends DiffUtil.ItemCallback<Uri> {
        @Override
        public boolean areItemsTheSame(@NonNull Uri oldItem, @NonNull Uri newItem) {
            return Objects.equals(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Uri oldItem, @NonNull Uri newItem) {
            return Objects.equals(oldItem, newItem);
        }
    }
}