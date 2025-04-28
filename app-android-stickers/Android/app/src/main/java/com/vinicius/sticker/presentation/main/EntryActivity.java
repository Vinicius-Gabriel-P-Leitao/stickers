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
package com.vinicius.sticker.presentation.main;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.vinicius.sticker.R;
import com.vinicius.sticker.core.BaseActivity;
import com.vinicius.sticker.core.validation.StickerPackValidator;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.service.StickerPackLoader;
import com.vinicius.sticker.presentation.feature.stickerpack.presentation.activity.StickerPackDetailsActivity;
import com.vinicius.sticker.presentation.feature.stickerpack.presentation.activity.StickerPackListActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class EntryActivity extends BaseActivity {
   private View progressBar;
   private LoadListAsyncTask loadListAsyncTask;

   @Override
   protected void onCreate(
       @Nullable Bundle savedInstanceState
   ) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_entry);
      overridePendingTransition(0, 0);
      if ( getSupportActionBar() != null ) {
         getSupportActionBar().hide();
      }
      progressBar = findViewById(R.id.entry_activity_progress);
      loadListAsyncTask = new LoadListAsyncTask(this);
      loadListAsyncTask.execute();
   }

   private void showStickerPack(ArrayList<StickerPack> stickerPackList) {
      progressBar.setVisibility(View.GONE);
      if ( stickerPackList.size() > 1 ) {
         final Intent intent = new Intent(this, StickerPackListActivity.class);
         intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_STICKER_PACK_LIST_DATA,
                                            stickerPackList
         );
         startActivity(intent);
         finish();
         overridePendingTransition(0, 0);
      } else {
         final Intent intent = new Intent(this, StickerPackDetailsActivity.class);
         intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, false);
         intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA,
                         stickerPackList.get(0)
         );
         startActivity(intent);
         finish();
         overridePendingTransition(0, 0);
      }
   }

   private void showErrorMessage(String errorMessage) {
      progressBar.setVisibility(View.GONE);
      Log.e("EntryActivity", "error fetching sticker packs, " + errorMessage);
      final TextView errorMessageTV = findViewById(R.id.error_message);
      errorMessageTV.setText(getString(R.string.error_message, errorMessage));
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      if ( loadListAsyncTask != null && !loadListAsyncTask.isCancelled() ) {
         loadListAsyncTask.cancel(true);
      }
   }

   // Carrega a lista de sticker de forma assincrona
   static class LoadListAsyncTask extends AsyncTask<Void, Void, Pair<String, ArrayList<StickerPack>>> {
      private final WeakReference<EntryActivity> contextWeakReference;

      LoadListAsyncTask(EntryActivity activity) {
         this.contextWeakReference = new WeakReference<>(activity);
      }

      @Override
      protected Pair<String, ArrayList<StickerPack>> doInBackground(Void... voids) {
         ArrayList<StickerPack> stickerPackList = null;
         try {
            final Context context = contextWeakReference.get();
            if ( context != null ) {
               stickerPackList = StickerPackLoader.fetchStickerPacks(context);
               if ( stickerPackList.isEmpty() ) {
                  return new Pair<>("No sticker packs available", null);
               }
               for (StickerPack stickerPack : stickerPackList) {
                  StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
               }
               return new Pair<>(null, stickerPackList);
            } else {
               return new Pair<>("could not fetch sticker packs", null);
            }
         } catch (IllegalStateException exception) {
            Context context = contextWeakReference.get();
            if ( context != null ) {
               Intent intent = new Intent(context, StickerFirstPackCreatorActivity.class);
               // Note: flags para transformar essa activity como main
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
               intent.putExtra("database_empty", true);
               context.startActivity(intent);
            }
            return new Pair<>("Error encountered, redirecting...", null);
         } catch (Exception exception) {
            Log.e("EntryActivity", "Error fetching sticker packs", exception);
            return new Pair<>(exception.getMessage(), null);
         }
      }

      @Override
      protected void onPostExecute(Pair<String, ArrayList<StickerPack>> stringListPair) {

         final EntryActivity entryActivity = contextWeakReference.get();
         if ( entryActivity != null ) {
            if ( stringListPair.first != null ) {
               entryActivity.showErrorMessage(stringListPair.first);
            } else {
               entryActivity.showStickerPack(stringListPair.second);
            }
         }
      }
   }
}
