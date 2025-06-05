/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package com.vinicius.sticker.view.feature.stickerpack.list.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vinicius.sticker.R;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.dto.StickerPackWithInvalidStickers;
import com.vinicius.sticker.domain.service.fetch.FetchStickerAssetService;
import com.vinicius.sticker.view.feature.stickerpack.details.activity.StickerPackDetailsActivity;
import com.vinicius.sticker.view.feature.stickerpack.list.model.StickerPackListItem;
import com.vinicius.sticker.view.feature.stickerpack.list.viewholder.StickerPackListViewHolder;

import java.util.List;

// @formatter:off
public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListViewHolder> {
    @NonNull
    private final OnAddButtonClickedListener onAddButtonClickedListener;
    @NonNull
    private final List<StickerPackListItem> stickerPackListItems;

    private int maxNumberOfStickersInARow;
    private int minMarginBetweenImages;

    public StickerPackListAdapter(
            @NonNull List<StickerPackListItem> stickerPackListItems, @NonNull OnAddButtonClickedListener onAddButtonClickedListener) {
        this.stickerPackListItems = stickerPackListItems;
        this.onAddButtonClickedListener = onAddButtonClickedListener;
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);
    }

    @NonNull
    @Override
    public StickerPackListViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListViewHolder viewHolder, final int index) {
        StickerPackListItem stickerPackListItem = stickerPackListItems.get(index);
        final Context context = viewHolder.publisherView.getContext();

        switch (stickerPackListItem.getStatus()) {
            case VALID:
                StickerPack validStickerPack = (StickerPack) stickerPackListItem.getStickerPack();
                bindStickerPack(context, viewHolder, validStickerPack, null, true);
                break;

            case INVALID:
                StickerPack invalidStickerPack = (StickerPack) stickerPackListItem.getStickerPack();
                bindStickerPack(context, viewHolder, invalidStickerPack, null, false);
                break;

            case WITH_INVALID_STICKER:
                StickerPackWithInvalidStickers invalidStickers = (StickerPackWithInvalidStickers) stickerPackListItem.getStickerPack();
                bindStickerPack(context, viewHolder, invalidStickers.stickerPack, invalidStickers.invalidStickers, true);
                break;
        }
    }

    private void bindStickerPack(@NonNull Context context,@NonNull StickerPackListViewHolder viewHolder,@NonNull StickerPack stickerPack, @Nullable List<Sticker> stickers, boolean isValid) {
        viewHolder.publisherView.setText(stickerPack.publisher);
        viewHolder.filesizeView.setText(Formatter.formatShortFileSize(context, stickerPack.getTotalSize()));

        viewHolder.titleView.setText(stickerPack.name);
        viewHolder.container.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);

            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPack);

            view.getContext().startActivity(intent);
        });

        viewHolder.imageRowView.removeAllViews();

        int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, stickerPack.getStickers().size());
        for (int i = 0; i < actualNumberOfStickersToShow; i++) {
            final ImageView rowImage = (ImageView) LayoutInflater.from(context)
                    .inflate(R.layout.sticker_packs_list_media_item, viewHolder.imageRowView, false);

            rowImage.setImageURI(
                    FetchStickerAssetService.buildStickerAssetUri(stickerPack.identifier, stickerPack.getStickers().get(i).imageFileName));

            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
            final int marginBetweenImages = minMarginBetweenImages - layoutParams.leftMargin - layoutParams.rightMargin;

            if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) {
                layoutParams.setMargins(
                        layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin + marginBetweenImages,
                        layoutParams.bottomMargin);
                rowImage.setLayoutParams(layoutParams);
            }

            viewHolder.imageRowView.addView(rowImage);
        }

        setAddValidButtonAppearance(viewHolder.addButton, stickerPack);
        viewHolder.animatedStickerPackIndicator.setVisibility(stickerPack.animatedStickerPack ? View.VISIBLE : View.GONE);
    }

    private void setAddValidButtonAppearance(ImageView addButton, StickerPack pack) {
        if (pack.getIsWhitelisted()) {
            addButton.setImageResource(R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);

            setBackground(addButton);
        } else {
            addButton.setImageResource(R.drawable.sticker_3rdparty_add);
            addButton.setOnClickListener(view -> onAddButtonClickedListener.onAddButtonClicked(pack));

            TypedValue outValue = new TypedValue();

            addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            addButton.setBackgroundResource(outValue.resourceId);
        }
    }

    private void setBackground(View view) {
        view.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return stickerPackListItems.size();
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
    public void updateStickerPackItems(List<StickerPackListItem> newItems) {
        this.stickerPackListItems.clear();
        this.stickerPackListItems.addAll(newItems);
        notifyDataSetChanged();
    }
}
