package com.vinicius.sticker.ui.view;

import static com.vinicius.sticker.core.util.ConvertMediaToStickerFormat.convertMediaToWebP;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vinicius.sticker.R;
import com.vinicius.sticker.ui.adapter.DrawerHandlerAdapter;
import com.vinicius.sticker.ui.adapter.PickMediaListAdapter;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ImagePickerBottomSheet extends BottomSheetDialogFragment {
   private final List<String> imagePaths;
   private final PickMediaListAdapter.OnItemClickListener listener;

   public ImagePickerBottomSheet(List<String> imagePaths, PickMediaListAdapter.OnItemClickListener listener) {
      this.imagePaths = imagePaths;
      this.listener = listener;
   }

   @Override
   public int getTheme() {
      return R.style.TransparentBottomSheet;
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.activity_recyclerview_select_image, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
      recyclerView.setHasFixedSize(true);

      GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
      layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
         @Override
         public int getSpanSize(int position) {
            if (position == 0) {
               return 3;
            } else {
               return 1;
            }
         }
      });
      recyclerView.setLayoutManager(layoutManager);

      DrawerHandlerAdapter drawerHandlerAdapter = new DrawerHandlerAdapter(getContext());
      PickMediaListAdapter mediaListAdapter = new PickMediaListAdapter(getContext(), imagePaths, imagePath -> {
         listener.onItemClick(imagePath);
         dismiss();
      });
      ConcatAdapter concatAdapter = new ConcatAdapter(drawerHandlerAdapter, mediaListAdapter);

      recyclerView.setAdapter(concatAdapter);

      Button selectButton = view.findViewById(R.id.select_medias_button);
      selectButton.setOnClickListener(buttonView -> {
         Set<String> selectedMediaPaths = mediaListAdapter.getSelectedMediaPaths();

         if (selectedMediaPaths.isEmpty()) {
            Toast.makeText(getContext(), "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
         } else {
            for (String path : selectedMediaPaths) {
               Log.i("ImagePath", String.format("Caminho: %s", path));
               convertMediaToWebP(getContext(), String.valueOf(Uri.fromFile(new File(path))), new File(path).getName());
               ;
            }
         }
      });
   }
}