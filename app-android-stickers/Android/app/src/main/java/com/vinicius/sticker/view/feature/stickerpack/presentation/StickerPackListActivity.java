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

import static com.vinicius.sticker.view.feature.stickerpack.presentation.StickerPackCreatorActivity.ANIMATED_STICKER;
import static com.vinicius.sticker.view.feature.stickerpack.presentation.StickerPackCreatorActivity.STATIC_STICKER;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.validation.WhitelistCheck;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.view.feature.stickerpack.adapter.StickerPackListAdapter;
import com.vinicius.sticker.view.feature.stickerpack.component.FormatStickerPopup;
import com.vinicius.sticker.view.feature.stickerpack.usecase.AddStickerPackActivity;
import com.vinicius.sticker.view.feature.stickerpack.viewholder.StickerPackListItemViewHolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StickerPackListActivity extends AddStickerPackActivity {
   /* Values */
   public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
   private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
   private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> addStickerPackToWhatsApp(
       pack.identifier, pack.name);
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
      if ( getSupportActionBar() != null ) {
         getSupportActionBar().setTitle(
             getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list,
                                              stickerPackList.size()
             ));
      }

      buttonCreateStickerPackage = findViewById(R.id.button_redirect_create_stickers);
      buttonCreateStickerPackage.setOnClickListener(view -> {
         FormatStickerPopup.popUpButtonChooserStickerModel(this, buttonCreateStickerPackage,
                                                           new FormatStickerPopup.OnOptionClickListener() {
                                                              @Override
                                                              public void onStaticStickerSelected() {
                                                                 openCreateStickerPackActivity(
                                                                     STATIC_STICKER);
                                                              }

                                                              @Override
                                                              public void onAnimatedStickerSelected() {
                                                                 openCreateStickerPackActivity(
                                                                     ANIMATED_STICKER);
                                                              }
                                                           }
         );
      });
   }

   private void openCreateStickerPackActivity(String format) {
      Intent intent = new Intent(StickerPackListActivity.this, StickerPackCreatorActivity.class);
      intent.putExtra(StickerPackCreatorActivity.EXTRA_STICKER_FORMAT, format);
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
      if ( whiteListCheckAsyncTask != null && !whiteListCheckAsyncTask.isCancelled() ) {
         whiteListCheckAsyncTask.cancel(true);
      }
   }

   private void showStickerPackList(List<StickerPack> stickerPackList) {
      allStickerPacksListAdapter = new StickerPackListAdapter(stickerPackList,
                                                              onAddButtonClickedListener
      );
      packRecyclerView.setAdapter(allStickerPacksListAdapter);

      packLayoutManager = new LinearLayoutManager(this);
      packLayoutManager.setOrientation(RecyclerView.VERTICAL);
      DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
          packRecyclerView.getContext(), packLayoutManager.getOrientation());

      packRecyclerView.addItemDecoration(dividerItemDecoration);
      packRecyclerView.setLayoutManager(packLayoutManager);
      packRecyclerView.getViewTreeObserver()
          .addOnGlobalLayoutListener(this::recalculateColumnCount);
   }

   private void recalculateColumnCount() {
      final int previewSize = getResources().getDimensionPixelSize(
          R.dimen.sticker_pack_list_item_preview_image_size);
      int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();
      StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) packRecyclerView.findViewHolderForAdapterPosition(
          firstVisibleItemPosition);
      if ( viewHolder != null ) {
         final int widthOfImageRow = viewHolder.imageRowView.getMeasuredWidth();
         final int max = Math.max(widthOfImageRow / previewSize, 1);
         int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
         int minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) /
             (maxNumberOfImagesInARow - 1);
         allStickerPacksListAdapter.setImageRowSpec(maxNumberOfImagesInARow,
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
         if ( stickerPackListActivity == null ) {
            return Arrays.asList(stickerPackArray);
         }
         for (StickerPack stickerPack : stickerPackArray) {
            stickerPack.setIsWhitelisted(
                WhitelistCheck.isWhitelisted(stickerPackListActivity, stickerPack.identifier));
         }
         return Arrays.asList(stickerPackArray);
      }

      @Override
      protected void onPostExecute(List<StickerPack> stickerPackList) {
         final StickerPackListActivity stickerPackListActivity = stickerPackListActivityWeakReference.get();
         if ( stickerPackListActivity != null ) {
            stickerPackListActivity.allStickerPacksListAdapter.setStickerPackList(stickerPackList);
            stickerPackListActivity.allStickerPacksListAdapter.notifyDataSetChanged();
         }
      }
   }
}
