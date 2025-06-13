/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.service.fetch.FetchStickerAssetService;
import br.arch.sticker.view.feature.preview.viewholder.InvalidStickerListViewHolder;

public class InvalidStickerPreviewAdapter extends RecyclerView.Adapter<InvalidStickerListViewHolder> {
    @NonNull
    private final String stickerPackIdentifier;
    @NonNull
    private final List<Sticker> stickerList;

    private int maxNumberOfStickersInARow;
    private int minMarginBetweenImages;

    public InvalidStickerPreviewAdapter(@NonNull String stickerPackIdentifier, @NonNull List<Sticker> stickerList) {
        this.stickerPackIdentifier = stickerPackIdentifier;
        this.stickerList = stickerList;
    }

    @NonNull
    @Override
    public InvalidStickerListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickersRow = layoutInflater.inflate(R.layout.invalid_stickers_list_item, viewGroup, false);
        return new InvalidStickerListViewHolder(stickersRow);
    }

    @Override
    public void onBindViewHolder(@NonNull InvalidStickerListViewHolder viewHolder, int position) {
        final Context context = viewHolder.stickerPreview.getContext();

        final Sticker sticker = stickerList.get(position);

        viewHolder.stickerPreview.setImageURI(FetchStickerAssetService.buildStickerAssetUri(stickerPackIdentifier, sticker.imageFileName));
        viewHolder.textErrorMessage.setText(sticker.stickerIsValid);

    }

    @Override
    public int getItemCount() {
        return stickerList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setImageRowSpec(int maxNumberOfStickersInARow, int minMarginBetweenImages) {
        this.minMarginBetweenImages = minMarginBetweenImages;
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateStickerPackItems(List<Sticker> newItems) {
        this.stickerList.clear();
        this.stickerList.addAll(newItems);
        notifyDataSetChanged();
    }
}
