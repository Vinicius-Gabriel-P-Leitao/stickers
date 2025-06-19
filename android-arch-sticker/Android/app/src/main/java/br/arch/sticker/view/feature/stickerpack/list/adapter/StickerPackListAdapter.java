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

package br.arch.sticker.view.feature.stickerpack.list.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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

import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.util.BuildStickerUri;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.StickerPackWithInvalidStickers;
import br.arch.sticker.view.core.model.StickerPackListItem;
import br.arch.sticker.view.feature.stickerpack.list.activity.StickerPackListActivity;
import br.arch.sticker.view.feature.stickerpack.list.viewholder.StickerPackListViewHolder;

// @formatter:off
public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListViewHolder> {
    @NonNull
    private final StickerPackListActivity.OnEventClickedListener onEventClickedListener;
    @NonNull
    private final List<StickerPackListItem> stickerPackListItems;

    private int maxNumberOfStickersInARow;
    private int minMarginBetweenImages;

    public StickerPackListAdapter(
            @NonNull List<StickerPackListItem> stickerPackListItems, @NonNull StickerPackListActivity.OnEventClickedListener onEventClickedListener) {
        this.stickerPackListItems = stickerPackListItems;
        this.onEventClickedListener = onEventClickedListener;
    }

    @NonNull
    @Override
    public StickerPackListViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListViewHolder viewHolder, final int index) {
        StickerPackListItem stickerPackListItem = stickerPackListItems.get(index);
        final Context context = viewHolder.publisherView.getContext();

        viewHolder.addButton.setVisibility(View.VISIBLE);
        viewHolder.alertMessage.setVisibility(View.GONE);
        viewHolder.imageRowView.setVisibility(View.VISIBLE);
        viewHolder.imageRowView.removeAllViews();

        if (stickerPackListItem.status() == StickerPackListItem.Status.INVALID) {
            bindInvalidStickerPack(context, viewHolder, (StickerPack) stickerPackListItem.stickerPack(), stickerPackListItem.status());
        }

        if(stickerPackListItem.status() == StickerPackListItem.Status.VALID) {
            bindStickerPack(context, viewHolder, (StickerPack) stickerPackListItem.stickerPack(), null, stickerPackListItem.status());
        }

        if (stickerPackListItem.status() == StickerPackListItem.Status.WITH_INVALID_STICKER) {
            StickerPackWithInvalidStickers stickerPackWithInvalidStickers = (StickerPackWithInvalidStickers) stickerPackListItem.stickerPack();
            bindStickerPack(context, viewHolder, stickerPackWithInvalidStickers.getStickerPack(), stickerPackWithInvalidStickers.getInvalidStickers(), stickerPackListItem.status());
        }
    }

    @Override
    public int getItemCount() {
        return stickerPackListItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        StickerPackListItem item = stickerPackListItems.get(position);
        if (item.status() == StickerPackListItem.Status.INVALID) {
            return 1;
        }

        return 0;
    }

    private void bindStickerPack(
            @NonNull Context context, @NonNull StickerPackListViewHolder viewHolder, @NonNull StickerPack stickerPack,
            @Nullable List<Sticker> stickers, StickerPackListItem.Status status) {
        viewHolder.titleView.setText(stickerPack.name);
        viewHolder.publisherView.setText(stickerPack.publisher);
        viewHolder.filesizeView.setText(Formatter.formatShortFileSize(context, stickerPack.getTotalSize()));
        viewHolder.container.setOnClickListener(view -> {  onEventClickedListener.onStickerPackClicked(stickerPack, stickers, status); });
        viewHolder.imageRowView.removeAllViews();

        int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, stickerPack.getStickers().size());
        for (int counter = 0; counter < actualNumberOfStickersToShow; counter++) {
            final ImageView rowImage = (ImageView) LayoutInflater.from(context)
                    .inflate(R.layout.sticker_packs_list_media_item, viewHolder.imageRowView, false);

            rowImage.setImageURI(BuildStickerUri.buildStickerAssetUri(stickerPack.identifier, stickerPack.getStickers().get(counter).imageFileName));

            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
            final int marginBetweenImages = minMarginBetweenImages - layoutParams.leftMargin - layoutParams.rightMargin;

            if (counter != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) {
                layoutParams.setMargins(
                        layoutParams.leftMargin,
                        layoutParams.topMargin,
                        layoutParams.rightMargin + marginBetweenImages,
                        layoutParams.bottomMargin
                );

                rowImage.setLayoutParams(layoutParams);
            }

            viewHolder.imageRowView.addView(rowImage);
        }

        // NOTE: Só passar null em stickers por que o pacote é valido.
        setAddButtonAppearance(viewHolder.addButton, stickerPack, null, R.drawable.sticker_3rdparty_add, status);

        if (status == StickerPackListItem.Status.WITH_INVALID_STICKER) {
            setAddButtonAppearance(viewHolder.addButton, stickerPack, stickers,R.drawable.sticker_3rdparty_warning, status);
        }

        viewHolder.animatedStickerPackIndicator.setVisibility(stickerPack.animatedStickerPack ? View.VISIBLE : View.GONE);
    }

    private void bindInvalidStickerPack(@NonNull Context context, @NonNull StickerPackListViewHolder viewHolder, @NonNull StickerPack stickerPack,
                                        StickerPackListItem.Status status) {
        viewHolder.addButton.setVisibility(View.GONE);
        viewHolder.imageRowView.setVisibility(View.GONE);

        viewHolder.publisherView.setText(stickerPack.publisher);
        viewHolder.filesizeView.setText(Formatter.formatShortFileSize(context, stickerPack.getTotalSize()));
        viewHolder.titleView.setText(stickerPack.name);

        viewHolder.container.setOnClickListener(view -> onEventClickedListener.onStickerPackClicked(stickerPack, stickerPack.getStickers(), status));

        viewHolder.animatedStickerPackIndicator.setVisibility(stickerPack.animatedStickerPack ? View.VISIBLE : View.GONE);
        viewHolder.alertMessage.setVisibility(View.VISIBLE);
    }

    private void setAddButtonAppearance(ImageView addButton, StickerPack stickerPack, List<Sticker> stickers, int drawableIcon,
                                        StickerPackListItem.Status status) {
        if (stickerPack.getIsWhitelisted()) {
            addButton.setImageResource( R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);

            setBackground(addButton);
        } else {
            addButton.setImageResource(drawableIcon);
            addButton.setOnClickListener(view -> onEventClickedListener.onAddButtonClicked(stickerPack, stickers, status));

            TypedValue outValue = new TypedValue();
            addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            addButton.setBackgroundResource(outValue.resourceId);
        }
    }

    private void setBackground(View view) {
        view.setBackground(null);
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
