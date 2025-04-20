package com.vinicius.sticker.ui.activity.feature.implementation;

import static com.vinicius.sticker.core.util.OpenOwnGalleryActivity.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.core.util.OpenOwnGalleryActivity.IMAGE_MIME_TYPES;
import static com.vinicius.sticker.core.util.OpenOwnGalleryActivity.launchOwnGallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.BaseActivity;

public class NewStickerPackActivity extends BaseActivity {
   /* Values  */
   public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
   public static final String EXTRA_STICKER_FORMAT = "sticker_format";
   public static final String STATIC_STICKER = "animated";
   public static final String ANIMATED_STICKER = "static";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_create_sticker_package);

      boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
      if (getSupportActionBar() != null) {
         getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
         getSupportActionBar().setTitle(showUpButton ? getResources().getString(R.string.title_activity_sticker_packs_creator) : getResources().getQuantityString(R.plurals.title_activity_sticker_packs_creator_list, 1));
      }

      if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_MEDIA_IMAGES"}, 1);
      } else {
         Toast.makeText(this, "Selecione os arquivos!", Toast.LENGTH_SHORT).show();
      }

      String format = getIntent().getStringExtra(EXTRA_STICKER_FORMAT);

      MaterialButton buttonSelectMedia = findViewById(R.id.button_select_media);
      buttonSelectMedia.setOnClickListener(view -> {
         openGallery(format);
      });
   }

   private void openGallery(String format) {
      if (format.equals(STATIC_STICKER)) {
         launchOwnGallery(this, IMAGE_MIME_TYPES);
      }

      if (format.equals(ANIMATED_STICKER)) {
         launchOwnGallery(this, ANIMATED_MIME_TYPES);
      }
   }

   @Override
   public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);

      if (requestCode == 1) {
         if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
         } else {
            Toast.makeText(this, "Permissão negada, não será possível continuar.", Toast.LENGTH_SHORT).show();
         }
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == 1 && resultCode == RESULT_OK) {
         Uri selectedUri = data.getData();
         Log.d("MediaPicker", "Selected URI: " + selectedUri);
      }
   }
}
