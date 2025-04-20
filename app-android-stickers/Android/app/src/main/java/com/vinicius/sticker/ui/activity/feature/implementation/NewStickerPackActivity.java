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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.BaseActivity;
import com.vinicius.sticker.data.model.StickerPack;
import com.vinicius.sticker.ui.adapter.StickerPreviewAdapter;

public class NewStickerPackActivity extends BaseActivity {
   public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
   private MaterialButton buttonCreateStickerPackage;
   private RecyclerView recyclerView;
   private GridLayoutManager layoutManager;
   private StickerPreviewAdapter stickerPreviewAdapter;
   private StickerPack stickerPack;
   private int numColumns;
   View divider;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_create_sticker_package);

      if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(this,
             new String[]{"android.permission.READ_MEDIA_IMAGES"},
             1);
      } else {
         Toast.makeText(this, "Permissão já concedida!", Toast.LENGTH_SHORT).show();
      }

      boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
      if (getSupportActionBar() != null) {
         getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
         getSupportActionBar().setTitle(showUpButton ? getResources().getString(R.string.title_activity_sticker_packs_creator) : getResources().getQuantityString(R.plurals.title_activity_sticker_packs_creator_list, 1));
      }

      buttonCreateStickerPackage = findViewById(R.id.button_select_media_sticker);
      buttonCreateStickerPackage.setOnClickListener(view -> {
         popUpButtonChooserStickerModel(buttonCreateStickerPackage);
      });
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

   private void popUpButtonChooserStickerModel(@NonNull MaterialButton materialButton) {
      View popupView = LayoutInflater.from(this).inflate(R.layout.dropdown_custom_menu, null);
      popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

      PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
      popupWindow.setElevation(12f);
      popupWindow.setOutsideTouchable(true);
      popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.background_menu_dropdown));
      popupWindow.setAnimationStyle(R.style.popup_bounce_animation);

      int[] location = new int[2];
      materialButton.getLocationOnScreen(location);

      int popupHeight = popupView.getMeasuredHeight();
      int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
      int yPosition = location[1] - popupHeight - margin;

      popupWindow.showAtLocation(materialButton, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yPosition);

      popupView.findViewById(R.id.item_option_static).setOnClickListener(view -> {
         launchOwnGallery(this, IMAGE_MIME_TYPES);
      });

      popupView.findViewById(R.id.item_option_animated).setOnClickListener(view -> {
         launchOwnGallery(this, ANIMATED_MIME_TYPES);
      });
   }

   private void setNumColumns(int columns) {
      if (this.numColumns != columns) {
         this.numColumns = columns;
         layoutManager.setSpanCount(columns);
         if (stickerPreviewAdapter != null) stickerPreviewAdapter.notifyDataSetChanged();
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
