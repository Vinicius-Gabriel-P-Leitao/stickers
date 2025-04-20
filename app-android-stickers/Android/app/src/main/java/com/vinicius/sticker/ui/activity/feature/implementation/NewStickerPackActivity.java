package com.vinicius.sticker.ui.activity.feature.implementation;

import static com.vinicius.sticker.core.util.OpenOwnGalleryActivity.launchOwnGallery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.BaseActivity;
import com.vinicius.sticker.core.util.OpenOwnGalleryActivity;
import com.vinicius.sticker.data.model.StickerPack;
import com.vinicius.sticker.ui.adapter.StickerPreviewAdapter;

public class NewStickerPackActivity extends BaseActivity {
   public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
   public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
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

      boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
      if (getSupportActionBar() != null) {
         getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
         getSupportActionBar().setTitle(showUpButton ? getResources().getString(R.string.title_activity_sticker_packs_creator) : getResources().getQuantityString(R.plurals.title_activity_sticker_packs_creator_list, 1));
      }

      buttonCreateStickerPackage = findViewById(R.id.button_select_media_sticker);
      buttonCreateStickerPackage.setOnClickListener(view -> {
         if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {

            // Se a versão do Android for menor que a (API 33), também verificar READ_EXTERNAL_STORAGE
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
               if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                  ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
               }
            }

            // Solicita permissões de mídia para versões da (API 33) ou superiores
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
               ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, 100);
            }
         } else {
            popUpButtonChooserStickerModel(buttonCreateStickerPackage);
         }
      });

      // Todo: Pacote tem que ser passado aqui para poder renderizar na tela
      stickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);
      if (stickerPack == null) {
         return;
      }

      SimpleDraweeView expandedStickerView = findViewById(R.id.sticker_details_expanded_sticker);
      layoutManager = new GridLayoutManager(this, 1);
      recyclerView = findViewById(R.id.sticker_list_to_package);
      recyclerView.setLayoutManager(layoutManager);
      recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
      recyclerView.addOnScrollListener(dividerScrollListener);
      divider = findViewById(R.id.divider);
      if (stickerPreviewAdapter == null) {
         stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size), getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack, expandedStickerView);
         recyclerView.setAdapter(stickerPreviewAdapter);
      }
      if (getSupportActionBar() != null) {
         getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
         getSupportActionBar().setTitle(showUpButton ? getResources().getString(R.string.title_activity_sticker_pack_details_multiple_pack) : getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, 1));
      }
   }

   private final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
         super.onScrollStateChanged(recyclerView, newState);
         updateDivider(recyclerView);
      }

      @Override
      public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
         super.onScrolled(recyclerView, dx, dy);
         updateDivider(recyclerView);
      }

      private void updateDivider(RecyclerView recyclerView) {
         boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
         if (divider != null) {
            divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
         }
      }
   };

   private final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
         setNumColumns(recyclerView.getWidth() / recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
      }
   };

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
         launchOwnGallery(this, OpenOwnGalleryActivity.IMAGE_MIME_TYPES);
      });

      popupView.findViewById(R.id.item_option_animated).setOnClickListener(view -> {
         launchOwnGallery(this, OpenOwnGalleryActivity.ANIMATED_MIME_TYPE);
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
