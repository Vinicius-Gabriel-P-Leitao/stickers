/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import br.arch.sticker.R;

public class MediaViewHolder extends RecyclerView.ViewHolder {
   public final ImageView imageView;
   public final MaterialCheckBox radioCheckBox;

   public MediaViewHolder(View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      radioCheckBox = itemView.findViewById(R.id.radio_checkbox);
   }
}
