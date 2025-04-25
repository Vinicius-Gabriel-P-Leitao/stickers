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

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
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
      super.onCreate( savedInstanceState );
      setContentView( R.layout.activity_create_sticker_package );

      boolean showUpButton = getIntent().getBooleanExtra( EXTRA_SHOW_UP_BUTTON, true );
      if ( getSupportActionBar() != null ) {
         getSupportActionBar().setDisplayHomeAsUpEnabled( showUpButton );
         getSupportActionBar().setTitle( showUpButton ? getResources().getString(
             R.string.title_activity_sticker_packs_creator ) : getResources().getQuantityString(
             R.plurals.title_activity_sticker_packs_creator_list, 1 ) );
      }

      PermissionRequestBottomSheetDialogFragment permissionRequestBottomSheetDialogFragment = new PermissionRequestBottomSheetDialogFragment();

      ImageButton buttonSelectMedia = findViewById( R.id.button_select_media );
      buttonSelectMedia.setOnClickListener( view -> {
         ObjectAnimator rotation = ObjectAnimator.ofFloat(
             buttonSelectMedia, "rotation", 0f, 360f );
         rotation.setDuration( 500 );
         rotation.start();

         String[] permissions = getPermissionsToRequest( this );
         Log.i( "Permissions Media", Arrays.toString( permissions ) );

         if ( permissions.length > 0 ) {
            permissionRequestBottomSheetDialogFragment.setPermissions( permissions );
            permissionRequestBottomSheetDialogFragment.setCallback(
                new PermissionRequestBottomSheetDialogFragment.PermissionCallback() {
                   @Override
                   public void onPermissionsGranted() {
                      openMetadataGetter();
                   }

                   @Override
                   public void onPermissionsDenied() {
                      Toast.makeText(
                          StickerPackCreatorActivity.this, "Galeria não foi liberada.",
                          Toast.LENGTH_SHORT
                      ).show();
                   }
                } );

            permissionRequestBottomSheetDialogFragment.show(
                getSupportFragmentManager(), "permissionRequestBottomSheetDialogFragment" );
         } else {
            openMetadataGetter();
         }
      } );
   }

   private void openMetadataGetter() {
      PackMetadataBottomSheetDialogFragment packMetadataBottomSheetDialogFragment = new PackMetadataBottomSheetDialogFragment();
      packMetadataBottomSheetDialogFragment.setCallback(
          new PackMetadataBottomSheetDialogFragment.PermissionCallback() {

             @Override
             public void onPermissionsGranted() {
                String format = getIntent().getStringExtra( EXTRA_STICKER_FORMAT );
                if ( format == null ) {
                   Toast.makeText(
                       StickerPackCreatorActivity.this, "Erro ao abrir galeira!",
                       Toast.LENGTH_SHORT
                   ).show();

                   throw new RuntimeException(
                       "Erro ao abrir galeria, pode o valor pode ser nulo" );
                }

                openGallery( format );
             }

             @Override
             public void onPermissionsDenied() {
                Toast.makeText(
                    StickerPackCreatorActivity.this,
                    "Não foi possivel dar continuidade", Toast.LENGTH_SHORT
                ).show();
             }
          } );

      packMetadataBottomSheetDialogFragment.show(
          getSupportFragmentManager(), "PackMetadataBottomSheetDialogFragment" );
   }

   private void openGallery(String format) {
      if ( format.equals( STATIC_STICKER ) ) {
         launchOwnGallery( StickerPackCreatorActivity.this, IMAGE_MIME_TYPES );
      }

      if ( format.equals( ANIMATED_STICKER ) ) {
         launchOwnGallery( StickerPackCreatorActivity.this, ANIMATED_MIME_TYPES );
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult( requestCode, resultCode, data );
      if ( requestCode == 1 && resultCode == RESULT_OK ) {
         Uri selectedUri = data.getData();
         Log.d( "MediaPicker", "Selected URI: " + selectedUri );
      }
   }

   public static class PackMetadataBottomSheetDialogFragment extends BottomSheetDialogFragment {
      public interface PermissionCallback {
         void onPermissionsGranted();

         void onPermissionsDenied();
      }

      public void setCallback(
          PermissionCallback callback
      ) {
         this.callback = callback;
      }

      private PermissionCallback callback;

      @Nullable
      @Override
      public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState
      ) {
         View view = inflater.inflate( R.layout.dialog_metadata_package, container, false );

         ImageButton buttonGrantPermission = view.findViewById( R.id.grant_permission_button );
         buttonGrantPermission.setOnClickListener( viewAccept -> {
            TextInputEditText textInputEditText = view.findViewById( R.id.et_user_input );
            String inputText = textInputEditText.getText().toString().trim();
            if ( inputText.isEmpty() ) {
               callback.onPermissionsDenied();
               Toast.makeText(
                   getContext(), "Preecha o nome do pacote!", Toast.LENGTH_SHORT ).show();

               dismiss();
               return;
            }

            callback.onPermissionsGranted();
            dismiss();
         } );

         Button buttonCancelPermission = view.findViewById( R.id.cancel_permission_button );
         buttonCancelPermission.setOnClickListener( viewCancel -> {
            callback.onPermissionsDenied();
            dismiss();
         } );

         return view;
      }

      @Override
      public int getTheme() {
         return R.style.TransparentBottomSheet;
      }
   }
}
