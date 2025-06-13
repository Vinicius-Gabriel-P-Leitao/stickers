/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import br.arch.sticker.R;

public class InvalidStickerListViewHolder extends RecyclerView.ViewHolder {
    public final ImageView stickerPreview;
    public final TextView textErrorMessage;
    public final Button buttonFix;

    public InvalidStickerListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.stickerPreview = itemView.findViewById(R.id.invalid_sticker_preview);
        this.textErrorMessage = itemView.findViewById(R.id.text_error_message);
        this.buttonFix = itemView.findViewById(R.id.button_fix_invalid);
    }
}
