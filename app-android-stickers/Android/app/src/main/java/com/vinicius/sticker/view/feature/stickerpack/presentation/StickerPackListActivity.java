/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */
package com.vinicius.sticker.view.feature.stickerpack.presentation;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.view.feature.stickerpack.adapter.StickerPackListAdapter;
import com.vinicius.sticker.view.feature.stickerpack.viewholder.StickerPackListItemViewHolder;
import com.vinicius.sticker.view.feature.stickerpack.usecase.AddStickerPackActivity;
import com.vinicius.sticker.core.validation.WhitelistCheck;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StickerPackListActivity extends AddStickerPackActivity {
   /* Values */
   public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
   private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
   private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> addStickerPackToWhatsApp(
       pack.identifier,
       pack.name
   );
   /* UI */
   private StickerPackListAdapter allStickerPacksListAdapter;
   private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
   private MaterialButton buttonCreateStickerPackage;
   private ArrayList<StickerPack> stickerPackList;
   private LinearLayoutManager packLayoutManager;
   private RecyclerView packRecyclerView;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_sticker_pack_list);

      packRecyclerView = findViewById(R.id.sticker_pack_list);

      stickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
      showStickerPackList(stickerPackList);
      if (getSupportActionBar() != null) {
         getSupportActionBar().setTitle(getResources().getQuantityString(
             R.plurals.title_activity_sticker_packs_list,
             stickerPackList.size()
         ));
      }

      buttonCreateStickerPackage = findViewById(R.id.button_redirect_create_stickers);
      buttonCreateStickerPackage.setOnClickListener(view -> {
         popUpButtonChooserStickerModel(buttonCreateStickerPackage);
      });
   }

   private void popUpButtonChooserStickerModel(@NonNull MaterialButton materialButton) {
      View popupView = LayoutInflater.from(this).inflate(
          R.layout.dropdown_custom_menu,
          null
      );
      popupView.measure(
          View.MeasureSpec.UNSPECIFIED,
          View.MeasureSpec.UNSPECIFIED
      );

      PopupWindow popupWindow = new PopupWindow(
          popupView,
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
          true
      );
      popupWindow.setElevation(12f);
      popupWindow.setOutsideTouchable(true);
      popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(
          this,
          R.drawable.menu_dropdown_background
      ));
      popupWindow.setAnimationStyle(R.style.PopupBounceAnimation);

      int[] location = new int[2];
      materialButton.getLocationOnScreen(location);

      int popupHeight = popupView.getMeasuredHeight();
      int margin = (int) TypedValue.applyDimension(
          TypedValue.COMPLEX_UNIT_DIP,
          16,
          getResources().getDisplayMetrics()
      );
      int yPosition = location[1] - popupHeight - margin;

      popupWindow.showAtLocation(
          materialButton,
          Gravity.TOP | Gravity.CENTER_HORIZONTAL,
          0,
          yPosition
      );

      popupView.findViewById(R.id.item_option_static).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            openCreateStickerPackActivity(
                StickerPackCreatorActivity.EXTRA_STICKER_FORMAT,
                StickerPackCreatorActivity.STATIC_STICKER
            );
         }
      });

      popupView.findViewById(R.id.item_option_animated).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            openCreateStickerPackActivity(
                StickerPackCreatorActivity.EXTRA_STICKER_FORMAT,
                StickerPackCreatorActivity.ANIMATED_STICKER
            );
         }
      });
   }

   private void openCreateStickerPackActivity(String typePackage, String format) {
      Intent intent = new Intent(
          StickerPackListActivity.this,
          StickerPackCreatorActivity.class
      );
      intent.putExtra(
          typePackage,
          format
      );
      startActivity(intent);
   }

   @Override
   protected void onResume() {
      super.onResume();
      whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
      whiteListCheckAsyncTask.execute(stickerPackList.toArray(new StickerPack[0]));
   }

   @Override
   protected void onPause() {
      super.onPause();
      if (whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled()) {
         whiteListCheckAsyncTask.cancel(true);
      }
   }

   private void showStickerPackList(List<StickerPack> stickerPackList) {
      allStickerPacksListAdapter = new StickerPackListAdapter(
          stickerPackList,
          onAddButtonClickedListener
      );
      packRecyclerView.setAdapter(allStickerPacksListAdapter);

      packLayoutManager = new LinearLayoutManager(this);
      packLayoutManager.setOrientation(RecyclerView.VERTICAL);
      DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
          packRecyclerView.getContext(),
          packLayoutManager.getOrientation()
      );

      packRecyclerView.addItemDecoration(dividerItemDecoration);
      packRecyclerView.setLayoutManager(packLayoutManager);
      packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
   }

   private void recalculateColumnCount() {
      final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
      int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
      StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) packRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
      if (viewHolder != null) {
         final int widthOfImageRow = viewHolder.imageRowView.getMeasuredWidth();
         final int max = Math.max(
             widthOfImageRow / previewSize,
             1
         );
         int maxNumberOfImagesInARow = Math.min(
             STICKER_PREVIEW_DISPLAY_LIMIT,
             max
         );
         int minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);
         allStickerPacksListAdapter.setImageRowSpec(
             maxNumberOfImagesInARow,
             minMarginBetweenImages
         );
      }
   }

   static class WhiteListCheckAsyncTask extends AsyncTask<StickerPack, Void, List<StickerPack>> {
      private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

      WhiteListCheckAsyncTask(StickerPackListActivity stickerPackListActivity) {
         this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackListActivity);
      }

      @Override
      protected final List<StickerPack> doInBackground(StickerPack... stickerPackArray) {
         final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
         if (stickerPackListActivity == null) {
            return Arrays.asList(stickerPackArray);
         }
         for (StickerPack stickerPack : stickerPackArray) {
            stickerPack.setIsWhitelisted(WhitelistCheck.isWhitelisted(
                stickerPackListActivity,
                stickerPack.identifier
            ));
         }
         return Arrays.asList(stickerPackArray);
      }

      @Override
      protected void onPostExecute(List<StickerPack> stickerPackList) {
         final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
         if (stickerPackListActivity != null) {
            stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
            stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
         }
      }
   }
}
