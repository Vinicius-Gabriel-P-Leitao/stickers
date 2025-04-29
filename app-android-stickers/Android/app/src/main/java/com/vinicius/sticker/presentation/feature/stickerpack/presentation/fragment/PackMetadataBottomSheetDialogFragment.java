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

package com.vinicius.sticker.presentation.feature.stickerpack.presentation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.vinicius.sticker.R;

public class PackMetadataBottomSheetDialogFragment extends BottomSheetDialogFragment {
   public interface MetadataCallback {
      void onGetMetadata(String namePack);

      void onError(String error);
   }

   public void setCallback(
       MetadataCallback callback
   ) {
      this.callback = callback;
   }

   private MetadataCallback callback;

   @Nullable
   @Override
   public View onCreateView(
       LayoutInflater inflater,
       @Nullable ViewGroup container,
       @Nullable Bundle savedInstanceState
   ) {
      View view = inflater.inflate(R.layout.dialog_metadata_pack, container, false);

      ImageButton buttonGrantPermission = view.findViewById(R.id.grant_permission_button);
      buttonGrantPermission.setOnClickListener(viewAccept -> {
         TextInputEditText textInputEditText = view.findViewById(R.id.et_user_input);
         String inputText = textInputEditText.getText().toString().trim();
         if ( inputText.isEmpty() ) {
            callback.onError("Preecha o nome do pacote!");
            dismiss();
            return;
         }

         callback.onGetMetadata(inputText);
         dismiss();
      });
      return view;
   }

   @Override
   public int getTheme() {
      return R.style.TransparentBottomSheet;
   }
}
