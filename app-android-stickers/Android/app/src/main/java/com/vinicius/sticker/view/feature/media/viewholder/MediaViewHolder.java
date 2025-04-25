/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */
package com.vinicius.sticker.view.feature.media.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.vinicius.sticker.R;

public class MediaViewHolder extends RecyclerView.ViewHolder {
   public final ImageView imageView;
   public final MaterialRadioButton radioChip;

   public MediaViewHolder(View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.image_view);
      radioChip = itemView.findViewById(R.id.radio_button);
   }
}
