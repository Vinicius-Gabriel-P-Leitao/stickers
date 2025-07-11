/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package br.arch.sticker.view.feature.stickerpack.list.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import br.arch.sticker.R;

public class StickerPackListViewHolder extends RecyclerView.ViewHolder {

    public final View container;
    public final TextView titleView;
    public final TextView publisherView;
    public final TextView stickerPackListItemDot;
    public final TextView filesizeView;
    public final ImageView addButton;
    public final ImageView animatedStickerPackIndicator;
    public final LinearLayout imageRowView;
    public final TextView alertMessage;

    public StickerPackListViewHolder(final View itemView) {
        super(itemView);
        container = itemView;
        titleView = itemView.findViewById(R.id.sticker_pack_title);
        publisherView = itemView.findViewById(R.id.sticker_pack_publisher);
        stickerPackListItemDot = itemView.findViewById(R.id.sticker_pack_list_item_dot);
        filesizeView = itemView.findViewById(R.id.sticker_pack_filesize);
        addButton = itemView.findViewById(R.id.add_button_on_list);
        imageRowView = itemView.findViewById(R.id.sticker_packs_list_item_image_list);
        animatedStickerPackIndicator = itemView.findViewById(R.id.sticker_pack_animation_indicator);
        alertMessage = itemView.findViewById(R.id.alert_message);
    }
}