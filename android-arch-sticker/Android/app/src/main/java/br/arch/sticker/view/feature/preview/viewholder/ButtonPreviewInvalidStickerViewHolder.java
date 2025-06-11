/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import br.arch.sticker.R;

public class ButtonPreviewInvalidStickerViewHolder extends RecyclerView.ViewHolder {
    public final MaterialButton materialButton;

    public ButtonPreviewInvalidStickerViewHolder(@NonNull View itemView) {
        super(itemView);
        materialButton = itemView.findViewById(R.id.button_invalid_preview);
    }
}
