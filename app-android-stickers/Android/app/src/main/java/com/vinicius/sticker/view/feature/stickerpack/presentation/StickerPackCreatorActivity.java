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
package com.vinicius.sticker.view.feature.stickerpack.presentation;

import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.IMAGE_MIME_TYPES;
import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.launchOwnGallery;
import static com.vinicius.sticker.view.feature.permission.util.DefinePermissionsToRequest.getPermissionsToRequest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.BaseActivity;
import com.vinicius.sticker.view.feature.permission.presentation.PermissionRequestBottomSheetDialogFragment;

import java.util.Arrays;

public class StickerPackCreatorActivity extends BaseActivity {
   /* Values  */
   public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
   public static final String EXTRA_STICKER_FORMAT = "sticker_format";
   public static final String STATIC_STICKER = "animated";
   public static final String ANIMATED_STICKER = "static";

   private ActivityResultLauncher<Intent> permissionLauncher;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_create_sticker_package);

      boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
      if ( getSupportActionBar() != null ) {
         getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
         getSupportActionBar().setTitle(showUpButton ? getResources().getString(
             R.string.title_activity_sticker_packs_creator) : getResources().getQuantityString(
             R.plurals.title_activity_sticker_packs_creator_list, 1));
      }

      String format = getIntent().getStringExtra(EXTRA_STICKER_FORMAT);
      if ( format == null ) {
         Toast.makeText(
             StickerPackCreatorActivity.this, "Erro ao abrir galeira!",
             Toast.LENGTH_SHORT
         ).show();

         throw new RuntimeException("Erro ao abrir galeria, pode o valor pode ser nulo");
      }

      PermissionRequestBottomSheetDialogFragment permissionRequestBottomSheetDialogFragment = new PermissionRequestBottomSheetDialogFragment();

      MaterialButton buttonSelectMedia = findViewById(R.id.button_select_media);
      buttonSelectMedia.setOnClickListener(view -> {
         String[] permissions = getPermissionsToRequest(this);
         Log.i("Permissions Media", Arrays.toString(permissions));

         if ( permissions.length > 0 ) {
            permissionRequestBottomSheetDialogFragment.setPermissions(permissions);
            permissionRequestBottomSheetDialogFragment.setCallback( new PermissionRequestBottomSheetDialogFragment.PermissionCallback() {
               @Override
               public void onPermissionsGranted() {
                  openGallery(format);
               }

               @Override
               public void onPermissionsDenied() {
                  Toast.makeText(
                      StickerPackCreatorActivity.this, "Galeria não foi liberada.",
                      Toast.LENGTH_SHORT
                  ).show();
               }
            });

            permissionRequestBottomSheetDialogFragment.show(getSupportFragmentManager(), "permissionRequest");
         } else {
            openGallery(format);
         }
      });
   }

   private void openGallery(String format) {
      if ( format.equals(STATIC_STICKER) ) {
         launchOwnGallery( StickerPackCreatorActivity.this, IMAGE_MIME_TYPES);
      }

      if ( format.equals(ANIMATED_STICKER) ) {
         launchOwnGallery( StickerPackCreatorActivity.this, ANIMATED_MIME_TYPES);
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if ( requestCode == 1 && resultCode == RESULT_OK ) {
         Uri selectedUri = data.getData();
         Log.d("MediaPicker", "Selected URI: " + selectedUri);
      }
   }
}
