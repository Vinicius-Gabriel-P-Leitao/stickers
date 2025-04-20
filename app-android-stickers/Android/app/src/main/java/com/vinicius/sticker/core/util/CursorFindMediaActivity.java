package com.vinicius.sticker.core.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class CursorFindMediaActivity {
   public static final String[] IMAGE_MIME_TYPES = {"image/jpeg", "image/png"};
   public static final String[] ANIMATED_MIME_TYPE = {"video/mp4", "image/gif"};

   public static List<String> getMediaPaths(Context context, String[] mimeTypes) {
      List<String> mediaPaths = new ArrayList<>();

      String[] projection = {
          MediaStore.Files.FileColumns._ID,
          MediaStore.Files.FileColumns.DISPLAY_NAME,
          MediaStore.Files.FileColumns.DATA,
          MediaStore.Files.FileColumns.MIME_TYPE
      };

      StringBuilder selectionBuilder = new StringBuilder();
      for (int i = 0; i < mimeTypes.length; i++) {
         selectionBuilder.append(MediaStore.Files.FileColumns.MIME_TYPE).append("=?");
         if (i < mimeTypes.length - 1) selectionBuilder.append(" OR ");
      }
      String selection = selectionBuilder.toString();

      Uri collectionUri = MediaStore.Files.getContentUri("external");

      Cursor cursor = context.getContentResolver().query(
          collectionUri,
          projection,
          selection,
          mimeTypes,
          MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
      );

      if (cursor != null) {
         while (cursor.moveToNext()) {
            @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            mediaPaths.add(path);
         }
         cursor.close();
      }

      return mediaPaths;
   }
}
