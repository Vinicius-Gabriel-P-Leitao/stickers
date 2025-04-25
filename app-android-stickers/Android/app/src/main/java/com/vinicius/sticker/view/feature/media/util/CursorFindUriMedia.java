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
package com.vinicius.sticker.view.feature.media.util;

import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.IMAGE_MIME_TYPES;
import static com.vinicius.sticker.core.validation.MimeTypesValidator.validateArraysMimeTypes;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CursorFindUriMedia {

   public static List<Uri> getMediaUris(Context context, String[] mimeTypes) {
      List<Uri> mediaUris;

      if (validateArraysMimeTypes(mimeTypes,
          IMAGE_MIME_TYPES)) {
         mediaUris = getImageUris(context);
      } else if (validateArraysMimeTypes(mimeTypes,
          ANIMATED_MIME_TYPES)) {
         mediaUris = getAnimatedUris(context);
      } else {
         throw new IllegalArgumentException("Tipo MIME não suportado para conversão: " + mimeTypes);
      }

      return mediaUris;
   }

   public static Map<String, String> getFileDetailsFromUri(Context context, Uri uri) {
      Map<String, String> fileDetails = new HashMap<>();

      String fileName = null;
      String[] projection = {MediaStore.Files.FileColumns.DATA};
      Cursor cursor = context.getContentResolver().query(uri,
          projection,
          null,
          null,
          null);

      if (cursor != null && cursor.moveToFirst()) {
         int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
         if (dataColumn != -1) {
            fileName = cursor.getString(dataColumn);
         }
         cursor.close();
      }
      String mimeType = context.getContentResolver().getType(uri);
      fileDetails.put(fileName,
          mimeType);

      return fileDetails;
   }

   private static List<Uri> getImageUris(Context context) {
      List<Uri> imageUris = new ArrayList<>();

      String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.MIME_TYPE};
      Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
      String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
      String selection = MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?";

      Cursor cursor = context.getContentResolver().query(collection,
          projection,
          selection,
          IMAGE_MIME_TYPES,
          sortOrder);

      if (cursor != null) {
         int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
         int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

         while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumn);
            Uri imageUri = ContentUris.withAppendedId(collection,
                id);
            imageUris.add(imageUri);

            Log.i("imageUri",
                "Uri: " + cursor.getString(dataColumn));
         }
         cursor.close();
      }
      return imageUris;
   }

   private static List<Uri> getAnimatedUris(Context context) {
      List<Uri> animatedUris = new ArrayList<>();

      Uri collection = MediaStore.Files.getContentUri("external");
      String[] projection = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Files.FileColumns.MIME_TYPE};
      String selection = MediaStore.Files.FileColumns.MIME_TYPE + "=? OR " + MediaStore.Files.FileColumns.MIME_TYPE + "=?";
      String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

      Cursor cursor = context.getContentResolver().query(collection,
          projection,
          selection,
          ANIMATED_MIME_TYPES,
          sortOrder);

      if (cursor != null) {
         int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
         int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);

         while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumn);
            Uri fileUri = ContentUris.withAppendedId(collection,
                id);
            animatedUris.add(fileUri);

            Log.i("animatedUri",
                "Uri: " + cursor.getString(dataColumn));
         }

         cursor.close();
      }

      return animatedUris;
   }
}
