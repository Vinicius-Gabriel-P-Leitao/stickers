/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.vinicius.sticker.view.feature.stickerpack.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.vinicius.sticker.R;

public class StickerPreviewViewHolder extends RecyclerView.ViewHolder {

    public final ImageView stickerPreviewView;

    public StickerPreviewViewHolder(final View itemView) {
        super(itemView);
        stickerPreviewView = itemView.findViewById(R.id.sticker_preview);
    }
}