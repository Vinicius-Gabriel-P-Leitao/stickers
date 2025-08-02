/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
package br.arch.sticker.view.feature.preview.adapter;

import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_ANIMATED;
import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_STATIC;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.util.BuildStickerUri;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.feature.preview.viewholder.InvalidStickerListViewHolder;

public class PreviewInvalidStickerAdapter extends RecyclerView.Adapter<InvalidStickerListViewHolder> {
    @NonNull
    private final String stickerPackIdentifier;
    @NonNull
    private final List<Sticker> stickerList;
    @Nullable
    private final StickerPack stickerPack;

    private int maxNumberOfStickersInARow;

    public interface OnFixClickListener {
        void onStickerFixClick(Sticker sticker, String stickerPackIdentifier);
    }

    private final OnFixClickListener listener;

    public PreviewInvalidStickerAdapter(@NonNull String stickerPackIdentifier, @NonNull List<Sticker> stickerList, OnFixClickListener listener) {
        this.stickerPackIdentifier = stickerPackIdentifier;
        this.stickerList = filterValidStickers(stickerList);
        this.stickerPack = null;
        this.listener = listener;
    }

    public PreviewInvalidStickerAdapter(@NonNull StickerPack stickerPack, OnFixClickListener listener) {
        this.stickerPack = stickerPack;
        this.stickerList = filterValidStickers(stickerPack.getStickers());
        this.stickerPackIdentifier = stickerPack.identifier;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvalidStickerListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickersRow = layoutInflater.inflate(R.layout.container_invalid_sticker, viewGroup, false);
        return new InvalidStickerListViewHolder(stickersRow);
    }

    @Override
    public void onBindViewHolder(@NonNull InvalidStickerListViewHolder viewHolder, int position) {
        final Context context = viewHolder.itemView.getContext();
        final Sticker sticker = stickerList.get(position);

        ErrorCode code = ErrorCode.fromName(sticker.stickerIsValid);
        int resId = (code != null) ? code.getMessageResId() : R.string.error_unknown;

        if (!stickerList.isEmpty()) {
            viewHolder.stickerPreview.setImageURI(BuildStickerUri.buildStickerAssetUri(stickerPackIdentifier, sticker.imageFileName));
            viewHolder.textErrorMessage.setText(context.getString(resId));
        }

        if (stickerPack != null) {
            viewHolder.stickerPreview.setImageURI(BuildStickerUri.buildStickerAssetUri(stickerPackIdentifier, sticker.imageFileName));
            viewHolder.textErrorMessage.setText(
                    TextUtils.isEmpty(sticker.stickerIsValid) ? context.getString(R.string.information_sticker_is_valid) : context.getString(resId));
            viewHolder.buttonFix.setVisibility(TextUtils.isEmpty(sticker.stickerIsValid) ? View.GONE : View.VISIBLE);
        }

        viewHolder.buttonFix.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                long now = SystemClock.elapsedRealtime();
                if (now - lastClickTime < 1000) return;
                lastClickTime = now;

                if (listener != null) {
                    listener.onStickerFixClick(sticker, stickerPackIdentifier);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return stickerList != null ? stickerList.size() : 0;
    }

    private static ArrayList<Sticker> filterValidStickers(@NonNull List<Sticker> rawList) {
        ArrayList<Sticker> result = new ArrayList<>();
        for (Sticker sticker : rawList) {
            if (!PLACEHOLDER_ANIMATED.equals(sticker.imageFileName) && !PLACEHOLDER_STATIC.equals(sticker.imageFileName)) {
                result.add(sticker);
            }
        }
        return result;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setImageRowSpec(int maxNumberOfStickersInARow, int minMarginBetweenImages) {
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    public void removeSticker(Sticker sticker) {
        int pos = stickerList.indexOf(sticker);
        if (pos >= 0) {
            stickerList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateStickerPackItems(List<Sticker> newItems) {
        this.stickerList.clear();
        this.stickerList.addAll(newItems);
        notifyDataSetChanged();
    }
}
