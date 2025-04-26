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
package com.vinicius.sticker.view.feature.media.presentation;

import static com.vinicius.sticker.view.feature.media.util.ConvertMediaToStickerFormat.convertMediaToWebP;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.exception.MediaConversionException;
import com.vinicius.sticker.view.feature.media.adapter.PickMediaListAdapter;
import com.vinicius.sticker.view.feature.media.util.ConvertMediaToStickerFormat;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MediaPickerBottomSheetDialogFragment extends BottomSheetDialogFragment {
   private final boolean isAnimatedPack;
   private final List<Uri> mediaUris;
   private ProgressBar progressBar;
   private final String namePack;
   private int completedConversions = 0;
   private int totalConversions = 0;
   private final PickMediaListAdapter.OnItemClickListener listener;
   ExecutorService executor = new ThreadPoolExecutor(5, 10, 1L, TimeUnit.SECONDS,
       new LinkedBlockingQueue<>()
   );

   public MediaPickerBottomSheetDialogFragment(
       List<Uri> mediaUris, String namePack, boolean isAnimatedPack,
       PickMediaListAdapter.OnItemClickListener listener
   ) {
      this.mediaUris = mediaUris;
      this.namePack = namePack;
      this.isAnimatedPack = isAnimatedPack;
      this.listener = listener;
   }

   @Override
   public int getTheme() {
      return R.style.TransparentBottomSheet;
   }

   @Nullable
   @Override
   public View onCreateView(
       @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
       @Nullable Bundle savedInstanceState
   ) {
      return inflater.inflate(R.layout.dialog_fragment_recyclerview_select_media, container, false);
   }

   @Override
   public void onViewCreated(
       @NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
      recyclerView.setHasFixedSize(true);

      GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);

      PickMediaListAdapter mediaListAdapter = new PickMediaListAdapter(getContext(), mediaUri -> {
         listener.onItemClick(mediaUri);
         dismiss();
      }
      );
      mediaListAdapter.submitList(mediaUris);

      recyclerView.setLayoutManager(layoutManager);
      recyclerView.setAdapter(mediaListAdapter);

      progressBar = view.findViewById(R.id.progress_bar_media);
      if ( progressBar == null ) {
         Log.e("MediaPickerFragment", "ProgressBar não encontrado!");
      } else {
         progressBar.setVisibility(View.GONE);
      }

      Button selectButton = view.findViewById(R.id.select_medias_button);
      selectButton.setOnClickListener(buttonView -> {
         Set<Uri> selectedMediaPaths = mediaListAdapter.getSelectedMediaPaths();

         if ( selectedMediaPaths.isEmpty() ) {
            Toast.makeText(getContext(), "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
         } else {
            totalConversions = selectedMediaPaths.size();
            completedConversions = 0;
            progressBar.setVisibility(View.VISIBLE);

            for (Uri uri : selectedMediaPaths) {
               convertMediaAndSaveAsync(uri);
            }
         }
      });
   }

   private void convertMediaAndSaveAsync(Uri uri) {
      executor.submit(() -> {
         convertMediaToWebP(getContext(), uri,
             new File(Objects.requireNonNull(uri.getPath())).getName(),
             new ConvertMediaToStickerFormat.MediaConversionCallback() {
                @Override
                public void onSuccess(File outputFile) {
                   new Handler(Looper.getMainLooper()).post(() -> {
                      // NOTE: Colocar lógica de converter json aqui:
                      // isAnimatedPack;
                      // namePack;
                      checkAllConversionsCompleted();
                   });
                }

                @Override
                public void onError(Exception exception) {
                   new Handler(Looper.getMainLooper()).post(() -> {
                      if ( exception instanceof MediaConversionException ) {
                         Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT)
                             .show();
                      } else {
                         Toast.makeText(getContext(), "Erro critico ao converter imagens!",
                             Toast.LENGTH_SHORT
                         ).show();
                      }
                   });
                }
             }
         );
      });
   }

   private void checkAllConversionsCompleted() {
      completedConversions++;

      if ( completedConversions == totalConversions ) {
         progressBar.setVisibility(View.GONE);
         Toast.makeText(getContext(), "Todas as conversões completadas!", Toast.LENGTH_SHORT)
             .show();
      }
   }
}