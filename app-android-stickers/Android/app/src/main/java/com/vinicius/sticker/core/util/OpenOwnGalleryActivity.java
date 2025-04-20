package com.vinicius.sticker.core.util;

import static android.app.Activity.RESULT_OK;
import static com.vinicius.sticker.core.util.CursorFindMediaActivity.getMediaPaths;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.vinicius.sticker.ui.adapter.PickMediaListAdapter;
import com.vinicius.sticker.ui.view.ImagePickerBottomSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenOwnGalleryActivity {
   public static final String[] IMAGE_MIME_TYPES = {"image/jpeg", "image/png"};
   public static final String[] ANIMATED_MIME_TYPE = {"video/mp4", "image/gif"};

   public static void launchOwnGallery(FragmentActivity activity, String[] mimeType) {
      List<String> paths = new ArrayList<>();

      if (Arrays.equals(mimeType, IMAGE_MIME_TYPES)) {
         paths = getMediaPaths(activity, CursorFindMediaActivity.IMAGE_MIME_TYPES);
      }

      if (Arrays.equals(mimeType, ANIMATED_MIME_TYPE)) {
         paths = getMediaPaths(activity, CursorFindMediaActivity.ANIMATED_MIME_TYPE);
      }

      ImagePickerBottomSheet sheet = new ImagePickerBottomSheet(paths, new PickMediaListAdapter.OnItemClickListener() {
         @Override
         public void onItemClick(String imagePath) {
            Uri selectedImageUri = Uri.fromFile(new File(imagePath));
            Intent resultIntent = new Intent();
            resultIntent.setData(selectedImageUri);

            activity.setResult(RESULT_OK, resultIntent);
            activity.finish();
         }
      });

      sheet.show(activity.getSupportFragmentManager(), "ImagePickerBottomSheet");
   }
}
