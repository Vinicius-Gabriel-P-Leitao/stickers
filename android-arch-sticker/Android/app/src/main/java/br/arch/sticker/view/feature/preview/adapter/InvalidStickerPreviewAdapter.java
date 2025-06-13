/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.view.feature.preview.viewholder.InvalidStickerListViewHolder;

public class InvalidStickerPreviewAdapter extends RecyclerView.Adapter<InvalidStickerListViewHolder> {
    @NonNull
    private final List<Sticker> stickerList;

    private int maxNumberOfStickersInARow;
    private int minMarginBetweenImages;

    public InvalidStickerPreviewAdapter(@NonNull List<Sticker> stickerList) {
        this.stickerList = stickerList;
    }

    @NonNull
    @Override
    public InvalidStickerListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull InvalidStickerListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
